package com.grzeng.keycloak.authentication.authenticators.broker;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Collections;
import java.util.List;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.authentication.authenticators.broker.IdpCheckUserExistsAuthenticatorFactory </p >
 * <p>Description:  </p >
 * <p>Date: 2025/7/9 10:36 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class IdpCheckUserExistsAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "idp-check-user-exists";
    static final IdpCheckUserExistsAuthenticator SINGLETON = new IdpCheckUserExistsAuthenticator();

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        // No initialization required
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // No post-initialization required
    }

    @Override
    public void close() {
        // No resources to close
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getReferenceCategory() {
        return "checkUserExists";
    }

    @Override
    public boolean isConfigurable() {
        return false; // No configuration needed
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public String getDisplayType() {
        return "Check User Exists";
    }

    @Override
    public String getHelpText() {
        return "Checks if a user exists with the same email or username as the identity provider. If the user exists, continues the flow; otherwise, stops with an error.";
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList(); // No configuration properties needed
    }
}
