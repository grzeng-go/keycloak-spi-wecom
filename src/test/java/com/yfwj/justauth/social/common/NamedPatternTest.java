package com.yfwj.justauth.social.common;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class NamedPatternTest {

	@Test
	public void replace() {

		String regex = ".*(?<mail>localhost\\.com).*";
		Matcher matcher = Pattern.compile(regex).matcher("yuanzhencai@localhost.com");
		assertTrue(matcher.find());
		String mailSuffix = NamedPattern.replace(matcher, regex, "mailSuffix = ${mail}");
		assertEquals("mailSuffix = localhost.com", mailSuffix);

	}

	@Test
	public void matcher() {
		String regex = "localhost\\.com";
		Matcher matcher = Pattern.compile(regex).matcher("yuanzhencai@localhost.com");
		assertTrue(matcher.find());
	}
}
