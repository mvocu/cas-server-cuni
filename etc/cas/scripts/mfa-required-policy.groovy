import java.util.*
import org.apache.http.client.utils.URIBuilder

import org.apereo.cas.authentication.AuthenticationException
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException
import org.apereo.cas.util.spring.ApplicationContextProvider

import org.springframework.webflow.execution.RequestContextHolder

def String run(final Object... args) {
    def service = args[0]
    def registeredService = args[1]
    def authentication = args[2]
    def httpRequest = args[3]
    def logger = args[4]

    def mfaRequired = false
    def mfaAvailable = false
    def trustedAuth = false

    def flowScope = RequestContextHolder?.getRequestContext()?.getFlowScope()

    def mfaResetRole = "cn=mfaresetrole,dc=cuni,dc=cz"

    def serviceMfaLevel = registeredService.getProperties()?.get("mfaLevel") ?: ["none"]
    def mfaRegistrationAllowed = (registeredService.getProperties()?.get("mfaAllowRegistration") ?: ["false"]).contains("true")
    def principalMfaPolicy = authentication.principal.attributes?.cunimfapolicy ?: ["none"]
    def principalLoA = authentication.principal.attributes?.auth_loa ?: ["http://cas.cuni.cz/LoA/none"]
    def needsMfaReset = (authentication.principal.attributes?.nsrole ?: []).contains(mfaResetRole)
    def hasWebAuthn = authentication.principal.attributes?.caswebauthnrecord != null ? true : false
    def hasGAuth = authentication.principal.attributes?.casgauthrecord ? true : false
    def hasSimple = authentication.principal.attributes?.mobile ? true : false
    def requestMfaMethod = httpRequest.getParameterValues("acr_values")  ?:  ( httpRequest.getParameterValues("authn_method") ?: [] )
    def requestMfaLevel = httpRequest.getParameterValues("mfa")

    def defaultMfaMethod = "mfa-composite"
    def mfaMethod = null

    // try to obtain requestMfaMethod from service redirect uri
    if(!requestMfaMethod) {
        def svc = httpRequest.getParameterValues("service")
        if(svc) {
            def params = (new URIBuilder(svc[0])).getQueryParams()
            for(param in params) {
                        switch(param.getName()) {
                                case 'acr_values':
                                        requestMfaMethod = param.getValue()
                                        break

                                case 'authn_method':
                                        requestMfaMethod = param.getValue()
                                        break
                        }
            }
        }
    } else {
        requestMfaMethod = requestMfaMethod.isEmpty() ? null : requestMfaMethod.first()
    }

    def availableHandlers = [ ] 
    def preferredHandlers = [ ]
    def configuredHandlers = ["mfa-webauthn", "mfa-gauth", "mfa-simple"]

    if(hasWebAuthn) { availableHandlers.add("mfa-webauthn"); preferredHandlers.add("mfa-webauthn") }
    if(hasGAuth)    { availableHandlers.add("mfa-gauth"); preferredHandlers.add("mfa-gauth") }
    if(hasSimple)   { availableHandlers.add("mfa-simple") }

    if(availableHandlers.size() == 1) {
        defaultMfaMethod = availableHandlers.first()
    }

    if(availableHandlers && !preferredHandlers) { preferredHandlers.add(availableHandlers.first()) }

    flowScope?.put("cuniMfaAvailableHandlers", availableHandlers)
    flowScope?.put("cuniMfaPreferredHandlers", preferredHandlers)

    logger.debug("XXX Evaluating MFA requirements for principal [{}], service policy [{}], service registration [{}], principal policy [{}], request method [{}], request level [{}], flow scope [{}]",
            authentication.principal.id, serviceMfaLevel, mfaRegistrationAllowed, principalMfaPolicy, requestMfaMethod,
            requestMfaLevel, flowScope)
    logger.debug("XXX Setting MFA available handlers [{}] and preferred handlers [{}]", availableHandlers, preferredHandlers);
 
    // throw new AuthenticationException(new MultifactorAuthenticationRequiredException())
     
    if(serviceMfaLevel.contains("required")) {
        mfaRequired = true
    }

    if(principalMfaPolicy.contains("always")) {
        mfaRequired = true
    }

    if(serviceMfaLevel.contains("optional") && principalMfaPolicy.contains("important")) {
        mfaRequired = true
    }

    // this is used by OIDC/CAS clients (such as IdPortal] requesting specific MFA authn context
    if(requestMfaMethod && configuredHandlers.contains(requestMfaMethod)) {
        mfaRequired = true
        mfaMethod = requestMfaMethod
    }

    // this parameter is used by Shibboleth IdP when MFA authn context is required
    if(requestMfaLevel && requestMfaLevel.contains("true")) {
        mfaRequired = true
    }

    // if MFA reset is mandated by role, set mfaRequired to enforce MFA except for registration
    if(needsMfaReset) {
        mfaRequired = true
    }

    /*  Relevant conditions:
     *    - user has MFA on
     *    - user has MFA method available
     *    - request needs MFA
     *    - request needs specific MFA method
     *    - service allows MFA registration <=> mfaRegistrationAllowed
     *    - principal LoA in first auth step
     */

    mfaAvailable = !availableHandlers.isEmpty() && !needsMfaReset

    def trustedLoA = [ "http://cas.cuni.cz/LoA/substantial", "http://cas.cuni.cz/LoA/high" ]
    trustedAuth = trustedLoA.contains(principalLoA.first())

    /* XXX - disabled
    if(!principalMfaPolicy.contains("none") || mfaRegistrationAllowed) {
        mfaAvailable = true
    }
    */

    logger.info("XXX MFA trigger conditions for [{}]: required [{}], available [{}], registration [{}], method [{}], trusted [{}]",
          authentication.principal?.id, mfaRequired, mfaAvailable, mfaRegistrationAllowed, mfaMethod, trustedAuth)

    if(mfaRegistrationAllowed && (!mfaAvailable || trustedAuth)) {
        // For registration apps, if there is no method available and none particular is requested, skip MFA at all.
        return mfaMethod // this may return null if no particular method was requested
    }

    if(mfaRequired) {
        if(mfaAvailable) {
            return mfaMethod ?: defaultMfaMethod
        } else {
            if(flowScope) {
                throw new AuthenticationException(new MultifactorAuthenticationRequiredException())
            }
        }
    }

    return null
}
