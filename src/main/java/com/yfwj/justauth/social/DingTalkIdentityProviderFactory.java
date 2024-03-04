package com.yfwj.justauth.social;

import com.yfwj.justauth.social.common.JustAuthKey;
import com.yfwj.justauth.social.common.JustIdentityProvider;
import com.yfwj.justauth.social.common.JustIdentityProviderConfig;
import com.yfwj.justauth.social.common.JustauthProviderConfigurationBuilder;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AbstractIdentityProviderFactory;
import org.keycloak.broker.social.SocialIdentityProviderFactory;
import org.keycloak.models.IdentityProviderModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2021/1/10 5:48 下午
 */

public class DingTalkIdentityProviderFactory extends
  AbstractIdentityProviderFactory<JustIdentityProvider>
  implements SocialIdentityProviderFactory<JustIdentityProvider> {

  public static final JustAuthKey JUST_AUTH_KEY = JustAuthKey.  DING_TALK;

  @Override
  public String getName() {
    return JUST_AUTH_KEY.getName();
  }

  @Override
  public JustIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
    return new DingTalkIdentityProvider(session, new JustIdentityProviderConfig(model,JUST_AUTH_KEY));
  }

  @Override
  public OAuth2IdentityProviderConfig createConfig() {
    return new OAuth2IdentityProviderConfig();
  }

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return JustauthProviderConfigurationBuilder.create()
				.property()
				.name("autoLoginDingTalkEnabled")
				.label("autoLoginDingTalkEnabled")
				.helpText("钉钉内自动登录")
				.type(ProviderConfigProperty.BOOLEAN_TYPE)
				.defaultValue(false)
				.add()
				.build();
	}

	@Override
  public String getId() {
    return JUST_AUTH_KEY.getId();
  }
}
