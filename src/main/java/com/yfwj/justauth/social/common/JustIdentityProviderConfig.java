package com.yfwj.justauth.social.common;

import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.models.IdentityProviderModel;

/**
 * @author yanfeiwuji
 * @date 2021/1/12 7:53 下午
 */
public class JustIdentityProviderConfig extends OAuth2IdentityProviderConfig {

	private static final String AGENT_ID_KEY = "weworkAgentId";
	private static final String ALIPAY_PUBLIC_KEY = "alipayPublicKey";
	private static final String CODING_GROUP_NAME = "codingGroupName";
	private static final String APP_KEY = "appKey";
	private static final String APP_SECRET = "appSecret";
	/**
	 * 另外的用户属性
	 * 示例：username=$['extension']['用户名'],avatar=$['avatar'],email=$['email']
	 */
	private static final String ADDITION_USER_JSON_FIELDS = "additionUserJsonFields";

	private JustAuthKey justAuthKey;

	public JustIdentityProviderConfig(IdentityProviderModel model, JustAuthKey justAuthKey) {
		super(model);
		this.justAuthKey = justAuthKey;
	}

	public JustIdentityProviderConfig(JustAuthKey justAuthKey) {
		this.justAuthKey = justAuthKey;
	}


	public JustAuthKey getJustAuthKey() {
		return this.justAuthKey;
	}

	public String getAgentId() {
		return getConfig().get(AGENT_ID_KEY);
	}

	public void setAgentId(String agentId) {
		getConfig().put(AGENT_ID_KEY, agentId);
	}

	public String getAlipayPublicKey() {
		return getConfig().get(ALIPAY_PUBLIC_KEY);
	}

	public void setAlipayPublicKey(String alipayPublicKey) {

		getConfig().put(ALIPAY_PUBLIC_KEY, alipayPublicKey);
	}

	public String getCodingGroupName() {
		return getConfig().get(CODING_GROUP_NAME);
	}

	public void setCodingGroupName(String codingGroupName) {
		getConfig().put(CODING_GROUP_NAME, codingGroupName);

	}

	public void setAppKey(String appKey) {
		getConfig().put(APP_KEY, appKey);
	}

	public String getAppKey() {
		return getConfig().get(APP_KEY);
	}

	public void setAppSecret(String appSecret) {
		getConfig().put(APP_SECRET, appSecret);
	}

	public String getAppSecret() {
		return getConfig().get(APP_SECRET);
	}

	public void setAdditionUserJsonFields(String additionUserJsonFields) {
		getConfig().put(ADDITION_USER_JSON_FIELDS, additionUserJsonFields);
	}

	public String getAdditionUserJsonFields() {
		return getConfig().get(ADDITION_USER_JSON_FIELDS);
	}
}
