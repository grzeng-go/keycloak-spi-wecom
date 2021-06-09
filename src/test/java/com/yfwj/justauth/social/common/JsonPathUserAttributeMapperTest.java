package com.yfwj.justauth.social.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonPathUserAttributeMapperTest {

	@Test
	public void preprocessUserInfoByJson() {
		String json = "{\n" +
				"  \"openid\": \"OPENID\",\n" +
				"  \"nickname\": \"NICKNAME\",\n" +
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

		JSONObject jsonObject = JSON.parseObject(json);

		StrSubstitutor substitutor = new StrSubstitutor();
		substitutor.setVariableResolver(new JsonPathStrLookup(jsonObject));

		System.out.println(substitutor.replace("${$['openid']}"));
		System.out.println(substitutor.replace("${$['openid']}@localhost.com"));
		System.out.println(substitutor.replace("${$['privilege'][0]}"));
		System.out.println(substitutor.replace("${$['province']} ${$['city']} ${$['country']}"));
		System.out.println(substitutor.replace("${$['xxxx']}"));

	}
}
