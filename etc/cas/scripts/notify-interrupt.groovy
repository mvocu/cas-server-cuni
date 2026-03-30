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

    return new InterruptResponse("Message", [link1:"ldapuser.cuni.cz/idportal/mfa", link2:"ldapuser.cuni.cz/idportal/ext"], block, ssoEnabled)
}
