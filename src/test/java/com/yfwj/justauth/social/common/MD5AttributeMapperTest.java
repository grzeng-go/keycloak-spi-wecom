package com.yfwj.justauth.social.common;

import cn.hutool.crypto.SecureUtil;
import org.junit.Test;

import static org.junit.Assert.*;

public class MD5AttributeMapperTest {

	@Test
	public void md5() {
		String md5 = SecureUtil.md5("zhencai.yuan@datarx.cn");
		assertEquals("09c6d25b1c5449350bb6b0d0796e3c01", md5);
	}
}
