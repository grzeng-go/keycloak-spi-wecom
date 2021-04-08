package com.yfwj.justauth.social;

import com.yfwj.justauth.social.common.JustIdentityProvider;
import com.yfwj.justauth.social.common.JustIdentityProviderConfig;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

public class DingTalkIdentityProvider extends JustIdentityProvider {

	private static final String AUTO_AUTH_URL = "https://oapi.dingtalk.com/connect/oauth2/sns_authorize";

	private boolean autoLoginDingTalkEnabled;

	public DingTalkIdentityProvider(KeycloakSession session, JustIdentityProviderConfig config) {
		super(session, config);
		this.autoLoginDingTalkEnabled = config.isAutoLoginDingTalkEnabled();
	}

	@Override
	protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
		UriBuilder builder = super.createAuthorizationUrl(request);
		if (autoLoginDingTalkEnabled && isFromDingTalk(request)) {
			String query = builder.build().getQuery();
			builder = UriBuilder.fromUri(AUTO_AUTH_URL + "?" + query);
			builder.replaceQueryParam("scope", "snsapi_auth");
		}
		return builder;
	}

	private boolean isFromDingTalk(AuthenticationRequest request) {
		return isDingTalkLoginHint(request) || isDingTalkUserAgent(request);
	}

	private boolean isDingTalkLoginHint(AuthenticationRequest request) {
		String loginHint = request.getAuthenticationSession().getClientNote("login_hint");
		return "dingtalk".equals(loginHint);
	}

	private boolean isDingTalkUserAgent(AuthenticationRequest request) {
		HttpHeaders headers = request.getHttpRequest().getHttpHeaders();
		String userAgent = headers.getHeaderString("User-Agent");
		return userAgent != null && userAgent.contains("DingTalk");
	}
}
