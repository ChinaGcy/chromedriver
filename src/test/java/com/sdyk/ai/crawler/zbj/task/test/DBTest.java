package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.docker.model.DockerHostImpl;
import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.model.TendererRating;
import com.sdyk.ai.crawler.zbj.util.DBUtil;
import one.rewind.db.Refacter;
import one.rewind.io.docker.model.DockerHost;
import org.junit.Test;

public class DBTest {

	@Test
	public void testInitDao() {
		DBUtil.createTables();
	}

	@Test
	public void Con() throws Exception {
		//Refacter.dropTable(DockerHostImpl.class);
		Refacter.createTable(Project.class);
		/*try {

			DockerHostImpl host = new DockerHostImpl("10.0.0.62", 22, "root");
			host.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}
*/

	}
}
