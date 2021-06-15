package com.yfwj.justauth.social.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.aviator.AviatorEvaluator;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class JsonExpressionUserAttributeMapperTest {

	@Before
	public void setUp() throws Exception {
		AviatorEvaluator.addFunction(new JsonExpressionUserAttributeMapper.FirstNameFunction());
		AviatorEvaluator.addFunction(new JsonExpressionUserAttributeMapper.LastNameFunction());
		AviatorEvaluator.addFunction(new JsonExpressionUserAttributeMapper.RegexMatchFunction());
		AviatorEvaluator.addFunction(new JsonExpressionUserAttributeMapper.MD5Function());
	}

	@Test
	public void preprocessUserInfoByJson() throws JsonProcessingException {
		String json = "{\n" +
				"  \"openid\": \"OPENID\",\n" +
				"  \"nickname\": \"袁振才\",\n" +
				"  \"email\": \"yuanzhencai@@localhost.com\",\n" +
				"  \"extension\": {\n" +
				"\t\"用户名\": \"yuanzhencai\"\n" +
				"  },\n" +
				"  \"sex\": 1,\n" +
				"  \"province\": \"PROVINCE\",\n" +
				"  \"city\": \"CITY\",\n" +
				"  \"country\": \"COUNTRY\",\n" +
				"  \"headimgurl\": \"https://thirdwx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46\",\n" +
				"  \"privilege\": [\n" +
				"\t\"PRIVILEGE1\",\n" +
				"\t\"PRIVILEGE2\"\n" +
				"  ],\n" +
				"  \"unionid\": \"o6_bmasdasdsad6_2sgVt7hMZOPfL\"\n" +
				"}";

		Map<String, Object> env = new ObjectMapper().readValue(json, Map.class);

		System.out.println(AviatorEvaluator.compile("openid").execute(env));
		System.out.println(AviatorEvaluator.compile("openid + '@localhost.com'").execute(env));
		System.out.println(AviatorEvaluator.compile("privilege[0]").execute(env));
		System.out.println(AviatorEvaluator.compile("province + city + country").execute(env));
		System.out.println(AviatorEvaluator.compile("xxxx").execute(env));
		System.out.println(AviatorEvaluator.compile("extension['用户名']").execute(env));
		System.out.println(AviatorEvaluator.compile("md5(email)").execute(env));

	}
}
