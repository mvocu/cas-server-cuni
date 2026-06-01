package org.apereo.cas.configuration.flow;

import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.stream.Stream;

public class CuniMfaCompositeWebflowConfigurer extends AbstractCasWebflowConfigurer
        implements CasWebflowConfigurer {

    public CuniMfaCompositeWebflowConfigurer(
            FlowBuilderServices flowBuilderServices,
            FlowDefinitionRegistry mainFlowDefinitionRegistry,
            ConfigurableApplicationContext applicationContext,
            CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
        setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationWebflowCustomizers(applicationContext)
                    .forEach(customizer -> {
                        customizer.getCandidateStatesForMultifactorAuthentication()
                                .forEach(name -> {
                                    val state = getState(flow, name);
                                    createTransitionForState(state,
                                            CasWebflowConstants.TRANSITION_ID_MFA_COMPOSITE,
                                            CasWebflowConstants.STATE_ID_MFA_COMPOSITE);
                                });
                    });
        }
    }
}
