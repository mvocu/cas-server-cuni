package cz.cuni.cas.mfa.gauth.config;

import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import cz.cuni.cas.mfa.gauth.flow.CuniGAuthSendRequestNotificationAction;
import cz.cuni.cas.mfa.gauth.flow.CuniGAuthWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlan;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;

import lombok.val;
import org.springframework.webflow.execution.Action;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CuniGAuthConfiguration {
	private static final int WEBFLOW_CONFIGURER_ORDER = 100;

	@ConditionalOnMissingBean(name = "cuniGAuthWebflowConfigurer")
	@Bean
	public CasWebflowConfigurer cuniGAuthWebflowConfigurer(
			final CasConfigurationProperties casProperties,
			final ConfigurableApplicationContext applicationContext,
			@Qualifier("googleAuthenticatorFlowRegistry")
			final FlowDefinitionRegistry googleAuthenticatorFlowRegistry,
			@Qualifier(CasWebflowConstants.BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY)
			final FlowDefinitionRegistry loginFlowDefinitionRegistry,
			@Qualifier(CasWebflowConstants.BEAN_NAME_FLOW_BUILDER_SERVICES)
			final FlowBuilderServices flowBuilderServices) {
		val cfg = new CuniGAuthWebflowConfigurer(flowBuilderServices,
				loginFlowDefinitionRegistry, googleAuthenticatorFlowRegistry, applicationContext, casProperties,
				MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext));
		cfg.setOrder(WEBFLOW_CONFIGURER_ORDER);
		return cfg;
	}

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@ConditionalOnMissingBean(name = CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_REQUEST_NOTIFICATION)
	public Action sendGAuthRequestNotificationAction(
			final ConfigurableApplicationContext applicationContext,
			final CasConfigurationProperties casProperties) {
		return WebflowActionBeanSupplier.builder()
				.withApplicationContext(applicationContext)
				.withProperties(casProperties)
				.withAction(() -> new CuniGAuthSendRequestNotificationAction(casProperties))
				.withId(CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_REQUEST_NOTIFICATION)
				.build()
				.get();
	}

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@ConditionalOnMissingBean(name = "cuniGAuthWebflowExecutionPlanConfigurer")
	public CasWebflowExecutionPlanConfigurer cuniGAuthWebflowExecutionPlanConfigurer(
			@Qualifier("cuniGAuthWebflowConfigurer")
			final CasWebflowConfigurer cuniGAuthWebflowConfigurer) {
		return plan -> plan.registerWebflowConfigurer(cuniGAuthWebflowConfigurer);
	}

}
