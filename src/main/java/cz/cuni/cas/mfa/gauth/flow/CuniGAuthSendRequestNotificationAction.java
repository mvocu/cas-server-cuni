package cz.cuni.cas.mfa.gauth.flow;

import cz.cuni.cas.CuniConfigurationProperties;
import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import cz.cuni.cas.mfa.gauth.api.CuniGAuthNotificationService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CuniGAuthSendRequestNotificationAction extends BaseCasWebflowAction {

    private final CuniGAuthNotificationService notificationService;
    private final CuniConfigurationProperties cuniProperties;

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        val service = WebUtils.getRegisteredService(requestContext);
        val nameAttr = cuniProperties.getGauth().getNameAttribute();
        val mailAttr = cuniProperties.getGauth().getEmailAttribute();

        val id = UUID.randomUUID().toString();
        val request = new CuniGAuthNotificationService.NotificationRequest(id, principal.getId(),
                service != null ? service.getFriendlyName() : null);
        if(principal.getAttributes().containsKey(nameAttr)) {
            request.setName(getFirstString(principal.getAttributes().get(nameAttr)));
        }
        if(principal.getAttributes().containsKey(mailAttr)) {
            request.setEmail(getFirstString(principal.getAttributes().get(mailAttr)));
        }
        request.setBrowser(WebUtils.getHttpServletRequestUserAgentFromRequestContext(requestContext));
        request.setRequested_at(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
        val response = notificationService.sendNotificationRequest(request);
        if(response != null && response.getCode() == 200) {
            LOGGER.debug("Notification request sent successfully, setting scope variables.");
        }
        val flowScope = requestContext.getFlowScope();
        flowScope.put("gauthChannel", id);
        flowScope.put("gauthPrefix", CuniGAuthWebflowConstants.GAUTH_SIMPLE_BROKER_DESTINATION_PREFIX);
        return null;
    }

    private String getFirstString(List<Object> values) {
        if(values == null) {
            return null;
        }
        val iterator = values.iterator();
        if(iterator.hasNext()) {
            return String.valueOf(iterator.next());
        }
        return null;
    }
}
