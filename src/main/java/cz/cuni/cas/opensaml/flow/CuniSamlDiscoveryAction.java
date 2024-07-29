package cz.cuni.cas.opensaml.flow;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.net.URI;
import java.util.Optional;


@Slf4j
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

        String selectedIdP = request.getParameter("entityID");

        if(selectedIdP != null) {
            LOGGER.debug("Discovery service seems to have chosen IdP [{}]", selectedIdP);
            return new Event(this, CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        val samlProperties = getClientProperties(clientName);
        if(samlProperties.isEmpty()) {
            LOGGER.info("No discovery configuration for [{}], going on with redirection flow", clientName);
            return new Event(this, CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        val client = getClient(clientName);
        if(client == null) {
            LOGGER.warn("No client with name [{}] found for SAML2 discovery", clientName);
            return new Event(this, CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS);
        }
        respondWithExternalRedirect(requestContext, samlProperties.get().getDiscoveryServiceUrl(), clientName,
                client.getServiceProviderResolvedEntityId());
        return new Event(this, CuniDiscoveryWebflowConstants.TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_REDIRECT);
    }

    private void respondWithExternalRedirect(RequestContext requestContext, String discoveryUrl, String clientName,
                                             String entityId)
            throws Exception {
        val builder = new URIBuilder(discoveryUrl);
        builder.addParameter("entityID", entityId);
        val returnBuilder = new URIBuilder(requestContext.getFlowExecutionUrl());
        returnBuilder.setParameter(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, clientName);
        returnBuilder.addParameter("execution", requestContext.getFlowExecutionContext().getKey().toString());
        returnBuilder.addParameter("_eventId", "success");
        builder.addParameter("return",  returnBuilder.toString());
        val url = builder.toString();

        LOGGER.debug("Redirecting to discovery [{}] via client [{}]", url, clientName);
        requestContext.getExternalContext().requestExternalRedirect(url);

    }

    private Optional<Pac4jSamlClientProperties> getClientProperties(String name) {
        return casProperties.getAuthn().getPac4j().getSaml()
                .stream()
                .filter(saml -> saml.getClientName().equals(name))
                .findFirst();
    }

    private SAML2Client getClient(String clientName) {
        val client = configContext.getClients().findClient(clientName);
        if(client.isEmpty() || !(client.get() instanceof SAML2Client)) {
            return null;
        }
        return (SAML2Client) client.get();
    }
}
