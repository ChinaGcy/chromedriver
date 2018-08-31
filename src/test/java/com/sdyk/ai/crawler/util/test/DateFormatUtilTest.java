package com.sdyk.ai.crawler.util.test;

import com.sdyk.ai.crawler.util.DateFormatUtil;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateFormatUtilTest {

	@Test
	public void time() throws ParseException {

		String s = "2个月前来过";
		String s1 = "2018-02-06";
		Date ss = DateFormatUtil.parseTime(s1);

		//Date date = new Date();

		/*date.setTime(date.getTime() + 60000*60*24);

		System.err.println(new Date(Long.valueOf(60000*60*24) + System.currentTimeMillis()));*/
		System.err.println(ss);

		Pattern p = Pattern.compile("今天|昨天|前天|\\d+(年|个月|个星期|天|分钟|小时)前");
		Matcher m = p.matcher(s);
		if (m.find()) {
			String prefix = m.group();
			s = s.replaceAll(prefix, "");
			//System.err.println(s);
		}
	}
	@Test
	public void date() {
		Date date = new Date();
		Date date1 = new Date();
		System.err.println(date.getTime());
		System.err.println(System.currentTimeMillis() - 30*24*60*60*1000L);
		date.setTime((System.currentTimeMillis() - 30*24*60*60*1000L));
		date1.setTime(System.currentTimeMillis() );

		System.err.println(date);
		System.err.println(date1);

	}
}
