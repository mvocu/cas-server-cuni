import org.apereo.cas.authentication.principal.ClientCredential
import org.pac4j.saml.credentials.SAML2Credentials
import org.springframework.webflow.execution.RequestContextHolder
import groovy.json.JsonOutput

def Map<String, List<Object>> run(final Object... args) {
    def username = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def appContext = args[4]

    logger.debug("[{}]: Producing additional attributes for uid [{}], current attributes [{}]", this.class.simpleName, username, attributes)

    def values = ["username" : username ]

    def matcher =  (attributes["cunimailverificationexpiration"] =~ /(\d\d\d\d)(\d\d)(\d\d)\d\d\d\d\d\dZ/)
    if( matcher ) {
       def (year, month, day) = matcher[0][1..3]*.toInteger()
       def expiration = new Date(year, month, day)
       def now = new Date()
       if(now < expiration) {
            values["email"] = attributes["cuniauthorizedmail"]
	    values["email_verified"] = true
       } else {
            values["email"] = attributes["mail"]
	    values["email_verified"] = false
       }
    }

    def requestContext = RequestContextHolder.getRequestContext()
    def clientCredential = requestContext?.getRequestScope()?.get("credential", ClientCredential.class)

    values["auth_delegated_client"] = clientCredential?.getClientName()
    values["auth_saml2_credentials"] = (clientCredential?.getCredentials() instanceof SAML2Credentials) 
	? JsonOutput.toJson(clientCredential?.getCredentials()) : null 

    // amr as presented by remote client
    def amr = attributes["amr"] ?: []
    if(clientCredential?.getCredentials() instanceof SAML2Credentials) {
        def saml2creds = (SAML2Credentials)clientCredential.getCredentials()
        amr.addAll(saml2creds.authnContexts)
    }
    values["auth_amr"] = amr

    // define LoA based on remote client and amr
    def loa = "http://cas.cuni.cz/LoA/none"
    if(attributes["cuniauthservice"] == null || attributes["cuniauthservice"].isEmpty()) {
	loa = "http://cas.cuni.cz/LoA/low"
    }
    logger.debug("XXX Producing LoA based on remote client [{}] and authentication method [{}]", values["auth_delegated_client"], amr)
    logger.debug("XXX typeof amr [{}]", amr.class.simpleName)
    logger.debug("XXX typeof amr[0] [{}]", (amr instanceof List && amr.size() > 0) ? amr.first().class.simpleName : null)

    switch(values["auth_delegated_client"]) {

	case "NIA":
		if(amr.contains("http://eidas.europa.eu/LoA/low")) {
			loa = "http://cas.cuni.cz/LoA/low";
		} else if(amr.contains("http://eidas.europa.eu/LoA/substantial")) {
			loa = "http://cas.cuni.cz/LoA/substantial"
		} else if(amr.contains("http://eidas.europa.eu/LoA/high")) {
			loa = "http://cas.cuni.cz/LoA/high"
		}
		break;

	case "svipeid":
		if(amr.contains("face")) {
                        loa = "http://cas.cuni.cz/LoA/high"
		} else if(amr.contains("user")) {
                        loa = "http://cas.cuni.cz/LoA/substantial"
		} else {
			loa = "http://cas.cuni.cz/LoA/substantial"
		}
		break;

	case "eduid":
		loa = "http://cas.cuni.cz/LoA/none"
                logger.debug("XXX value of edu_assurance [{}]", attributes["edu_assurance"])
		break;

	case "edugain":
		loa = "http://cas.cuni.cz/LoA/none"
                logger.debug("XXX value of edu_assurance [{}]", attributes["edu_assurance"])
		break;

	default:
		break;
    }
    values["auth_loa"] = loa

    //logger.debug("[{}]: Producing additional attributes for uid [{}], new attributes [{}] from context [{}]", this.class.simpleName, username, values, RequestContextHolder.getRequestContext()) 

    return values
}
