package cz.cuni.cas.mfa.gauth.flow;

import cz.cuni.cas.CuniConfigurationProperties;
import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(CuniConfigurationProperties.class)
public class CuniGAuthSendRequestNotificationAction extends BaseCasWebflowAction {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();

    @RequiredArgsConstructor
    @Getter
    @Setter
    private static class NotificationRequest {
        @NonNull
        protected final String webflow_id;
        @NonNull
        protected final String username;
        protected final String application;
        protected String name;
        protected String email;
        protected String ip_address;
        protected String browser;
        protected String requested_at;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    private static class NotificationResponse {
        protected String title;
        protected String message;
        protected Integer code;
        protected String status;
    }

    private final CuniConfigurationProperties cuniProperties;

    @Override
    protected Event doExecute(RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        val service = WebUtils.getRegisteredService(requestContext);

        val url = cuniProperties.getGauth().getNotification_url();
        val id = UUID.randomUUID().toString();
        val request = new NotificationRequest(id, principal.getId(), service != null ? service.getFriendlyName() : null);
        LOGGER.debug("Sending notification request for principal [{}] with id [{}] for service [{}]", principal.getId(),
                id, service != null ? service.getFriendlyName() : "");
        val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(url)
                .entity(MAPPER.writeValueAsString(request))
                .headers(CollectionUtils.wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .headers(CollectionUtils.wrap("Bearer", cuniProperties.getGauth().getToken()))
                .build();
        val response = HttpUtils.execute(exec);
        val statusCode = response.getStatusLine().getStatusCode();
        if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
            val responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            val status = MAPPER.readValue(responseBody, NotificationResponse.class);
            if(status.code == 200) {
                LOGGER.debug("Notification request sent, setting scope variables.");
                val flowScope = requestContext.getFlowScope();
                flowScope.put("gauthChannel", id);
                flowScope.put("gauthPrefix", CuniGAuthWebflowConstants.GAUTH_SIMPLE_BROKER_DESTINATION_PREFIX);
            } else {
                LOGGER.warn("Notification service responded with [{}]", status.message);
            }
        } else {
            LOGGER.warn("Error sending notification request: [{}]", response.getStatusLine().getReasonPhrase());
        }
        return null;
    }
}
