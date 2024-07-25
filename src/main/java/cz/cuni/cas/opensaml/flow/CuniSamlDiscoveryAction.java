package cz.cuni.cas.opensaml.flow;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@RequiredArgsConstructor
public class CuniSamlDiscoveryAction extends BaseCasWebflowAction {

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
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);

        val webContext = new JEEContext(request, response);
        val clientName = configContext.getDelegatedClientNameExtractor().extract(webContext)
                .orElseGet(() -> (String) request.getAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER));

        String selectedIdP = request.getParameter(CuniDiscoveryWebflowConstants.CONVERSATION_VAR_ID_DELEGATED_AUTHENTICATION_IDP);

        if(selectedIdP != null) {
            return new Event(this, CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        // create redirect to discovery service
        return new Event(this, CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_REDIRECT);
    }
}
