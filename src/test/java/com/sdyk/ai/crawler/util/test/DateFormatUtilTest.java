package com.sdyk.ai.crawler.util.test;

import org.junit.Test;
import one.rewind.txt.DateFormatUtil;

import java.text.ParseException;
import java.util.Date;

public class DateFormatUtilTest {

	@Test
	public void time() throws ParseException {

		String s = "1天前来过";
		String s1 = "";
		Date ss = DateFormatUtil.parseTime(s);

		Date date = new Date();

		date.setTime(date.getTime() + 60000*60*24);

		System.err.println(new Date(Long.valueOf(60000*60*24) + System.currentTimeMillis()));
		System.err.println(date);
	}
}
