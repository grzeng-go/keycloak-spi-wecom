package com.grzeng.keycloak.authentication.authenticators.resetcredentials;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.authentication.authenticators.resetcredentials.LogResetPasswordExecutionFactory </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/2 15:27 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class ResetPasswordAndSyncExecutionFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "RESET_PASSWORD_AND_SYNC_EXECUTION";

    @Override
    public Authenticator create(KeycloakSession session) {
        return new ResetPasswordAndSyncExecution();
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "重置密码并同步到EasyGo";
    }

    @Override
    public String getReferenceCategory() {
        return "reset-credentials";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    // 必须实现这个方法！否则 26.x 编译报错
    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    // 如果你不需要配置项，可以直接返回空列表
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(); // 空列表
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}

    @Override
    public String getHelpText() {
        return "自定义 Reset Password Execution，用于重置密码并同步EasyGo";
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }
}
