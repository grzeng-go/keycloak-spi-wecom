package com.yfwj.justauth.social;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yfwj.justauth.social.common.JsonPathUserAttributeMapper;
import com.yfwj.justauth.social.common.JustAuthKey;
import com.yfwj.justauth.social.common.UnMatchedException;
import me.zhyd.oauth.utils.HttpUtils;
import me.zhyd.oauth.utils.UrlBuilder;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DingTalkEnterpriseUserInfoMapper extends JsonPathUserAttributeMapper {

	public static final String PROVIDER_ID = "ding-talk-enterprise-attribute-idp-mapper";

	private static final String[] COMPATIBLE_PROVIDERS = {JustAuthKey.DING_TALK.getId()};

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	private static final String APP_KEY_ATTRIBUTE = "dingtalk.enterprise.key";
	private static final String APP_SECRET_ATTRIBUTE = "dingtalk.enterprise.secret";
	private static final String FULLNAME_ATTRIBUTE = "dingtalk.enterprise.fullname";
	private static final String REGEX_MATCH_ATTRIBUTE = "dingtalk.enterprise.regex.match";
	private static final String REGEX_PATTERN_ATTRIBUTE = "dingtalk.enterprise.regex.pattern";
	private static final String REGEX_MESSAGE_ATTRIBUTE = "dingtalk.enterprise.regex.message";
	private static final String MD5_FROM_ATTRIBUTE = "dingtalk.enterprise.md5.from";
	private static final String MD5_TO_ATTRIBUTE = "dingtalk.enterprise.md5.to";

	private static final String TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
	private static final String USER_ID_URL = "https://oapi.dingtalk.com/topapi/user/getbyunionid";
	private static final String USER_INFO_URL = "https://oapi.dingtalk.com/topapi/v2/user/get";

	static {
		ProviderConfigProperty keyProperty = new ProviderConfigProperty();
		keyProperty.setName(APP_KEY_ATTRIBUTE);
		keyProperty.setLabel("DingTalk App Key");
		keyProperty.setHelpText("DingTalk Enterprise App Key");
		keyProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(keyProperty);
		ProviderConfigProperty secretProperty = new ProviderConfigProperty();
		secretProperty.setName(APP_SECRET_ATTRIBUTE);
		secretProperty.setLabel("DingTalk App Secret");
		secretProperty.setHelpText("DingTalk Enterprise App Secret");
		secretProperty.setType(ProviderConfigProperty.PASSWORD);
		configProperties.add(secretProperty);

		ProviderConfigProperty fullNameProperty = new ProviderConfigProperty();
		fullNameProperty.setName(FULLNAME_ATTRIBUTE);
		fullNameProperty.setLabel("Fullname Attribute");
		fullNameProperty.setHelpText("Split Fullname to FirstName and LastName");
		fullNameProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(fullNameProperty);

		ProviderConfigProperty regexMatchProperty = new ProviderConfigProperty();
		regexMatchProperty.setName(REGEX_MATCH_ATTRIBUTE);
		regexMatchProperty.setLabel("Regex Match Attribute");
		regexMatchProperty.setHelpText("Check Match Attribute by Regex Pattern");
		regexMatchProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(regexMatchProperty);

		ProviderConfigProperty regexPatternProperty = new ProviderConfigProperty();
		regexPatternProperty.setName(REGEX_PATTERN_ATTRIBUTE);
		regexPatternProperty.setLabel("Regex Pattern");
		regexPatternProperty.setHelpText("Regex Pattern Expression");
		regexPatternProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(regexPatternProperty);

		ProviderConfigProperty regexMessageProperty = new ProviderConfigProperty();
		regexMessageProperty.setName(REGEX_MESSAGE_ATTRIBUTE);
		regexMessageProperty.setLabel("Regex Error Message");
		regexMessageProperty.setHelpText("Not Match Message");
		regexMessageProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(regexMessageProperty);

		ProviderConfigProperty md5FromProperty = new ProviderConfigProperty();
		md5FromProperty.setName(MD5_FROM_ATTRIBUTE);
		md5FromProperty.setLabel("MD5 From Attribute");
		md5FromProperty.setHelpText("The Attribute convert by md5");
		md5FromProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(md5FromProperty);

		ProviderConfigProperty md5ToProperty = new ProviderConfigProperty();
		md5ToProperty.setName(MD5_TO_ATTRIBUTE);
		md5ToProperty.setLabel("MD5 To Attribute");
		md5ToProperty.setHelpText("The New Attribute convert by md5");
		md5ToProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(md5ToProperty);
	}

	@Override
	public String[] getCompatibleProviders() {
		return COMPATIBLE_PROVIDERS;
	}

	@Override
	public String getDisplayType() {
		return "DingTalk Enterprise User Importer";
	}


	@Override
	public String getHelpText() {
		return "Import user profile from DingTalk Enterprise App.";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		ArrayList<ProviderConfigProperty> properties = new ArrayList<>(super.getConfigProperties());
		properties.addAll(configProperties);
		return properties;
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

		JSONObject userInfo = getDingTalkUser(mapperModel, context);
		if (userInfo == null) {
			return;
		}
		preprocessJsonObject(userInfo, mapperModel, context);
		splitFullname(null, mapperModel, context);
		regexMatch(mapperModel, context);
		md5(null, mapperModel, context);
	}

	@Override
	public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		JSONObject userInfo = getDingTalkUser(mapperModel, context);
		if (userInfo == null) {
			return;
		}
		updateJsonObject(userInfo, user, mapperModel, context);
		splitFullname(user, mapperModel, context);
		regexMatch(mapperModel, context);
		md5(user, mapperModel, context);
	}


	private void splitFullname(UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String attribute = mapperModel.getConfig().get(FULLNAME_ATTRIBUTE);
		if (StringUtils.isEmpty(attribute)) {
			return;
		}
		String value = context.getUserAttribute(attribute);
		if (StringUtils.isEmpty(value)) {
			return;
		}

		String lastName = value.substring(0, 1);
		String firstName = value.substring(1);
		context.setFirstName(firstName);
		context.setLastName(lastName);

		if (user != null) {
			user.setFirstName(firstName);
			user.setLastName(lastName);
		}

	}

	private void regexMatch(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {

		String attribute = mapperModel.getConfig().get(REGEX_MATCH_ATTRIBUTE);
		String regex = mapperModel.getConfig().get(REGEX_PATTERN_ATTRIBUTE);

		if (StringUtils.isEmpty(attribute) || StringUtils.isEmpty(regex)) {
			return;
		}
		String value = context.getUserAttribute(attribute);
		if (StringUtils.isEmpty(value)) {
			return;
		}

		String message = mapperModel.getConfig().get(REGEX_MESSAGE_ATTRIBUTE);

		if (StringUtils.isEmpty(message)) {
			message = "Invalid user";
		}
		Matcher matcher = Pattern.compile(regex).matcher(value);
		if (!matcher.find()) {
			throw new UnMatchedException(message);
		}

	}

	private void md5(UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String from = mapperModel.getConfig().get(MD5_FROM_ATTRIBUTE);

		String to = mapperModel.getConfig().get(MD5_TO_ATTRIBUTE);

		if (StringUtils.isEmpty(from) || StringUtils.isEmpty(to)) {
			return;
		}
		String value = context.getUserAttribute(from);
		if (StringUtils.isEmpty(value)) {
			return;
		}

		String md5 = SecureUtil.md5(value);
		context.setUserAttribute(to, md5);
		if (user != null) {
			user.setSingleAttribute(to, md5);
		}

	}


	private JSONObject getDingTalkUser(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String key = mapperModel.getConfig().get(APP_KEY_ATTRIBUTE);
		if (StringUtils.isEmpty(key)) {
			return null;
		}
		String secret = mapperModel.getConfig().get(APP_SECRET_ATTRIBUTE);
		if (StringUtils.isEmpty(secret)) {
			return null;
		}
		String accessToken = getAccessToken(key, secret);
		String unionId = context.getBrokerUserId();
		String userId = getUserIdByUnionId(accessToken, unionId);
		return getUserInfo(accessToken, userId);
	}

	private String getAccessToken(String key, String secret) {
		String url = UrlBuilder.fromBaseUrl(TOKEN_URL)
				.queryParam("appkey", key)
				.queryParam("appsecret", secret)
				.build();
		String response = new HttpUtils().get(url);
		JSONObject object = JSON.parseObject(response);
		if (object.getIntValue("errcode") != 0) {
			throw new DingTalkEnterpriseErrorResponseException(object.getString("errmsg"));
		}
		return object.getString("access_token");
	}

	private String getUserIdByUnionId(String accessToken, String unionId) {
		String url = UrlBuilder.fromBaseUrl(USER_ID_URL)
				.queryParam("access_token", accessToken)
				.queryParam("unionid", unionId)
				.build();
		String response = new HttpUtils().get(url);
		JSONObject object = JSON.parseObject(response);
		if (object.getIntValue("errcode") != 0) {
			throw new DingTalkEnterpriseErrorResponseException(object.getString("errmsg"));
		}
		return object.getJSONObject("result").getString("userid");
	}

	private JSONObject getUserInfo(String accessToken, String userId) {
		String url = UrlBuilder.fromBaseUrl(USER_INFO_URL)
				.queryParam("access_token", accessToken)
				.queryParam("userid", userId)
				.build();
		String response = new HttpUtils().get(url);
		JSONObject object = JSON.parseObject(response);
		if (object.getIntValue("errcode") != 0) {
			throw new DingTalkEnterpriseErrorResponseException(object.getString("errmsg"));
		}
		return object.getJSONObject("result");
	}

}
