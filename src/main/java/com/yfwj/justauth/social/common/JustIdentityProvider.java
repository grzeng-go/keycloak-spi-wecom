package com.yfwj.justauth.social.common;


import com.yfwj.justauth.social.DingTalkEnterpriseErrorResponseException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthDefaultRequest;
import me.zhyd.oauth.request.AuthRequest;
import org.keycloak.OAuthErrorException;
import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.common.ClientConnection;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.messages.Messages;
import org.keycloak.sessions.AuthenticationSessionModel;

import java.lang.reflect.Constructor;

/**
 * @author yanfeiwuji
 * @date 2021/1/10 4:37 下午
 */


public class JustIdentityProvider extends AbstractOAuth2IdentityProvider<JustIdentityProviderConfig> implements SocialIdentityProvider<JustIdentityProviderConfig> {

	public static final String IDENTITY_PROVIDER_UNMATCHED_ATTRIBUTE_ERROR = "identityProviderUnmatchedAttributeErrorMessage";
	public static final String IDENTITY_PROVIDER_DING_TALK_RESPONSE_ERROR = "identityProviderDingTalkResponseErrorMessage";

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

	public AuthRequest getAuthRequest(AuthConfig authConfig, String redirectUri) {
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
		return new Endpoint(callback, realm, event, this);
	}


	protected static class Endpoint {
		protected AuthenticationCallback callback;
		protected RealmModel realm;
		protected EventBuilder event;

		private final JustIdentityProvider provider;

		protected final KeycloakSession session;
		protected final ClientConnection clientConnection;
		protected final HttpHeaders headers;

		public Endpoint(AuthenticationCallback callback, RealmModel realm, EventBuilder event, JustIdentityProvider provider) {
			this.callback = callback;
			this.realm = realm;
			this.event = event;
			this.provider = provider;
			this.session = provider.session;
			this.clientConnection = session.getContext().getConnection();
			this.headers = session.getContext().getRequestHeaders();
		}

		@GET
		@Path("")
		public Response authResponse(@QueryParam("state") String state,
																 @QueryParam("code") String authorizationCode,
																 @QueryParam("error") String error) {
			JustIdentityProviderConfig providerConfig = provider.getConfig();
			try {
				AuthenticationSessionModel authSession = this.callback.getAndVerifyAuthenticationSession(state);
				session.getContext().setAuthenticationSession(authSession);

				if (error != null) {
					logger.error(error + " for broker login " + providerConfig.getProviderId());
					if (error.equals(ACCESS_DENIED)) {
						return callback.cancelled(providerConfig);
					} else if (error.equals(OAuthErrorException.LOGIN_REQUIRED) || error.equals(OAuthErrorException.INTERACTION_REQUIRED)) {
						return callback.error(error);
					} else {
						return callback.error(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
					}
				}

				AuthCallback authCallback = AuthCallback.builder().code(authorizationCode).state(state).build();

				// 没有check 不通过
				String redirectUri = "https://www.yfwj.com";
				AuthRequest authRequest = provider.getAuthRequest(provider.AUTH_CONFIG, redirectUri);
				AuthResponse<AuthUser> response = authRequest.login(authCallback);

				if (response.ok()) {
					AuthUser authUser = response.getData();
					BrokeredIdentityContext federatedIdentity = new BrokeredIdentityContext(authUser.getUuid());

					if (providerConfig.isStoreToken()) {
						// make sure that token wasn't already set by getFederatedIdentity();
						// want to be able to allow provider to set the token itself.
						if (federatedIdentity.getToken() == null)
							federatedIdentity.setToken(authUser.getToken().getAccessToken());
					}

					federatedIdentity.setUsername(authUser.getUuid());
					federatedIdentity.setEmail(authUser.getEmail());
					federatedIdentity.setBrokerUserId(authUser.getUuid());
					federatedIdentity.setIdpConfig(providerConfig);
					federatedIdentity.setIdp(provider);
					federatedIdentity.setAuthenticationSession(authSession);

					JsonPathUserAttributeMapper.storeUserProfileForMapper(federatedIdentity, authUser.getRawUserInfo(), "JustAuth." + providerConfig.getJustAuthKey().getId());

					return this.callback.authenticated(federatedIdentity);
				}

			} catch (WebApplicationException e) {
				return e.getResponse();
			} catch (UnMatchedException e) {
				return errorIdentityProviderLogin(IDENTITY_PROVIDER_UNMATCHED_ATTRIBUTE_ERROR, e.getMessage());
			} catch (DingTalkEnterpriseErrorResponseException e) {
				return errorIdentityProviderLogin(IDENTITY_PROVIDER_DING_TALK_RESPONSE_ERROR, e.getMessage());
			} catch (Exception e) {
				logger.error("Failed to make identity provider oauth callback", e);
			}

			return errorIdentityProviderLogin(Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR);
		}

		private Response errorIdentityProviderLogin(String message, String... params) {
			event.event(EventType.LOGIN);
			event.error(Errors.IDENTITY_PROVIDER_LOGIN_FAILURE);
			return ErrorPage.error(session, null, Response.Status.BAD_GATEWAY, message, params);
		}
	}
}
