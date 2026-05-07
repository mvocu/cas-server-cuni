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
    def activate = [ "staff@ruk.cuni.cz", "employee@ruk.cuni.cz", "staff@lfp.cuni.cz", "employee@lfp.cuni.cz" ]

    def flowScope = requestContext.getFlowScope()
    def mfaAvailable = flowScope?.get("cuniMfaAvailableHandlers", [])
    def mfaEnabled = attributes?.cunimfapolicy?.contains("always") ?: false
    def mfaRegistrationAllowed = (registeredService.getProperties()?.get("mfaAllowRegistration") ?: ["false"]).contains("true")

    if(mfaEnabled || mfaRegistrationAllowed) {
         return InterruptResponse.none()
    }

    def isActivating = attributes?.edupersonscopedaffiliation?.any(it -> { return activate.contains(it) })

    if(!isActivating) {
         return InterruptResponse.none()
    }

    def response = new InterruptResponse(
         isActivating ? "screen.interrupt.mfa.message_activate('31.5.2026')" : "screen.interrupt.mfa.message('31.10.2026')",
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

    return response
}

