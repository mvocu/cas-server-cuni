import java.util.*
import org.apache.http.client.utils.URIBuilder

import org.apereo.cas.authentication.AuthenticationException
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException
import org.apereo.cas.authentication.MultifactorAuthenticationRequiredException

def String run(final Object... args) {
    def service = args[0]
    def registeredService = args[1]
    def authentication = args[2]
    def httpRequest = args[3]
    def logger = args[4]

    def mfaRequired = false
    def mfaAvailable = false

    def serviceMfaLevel = registeredService.getProperties()?.get("mfaLevel") ?: ["none"]
    def principalMfaPolicy = authentication.principal.attributes?.cunimfapolicy ?: ["none"]
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

    logger.info("Evaluating MFA requirements for principal [{}], service policy [{}], service registration [{}], principal policy [{}] and request method [{}]", 
	authentication.principal.id, serviceMfaLevel, registeredService.getProperties()?.get("mfaAllowRegistration"), principalMfaPolicy, requestMfaMethod)

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

    if(requestMfaMethod) {
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
