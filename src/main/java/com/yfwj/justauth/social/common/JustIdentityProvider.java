package com.yfwj.justauth.social.common;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.base.Splitter;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.StringUtils;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2021/1/10 4:37 下午
 */


public class JustIdentityProvider extends AbstractOAuth2IdentityProvider<JustIdentityProviderConfig> implements SocialIdentityProvider<JustIdentityProviderConfig> {

	public final String DEFAULT_SCOPES = "default";
	//OAuth2IdentityProviderConfig
	public final AuthConfig AUTH_CONFIG;
	public final Class<? extends AuthDefaultRequest> tClass;


	public JustIdentityProvider(KeycloakSession session, JustIdentityProviderConfig config) {
		super(session, config);
		JustAuthKey justAuthKey = config.getJustAuthKey();
		this.AUTH_CONFIG = JustAuthKey.getAuthConfig(config);
		this.tClass = justAuthKey.getTClass();
	}

	@Override
	protected UriBuilder createAuthorizationUrl(AuthenticationRequest request) {
		String redirectUri = request.getRedirectUri();
		AuthRequest authRequest = getAuthRequest(AUTH_CONFIG, redirectUri);
		String uri = authRequest.authorize(request.getState().getEncoded());
		return UriBuilder.fromUri(uri);
	}

	private AuthRequest getAuthRequest(AuthConfig authConfig, String redirectUri) {
		AuthRequest authRequest = null;
		authConfig.setRedirectUri(redirectUri);
		try {
			Constructor<? extends AuthDefaultRequest> constructor = tClass.getConstructor(AuthConfig.class);
			authRequest = constructor.newInstance(authConfig);
		} catch (Exception e) {
			// can't
			logger.error(e.getMessage());
		}
		return authRequest;
	}

	@Override
	protected String getDefaultScopes() {
		return DEFAULT_SCOPES;
	}

	@Override
	public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
		return new Endpoint(callback, realm, event);
	}


	protected class Endpoint {
		protected AuthenticationCallback callback;
		protected RealmModel realm;
		protected EventBuilder event;
		@Context
		protected KeycloakSession session;
		@Context
		protected ClientConnection clientConnection;
		@Context
		protected HttpHeaders headers;


		public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event) {
			this.callback = callback;
			this.realm = realm;
			this.event = event;
		}

		@GET
		public Response authResponse(@QueryParam("state") String state,
									 @QueryParam("code") String authorizationCode,
									 @QueryParam("error") String error) {
			AuthCallback authCallback = AuthCallback.builder().code(authorizationCode).state(state).build();

			// 没有check 不通过
			String redirectUri = "https://www.yfwj.com";
			AuthRequest authRequest = getAuthRequest(AUTH_CONFIG, redirectUri);
			AuthResponse<AuthUser> response = authRequest.login(authCallback);

			if (response.ok()) {
				AuthUser authUser = response.getData();
				JustIdentityProviderConfig config = JustIdentityProvider.this.getConfig();
				BrokeredIdentityContext federatedIdentity = new BrokeredIdentityContext(authUser.getUuid());

				if (getConfig().isStoreToken()) {
					// make sure that token wasn't already set by getFederatedIdentity();
					// want to be able to allow provider to set the token itself.
					if (federatedIdentity.getToken() == null)
						federatedIdentity.setToken(authUser.getToken().getAccessToken());
				}

				Map<String, String> fields = getAdditionUserFields(authUser);

				logger.debug("user-json-fields: " + fields);

				String username = fields.remove("username");
				federatedIdentity.setUsername(StringUtils.isEmpty(username) ? authUser.getUuid() : username);
				federatedIdentity.setEmail(fields.remove("email"));
				federatedIdentity.setFirstName(fields.remove("firstName"));
				federatedIdentity.setLastName(fields.remove("lastName"));
				federatedIdentity.setBrokerUserId(authUser.getUuid());
				federatedIdentity.setIdpConfig(config);
				federatedIdentity.setIdp(JustIdentityProvider.this);
				federatedIdentity.setCode(state);

				for (Map.Entry<String, String> entry : fields.entrySet()) {
					federatedIdentity.setUserAttribute(entry.getKey(), entry.getValue());
				}

				return this.callback.authenticated(federatedIdentity);
			} else {
				return this.errorIdentityProviderLogin("identityProviderUnexpectedErrorMessage");
			}
		}

		private Map<String, String> getAdditionUserFields(AuthUser authUser) {
			JSONObject rawUserInfo = authUser.getRawUserInfo();
			String additionUserJsonFields = JustIdentityProvider.this.getConfig().getAdditionUserJsonFields();
			if (StringUtils.isEmpty(additionUserJsonFields)) {
				return new HashMap<>();
			}
			Map<String, String> map = Splitter.on(",").withKeyValueSeparator("=").split(additionUserJsonFields);
			Map<String, String> result = new HashMap<>();

			try {
				for (Map.Entry<String, String> entry : map.entrySet()) {
					Object eval = JSONPath.eval(rawUserInfo, entry.getValue());
					if (eval != null) {
						result.put(entry.getKey(), String.valueOf(eval));
					}
				}
			} catch (Exception e) {
				throw new IdentityBrokerException("Could not obtain user profile: " + JSON.toJSONString(rawUserInfo) + "from json path: " + map.toString(), e);
			}
			return result;

		}

		private Response errorIdentityProviderLogin(String message) {
			this.event.event(EventType.LOGIN);
			this.event.error("identity_provider_login_failure");
			return ErrorPage.error(this.session, (AuthenticationSessionModel) null, Response.Status.BAD_GATEWAY, message, new Object[0]);
		}
	}
}
