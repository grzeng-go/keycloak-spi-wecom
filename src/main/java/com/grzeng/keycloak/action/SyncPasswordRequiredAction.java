package com.grzeng.keycloak.action;

import com.grzeng.api.easygo.EasyGoHttpClient;
import com.grzeng.api.easygo.request.UserRequest;
import com.grzeng.api.easygo.response.UserResponse;
import com.grzeng.api.utils.MD5Utils;
import com.grzeng.api.utils.RSAUtils;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.keycloak.authentication.AuthenticatorUtil;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.requiredactions.UpdatePassword;
import org.jboss.logging.Logger;
import org.keycloak.common.util.Time;
import org.keycloak.credential.CredentialModel;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.PasswordCredentialProvider;
import org.keycloak.credential.PasswordCredentialProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.OTPCredentialModel;

import java.util.concurrent.TimeUnit;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.keycloak.action.LogPasswordRequiredAction </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/2 12:35 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class SyncPasswordRequiredAction extends UpdatePassword {

    private static final Logger logger = Logger.getLogger(SyncPasswordRequiredAction.class);

    private EasyGoHttpClient easyGoClient;

    public SyncPasswordRequiredAction(KeycloakSession session) {
        super(session);
    }

    @Override
    public void processAction(RequiredActionContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String passwordNew = formData.getFirst("password-new");

        syncPassword(context, passwordNew);

        // 继续执行原有的密码更新逻辑
        super.processAction(context);
    }

    /**
     * 同步密码
     * @param context
     * @param password
     */
    public void syncPassword(RequiredActionContext context, String password) {
        // 读取配置项（关键！）
        RequiredActionConfigModel config = context.getRealm().getRequiredActionConfigByAlias(getId());
        String url   = config != null ? config.getConfigValue("url") : null;
        String token      = config != null ? config.getConfigValue("token") : null;
        String rsaKey        = config != null ? config.getConfigValue("rsaKey") : null;
        logger.infof("【密码同步配置】 url=%s | token=%s | rsa key=%s", url, token, rsaKey);

        UserModel user = context.getUser();
        String usernameForEasyGo = user.getFirstAttribute("EasyGo");
        String passwordMd5 = MD5Utils.md5(password);
        String data = String.format("%s%s%s", usernameForEasyGo, passwordMd5, token);
        try {
            if (this.easyGoClient == null) {
                assert url != null;
                this.easyGoClient = new EasyGoHttpClient(url);
            }
            String sign = RSAUtils.sign(data, rsaKey);
            UserRequest userRequest = new UserRequest();
            userRequest.setAction("syncPassword");
            userRequest.setToken(token);
            userRequest.setUsername(usernameForEasyGo);
            userRequest.setPassword(passwordMd5);
            userRequest.setSign(sign);
            UserResponse userResponse = this.easyGoClient.syncPassword(userRequest);
            if (userResponse != null && userResponse.getPanformJson() != null && userResponse.getPanformJson().getCoustom() != null) {
                UserResponse.Coustom coustom = userResponse.getPanformJson().getCoustom();
                if (coustom.getSuccess()) {
                    logger.infof("【密码同步】成功, 响应=%s", coustom);
                } else {
                    if (StringUtils.equalsIgnoreCase(coustom.getStatus(), "401")) {
                        // token失效，重置easyGo
                        this.easyGoClient = null;
                    } else {
                        logger.errorf("【密码同步】失败, 响应=%s", coustom);
                    }
                }
            } else {
                logger.error("【密码同步】失败, 请求无返回");
                this.easyGoClient = null;
            }
        } catch (Exception e) {
            logger.error("【密码同步】失败", e);
            this.easyGoClient = null;
        }

    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        if (!AuthenticatorUtil.isPasswordValidated(context.getAuthenticationSession())) {
            return;
        }
        int daysToExpirePassword = context.getRealm().getPasswordPolicy().getDaysToExpirePassword();
        if (daysToExpirePassword != -1) {
            PasswordCredentialProvider passwordProvider = (PasswordCredentialProvider) context.getSession().getProvider(CredentialProvider.class, PasswordCredentialProviderFactory.PROVIDER_ID);
            CredentialModel password = passwordProvider.getPassword(context.getRealm(), context.getUser());
            if (password != null) {
                if (password.getCreatedDate() == null) {
                    context.getUser().addRequiredAction(getId());
                    logger.debug("User is required to update password");
                } else {
                    long timeElapsed = Time.toMillis(Time.currentTime()) - password.getCreatedDate();
                    long timeToExpire = TimeUnit.DAYS.toMillis(daysToExpirePassword);

                    if (timeElapsed > timeToExpire) {
                        context.getUser().addRequiredAction(getId());
                        logger.debug("User is required to update password");
                    }
                }
            }
        }

        // 先检查用户是否已经有 UPDATE_PASSWORD
        boolean hasDefaultUpdate = context.getUser().getRequiredActionsStream()
                .anyMatch(action -> UserModel.RequiredAction.UPDATE_PASSWORD.name().equals(action));

        if (hasDefaultUpdate) {
            // 移除内置的 UPDATE_PASSWORD
            context.getUser().removeRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD.name());
            // 添加你自己的自定义 action（替换它）
            context.getUser().addRequiredAction(getId());  // 替换成你的 providerId
            // 可选：加日志，便于排查
            logger.info(String.format("用户 %s 原有 UPDATE_PASSWORD 已替换为自定义 action", context.getUser().getUsername()));
        }

        RequiredActionConfigModel config = context.getRealm().getRequiredActionConfigByAlias(getId());
        boolean enableOTP = Boolean.parseBoolean(config.getConfigValue("enableOTP"));
        if (enableOTP) {
            boolean hasTotpConfigured = context.getUser().credentialManager().isConfiguredFor(OTPCredentialModel.TYPE);

            if (!hasTotpConfigured) {
                // 未配置 → 添加 CONFIGURE_TOTP
                context.getUser().addRequiredAction(UserModel.RequiredAction.CONFIGURE_TOTP.name());
                // 可选：加日志，便于调试
                logger.info(String.format("用户 %s 未配置 TOTP，已添加 CONFIGURE_TOTP required action", context.getUser().getUsername()));
            }
        }
    }

    @Override
    public String getDisplayText() {
        return "重置密码并同步到EasyGo";
    }

    @Override
    public String getId() {
        return "SYNC_PASSWORD_REQUIRED_ACTION";
    }
}
