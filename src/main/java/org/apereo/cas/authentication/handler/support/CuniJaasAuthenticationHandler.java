package org.apereo.cas.authentication.handler.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;

public class CuniJaasAuthenticationHandler extends JaasAuthenticationHandler {

	@Override
	protected HandlerResult postAuthenticate(Credential credential, HandlerResult result) {
		// stuff back the authenticated usernamne into credential...
		((UsernamePasswordCredential)credential).setUsername(result.getPrincipal().getId());
		logger.debug("Replaced authenticated username in credential with: {}", credential.getId());
		return result;
	}

}
