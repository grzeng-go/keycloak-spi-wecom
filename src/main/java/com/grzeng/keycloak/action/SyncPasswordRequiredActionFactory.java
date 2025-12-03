package com.grzeng.keycloak.action;

import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
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
        return new SyncPasswordRequiredAction();
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
