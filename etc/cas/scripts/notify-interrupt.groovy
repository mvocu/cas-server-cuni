import org.apereo.cas.interrupt.InterruptResponse

import org.springframework.webflow.execution.RequestContextHolder
import groovy.json.JsonOutput

def run(final Object... args) {
    def principal = args[0]
    def attributes = args[1]
    def service = args[2]
    def registeredService = args[3]
    def requestContext = args[4]
    def logger = args[5]

    def block = false
    def ssoEnabled = true

    def flowScope = requestContext.getFlowScope()
    def mfaAvailable = flowScope?.get("cuniMfaAvailableHandlers", [])
    def mfaEnabled = attributes?.cunimfapolicy?.contains("always") ?: false
    def mfaRegistrationAllowed = (registeredService.getProperties()?.get("mfaAllowRegistration") ?: ["false"]).contains("true")

    if(mfaEnabled || mfaRegistrationAllowed) {
         return InterruptResponse.none()
    }

    def isActivating = attributes?.edupersonscopedaffiliation?.any(it -> {
        return String.valueOf(it).startsWith("employee@") || String.valueOf(it).startsWith("staff@") 
    })

    if(!isActivating) {
         logger.debug("XXX no interrupt notification for [{}] with affiliations [{}]", principal?.id, attributes?.edupersonscopedaffiliation)
         return InterruptResponse.none()
    }

    def response = new InterruptResponse(
        // isActivating ? "screen.interrupt.mfa.message_activate('31.5.2026')" : "screen.interrupt.mfa.message('31.10.2026')",
         "screen.interrupt.mfa.message('31.10.2026')",
         [ "activatemfa" : "https://ldapuser.cuni.cz/idportal/mfa"],
         block,
         ssoEnabled
    )

    /*
    response.data = [ 
       "mfaAvailable" : mfaAvailable.toString(),
       "mfaPolicy" : attributes?.cunimfapolicy,
       "mfaEnabled" : mfaEnabled,
       "auth_loa" : attributes?.auth_loa,
       "affiliation" : attributes?.edupersonscopedaffiliation
    ] 
    */

    logger.debug("XXX showing interrupt notification for [{}] with affiliations [{}]", principal?.id, attributes?.edupersonscopedaffiliation)

    return response
}

