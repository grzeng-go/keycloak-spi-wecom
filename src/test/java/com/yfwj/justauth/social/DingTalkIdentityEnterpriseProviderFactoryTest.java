package com.yfwj.justauth.social;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class DingTalkIdentityEnterpriseProviderFactoryTest {


	@Test
	public void url() {
		UriBuilder builder = UriBuilder.fromUri("https://oapi.dingtalk.com/connect/qrconnect?response_type=code&appid=dingoaxdkfspbtihv2ohrj&scope=snsapi_login&redirect_uri=http://192.168.3.24:4080/auth/realms/palan/broker/ding_talk_enterprise/endpoint&state=Yr8XllPvI5lqdD82BDf4fNPpSwvpn4YVcJwrxtnCBQk.fDQcGofG1VY.jira");
		URI uri = builder.build();
		String query = uri.getQuery();
		UriBuilder fromUri = UriBuilder.fromUri("https://oapi.dingtalk.com/connect/oauth2/sns_authorize?" + query);
		fromUri.replaceQueryParam("scope", "snsapi_auth");
		Assert.assertEquals("https://oapi.dingtalk.com/connect/oauth2/sns_authorize?response_type=code&appid=dingoaxdkfspbtihv2ohrj&redirect_uri=http://192.168.3.24:4080/auth/realms/palan/broker/ding_talk_enterprise/endpoint&state=Yr8XllPvI5lqdD82BDf4fNPpSwvpn4YVcJwrxtnCBQk.fDQcGofG1VY.jira&scope=snsapi_auth", fromUri.build().toString());
	}
}
