package com.yfwj.justauth.social.common;

import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorNil;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import org.apache.commons.lang.StringUtils;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.keycloak.models.IdentityProviderMapperModel;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonExpressionUserAttributeMapper extends JsonPathUserAttributeMapper {

	public static final String PROVIDER_ID = "json-expression-attribute-idp-mapper";

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<>();

	private static final String JUSTAUTH_JSON_EXPRESSION_ATTRIBUTE = "justauth.json.expression";

	static {
		ProviderConfigProperty property = new ProviderConfigProperty();
		property.setName(JUSTAUTH_JSON_EXPRESSION_ATTRIBUTE);
		property.setLabel("User Json Expression Attribute Name");
		property.setHelpText("Import user profile by json expression like username=extension['用户名'],firstName=firstName(name),lastName=lastName(name),avatar=avatar,email=email");
		property.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(property);
		AviatorEvaluator.addFunction(new FirstNameFunction());
		AviatorEvaluator.addFunction(new LastNameFunction());
		AviatorEvaluator.addFunction(new FindAttrByNameFunction());
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return "Json Expression Attribute Importer";
	}

	@Override
	public String getHelpText() {
		return "Import user profile information if it exists in Social provider JSON expression data into the specified user attributes.";
	}

	@Override
	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}


	@Override
	protected Map<String, String> getValuesByJsonPath(String json, IdentityProviderMapperModel mapperModel) {

		// firstName=firstName(nickname),lastName=lastName(nickname),email=openid + '@localhost.com',openId=openid,name=nickname,unionId=unionid,avatar=headimgurl
		String attribute = mapperModel.getConfig().get(JUSTAUTH_JSON_EXPRESSION_ATTRIBUTE);

		if (attribute == null || attribute.trim().isEmpty()) {
			logger.warnf("Attribute is not configured for mapper %s", mapperModel.getName());
			return null;
		}

		//Map<String, String> map = Splitter.on(",").withKeyValueSeparator("=").split(attribute);

		Map<String, String> result = new HashMap<>();

		try {
			List<Map<String, String>> mappings = new ObjectMapper().readValue(attribute, List.class);
			Map<String, Object> env = new ObjectMapper().readValue(json, Map.class);

			for (Map<String, String> mapping : mappings) {
				String target = mapping.get("target");
				String expression = mapping.get("expression");

				if (target == null || expression == null) continue;

				Object eval = AviatorEvaluator.compile(expression).execute(env);
				if (eval != null) {
					String value = String.valueOf(eval);
					if (StringUtils.isNotEmpty(value)) {
						result.put(target, value);
					}
				}
			}

//			logger.error("user profile: " + json + "from json expression: " + attribute);

		} catch (Exception e) {
			throw new IdentityBrokerException("Could not obtain user profile: " + json + "from json expression: " + attribute, e);
		}
		return result;


	}

	static class FirstNameFunction extends AbstractFunction {
		@Override
		public AviatorObject call(Map<String, Object> env,
								  AviatorObject arg1) {
			String name = FunctionUtils.getStringValue(arg1, env);
			if (name == null || name.length() == 0) {
				return AviatorNil.NIL;
			}
			String firstName = name.substring(1);
			return new AviatorString(firstName);
		}

		public String getName() {
			return "firstName";
		}
	}

	static class LastNameFunction extends AbstractFunction {
		@Override
		public AviatorObject call(Map<String, Object> env,
								  AviatorObject arg1) {
			String name = FunctionUtils.getStringValue(arg1, env);
			if (name == null || name.length() == 0) {
				return AviatorNil.NIL;
			}
			String lastName = name.substring(0, 1);
			return new AviatorString(lastName);
		}

		public String getName() {
			return "lastName";
		}
	}

	// regexMatch(email, 'datarx\.cn', '非法的企业邮箱')
	static class RegexMatchFunction extends AbstractFunction {
		@Override
		public AviatorObject call(Map<String, Object> env,
								  AviatorObject arg1,
								  AviatorObject arg2,
								  AviatorObject arg3) {

			String value = FunctionUtils.getStringValue(arg1, env);
			String regex = FunctionUtils.getStringValue(arg2, env);
			String message = FunctionUtils.getStringValue(arg3, env);
			if (StringUtils.isEmpty(message)) {
				message = "Invalid user";
			}
			if (StringUtils.isEmpty(value)) {
				throw new UnMatchedException(message);
			}
			Matcher matcher = Pattern.compile(regex).matcher(value);
			if (!matcher.find()) {
				throw new UnMatchedException(message);
			}

			return AviatorNil.NIL;
		}

		public String getName() {
			return "regexMatch";
		}
	}

	// md5(org_email)
	static class MD5Function extends AbstractFunction {
		@Override
		public AviatorObject call(Map<String, Object> env,
								  AviatorObject arg1) {
			String value = FunctionUtils.getStringValue(arg1, env);
			String md5 = SecureUtil.md5(value);
			return new AviatorString(md5);
		}

		public String getName() {
			return "md5";
		}
	}

	static class FindAttrByNameFunction extends AbstractFunction {
		@Override
		public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
			// 获取 attrs 数组（List<Map<String, Object>>）
			Object attrsObj = FunctionUtils.getJavaObject(arg1, env);
			if (!(attrsObj instanceof List)) {
				return AviatorNil.NIL;
			}
			List<?> attrs = (List<?>) attrsObj;

			// 获取 name 参数
			String targetName = FunctionUtils.getStringValue(arg2, env);

			// 遍历查找 name 匹配的属性
			for (Object obj : attrs) {
				if (obj instanceof Map) {
					Map<?, ?> attr = (Map<?, ?>) obj;
					if (targetName.equals(attr.get("name"))) {
						Object text = attr.get("text");
						if (text instanceof Map) {
							Object value = ((Map<?, ?>) text).get("value");
							if (value != null) {
								return new AviatorString(value.toString());
							}
						}
					}
				}
			}

			// 未找到返回 nil
			return AviatorNil.NIL;
		}

		@Override
		public String getName() {
			return "findAttrByName";
		}
	}

}
