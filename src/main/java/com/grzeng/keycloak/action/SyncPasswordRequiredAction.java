package com.grzeng.keycloak.action;

import com.grzeng.api.easygo.EasyGoHttpClient;
import com.grzeng.api.easygo.request.UserRequest;
import com.grzeng.api.easygo.response.UserResponse;
import com.grzeng.api.utils.MD5Utils;
import com.grzeng.api.utils.RSAUtils;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.requiredactions.UpdatePassword;
import org.jboss.logging.Logger;
import org.keycloak.models.RequiredActionConfigModel;
import org.keycloak.models.UserModel;

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

    private static final Logger log = Logger.getLogger(SyncPasswordRequiredAction.class);

    private EasyGoHttpClient easyGoClient;

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
        String url   = config != null ? config.getConfig().get("url") : null;
        String token      = config != null ? config.getConfig().get("token") : null;
        String rsaKey        = config != null ? config.getConfig().get("rsaKey") : null;
        log.infof("【密码同步配置】 url=%s | token=%s | rsa key=%s", url, token, rsaKey);

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
                    log.infof("【密码同步】成功, 响应=%s", coustom);
                } else {
                    if (StringUtils.equalsIgnoreCase(coustom.getStatus(), "401")) {
                        // token失效，重置easyGo
                        this.easyGoClient = null;
                    } else {
                        log.errorf("【密码同步】失败, 响应=%s", coustom);
                    }
                }
            } else {
                log.error("【密码同步】失败, 请求无返回");
                this.easyGoClient = null;
            }
        } catch (Exception e) {
            log.error("【密码同步】失败", e);
            this.easyGoClient = null;
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
