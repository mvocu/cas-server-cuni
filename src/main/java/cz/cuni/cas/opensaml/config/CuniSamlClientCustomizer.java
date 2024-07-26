package cz.cuni.cas.opensaml.config;

import cz.cuni.cas.opensaml.NIAUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.pac4j.core.client.Client;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CuniSamlClientCustomizer implements DelegatedClientFactoryCustomizer<Client> {

    private final CasConfigurationProperties casProperties;

    /**
     * @param client
     */
    @Override
    public void customize(Client client) {
        if(!(client instanceof SAML2Client)) {
            return;
        }
        SAML2Client saml2Client = (SAML2Client)client;
        Optional<Pac4jSamlClientProperties> saml = getClientProperties(saml2Client.getName());
        if(saml.isPresent()) {
            LOGGER.info("Applying custom configuration to client {}", saml2Client.getName());

            Pac4jSamlClientProperties samlProperties = saml.get();
            SAML2Configuration cfg = saml2Client.getConfiguration();
            if(samlProperties.getExtensions().contains("nia")) {
                LOGGER.debug("Configuring NIA extension for SAML2 client [{}]", saml2Client.getName());
                cfg.setAuthnRequestExtensions(() -> NIAUtils.buildNIARequestExtension(samlProperties));
            }
            if(samlProperties.getIdentityProviderEntityId() != null &&
                    !samlProperties.getIdentityProviderEntityId().isEmpty()) {
                LOGGER.debug("Setting identity provider entity id to [{}] for SAML2 client [{}]",
                        samlProperties.getIdentityProviderEntityId(), saml2Client.getName());
                cfg.setIdentityProviderEntityId(samlProperties.getIdentityProviderEntityId());
            }
        }
    }

    private Optional<Pac4jSamlClientProperties> getClientProperties(String name) {
        return casProperties.getAuthn().getPac4j().getSaml()
                .stream()
                .filter(saml -> saml.getClientName().equals(name))
                .findFirst();
    }
}
