package cz.cuni.cas.opensaml.config;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import cz.cuni.cas.opensaml.flow.CuniDiscoveryWebflowConfigurer;
import cz.cuni.cas.opensaml.flow.CuniSamlDiscoveryAction;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientFactoryCustomizer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

@AutoConfiguration
@Configuration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CuniSamlClientDelegationConfiguration {

    private static final int WEBFLOW_CONFIGURER_ORDER = 200;

    @ConditionalOnMissingBean(name="niaClientCustomizer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientFactoryCustomizer<SAML2Client> niaClientCustomizer(
            final CasConfigurationProperties casProperties
    ) {
        return new CuniSamlClientCustomizer(casProperties);
    }

    @ConditionalOnMissingBean(name="discoveryRequestCustomizer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public DelegatedClientAuthenticationRequestCustomizer discoveryRequestCustomizer(
            final CasConfigurationProperties casProperties
    ) {
        return new CuniSamlClientAuthenticationRequestCustomizer(casProperties);
    }

    @ConditionalOnMissingBean(name = "dscoveryWebflowConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasWebflowConfigurer cuniDiscoveryWebflowConfigurer(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
            final FlowDefinitionRegistry loginFlowDefinitionRegistry,
            @Qualifier("delegatedClientRedirectFlowRegistry")
            final FlowDefinitionRegistry delegatedClientRedirectFlowRegistry,
            @Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
            final FlowBuilderServices flowBuilderServices) {
        val cfg = new CuniDiscoveryWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                delegatedClientRedirectFlowRegistry, applicationContext, casProperties);
        cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
        return cfg;
    }

    @ConditionalOnMissingBean(name = CuniDiscoveryWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DISCOVERY)
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public Action sendGAuthConfirmationAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(DelegatedClientAuthenticationConfigurationContext.DEFAULT_BEAN_NAME)
            final DelegatedClientAuthenticationConfigurationContext configContext) {
        return WebflowActionBeanSupplier.builder()
                .withApplicationContext(applicationContext)
                .withProperties(casProperties)
                .withAction(() -> new CuniSamlDiscoveryAction(casProperties, configContext))
                .withId(CuniDiscoveryWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DISCOVERY)
                .build()
                .get();
    }

}
