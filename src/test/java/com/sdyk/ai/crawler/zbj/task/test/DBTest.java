package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.util.DBUtil;
import db.Refacter;
import org.junit.Test;

public class DBTest {

	@Test
	public void testInitDao() {
		DBUtil.createTables();
	}
}
