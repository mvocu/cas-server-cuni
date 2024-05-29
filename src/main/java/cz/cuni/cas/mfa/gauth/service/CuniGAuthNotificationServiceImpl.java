package cz.cuni.cas.mfa.gauth.service;

import cz.cuni.cas.CuniConfigurationProperties;
import cz.cuni.cas.mfa.gauth.api.CuniGAuthNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(CuniConfigurationProperties.class)
public class CuniGAuthNotificationServiceImpl implements CuniGAuthNotificationService {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();


    private final CuniConfigurationProperties cuniProperties;

    /**
     * @param request 
     * @return
     */
    @Override
    public NotificationResponse sendNotificationRequest(NotificationRequest request) {
        LOGGER.debug("Sending notification request for principal [{}] with id [{}] for service [{}]",
                request.getPrincipalId(), request.getChannelId(), request.getApplication());
        val url = cuniProperties.getGauth().getNotification_url();
        HttpUtils.HttpExecutionRequest exec = null;
        try {
            exec = HttpUtils.HttpExecutionRequest.builder()
                    .method(HttpMethod.POST)
                    .url(url)
                    .entity(MAPPER.writeValueAsString(request))
                    .headers(CollectionUtils.wrap(
                            "Bearer", cuniProperties.getGauth().getToken(),
                            "Content-Type", MediaType.APPLICATION_JSON_VALUE))
                    .build();
            val response = HttpUtils.execute(exec);
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val status = MAPPER.readValue(responseBody, CuniGAuthNotificationService.NotificationResponse.class);
                if(status.getCode() == 200) {
                    return status;
                } else {
                    LOGGER.warn("Notification service responded with [{}]", status.getMessage());
                }
            } else {
                LOGGER.warn("Error sending notification request: [{}]", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOGGER.warn("Error sending notification request: [{}]", e.getMessage());
        }
        return null;
    }

    /**
     * @return 
     */
    @Override
    public NotificationResponse sendConfirmationRequest(String channelId) {
        val url = cuniProperties.getGauth().getNotification_url();
        StringBuilder dest = new StringBuilder(url);
        if(!url.endsWith("/")) {
            dest.append('/');
        }
        dest.append(channelId);
        HttpUtils.HttpExecutionRequest exec = null;
        try {
            exec = HttpUtils.HttpExecutionRequest.builder()
                    .method(HttpMethod.DELETE)
                    .url(dest.toString())
                    .headers(CollectionUtils.wrap("Bearer", cuniProperties.getGauth().getToken()))
                    .build();
            val response = HttpUtils.execute(exec);
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val responseBody = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val status = MAPPER.readValue(responseBody, CuniGAuthNotificationService.NotificationResponse.class);
                if(status.getCode() == 200) {
                    return status;
                } else {
                    LOGGER.warn("Notification service responded with [{}]", status.getMessage());
                }
            } else {
                LOGGER.warn("Error removing notification request: [{}]", response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            LOGGER.warn("Error removing notification request: [{}]", e.getMessage());
        }
        return null;
    }
}
