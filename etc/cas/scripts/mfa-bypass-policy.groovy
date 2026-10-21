import java.util.*

import org.apereo.cas.util.spring.ApplicationContextProvider
import org.springframework.webflow.execution.RequestContextHolder

def boolean run(final Object... args) {
    def authentication = args[0]
    def principal = args[1]
    def registeredService = args[2]
    def provider = args[3]
    def logger = args[4]
    def httpRequest = args[5]

    def flowScope = RequestContextHolder?.getRequestContext()?.getFlowScope()

    def mfaRegistrationAllowed = (registeredService.getProperties()?.get("mfaAllowRegistration") ?: ["false"]).contains("true")

    logger.info("XXX Evaluating MFA bypass for principal [{}] with attributes [{}], service registration [{}]",
        authentication.principal.id, principal.attributes, registeredService?.getProperties()?.get("mfaAllowRegistration"))

    // Stuff happens...

    def loa = principal.attributes["auth_loa"]
    def trustedLoA = [ "http://cas.cuni.cz/LoA/substantial", "http://cas.cuni.cz/LoA/high" ]


    // true means continue with MFA, false means skip it

    // if we are not going for registration and we have established trust in authenticated principal, skip mfa
    if(trustedLoA.contains(loa?.first()) && !mfaRegistrationAllowed) {
        return false
    }

    return true
}

