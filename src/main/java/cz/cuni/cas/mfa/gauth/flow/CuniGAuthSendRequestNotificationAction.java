package cz.cuni.cas.mfa.gauth.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.action.EventFactorySupport;

@Slf4j
public class CuniGAuthSendRequestNotificationAction extends BaseCasWebflowAction {

    public CuniGAuthSendRequestNotificationAction(CasConfigurationProperties casProperties) {
        super();
    }

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        LOGGER.debug("XXX Executing action CuniGAuthSendRequestNotificationAction");
        return new EventFactorySupport().event(this, "null");
    }
}
