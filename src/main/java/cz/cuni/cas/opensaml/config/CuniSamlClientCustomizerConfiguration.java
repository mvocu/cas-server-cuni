package cz.cuni.cas.opensaml.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CuniSamlClientCustomizerConfiguration {

    @ConditionalOnMissingBean(name="niaClientCustomizer")
    @Bean
    public DelegatedClientFactoryCustomizer<SAML2Client> niaClientCustomizer(
            final CasConfigurationProperties casProperties
    ) {
        return new CuniSamlClientCustomizer(casProperties);
    }

    @ConditionalOnMissingBean(name="discoveryRequestCustomizer")
    @Bean
    public DelegatedClientAuthenticationRequestCustomizer discoveryRequestCustomizer(
            final CasConfigurationProperties casProperties
    ) {
        return new CuniSamlClientAuthenticationRequestCustomizer(casProperties);
    }
}
