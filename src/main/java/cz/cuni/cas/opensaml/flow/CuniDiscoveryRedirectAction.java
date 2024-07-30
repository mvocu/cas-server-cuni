package cz.cuni.cas.opensaml.flow;

import cz.cuni.cas.opensaml.CuniDiscoveryWebflowConstants;
import org.springframework.binding.expression.Expression;
import org.springframework.binding.expression.spel.SpringELExpressionParser;
import org.springframework.binding.expression.support.FluentParserContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

public class CuniDiscoveryRedirectAction extends AbstractAction {

    private Expression resourceUri;
    
    public CuniDiscoveryRedirectAction(SpringELExpressionParser parser) {
        this.resourceUri = parser.parseExpression(
                "#{requestScope." +
                        CuniDiscoveryWebflowConstants.REQUEST_VAR_ID_DELEGATED_AUTHENTICATION_REDIRECT_URL +
                        "}%26execution%3D#{getFlowExecutionContext().getKey().toString()}",
                (new FluentParserContext())
                        .expectResult(String.class)
                        .template()
        );
    }

    /**
     * @param context
     * @return
     * @throws Exception
     */
    @Override
    protected Event doExecute(RequestContext context) throws Exception {
        String resourceUri = (String)this.resourceUri.getValue(context);
        context.getExternalContext().requestExternalRedirect(resourceUri);
        return this.success();
    }
}
