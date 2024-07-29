package cz.cuni.cas.opensaml;

public class CuniDiscoveryWebflowConstants {

    public static final String STATE_ID_DELEGATED_AUTHENTICATION_DISCOVERY = "delegatedAuthenticationDiscoveryState";
    public static final String STATE_ID_DELEGATED_AUTHENTICATION_REDIRECT_TO_DISCOVERY = "delegatedAuthenticationDiscoveryRedirectState";
    public static final String STATE_ID_DELEGATED_AUTHENTICATION_FINALIZE_DISCOVERY = "delegatedAuthenticationFinalizeDiscovery";

    public static final String ACTION_ID_DELEGATED_AUTHENTICATION_DISCOVERY = "delegatedAuthenticationDiscoveryAction";
    public static final String ACTION_ID_DELEGATED_AUTHENTICATION_FINALIZE_DISCOVERY = "delegatedAuthenticationFinalizeDiscoveryAction";

    public static final String TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_SUCCESS = "success";
    public static final String TRANSITION_ID_DELEGATED_AUTHENTICATION_DISCOVERY_REDIRECT = "redirect";

    public static final String CONVERSATION_VAR_ID_DELEGATED_AUTHENTICATION_IDP = "samlSelectedIdP";

    public static final String REQUEST_VAR_ID_DELEGATED_AUTHENTICATION_REDIRECT_URL = "redirectTo";
}
