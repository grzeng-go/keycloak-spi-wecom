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

	/**
	 * 是否开启微信内免登陆
	 */
	private static final String AUTO_LOGIN_WEWORK_ENABLED = "autoLoginWeworkEnabled";

	/**
	 * 是否要敏感信息
	 */
	private static final String SCOPE_IS_PRIVATE = "scopeIsPrivate";

	/**
	 * 是否企业自建/代开发应用登录
	 */
	private static final String CORP_APP = "corpApp";

	private final JustAuthKey justAuthKey;

	private String displayIconClasses;

	public JustIdentityProviderConfig(IdentityProviderModel model, JustAuthKey justAuthKey) {
		this(model, justAuthKey, null);
	}

	public JustIdentityProviderConfig(IdentityProviderModel model, JustAuthKey justAuthKey, String displayIconClasses) {
		super(model);
		this.justAuthKey = justAuthKey;
		this.displayIconClasses = displayIconClasses;
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

	public boolean isAutoLoginWeworkEnabled() {
		return Boolean.parseBoolean(getConfig().getOrDefault(AUTO_LOGIN_WEWORK_ENABLED, "false"));
	}

	public void setAutoLoginWeworkEnabled(boolean autoLoginWeworkEnabled) {
		getConfig().put(AUTO_LOGIN_WEWORK_ENABLED, String.valueOf(autoLoginWeworkEnabled));
	}

	public void setDisplayIconClasses(String displayIconClasses) {
		this.displayIconClasses = displayIconClasses;
	}

	@Override
	public String getDisplayIconClasses() {
		return this.displayIconClasses;
	}

	public boolean isScopeIsPrivate() {
		return Boolean.parseBoolean(getConfig().getOrDefault(SCOPE_IS_PRIVATE, "false"));
	}

	public void setScopeIsPrivate(boolean scopeIsPrivate) {
		getConfig().put(SCOPE_IS_PRIVATE, String.valueOf(scopeIsPrivate));
	}

	public boolean isCorpApp() {
		return Boolean.parseBoolean(getConfig().getOrDefault(CORP_APP, "false"));
	}

	public void setCorpApp(boolean corpApp) {
		getConfig().put(SCOPE_IS_PRIVATE, String.valueOf(corpApp));
	}
}
