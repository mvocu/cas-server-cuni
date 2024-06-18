package cz.cuni.cas.opensaml.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.pac4j.saml.client.SAML2Client;

@Slf4j
@RequiredArgsConstructor
public class CuniNIAClientCustomizer implements DelegatedClientFactoryCustomizer<SAML2Client> {

    private final CasConfigurationProperties casProperties;

    /**
     * @param client
     */
    @Override
    public void customize(SAML2Client client) {
        LOGGER.info("Applying custom configuration to client {}", client.getName());
    }
}
