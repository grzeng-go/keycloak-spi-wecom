package com.yfwj.justauth.social;

import com.yfwj.justauth.social.common.JustIdentityProvider;
import com.yfwj.justauth.social.common.JustIdentityProviderConfig;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.models.KeycloakSession;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;

public class WeworkIdentityProvider extends JustIdentityProvider {

	private static final String WEB_AUTH_URL = "https://open.weixin.qq.com/connect/oauth2/authorize";

	private boolean autoLoginWeworkEnabled;

	private boolean scopeIsPrivate;

	public WeworkIdentityProvider(KeycloakSession session, JustIdentityProviderConfig config) {
		super(session, config);
		autoLoginWeworkEnabled = config.isAutoLoginWeworkEnabled();
		scopeIsPrivate = config.isScopeIsPrivate();
	}

	@Override
	protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
		UriBuilder builder = super.createAuthorizationUrl(request);
		if (autoLoginWeworkEnabled && isWework(request)) {
			String scope = scopeIsPrivate ? "snsapi_privateinfo" : "snsapi_base";
			String query = builder.build().getQuery();
			builder = UriBuilder.fromUri(WEB_AUTH_URL + "?" + query);
			builder.replaceQueryParam("scope", scope);
			// ✅ 添加 #wechat_redirect 到 URL 末尾, 在企业微信内部浏览器打开,必须加上,不然打不开
			builder.fragment("wechat_redirect");
		} else {
			builder.replaceQueryParam("login_type", "CorpApp");
		}
		logger.debug("scopeIsPrivate: " + scopeIsPrivate + ", url: " + builder.build().toString());
		return builder;
	}

	private boolean isWework(AuthenticationRequest request) {
		return isWeworkLoginHint(request) || isWeworkUserAgent(request);
	}

	private boolean isWeworkLoginHint(AuthenticationRequest request) {
		String loginHint = request.getAuthenticationSession().getClientNote("login_hint");
		return "wework".equals(loginHint);
	}

	private boolean isWeworkUserAgent(AuthenticationRequest request) {
		HttpHeaders headers = request.getHttpRequest().getHttpHeaders();
		String userAgent = headers.getHeaderString("User-Agent");
		// TODO: 2021/4/16 微信手机端进入后使用的手机浏览器，没有微信标识，不好判断
		return userAgent != null && (userAgent.contains("WeChat") || userAgent.contains("wxwork"));
	}
}
