package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.docker.model.DockerContainer;
import com.sdyk.ai.crawler.zbj.docker.model.DockerHost;
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
		Refacter.dropTable(DockerHost.class);
		Refacter.createTable(DockerHost.class);
	}

	@Test
	public void test() throws Exception {
		DockerContainer container = new DockerContainer();

		container.deleteAll();
	}
}
