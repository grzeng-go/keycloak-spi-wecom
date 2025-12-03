package com.grzeng.keycloak.event;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.event.PrintPasswordListenerFactory </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/2 12:13 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class PrintPasswordListenerFactory implements EventListenerProviderFactory {
    public static final String ID = "print-password-listener";

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new PrintPasswordListener();
    }

    @Override
    public String getId() { return ID; }

    @Override public void init(Config.Scope config) {}
    @Override public void postInit(KeycloakSessionFactory factory) {}
    @Override public void close() {}
}
