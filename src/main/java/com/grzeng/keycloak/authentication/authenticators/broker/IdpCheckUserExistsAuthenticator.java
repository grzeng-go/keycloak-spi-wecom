package com.grzeng.keycloak.authentication.authenticators.broker;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Errors;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import jakarta.ws.rs.core.Response;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.authentication.authenticators.broker.IdpLinkOrFailAuthenticator </p >
 * <p>Description: 用户名/邮箱校验用户是否存在,存在则继续,反之则终止流程 </p >
 * <p>Date: 2025/7/9 10:18 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class IdpCheckUserExistsAuthenticator extends AbstractIdpAuthenticator {

    private static final Logger logger = Logger.getLogger(IdpCheckUserExistsAuthenticator.class);

    @Override
    protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        // No action required
    }

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();

        // Check if user info is already set (from previous authenticator steps)
        if (context.getAuthenticationSession().getAuthNote(EXISTING_USER_INFO) != null) {
            context.attempted();
            return;
        }

        // Get the username or email based on realm configuration
        String username = getUsername(context, serializedCtx, brokerContext);
        if (username == null) {
            logger.warnf("No username or email provided for identity provider '%s'.", brokerContext.getIdpConfig().getAlias());
            context.getAuthenticationSession().setAuthNote(ENFORCE_UPDATE_PROFILE, "true");
            context.resetFlow();
            return;
        }

        // Check if the user exists
        ExistingUserInfo existingUser = checkExistingUser(context, username, serializedCtx, brokerContext);

        if (existingUser != null) {
            logger.debugf("User found with %s '%s' for identity provider '%s'. Continuing flow.",
                    existingUser.getDuplicateAttributeName(), existingUser.getDuplicateAttributeValue(), brokerContext.getIdpConfig().getAlias());
            context.getAuthenticationSession().setAuthNote(EXISTING_USER_INFO, existingUser.serialize());
            context.attempted();
        } else {
            logger.debugf("No user found with %s '%s' for identity provider '%s'. Stopping flow.",
                    realm.isRegistrationEmailAsUsername() ? "email" : "username", username, brokerContext.getIdpConfig().getAlias());

            // Stop the flow with a custom error message
            Response challengeResponse = context.form()
                    .setError("user_not_found", "User does not exist in the system. Please register first.")
                    .createErrorPage(Response.Status.BAD_REQUEST);
            context.challenge(challengeResponse);
        }
    }

    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        RealmModel realm = context.getRealm();

        // Check for duplicate email if email is provided and duplicates are not allowed
        if (brokerContext.getEmail() != null && !realm.isDuplicateEmailsAllowed()) {
            UserModel existingUser = context.getSession().users().getUserByEmail(realm, brokerContext.getEmail());
            if (existingUser != null) {
                return new ExistingUserInfo(existingUser.getId(), UserModel.EMAIL, existingUser.getEmail());
            }
        }

        // Check for existing username
        UserModel existingUser = context.getSession().users().getUserByUsername(realm, username);
        if (existingUser != null) {
            return new ExistingUserInfo(existingUser.getId(), UserModel.USERNAME, existingUser.getUsername());
        }

        return null;
    }

    protected String getUsername(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        RealmModel realm = context.getRealm();
        return realm.isRegistrationEmailAsUsername() ? brokerContext.getEmail() : brokerContext.getModelUsername();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }
}
