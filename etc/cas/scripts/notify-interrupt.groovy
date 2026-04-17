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

    def response = new InterruptResponse(
         "screen.interrupt.mfa.message_activate('31.5.2026', '31.10.2026')",
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

