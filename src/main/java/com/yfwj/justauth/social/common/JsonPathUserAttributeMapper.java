package com.yfwj.justauth.social.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.*;

public class JsonPathUserAttributeMapper extends AbstractIdentityProviderMapper {

	private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

	protected static final Logger logger = Logger.getLogger(JsonPathUserAttributeMapper.class);

	public static final String PROVIDER_ID = "json-path-attribute-idp-mapper";

	private static final String[] COMPATIBLE_PROVIDERS = Arrays.stream(JustAuthKey.values()).map(JustAuthKey::getId).toArray(String[]::new);

	public static final String CONTEXT_JUSTAUTH_JSON_NODE = "justauth.user.json";

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	private static final String JUSTAUTH_JSON_PATH_ATTRIBUTE = "justauth.json.path";

	static {
		ProviderConfigProperty property = new ProviderConfigProperty();
		property.setName(JUSTAUTH_JSON_PATH_ATTRIBUTE);
		property.setLabel("User Json Path Attribute Name");
		property.setHelpText("Import user profile by json path like username=$['extension']['用户名'],avatar=$['avatar'],email=$['email']");
		property.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(property);
	}

	@Override
	public String[] getCompatibleProviders() {
		return COMPATIBLE_PROVIDERS;
	}

	@Override
	public String getDisplayCategory() {
		return "Attribute Importer";
	}

	@Override
	public String getDisplayType() {
		return "Json Path Attribute Importer";
	}

	@Override
	public String getHelpText() {
		return "Import user profile information if it exists in Social provider JSON path data into the specified user attributes.";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
		return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
	}

	@Override
	public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		JSONObject jsonObject = getContextJsonObject(context);
		preprocessJsonObject(jsonObject, mapperModel, context);
	}

	protected void preprocessJsonObject(JSONObject jsonObject, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String attribute = getAttribute(mapperModel);
		if (attribute == null) {
			return;
		}
		Map<String, String> attributes = getValuesByJsonPath(jsonObject, attribute);
		String username = attributes.remove("username");
		if (StringUtils.isNotEmpty(username)) {
			context.setUsername(username);
		}
		String email = attributes.remove("email");
		if (StringUtils.isNotEmpty(email)) {
			context.setEmail(email);
		}
		String firstName = attributes.remove("firstName");
		if (StringUtils.isNotEmpty(firstName)) {
			context.setFirstName(firstName);
		}
		String lastName = attributes.remove("lastName");
		if (StringUtils.isNotEmpty(lastName)) {
			context.setLastName(lastName);
		}

		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			context.setUserAttribute(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		JSONObject jsonObject = getContextJsonObject(context);
		updateJsonObject(jsonObject, user, mapperModel, context);
	}

	private JSONObject getContextJsonObject(BrokeredIdentityContext context) {
		String json = (String) context.getContextData().get(CONTEXT_JUSTAUTH_JSON_NODE);
		return JSON.parseObject(json);
	}

	protected void updateJsonObject(JSONObject jsonObject, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String attribute = getAttribute(mapperModel);
		if (attribute == null) {
			return;
		}

		Map<String, String> attributes = getValuesByJsonPath(jsonObject, attribute);
		attributes.remove("username");
		attributes.remove("email");
		String firstName = attributes.remove("firstName");
		if (StringUtils.isNotEmpty(firstName)) {
			context.setFirstName(firstName);
		}
		String lastName = attributes.remove("lastName");
		if (StringUtils.isNotEmpty(lastName)) {
			context.setLastName(lastName);
		}
		for (Map.Entry<String, String> entry : attributes.entrySet()) {
			context.setUserAttribute(entry.getKey(), entry.getValue());
			user.setSingleAttribute(entry.getKey(), entry.getValue());
		}

		Set<String> keys = attributes.keySet();

		Set<String> existedKeys = user.getAttributes().keySet();
		for (String existedKey : existedKeys) {
			if (!keys.contains(existedKey)) {
				user.removeAttribute(existedKey);
			}
		}
	}

	private Map<String, String> getValuesByJsonPath(JSONObject jsonObject, String jsonPath) {
		Map<String, String> map = Splitter.on(",").withKeyValueSeparator("=").split(jsonPath);
		Map<String, String> result = new HashMap<>();

		try {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				Object eval = JSONPath.eval(jsonObject, entry.getValue());
				if (eval != null) {
					result.put(entry.getKey(), String.valueOf(eval));
				}
			}
		} catch (Exception e) {
			throw new IdentityBrokerException("Could not obtain user profile: " + JSON.toJSONString(jsonObject) + "from json path: " + map.toString(), e);
		}
		return result;

	}

	private String getAttribute(IdentityProviderMapperModel mapperModel) {
		String attribute = mapperModel.getConfig().get(JUSTAUTH_JSON_PATH_ATTRIBUTE);
		if (attribute == null || attribute.trim().isEmpty()) {
			logger.warnf("Attribute is not configured for mapper %s", mapperModel.getName());
			return null;
		}
		attribute = attribute.trim();
		return attribute;
	}


	public static void storeUserProfileForMapper(BrokeredIdentityContext user, JSONObject profile, String provider) {
		String json = JSON.toJSONString(profile);
		user.getContextData().put(CONTEXT_JUSTAUTH_JSON_NODE, json);
		logger.debug("User Profile JSON Data for provider " + provider + ": " + json);
	}
}
