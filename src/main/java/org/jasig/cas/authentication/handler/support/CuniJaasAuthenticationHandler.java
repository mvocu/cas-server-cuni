package org.jasig.cas.authentication.handler.support;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.UsernamePasswordCredential;

public class CuniJaasAuthenticationHandler extends JaasAuthenticationHandler {

	@Override
	protected HandlerResult postAuthenticate(Credential credential, HandlerResult result) {
		// stuff back the authenticated usernamne into credential...
		((UsernamePasswordCredential)credential).setUsername(result.getPrincipal().getId());
		logger.debug("Replaced authenticated username in credential with: {}", credential.getId());
		return result;
	}

}
