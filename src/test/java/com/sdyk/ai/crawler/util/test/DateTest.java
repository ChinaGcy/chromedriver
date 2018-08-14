package com.sdyk.ai.crawler.util.test;

import com.sdyk.ai.crawler.util.DateFormatUtil;
import org.junit.Test;

public class DateTest {

	@Test
	public void test(){

		String src = "2018-08-24";

		System.out.println(DateFormatUtil.parseTime(src));

	}
}
