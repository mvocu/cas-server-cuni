package cz.cuni.cas.mfa.trusted.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CuniMfaTrustedDeviceWebflowConfigurer extends AbstractCasWebflowConfigurer 
implements CasWebflowConfigurer {

	public CuniMfaTrustedDeviceWebflowConfigurer(
			FlowBuilderServices flowBuilderServices,
			FlowDefinitionRegistry mainFlowDefinitionRegistry,
			ConfigurableApplicationContext applicationContext,
			CasConfigurationProperties casProperties) 
	{
		super(flowBuilderServices, mainFlowDefinitionRegistry, applicationContext, casProperties);
	}

	@Override
	protected void doInitialize() {
		var flow = super.getLoginFlow();
		
		if(flow.containsState(CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION)) {

			//log.debug("Found webflow state " + CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION);
			
			ActionState state = (ActionState) flow.getState(CasWebflowConstants.ACTION_ID_MFA_SET_TRUST_ACTION);
			//state.getEntryActionList().add(null);
		}
	}
}
