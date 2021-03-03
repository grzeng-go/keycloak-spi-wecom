package com.yfwj.justauth.social;

import cn.hutool.core.util.URLUtil;
import org.junit.Test;

import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static org.junit.Assert.*;

public class DingTalkIdentityEnterpriseProviderFactoryTest {


	@Test
	public void url() {
		UriBuilder builder = UriBuilder.fromUri("https://oapi.dingtalk.com/connect/qrconnect?response_type=code&appid=dingoaxdkfspbtihv2ohrj&scope=snsapi_login&redirect_uri=http://192.168.3.24:4080/auth/realms/palan/broker/ding_talk_enterprise/endpoint&state=Yr8XllPvI5lqdD82BDf4fNPpSwvpn4YVcJwrxtnCBQk.fDQcGofG1VY.jira");
		URI uri = builder.build();
		String query = uri.getQuery();
		System.out.println(query);
		UriBuilder fromUri = UriBuilder.fromUri("https://oapi.dingtalk.com/connect/oauth2/sns_authorize?" + query);
		fromUri.replaceQueryParam("scope", "snsapi_auth");
		System.out.println(fromUri.build().toString());

	}
}
