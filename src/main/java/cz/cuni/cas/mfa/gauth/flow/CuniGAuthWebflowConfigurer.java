package cz.cuni.cas.mfa.gauth.flow;

import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.apereo.cas.web.flow.configurer.AbstractCasMultifactorWebflowConfigurer;
import org.apereo.cas.web.flow.configurer.CasMultifactorWebflowCustomizer;

import java.util.List;
import java.util.Optional;
import lombok.val;

public class CuniGAuthWebflowConfigurer
        extends AbstractCasMultifactorWebflowConfigurer
        implements CasWebflowConfigurer {


    public CuniGAuthWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                      final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                      final FlowDefinitionRegistry flowDefinitionRegistry,
                                      final ConfigurableApplicationContext applicationContext,
                                      final CasConfigurationProperties casProperties,
                                      final List<CasMultifactorWebflowCustomizer> mfaFlowCustomizers) {
        super(flowBuilderServices, loginFlowDefinitionRegistry,
                applicationContext, casProperties, Optional.of(flowDefinitionRegistry),
                mfaFlowCustomizers);
    }

    @Override
    protected void doInitialize() {
        multifactorAuthenticationFlowDefinitionRegistries
                .stream()
                .filter(registry -> registry.containsFlowDefinition(casProperties.getAuthn().getMfa().getGauth().getId()))
                .forEach(registry -> {
            val flow = getFlow(registry, casProperties.getAuthn().getMfa().getGauth().getId());
            val viewLoginFormState = flow.getStateInstance(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            viewLoginFormState.getEntryActionList().add(
                    createEvaluateAction(CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_REQUEST_NOTIFICATION));
            val realSubmitState = flow.getStateInstance(CasWebflowConstants.STATE_ID_REAL_SUBMIT);
            realSubmitState.getEntryActionList().add(
                    createEvaluateAction(CuniGAuthWebflowConstants.ACTION_ID_SEND_GAUTH_CONFIRMATION));
        });
    }
}
