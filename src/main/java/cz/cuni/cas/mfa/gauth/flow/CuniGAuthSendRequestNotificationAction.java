package cz.cuni.cas.mfa.gauth.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.action.EventFactorySupport;

@Slf4j
@RequiredArgsConstructor
public class CuniGAuthSendRequestNotificationAction extends BaseCasWebflowAction {

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        LOGGER.debug("XXX Executing action CuniGAuthSendRequestNotificationAction for principal [{}]", principal.getId());
        return new EventFactorySupport().event(this, "null");
    }
}
