package cz.cuni.cas.opensaml.flow;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.LinkedHashMap;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CuniDiscoveryRedirectAction extends AbstractAction {

    protected final CasConfigurationProperties casProperties;
    protected final DelegatedClientAuthenticationConfigurationContext configContext;

    // private Expression resourceUri;
    
    /*
    public CuniDiscoveryRedirectAction(SpringELExpressionParser parser) {
        this.resourceUri = parser.parseExpression(
                "#{requestScope." +
                        CuniDiscoveryWebflowConstants.REQUEST_VAR_ID_DELEGATED_AUTHENTICATION_REDIRECT_URL +
                        "}%26execution%3D#{getFlowExecutionContext().getKey().toString()}",
                (new FluentParserContext())
                        .expectResult(String.class)
                        .template()
        );
    }
    */

    /**
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        val requestScope = context.getRequestScope();

        String discoveryUrl = requestScope.getString(
                CuniDiscoveryWebflowConstants.VAR_ID_DELEGATED_AUTHENTICATION_DISCOVERY_URL);
        String clientName = requestScope.getString(
                CuniDiscoveryWebflowConstants.VAR_ID_DELEGATED_AUTHENTICATION_CLIENT_NAME);
        String entityId = requestScope.getString(
                CuniDiscoveryWebflowConstants.VAR_ID_DELEGATED_AUTHENTICATION_ENTITY_ID);
        respondWithExternalRedirect(context, discoveryUrl, clientName, entityId);
        return this.success();
    }

    private void respondWithExternalRedirect(RequestContext requestContext, String discoveryUrl, String clientName,
                                             String entityId)
            throws Exception {
        val builder = new URIBuilder(discoveryUrl);
        builder.addParameter("entityID", entityId);
        String flowId = saveWebflowKey(requestContext, clientName);
        val returnBuilder = new URIBuilder((new StringBuilder(casProperties.getServer().getPrefix()))
                .append("/discovery/")
                .append(flowId)
                .toString()
        );
        builder.addParameter("return",  returnBuilder.toString());
        val url = builder.toString();

        LOGGER.debug("Redirecting to discovery [{}] via client [{}]", url, clientName);
        requestContext.getExternalContext().requestExternalRedirect(url);
    }

    private String saveWebflowKey(RequestContext requestContext, String clientName) throws Exception {
        val transientFactory = (TransientSessionTicketFactory) configContext.getTicketFactory().get(TransientSessionTicket.class);
        val properties = new LinkedHashMap<>();
        properties.put(
                CuniDiscoveryWebflowConstants.PROPERTY_ID_WEBFLOW_KEY,
                requestContext.getFlowExecutionContext().getKey().toString()
        );
        properties.put(
                Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER,
                clientName
        );
        String id = UUID.randomUUID().toString();
        val ticket = transientFactory.create(id, properties);
        configContext.getTicketRegistry().addTicket(ticket);
        return ticket.getId();
        /*
        webContext.getRequestParameter(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN)
            .or(() -> Optional.of(Boolean.toString(RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_FORCE_AUTHN.isAssignedTo(registeredService))))
            .filter(value -> StringUtils.equalsIgnoreCase(value, "true"))
            .ifPresent(attr -> properties.put(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true));
        webContext.getRequestParameter(RedirectionActionBuilder.ATTRIBUTE_PASSIVE)
            .or(() -> Optional.of(Boolean.toString(RegisteredServiceProperty.RegisteredServiceProperties.DELEGATED_AUTHN_PASSIVE_AUTHN.isAssignedTo(registeredService))))
            .filter(value -> StringUtils.equalsIgnoreCase(value, "true"))
            .ifPresent(attr -> properties.put(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true));
         */
    }
}
