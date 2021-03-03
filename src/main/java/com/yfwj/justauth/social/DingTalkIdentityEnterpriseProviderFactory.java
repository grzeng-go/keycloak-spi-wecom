package com.yfwj.justauth.social;

import com.yfwj.justauth.social.common.JustAuthKey;
import com.yfwj.justauth.social.common.JustIdentityProvider;
import com.yfwj.justauth.social.common.JustIdentityProviderConfig;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;

/**
 * @author yuanzhencai
 */
public class DingTalkIdentityEnterpriseProviderFactory extends
		AbstractIdentityProviderFactory<JustIdentityProvider>
		implements SocialIdentityProviderFactory<JustIdentityProvider> {

	public static final JustAuthKey JUST_AUTH_KEY = JustAuthKey.DING_TALK_ENTERPRISE;

	@Override
	public String getName() {
		return JUST_AUTH_KEY.getName();
	}

	@Override
	public JustIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
		return new DingTalkIdentityEnterpriseProvider(session, new JustIdentityProviderConfig(model, JUST_AUTH_KEY));
	}

	@Override
	public OAuth2IdentityProviderConfig createConfig() {
		return new OAuth2IdentityProviderConfig();
	}

	@Override
	public String getId() {
		return JUST_AUTH_KEY.getId();
	}

	public static class DingTalkIdentityEnterpriseProvider extends JustIdentityProvider {

		public DingTalkIdentityEnterpriseProvider(KeycloakSession session, JustIdentityProviderConfig config) {
			super(session, config);
		}

		@Override
		protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
			UriBuilder builder = super.createAuthorizationUrl(request);
			if (isFromDingTalk(request)) {
				String query = builder.build().getQuery();
				builder = UriBuilder.fromUri("https://oapi.dingtalk.com/connect/oauth2/sns_authorize?" + query);
				builder.replaceQueryParam("scope", "snsapi_auth");
			}
			System.out.println("AuthorizationUrl: " + builder.build().toString());
			return builder;
		}

		private boolean isFromDingTalk(AuthenticationRequest request) {
			return isDingTalkLoginHint(request) || isDingTalkUserAgent(request);
		}

		private boolean isDingTalkLoginHint(AuthenticationRequest request) {
			String loginHint = request.getAuthenticationSession().getClientNote("login_hint");
			return  "dingtalk".equals(loginHint);
		}

		private boolean isDingTalkUserAgent(AuthenticationRequest request) {
			HttpHeaders headers = request.getHttpRequest().getHttpHeaders();
			String userAgent = headers.getHeaderString("User-Agent");
			System.out.println("userAgent:" + userAgent);
			return userAgent != null && userAgent.contains("DingTalk");
		}
	}
}
