package cz.cuni.cas.mfa.gauth.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cuni.cas.mfa.gauth.CuniGAuthWebflowConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BaseCasActuatorEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestControllerEndpoint(id = "cuniTOTPNotification", enableByDefault = true)
@Slf4j
public class CuniGAuthNotificationEndpoint extends BaseCasActuatorEndpoint {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
            .defaultTypingEnabled(false).build().toObjectMapper();

    private final MessageSendingOperations<String> messageTemplate;

    public CuniGAuthNotificationEndpoint(CasConfigurationProperties casProperties, MessageSendingOperations<String> messageTemplate) {
        super(casProperties);
        this.messageTemplate = messageTemplate;
    }


    @NoArgsConstructor
    @Getter
    @Setter
    private static class NotifyTOTPMessage {
        protected String channelId;
        protected String code;
        protected String principalId;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private static class NotifyTOTPResponse {
        protected String result;
        protected String message;
    }

    @PostMapping(path = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Receive notification as JSON document", parameters = @Parameter(name = "request"))
    public NotifyTOTPResponse notifyTOTP(final HttpServletRequest request) throws Exception {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Received payload [{}]", requestBody);
        val message = MAPPER.readValue(requestBody, NotifyTOTPMessage.class);
        String destination = String.format("%s/%s/code",
                CuniGAuthWebflowConstants.GAUTH_SIMPLE_BROKER_DESTINATION_PREFIX,
                message.channelId
                );
        messageTemplate.convertAndSend(destination, Map.of("code", message.getCode()));
        return new NotifyTOTPResponse("success", "success");
    }

}
