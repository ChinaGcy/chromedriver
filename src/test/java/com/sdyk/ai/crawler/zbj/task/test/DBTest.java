package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.docker.model.DockerHostImpl;
import com.sdyk.ai.crawler.zbj.util.DBUtil;
import one.rewind.db.Refacter;
import org.junit.Test;

public class DBTest {

	@Test
	public void testInitDao() {
		DBUtil.createTables();
	}

	@Test
	public void Con() throws Exception {
		Refacter.dropTable(DockerHostImpl.class);
		Refacter.createTable(DockerHostImpl.class);
	}
}
