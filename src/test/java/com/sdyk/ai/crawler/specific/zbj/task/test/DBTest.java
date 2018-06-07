package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.model.*;
import com.sdyk.ai.crawler.specific.zbj.model.EvalProjects;
import com.sdyk.ai.crawler.util.DBUtil;
import one.rewind.db.Refacter;
import org.junit.Test;

public class DBTest {

	@Test
	public void testInitDao() {
		DBUtil.createTables();
	}

	@Test
	public void Con() throws Exception {
		//Refacter.dropTable(EvalProjects.class);
		Refacter.createTable(Work.class);
		/*try {

			DockerHostImpl host = new DockerHostImpl("10.0.0.62", 22, "root");
			host.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}
*/

	}
}
