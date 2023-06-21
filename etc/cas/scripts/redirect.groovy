import org.apereo.cas.web.*
import org.pac4j.core.context.*
import org.apereo.cas.pac4j.*
import org.apereo.cas.web.support.*
import java.util.stream.*
import java.util.*
import org.apereo.cas.configuration.model.support.delegation.*
import org.apache.http.client.utils.URIBuilder

def run(Object[] args) {
    def requestContext = args[0]
    def service = args[1]
    def registeredService = args[2]
    def providers = args[3] as Set<DelegatedClientIdentityProviderConfiguration>
    def applicationContext = args[4]
    def logger = args[5]


    def params = requestContext.getRequestParameters()
    def client = params['acr_values']
    if(client == null) {
        def svc = params['service']
        params = svc ? (new URIBuilder(svc)).getQueryParams() : null
        client = params?.find( { it.getName() == 'acr_values' } )?.getValue()
    }

    providers.forEach(provider -> {
        logger.info("Checking ${provider.name}...")
        if (provider.name.equals(client)) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
            return provider
        }
    })
    return null
}
