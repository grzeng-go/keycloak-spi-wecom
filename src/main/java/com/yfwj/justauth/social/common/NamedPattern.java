package com.yfwj.justauth.social.common;

import org.apache.commons.text.StringSubstitutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamedPattern {

	private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?<(\\w+)>");

	public static List<String> extractGroupNames(String namedPattern) {
		List<String> groupNames = new ArrayList<String>();
		Matcher matcher = NAMED_GROUP_PATTERN.matcher(namedPattern);
		while(matcher.find()) {
			groupNames.add(matcher.group(1));
		}
		return groupNames;
	}

	public static String replace(Matcher matcher,  String namedPattern,  String replacement) {
		Map<String, String> params = new HashMap<>();
		List<String> names = extractGroupNames(namedPattern);
		if(names.isEmpty()) {
			return matcher.replaceAll(replacement);
		}
		for (String name : names) {
			params.put(name, matcher.group(name));
		}
		return StringSubstitutor.replace(replacement, params);
	}


}
