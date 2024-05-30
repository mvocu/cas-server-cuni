package org.apereo.cas.support.pac4j.authentication.clients;

import cz.cuni.cas.opensaml.NIAUtils;
import lombok.extern.slf4j.Slf4j;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;

import com.github.benmanes.caffeine.cache.Cache;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.saml.store.SAMLMessageStoreFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collection;
import java.util.Optional;

/**
 * This is {@link DefaultDelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultDelegatedClientFactory extends BaseDelegatedClientFactory implements DisposableBean {

    public DefaultDelegatedClientFactory(
        final CasConfigurationProperties casProperties,
        final Collection<DelegatedClientFactoryCustomizer> customizers,
        final CasSSLContext casSSLContext,
        final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
        final Cache<String, Collection<IndirectClient>> clientsCache) {
        super(casProperties, customizers, casSSLContext, samlMessageStoreFactory, clientsCache);
    }

    @Override
    protected Collection<IndirectClient> loadClients() {
        return buildAllIdentityProviders(casProperties);
    }

    @Override
    public void destroy() {
        Optional.ofNullable(getCachedClients())
            .stream()
            .filter(client -> client instanceof SAML2Client)
            .map(SAML2Client.class::cast)
            .forEach(Unchecked.consumer(SAML2Client::destroy));
    }

    @Override
    protected void configureClient(IndirectClient client, Pac4jBaseClientProperties clientProperties, CasConfigurationProperties givenProperties) {
        super.configureClient(client, clientProperties, givenProperties);
        if(clientProperties instanceof Pac4jSamlClientProperties && client instanceof SAML2Client) {
            SAML2Client samlClient = (SAML2Client) client;
            Pac4jSamlClientProperties samlProperties = (Pac4jSamlClientProperties) clientProperties;
            SAML2Configuration cfg = samlClient.getConfiguration();
            if(samlProperties.getExtensions().contains("nia")) {
                LOGGER.debug("Configuring NIA extension for SAML2 client [{}]", samlClient.getName());
                cfg.setAuthnRequestExtensions(() -> NIAUtils.buildNIARequestExtension(samlProperties));
            }
            if(samlProperties.getIdentityProviderEntityId() != null &&
                    !samlProperties.getIdentityProviderEntityId().isEmpty()) {
                LOGGER.debug("Setting identity provider entity id to [{}] for SAML2 client [{}]",
                        samlProperties.getIdentityProviderEntityId(), samlClient.getName());
                cfg.setIdentityProviderEntityId(samlProperties.getIdentityProviderEntityId());
            }
        }
    }


}
