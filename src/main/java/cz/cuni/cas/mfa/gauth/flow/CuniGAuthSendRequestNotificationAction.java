package cz.cuni.cas.mfa.gauth.flow;

import cz.cuni.cas.CuniConfigurationProperties;
import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import cz.cuni.cas.mfa.gauth.api.CuniGAuthNotificationService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(CuniConfigurationProperties.class)
public class CuniGAuthSendRequestNotificationAction extends BaseCasWebflowAction {

    private final CuniGAuthNotificationService notificationService;

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        val service = WebUtils.getRegisteredService(requestContext);

        val id = UUID.randomUUID().toString();
        val request = new CuniGAuthNotificationService.NotificationRequest(id, principal.getId(),
                service != null ? service.getFriendlyName() : null);
        val response = notificationService.sendNotificationRequest(request);
        if(response != null && response.getCode() == 200) {
            LOGGER.debug("Notification request sent successfully, setting scope variables.");
        }
        val flowScope = requestContext.getFlowScope();
        flowScope.put("gauthChannel", id);
        flowScope.put("gauthPrefix", CuniGAuthWebflowConstants.GAUTH_SIMPLE_BROKER_DESTINATION_PREFIX);
        return null;
    }
}
