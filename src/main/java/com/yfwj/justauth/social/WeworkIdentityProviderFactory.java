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

public class WeworkIdentityProviderFactory extends
  AbstractIdentityProviderFactory<JustIdentityProvider>
  implements SocialIdentityProviderFactory<JustIdentityProvider> {

  public static final JustAuthKey JUST_AUTH_KEY = JustAuthKey.  WEWORK;

  @Override
  public String getName() {
    return JUST_AUTH_KEY.getName();
  }

  @Override
  public JustIdentityProvider create(KeycloakSession session, IdentityProviderModel model) {
    return new WeworkIdentityProvider(session, new JustIdentityProviderConfig(model,JUST_AUTH_KEY));
  }

  @Override
  public OAuth2IdentityProviderConfig createConfig() {
    return new OAuth2IdentityProviderConfig();
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return JustauthProviderConfigurationBuilder.create()
            .property()
            .name("weworkAgentId")
            .label("Agent Id")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name("autoLoginWeworkEnabled")
            .label("autoLoginWeworkEnabled")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .defaultValue(false)
            .add()
            .property()
            .name("scopeIsPrivate")
            .label("scopeIsPrivate")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .helpText("网页授权登录时,是否需要授权获取敏感信息(如邮箱/手机等信息),")
            .defaultValue(false)
            .add()
            .property()
            .name("corpApp")
            .label("corpApp")
            .type(ProviderConfigProperty.BOOLEAN_TYPE)
            .helpText("企业微信Web登录时,是否企业自建/代开发应用登录")
            .defaultValue(false)
            .add()
            .build();
  }

  @Override
  public String getId() {
    return JUST_AUTH_KEY.getId();
  }
}
