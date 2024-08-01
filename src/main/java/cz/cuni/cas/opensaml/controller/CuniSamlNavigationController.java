package cz.cuni.cas.opensaml.controller;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.view.DynamicHtmlView;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

@Controller("defaultDelegatedAuthenticationNavigationController")
@Slf4j
@RequiredArgsConstructor
@Getter
public class CuniSamlNavigationController {

    private final CasConfigurationProperties casProperties;
    private final DelegatedClientAuthenticationConfigurationContext configContext;

    @GetMapping("/discovery/{id}")
    public View redirectBackToWebflow(
            @PathVariable("id")
            final String redirectId,
            final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        val ticket = configContext.getTicketRegistry().getTicket("TST-" + redirectId, TransientSessionTicket.class);
        String entityId = request.getParameter("entityID");
        if(ticket != null) {
            return new DynamicHtmlView(buildRedirectPostContent(ticket, entityId));
        }
        return new RedirectView(casProperties.getServer().getLoginUrl());
    }

    protected String buildRedirectPostContent(TransientSessionTicket ticket, String entityId) {
        val requestedUrl = casProperties.getServer().getLoginUrl();
        val buffer = new StringBuilder();
        buffer.append("<html>\n");
        buffer.append("<body>\n");
        buffer.append("<form action=\"").append(escapeHtml(requestedUrl)).append("\" name=\"f\" method=\"post\">\n");
        buffer.append("<input type='hidden' name=\"execution\" value=\"")
                .append(ticket.get(CuniDiscoveryWebflowConstants.PROPERTY_ID_WEBFLOW_KEY, String.class))
                .append("\" />\n");
        buffer.append("<input type='hidden' name=\"")
                .append(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER)
                .append("\" value=\"")
                .append(ticket.get(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER, String.class))
                .append("\" />\n");
        buffer.append("<input type='hidden' name=\"entityID\" value=\"")
                .append(entityId)
                .append("\" />\n");
        buffer.append("<input type='hidden' name=\"_eventId\" value=\"success\" />\n");
        buffer.append("<input value='POST' type='submit' />\n");
        buffer.append("</form>\n");
        buffer.append("<script type='text/javascript'>document.forms['f'].submit();</script>\n");
        buffer.append("</body>\n");
        buffer.append("</html>\n");
        return buffer.toString();
    }
}
