package cz.cuni.cas.mfa.gauth.flow;

import cz.cuni.cas.CuniConfigurationProperties;
import cz.cuni.cas.mfa.gauth.api.CuniGAuthNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

@Slf4j
@RequiredArgsConstructor
public class CuniGAuthSendConfirmationAction extends BaseCasWebflowAction {

    private final CuniGAuthNotificationService notificationService;

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        val flowScope = requestContext.getFlowScope();
        val channelId = flowScope.getString("gauthChannel");
        if(channelId != null) {
            val response = notificationService.sendConfirmationRequest(String.valueOf(channelId));
        }
        return null;
    }
}
