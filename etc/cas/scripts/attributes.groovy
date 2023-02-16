import java.util.*

def Map<String, List<Object>> run(final Object... args) {
    def username = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def appContext = args[4]

    logger.debug("[{}]: Producing additional attributes for uid [{}], current attributes [{}]", this.class.simpleName, username, attributes)

    def values = [:]

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

    logger.debug("[{}]: Producing additional attributes for uid [{}], new attributes [{}]", this.class.simpleName, username, values)

    return values
}
