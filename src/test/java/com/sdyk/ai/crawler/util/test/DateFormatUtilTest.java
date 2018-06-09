package com.sdyk.ai.crawler.util.test;

import org.junit.Test;
import one.rewind.txt.DateFormatUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormatUtilTest {

	@Test
	public void time() throws ParseException {

		String s = "1天前来过";
		String s1 = "";
		Date ss = DateFormatUtil.parseTime(s);

		Date date = new Date();

		/*date.setTime(date.getTime() + 60000*60*24);

		System.err.println(new Date(Long.valueOf(60000*60*24) + System.currentTimeMillis()));*/
		System.err.println(ss);

		Pattern p = Pattern.compile("今天|昨天|前天|\\d+(年|个月|个星期|天|分钟|小时)前");
		Matcher m = p.matcher(s);
		if (m.find()) {
			String prefix = m.group();
			s = s.replaceAll(prefix, "");
			System.err.println(s);
		}
	}
	@Test
	public void date() {
		Date date = new Date();
		System.err.println(date.getTime());
		System.err.println(System.currentTimeMillis() - 30*24*60*60*1000);
		date.setTime(System.currentTimeMillis() - 30*24*60*60);

		System.err.println(date);

	}
}
