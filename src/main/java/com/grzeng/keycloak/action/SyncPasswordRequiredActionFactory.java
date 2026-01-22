package com.grzeng.keycloak.action;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.requiredactions.UpdatePassword;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.policy.MaxAuthAgePasswordPolicyProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.action.SyncPasswordRequiredActionFactory </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/2 12:36 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class SyncPasswordRequiredActionFactory implements RequiredActionFactory {

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new SyncPasswordRequiredAction(session);
    }

    @Override
    public List<ProviderConfigProperty> getConfigMetadata() {
         return ProviderConfigurationBuilder.create()
                .property()
                .name("url")
                .label("url")
                .type(ProviderConfigProperty.STRING_TYPE)
                 .add()
                 .property()
                 .name("token")
                 .label("token")
                 .type(ProviderConfigProperty.STRING_TYPE)
                 .add()
                 .property()
                 .name("rsaKey")
                 .label("rsaKey")
                 .type(ProviderConfigProperty.STRING_TYPE)
                 .add()
                 .property() //
                 .name(UpdatePassword.MAX_AUTH_AGE_KEY) //
                 .label("Maximum Age of Authentication") //
                 .helpText("Configures the duration in seconds this action can be used after the last authentication before the user is required to re-authenticate. " + //
                         "This parameter is used just in the context of AIA when the kc_action parameter is available in the request, which is for instance when user " + //
                         "himself updates his password in the account console. When the 'Maximum Authentication Age' password policy is used in the realm, it's value has " + //
                         "precedence over the value configured here.") //
                 .type(ProviderConfigProperty.STRING_TYPE) //
                 .defaultValue(MaxAuthAgePasswordPolicyProviderFactory.DEFAULT_MAX_AUTH_AGE) //
                 .add() //
                 .property()
                 .name("enableOTP")
                 .label("是否启用OTP校验")
                 .helpText("如果启用OTP校验,则所有未设置OTP的,都会强制要求设置!")
                 .type(ProviderConfigProperty.BOOLEAN_TYPE)
                 .defaultValue(false)
                 .add()
                 .build();
    }

    @Override
    public String getId() {
        return "SYNC_PASSWORD_REQUIRED_ACTION";
    }

    @Override
    public String getDisplayText() {
        return "重置密码并同步到EasyGo";
    }

    @Override
    public void init(Config.Scope config) {}

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {}
}
