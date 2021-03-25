package com.yfwj.justauth.social.common;

import org.keycloak.broker.provider.AbstractIdentityProviderMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.*;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.*;

public class SplitFullnameAttributeMapper extends AbstractIdentityProviderMapper {

	public static final String PROVIDER_ID = "split-fullname-attribute-idp-mapper";

	private static final String[] COMPATIBLE_PROVIDERS = Arrays.stream(JustAuthKey.values()).map(JustAuthKey::getId).toArray(String[]::new);

	private static final Set<IdentityProviderSyncMode> IDENTITY_PROVIDER_SYNC_MODES = new HashSet<>(Arrays.asList(IdentityProviderSyncMode.values()));

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	private static final String USER_FULLNAME_ATTRIBUTE = "user.fullname";

	static {
		ProviderConfigProperty property;
		property = new ProviderConfigProperty();
		property.setName(USER_FULLNAME_ATTRIBUTE);
		property.setLabel("User Fullname Attribute Name");
		property.setHelpText("Split Fullname attribute value to firstName and lastName");
		property.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(property);
	}

	@Override
	public void preprocessFederatedIdentity(KeycloakSession session, RealmModel realm, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		splitFullnameToFirstAndLastName(mapperModel, context);
	}

	@Override
	public void updateBrokeredUser(KeycloakSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		splitFullnameToFirstAndLastName(mapperModel, context);
	}

	private void splitFullnameToFirstAndLastName(IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context) {
		String attribute = mapperModel.getConfig().get(USER_FULLNAME_ATTRIBUTE);
		if (attribute == null || attribute.length() == 0) {
			return;
		}
		String value = context.getUserAttribute(attribute);

		if (value == null || value.length() == 0) {
			return;
		}
		String lastName = value.substring(0, 1);
		String firstName = value.substring(1);
		context.setFirstName(firstName);
		context.setLastName(lastName);
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
		return "Attribute Importer";
	}

	@Override
	public String getHelpText() {
		return "Split fullname to firstName and lastName";
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
