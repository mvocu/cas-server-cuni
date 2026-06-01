package org.apereo.cas.configuration;

//import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.apereo.cas.configuration.flow.CuniMfaCompositeWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

@AutoConfiguration
@Configuration(value = "CasOverlayOverrideConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasOverlayOverrideConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name="cuniMfaCompositeWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer cuniMfaCompositeWebflowConfigurer() {
        return new CuniMfaCompositeWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties);
    }

    /*
    @Bean
    public MyCustomBean myCustomBean() {
        ...
    }
     */
}
