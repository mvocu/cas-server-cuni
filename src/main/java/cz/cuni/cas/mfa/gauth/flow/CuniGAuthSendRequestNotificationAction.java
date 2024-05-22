package cz.cuni.cas.mfa.gauth.flow;

import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CuniGAuthSendRequestNotificationAction extends BaseCasWebflowAction {

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        LOGGER.debug("XXX Executing action CuniGAuthSendRequestNotificationAction for principal [{}]", principal.getId());
        val id = UUID.randomUUID().toString();
        val flowScope = requestContext.getFlowScope();
        flowScope.put("gauthChannel", id);
        flowScope.put("gauthPrefix", CuniGAuthWebflowConstants.GAUTH_SIMPLE_BROKER_DESTINATION_PREFIX);
        return null;
    }
}
