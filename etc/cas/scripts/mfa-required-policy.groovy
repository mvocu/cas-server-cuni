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

    def flowScope = RequestContextHolder?.getRequestContext()?.getFlowScope()

    def serviceMfaLevel = registeredService.getProperties()?.get("mfaLevel") ?: ["none"]
    def principalMfaPolicy = authentication.principal.attributes?.cunimfapolicy ?: ["none"]
    def hasWebAuthn = authentication.principal.attributes?.caswebauthnrecord != null ? true : false
    def hasGAuth = authentication.principal.attributes?.casgauthrecord ? true : false
    def hasSimple = authentication.principal.attributes?.mobile ? true : false
    def requestMfaMethod = httpRequest.getParameterValues("acr_values")  ?:  ( httpRequest.getParameterValues("authn_method") ?: [] )

    def mfaMethod = "mfa-composite"

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
    }

    def availableHandlers = [ ] 
    def preferredHandlers = [ ]
    def configuredHandlers = ["mfa-webauthn", "mfa-gauth", "mfa-simple"]
    if(hasWebAuthn) { availableHandlers.add("mfa-webauthn") }
    if(hasGAuth)    { availableHandlers.add("mfa-gauth"); preferredHandlers.add("mfa-gauth") }
    if(hasSimple)   { availableHandlers.add("mfa-simple") }

    if(availableHandlers && !preferredHandlers) { preferredHandlers.add(availableHandlers.first()) }

    flowScope?.put("cuniMfaAvailableHandlers", availableHandlers)
    flowScope?.put("cuniMfaPreferredHandlers", preferredHandlers)

    logger.info("Evaluating MFA requirements for principal [{}], service policy [{}], service registration [{}], principal policy [{}] and request method [{}], flow scope [{}]", 
	authentication.principal.id, serviceMfaLevel, registeredService.getProperties()?.get("mfaAllowRegistration"), principalMfaPolicy, requestMfaMethod, flowScope)
    logger.info("Setting MFA available handlers [{}] and preferred handlers [{}]", availableHandlers, preferredHandlers);
 
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

    if(requestMfaMethod && configuredHandlers.contains(requestMfaMethod)) {
        mfaRequired = true
        mfaMethod = requestMfaMethod
    }

    if(!principalMfaPolicy.contains("none") || registeredService.getProperties()?.get("mfaAllowRegistration")?.contains("true")) {
        mfaAvailable = true
    }

    if(mfaRequired) {
        if(mfaAvailable) {
		return mfaMethod
        } else {
                throw new AuthenticationException(new MultifactorAuthenticationRequiredException())
        }
    }

    return null
}
