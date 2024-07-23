package cz.cuni.cas.opensaml.config;

import cz.cuni.cas.opensaml.NIAUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class CuniSamlClientCustomizer implements DelegatedClientFactoryCustomizer<SAML2Client> {

    private final CasConfigurationProperties casProperties;

    /**
     * @param client
     */
    @Override
    public void customize(SAML2Client client) {
        Optional<Pac4jSamlClientProperties> saml = getClientProperties(client.getName());
        if(saml.isPresent()) {
            LOGGER.info("Applying custom configuration to client {}", client.getName());

            Pac4jSamlClientProperties samlProperties = saml.get();
            SAML2Configuration cfg = client.getConfiguration();
            if(samlProperties.getExtensions().contains("nia")) {
                LOGGER.debug("Configuring NIA extension for SAML2 client [{}]", client.getName());
                cfg.setAuthnRequestExtensions(() -> NIAUtils.buildNIARequestExtension(samlProperties));
            }
            /*
            if(samlProperties.getIdentityProviderEntityId() != null &&
                    !samlProperties.getIdentityProviderEntityId().isEmpty()) {
                LOGGER.debug("Setting identity provider entity id to [{}] for SAML2 client [{}]",
                        samlProperties.getIdentityProviderEntityId(), client.getName());
                cfg.setIdentityProviderEntityId(samlProperties.getIdentityProviderEntityId());
            }
            */
        }
    }

    private Optional<Pac4jSamlClientProperties> getClientProperties(String name) {
        return casProperties.getAuthn().getPac4j().getSaml()
                .stream()
                .filter(saml -> saml.getClientName().equals(name))
                .findFirst();
    }
}
