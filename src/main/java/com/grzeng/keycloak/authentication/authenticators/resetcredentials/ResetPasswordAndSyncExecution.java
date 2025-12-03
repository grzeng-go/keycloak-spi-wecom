package com.grzeng.keycloak.authentication.authenticators.resetcredentials;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.resetcred.ResetPassword;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.jboss.logging.Logger;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.authentication.authenticators.resetcredentials.CustomResetCredentialPassword </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/2 15:06 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class ResetPasswordAndSyncExecution extends ResetPassword {

    private static final Logger log = Logger.getLogger(ResetPasswordAndSyncExecution.class);

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        log.warnf(">>> ENTERING custom reset password action <<<");
        if (context.getExecution().isRequired() ||
                (context.getExecution().isConditional() &&
                        configuredFor(context))) {
            context.getAuthenticationSession().addRequiredAction("SYNC_PASSWORD_REQUIRED_ACTION");
        }
        context.success();
    }

    @Override
    public String getDisplayType() {
        return "重置密码并推送到EasyGo";
    }

    @Override
    public String getId() {
        return "RESET_PASSWORD_AND_SYNC_EXECUTION";
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;  // 允许用户设置
    }

    @Override
    public boolean requiresUser() {
        return super.requiresUser();
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return super.configuredFor(session, realm, user);
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        super.setRequiredActions(session, realm, user);
    }
}
