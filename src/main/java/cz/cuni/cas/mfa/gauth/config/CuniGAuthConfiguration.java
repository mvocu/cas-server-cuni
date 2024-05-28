package cz.cuni.cas.mfa.gauth.config;

import cz.cuni.cas.CuniConfigurationProperties;
import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import cz.cuni.cas.mfa.gauth.api.CuniGAuthNotificationService;
import cz.cuni.cas.mfa.gauth.flow.CuniGAuthSendConfirmationAction;
import cz.cuni.cas.mfa.gauth.flow.CuniGAuthSendRequestNotificationAction;
import cz.cuni.cas.mfa.gauth.flow.CuniGAuthWebflowConfigurer;
import cz.cuni.cas.mfa.gauth.service.CuniGAuthNotificationServiceImpl;
import cz.cuni.cas.mfa.gauth.web.CuniGAuthNotificationEndpoint;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowExecutionPlanConfigurer;
import org.apereo.cas.web.flow.actions.WebflowActionBeanSupplier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

import lombok.val;
import org.springframework.webflow.execution.Action;
import org.apache.commons.lang3.ArrayUtils;

@EnableWebSocketMessageBroker
@AutoConfiguration
@EnableConfigurationProperties({CasConfigurationProperties.class, CuniConfigurationProperties.class})
public class CuniGAuthConfiguration {
	private static final int WEBFLOW_CONFIGURER_ORDER = 200;

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	public WebSocketMessageBrokerConfigurer cuniGAuthWebSocketMessageBrokerConfigurer(
			final CasConfigurationProperties casProperties) {
		return new WebSocketMessageBrokerConfigurer() {
			@Override
			public void registerStompEndpoints(final StompEndpointRegistry registry) {
				registry.addEndpoint("/gauth-websocket")
						.setAllowedOrigins(casProperties.getAuthn().getQr().getAllowedOrigins().toArray(ArrayUtils.EMPTY_STRING_ARRAY))
						.addInterceptors(new HttpSessionHandshakeInterceptor())
						.withSockJS();
			}

			@Override
			public void configureMessageBroker(final MessageBrokerRegistry config) {
				config.enableSimpleBroker(CuniGAuthWebflowConstants.GAUTH_SIMPLE_BROKER_DESTINATION_PREFIX);
				config.setApplicationDestinationPrefixes("/gauth");
			}
		};
	}

	@ConditionalOnMissingBean(name = "cuniGAuthWebflowConfigurer")
	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
	@ConditionalOnMissingBean(name = "cuniGAuthNotificationService")
	public CuniGAuthNotificationService cuniGAuthNotificationService(
			final CuniConfigurationProperties cuniProperties) {
		return new CuniGAuthNotificationServiceImpl(cuniProperties);
	}

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@ConditionalOnMissingBean(name = CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_REQUEST_NOTIFICATION)
	public Action sendGAuthRequestNotificationAction(
			final ConfigurableApplicationContext applicationContext,
			final CasConfigurationProperties casProperties,
			@Qualifier("cuniGAuthNotificationService")
			final CuniGAuthNotificationService notificationService) {
		return WebflowActionBeanSupplier.builder()
				.withApplicationContext(applicationContext)
				.withProperties(casProperties)
				.withAction(() -> new CuniGAuthSendRequestNotificationAction(notificationService))
				.withId(CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_REQUEST_NOTIFICATION)
				.build()
				.get();
	}

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@ConditionalOnMissingBean(name = CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_CONFIRMATION)
	public Action sendGAuthConfirmationAction(
			final ConfigurableApplicationContext applicationContext,
			final CasConfigurationProperties casProperties,
			@Qualifier("cuniGAuthNotificationService")
			final CuniGAuthNotificationService notificationService) {
		return WebflowActionBeanSupplier.builder()
				.withApplicationContext(applicationContext)
				.withProperties(casProperties)
				.withAction(() -> new CuniGAuthSendConfirmationAction(notificationService))
				.withId(CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_CONFIRMATION)
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

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	public CuniGAuthNotificationEndpoint cuniGAuthNotificationEndpoint(
			final CasConfigurationProperties casProperties,
			@Qualifier("brokerMessagingTemplate")
			final SimpMessagingTemplate template
	) {
		return new CuniGAuthNotificationEndpoint(casProperties, template);
	}
}
