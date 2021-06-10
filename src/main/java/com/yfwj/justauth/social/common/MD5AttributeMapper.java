package com.yfwj.justauth.social.common;

import cn.hutool.crypto.SecureUtil;
import org.apache.commons.lang.StringUtils;
import org.jboss.logging.Logger;
import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.*;

public class MD5AttributeMapper extends AbstractIdentityProviderMapper {
	protected static final Logger logger = Logger.getLogger(MD5AttributeMapper.class);

	public static final String PROVIDER_ID = "md5-attribute-idp-mapper";

	private static final String[] COMPATIBLE_PROVIDERS = {ANY_PROVIDER};

	private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	private static final String USER_MD5_FROM_ATTRIBUTE = "user.md5.from.attribute";
	private static final String USER_MD5_TO_ATTRIBUTE = "user.md5.to.attribute";

	static {
		ProviderConfigProperty fromProperty = new ProviderConfigProperty();
		fromProperty.setName(USER_MD5_FROM_ATTRIBUTE);
		fromProperty.setLabel("FROM Attribute");
		fromProperty.setHelpText("MD5 from User Attribute Name");
		fromProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(fromProperty);

		ProviderConfigProperty toProperty = new ProviderConfigProperty();
		toProperty.setName(USER_MD5_TO_ATTRIBUTE);
		toProperty.setLabel("To Attribute");
		toProperty.setHelpText("MD5 to User Attribute Name");
		toProperty.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(toProperty);
	}


	@Override
	public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		md5(mapperModel, context);
	}

	private void md5(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String from = mapperModel.getConfig().get(USER_MD5_FROM_ATTRIBUTE);
		if (StringUtils.isEmpty(from)) {
			logger.warnf("from attribute is not configured for mapper %s", mapperModel.getName());
			return;
		}
		String to = mapperModel.getConfig().get(USER_MD5_TO_ATTRIBUTE);
		if (StringUtils.isEmpty(to)) {
			logger.warnf("to attribute is not configured for mapper %s", mapperModel.getName());
			return;
		}

		String fromValue = context.getUserAttribute(from);
		String md5 = SecureUtil.md5(fromValue);
		context.setUserAttribute(to, md5);
	}

	public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		md5(mapperModel, context);
		String to = mapperModel.getConfig().get(USER_MD5_TO_ATTRIBUTE);
		if (StringUtils.isNotEmpty(to)) {
			user.setSingleAttribute(to, context.getUserAttribute(to));
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
		return "MD5 Attribute Importer";
	}

	@Override
	public String getHelpText() {
		return "MD5 convert user attribute";
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
