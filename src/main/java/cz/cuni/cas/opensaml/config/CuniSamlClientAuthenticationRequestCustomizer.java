package cz.cuni.cas.opensaml.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.saml.client.SAML2Client;

import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
public class CuniSamlClientAuthenticationRequestCustomizer implements DelegatedClientAuthenticationRequestCustomizer {

    private final CasConfigurationProperties casProperties;

    /**
     * @param client
     * @param webContext
     */
    @Override
    public void customize(IndirectClient client, WebContext webContext) {
        val samlProperties = getClientProperties(client.getName()).get();
        val samlClient = (SAML2Client)client;
        if(samlProperties.getIdentityProviderEntityId() != null &&
                !samlProperties.getIdentityProviderEntityId().isEmpty()) {
            LOGGER.debug("Setting identity provider entity id to [{}] for SAML2 client [{}]",
                    samlProperties.getIdentityProviderEntityId(), client.getName());
            samlClient.getConfiguration().setIdentityProviderEntityId(samlProperties.getIdentityProviderEntityId());
        }
    }

    /**
     * @param client
     * @param webContext
     * @return
     */
    @Override
    public boolean supports(IndirectClient client, WebContext webContext) {
        if(!(client instanceof SAML2Client)) {
            return false;
        }
        SAML2Client saml2Client = (SAML2Client) client;
        Optional<Pac4jSamlClientProperties> saml = getClientProperties(client.getName());
        if(saml.isEmpty()) {
            return false;
        }
        return saml.get().getDiscoveryServiceUrl() != null && !saml.get().getDiscoveryServiceUrl().isEmpty();
    }

    /**
     * @param webContext
     * @param client
     * @param currentService
     * @return
     */
    @Override
    public boolean isAuthorized(WebContext webContext, IndirectClient client, WebApplicationService currentService) {
        return true;
    }

    private Optional<Pac4jSamlClientProperties> getClientProperties(String name) {
        return casProperties.getAuthn().getPac4j().getSaml()
                .stream()
                .filter(saml -> saml.getClientName().equals(name))
                .findFirst();
    }

}
