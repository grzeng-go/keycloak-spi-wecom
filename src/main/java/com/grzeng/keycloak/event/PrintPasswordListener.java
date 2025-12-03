package com.grzeng.keycloak.event;

import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.event.PrintPasswordListener </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/2 12:12 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class PrintPasswordListener implements EventListenerProvider {
    private static final Logger log = Logger.getLogger(PrintPasswordListener.class);

    @Override
    public void onEvent(Event event) {
//        log.warnf("【onEvent】 Realm=%s | event=%s ",
//                event.getRealmId(), event.getType());
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {

    }

    @Override
    public void close() {}
}
