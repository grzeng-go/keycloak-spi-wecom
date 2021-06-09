package com.yfwj.justauth.social.common;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.lang.text.StrLookup;

public class JsonPathStrLookup extends StrLookup {

	private JSONObject jsonObject;

	public JsonPathStrLookup(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public String lookup(String key) {
		Object value = JSONPath.eval(jsonObject, key);
		return value == null ? "" : String.valueOf(value);
	}
}
