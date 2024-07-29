package cz.cuni.cas.opensaml.flow;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
@RequiredArgsConstructor
public class CuniSamlFinalizeDiscoveryAction  extends BaseCasWebflowAction {

    protected final CasConfigurationProperties casProperties;
    protected final DelegatedClientAuthenticationConfigurationContext configContext;


    /**
     * @param requestContext 
     * @return
     * @throws Exception
     */
    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        String selectedIdP = request.getParameter("entityID");
        LOGGER.debug("Saving[{}] as entityID from discovery service to conversation scope");
        if(selectedIdP != null) {
            requestContext.getConversationScope().put(
                    CuniDiscoveryWebflowConstants.CONVERSATION_VAR_ID_DELEGATED_AUTHENTICATION_IDP,
                    selectedIdP
            );
        }
        return null;
    }
}
