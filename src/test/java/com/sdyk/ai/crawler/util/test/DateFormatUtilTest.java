package com.sdyk.ai.crawler.util.test;

import org.junit.Test;
import one.rewind.txt.DateFormatUtil;

import java.text.ParseException;
import java.util.Date;

public class DateFormatUtilTest {

	@Test
	public void time() throws ParseException {

		String s = "1天前来过";
		Date ss = DateFormatUtil.parseTime(s);
		System.err.println(ss.getTime());
	}
}
