import org.springframework.webflow.execution.RequestContextHolder
import org.apereo.cas.services.RegisteredService
import org.apereo.cas.web.support.WebUtils

def run(final Object... args) {
    def response = args[0]
    def configuration = args[1]
    def logger = args[2]
    def applicationContext = args[3]

    // Obtain request/service context
    def requestContext = RequestContextHolder.getRequestContext()
    def targetService = requestContext ? WebUtils.getRegisteredService(requestContext) : null

    logger.debug("XXX Evaluating password policy for service: {}", targetService?.id)

    // Specify the service ID / regex pattern to bypass
    def bypassedServices = [ "10000029" ]

    if (targetService != null && bypassedServices.any { targetService.getId() ==~ it }) {
        logger.info("XXX Bypassing password expiration warning for service {}", targetService.id)
        return [] // Return empty warnings list to skip warning screen
    }

    // Default LDAP policy warning processing for other services...
    def accountStateHandler = configuration.getAccountStateHandler()
    return accountStateHandler ? accountStateHandler.handle(response, configuration) : []
}
