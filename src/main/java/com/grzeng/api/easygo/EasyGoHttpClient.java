package com.grzeng.api.easygo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grzeng.api.easygo.request.UserRequest;
import com.grzeng.api.easygo.response.UserResponse;
import com.grzeng.api.utils.GsonUtil;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jboss.logging.Logger;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>ProjectName: keycloak-spi-wecom </p >
 * <p>PackageName: com.grzeng.api.easygo.EasyGoHttpClient </p >
 * <p>Description:  </p >
 * <p>Date: 2025/12/3 17:56 </p >
 *
 * @author zguorong
 * @version v1.0
 */
public class EasyGoHttpClient {
    private static final Logger LOG = Logger.getLogger(EasyGoHttpClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String baseUrl;

    public EasyGoHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public UserResponse syncPassword(UserRequest request) throws Exception {
        String url = baseUrl + buildQueryString(request);
        LOG.infof("【密码同步】请求地址: %s", url);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            get.addHeader("Accept", "application/json");

            return client.execute(get, response -> {
                int status = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                LOG.infof("【密码同步】响应: %d %s", status, body);

                if (status >= 200 && status < 300) {
                    return GsonUtil.fromJson(body, UserResponse.class);
                } else {
                    throw new RuntimeException("密码同步失败: " + body);
                }
            });
        }
    }

    private String buildQueryString(UserRequest req) {
        StringBuilder sb = new StringBuilder("?");
        sb.append("action=").append(urlEncode(req.getAction()));
        sb.append("&token=").append(urlEncode(req.getToken()));
        sb.append("&username=").append(urlEncode(req.getUsername()));
        sb.append("&password=").append(urlEncode(req.getPassword()));
        sb.append("&sign=").append(urlEncode(req.getSign()));
        return sb.toString();
    }

    private String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
