package com.yfwj.justauth.social.common;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class JustauthProviderConfigurationBuilder {

	public static ProviderConfigurationBuilder create() {
		return ProviderConfigurationBuilder.create()
				.property()
				.name("ignoreCheckState")
				.label("ignoreCheckState")
				.type(ProviderConfigProperty.BOOLEAN_TYPE)
				.defaultValue(false)
				.add();

	}
}
