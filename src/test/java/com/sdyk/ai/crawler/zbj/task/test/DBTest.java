package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.util.DBUtil;
import org.junit.Test;

public class DBTest {

	@Test
	public void testInitDao() {
		DBUtil.createTables();
	}
}
