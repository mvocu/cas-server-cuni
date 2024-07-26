package cz.cuni.cas.opensaml.flow;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import java.util.List;
import java.util.Optional;

@Slf4j
public class CuniDiscoveryWebflowConfigurer
        extends AbstractCasWebflowConfigurer
        implements CasWebflowConfigurer {


    private final FlowDefinitionRegistry redirectFlowRegistry;

    public CuniDiscoveryWebflowConfigurer(FlowBuilderServices flowBuilderServices,
                                             FlowDefinitionRegistry mainFlowDefinitionRegistry,
                                             FlowDefinitionRegistry redirectFlowRegistry,
                                             ConfigurableApplicationContext applicationContext,
                                             CasConfigurationProperties casProperties) {
        super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
        this.redirectFlowRegistry = redirectFlowRegistry;
    }

    /**
     * Replace the current start state with newly created discoveryState and corresponding action.
     */
    @Override
    protected void doInitialize() {
        val redirectFlow = this.redirectFlowRegistry.getFlowDefinition(CasWebflowConfigurer.FLOW_ID_DELEGATION_REDIRECT);
        val storeWebflowAction = redirectFlow.getState(CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_STORE);

        LOGGER.info("Adding discovery state to the [{}] delegation authentication redirect flow.", redirectFlow.getId());
        val discoveryState = createActionState((Flow)redirectFlow,
                CuniDiscoveryWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_DISCOVERY,
                CuniDiscoveryWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_DISCOVERY);
        createTransitionForState(discoveryState,
                CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS,
                CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_STORE);
        createTransitionForState(discoveryState,
                CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_REDIRECT,
                CasWebflowConstants.STATE_ID_SUCCESS);
        createStateDefaultTransition(discoveryState, storeWebflowAction);
        setStartState((Flow)redirectFlow, discoveryState);
    }

}
