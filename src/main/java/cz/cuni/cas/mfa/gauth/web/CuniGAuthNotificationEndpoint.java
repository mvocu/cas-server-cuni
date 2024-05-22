package cz.cuni.cas.mfa.gauth.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
            .defaultTypingEnabled(true).build().toObjectMapper();

    private final MessageSendingOperations<String> messageTemplate;

    protected CuniGAuthNotificationEndpoint(CasConfigurationProperties casProperties, MessageSendingOperations<String> messageTemplate) {
        super(casProperties);
        this.messageTemplate = messageTemplate;
    }

    @PostMapping(path = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Receive notification as JSON document", parameters = @Parameter(name = "request"))
    public HttpStatus importAccount(final HttpServletRequest request) throws Exception {
        val requestBody = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        LOGGER.trace("Received payload [{}]", requestBody);
        val data = MAPPER.readValue(requestBody, new TypeReference<Map<String,Object>>() {});
        return HttpStatus.OK;
    }

}
