import org.apereo.cas.interrupt.InterruptResponse

def run(final Object... args) {
    def principal = args[0]
    def attributes = args[1]
    def service = args[2]
    def registeredService = args[3]
    def requestContext = args[4]
    def logger = args[5]

    def block = false
    def ssoEnabled = true

    def flowScope = requestContext?.getFlowScope()
    def mfaAvailable = flowScope?.get("cuniMfaAvailableHandlers", [])
    def mfaEnabled = attributes?.cunimfapolicy?.contains("always") ?: false


    def response = new InterruptResponse(
            "screen.interrupt.mfa.message(31.5.2026)",
            [ "screen.interrupt.mfa.link": "https://ldapuser.cuni.cz/idportal/mfa"],
            block,
            ssoEnabled
    )

    response.data = [
            "mfaAvailable" : mfaAvailable.toString()
    ]

    return response
}
