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

	/**
	 * 是否忽略校验 State
	 */
	private static final String IGNORE_CHECK_STATE = "ignoreCheckState";

	/**
	 * 是否开启钉钉内免登陆
	 */
	private static final String AUTO_LOGIN_DING_TALK_ENABLED = "autoLoginDingTalkEnabled";

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

	public boolean isIgnoreCheckState() {
		return Boolean.parseBoolean(getConfig().getOrDefault(IGNORE_CHECK_STATE, "false"));
	}

	public void setIgnoreCheckState(boolean ignoreCheckState) {
		getConfig().put(IGNORE_CHECK_STATE, String.valueOf(ignoreCheckState));
	}

	public boolean isAutoLoginDingTalkEnabled() {
		return Boolean.parseBoolean(getConfig().getOrDefault(AUTO_LOGIN_DING_TALK_ENABLED, "false"));
	}

	public void setAutoLoginDingTalkEnabled(boolean autoLoginDingTalkEnabled) {
		getConfig().put(AUTO_LOGIN_DING_TALK_ENABLED, String.valueOf(autoLoginDingTalkEnabled));
	}
}
