package cz.cuni.cas.mfa.trusted.config;

import cz.cuni.cas.mfa.trusted.flow.CuniMfaTrustedDeviceWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CuniMfaTrustedDeviceConfiguration implements CasWebflowExecutionPlanConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @ConditionalOnMissingBean(name="cuniMfaTrustedDeviceWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer cuniMfaTrustedDeviceWebflowConfigurer() {
    	return new CuniMfaTrustedDeviceWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry,
    			applicationContext, casProperties);
    }
    
    @Override
	public void configureWebflowExecutionPlan(final CasWebflowExecutionPlan plan) {
        plan.registerWebflowConfigurer(cuniMfaTrustedDeviceWebflowConfigurer());
	}

}
