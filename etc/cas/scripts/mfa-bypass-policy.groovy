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

    logger.debug("Evaluating MFA bypass for principal [{}] with attributes [{}], service registration [{}], flow scope [{}]", 
        authentication.principal.id, principal.attributes, registeredService.getProperties()?.get("mfaAllowRegistration"), flowScope)

    // Stuff happens...

    // true means continue with MFA, false means skip it
    return true
}

