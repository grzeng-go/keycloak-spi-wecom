package com.yfwj.justauth.social.common;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatchAttributeMapper extends AbstractIdentityProviderMapper {
	protected static final Logger logger = Logger.getLogger(RegexMatchAttributeMapper.class);

	public static final String PROVIDER_ID = "regex-match-attribute-idp-mapper";

	private static final String[] COMPATIBLE_PROVIDERS = {ANY_PROVIDER};

	private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	private static final String USER_REGEX_ATTRIBUTE = "user.regex.attribute";
	private static final String USER_REGEX_PATTERN = "user.regex.pattern";
	private static final String USER_REGEX_MESSAGE = "user.regex.message";
	private static final String USER_REGEX_REPLACE_TEMPLATE = "user.regex.replace.template";
	private static final String USER_REGEX_REPLACE_NAME = "user.regex.replace.name";

	static {
		ProviderConfigProperty attributeProperty = new ProviderConfigProperty();
		attributeProperty.setName(USER_REGEX_ATTRIBUTE);
		attributeProperty.setLabel("User Attribute");
		attributeProperty.setHelpText("User Attribute Name will be matched by regex");
		attributeProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(attributeProperty);

		ProviderConfigProperty patternProperty = new ProviderConfigProperty();
		patternProperty.setName(USER_REGEX_PATTERN);
		patternProperty.setLabel("Pattern");
		patternProperty.setHelpText("Regex Pattern");
		patternProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(patternProperty);

		ProviderConfigProperty messageProperty = new ProviderConfigProperty();
		messageProperty.setName(USER_REGEX_MESSAGE);
		messageProperty.setLabel("Invalid Message");
		messageProperty.setHelpText("The message will show after don't matched by regex");
		messageProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(messageProperty);

		ProviderConfigProperty replaceProperty = new ProviderConfigProperty();
		replaceProperty.setName(USER_REGEX_REPLACE_TEMPLATE);
		replaceProperty.setLabel("Replacement");
		replaceProperty.setHelpText("Named Regex Replacement");
		replaceProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(replaceProperty);

		ProviderConfigProperty replaceNameProperty = new ProviderConfigProperty();
		replaceNameProperty.setName(USER_REGEX_REPLACE_NAME);
		replaceNameProperty.setLabel("Replace Attribute");
		replaceNameProperty.setHelpText("The new Attribute Name to set Replacement value. if empty will override origin Attribute");
		replaceNameProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(replaceNameProperty);

	}


	@Override
	public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		regexMatchOrReplaceUserAttribute(mapperModel, context);
	}

	private void regexMatchOrReplaceUserAttribute(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String attribute = mapperModel.getConfig().get(USER_REGEX_ATTRIBUTE);
		if (StringUtils.isEmpty(attribute)) {
			logger.warnf("user regex attribute is not configured for mapper %s", mapperModel.getName());
			return;
		}

		String message = mapperModel.getConfig().get(USER_REGEX_MESSAGE);

		String value = context.getUserAttribute(attribute);
		String regex = mapperModel.getConfig().get(USER_REGEX_PATTERN);

		if (StringUtils.isEmpty(value) || StringUtils.isEmpty(regex)) {
			errorMessage(message);
		}

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(value);

		if (matcher.find()) {
			String replacement = mapperModel.getConfig().get(USER_REGEX_REPLACE_TEMPLATE);
			if (StringUtils.isNotEmpty(replacement)) {
				String replaceValue = NamedPattern.replace(matcher, pattern.pattern(), replacement);
				String replaceName = mapperModel.getConfig().get(USER_REGEX_REPLACE_NAME);
				if (StringUtils.isEmpty(replaceName)) {
					replaceName = attribute;
				}
				context.setUserAttribute(replaceName, replaceValue);
			}

		} else {
			errorMessage(message);
		}
	}

	private void errorMessage(String message) {
		if (StringUtils.isEmpty(message)) {
			message = "Invalid User Attribute";
		}
		throw new UnMatchedException(message);
	}

	@Override
	public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		regexMatchOrReplaceUserAttribute(mapperModel, context);
		String replacement = mapperModel.getConfig().get(USER_REGEX_REPLACE_TEMPLATE);
		if (StringUtils.isNotEmpty(replacement)) {
			String replaceName = mapperModel.getConfig().get(USER_REGEX_REPLACE_NAME);
			if (StringUtils.isEmpty(replaceName)) {
				replaceName = mapperModel.getConfig().get(USER_REGEX_ATTRIBUTE);
			}
			user.setSingleAttribute(replaceName, context.getUserAttribute(replaceName));
		}
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
		return "Regex Match Attribute Importer";
	}

	@Override
	public String getHelpText() {
		return "Regex Match with user Attribute";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	@Override
	public boolean supportsSyncMode(IdentityProviderSyncMode syncMode) {
		return IDENTITY_PROVIDER_SYNC_MODES.contains(syncMode);
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

}
