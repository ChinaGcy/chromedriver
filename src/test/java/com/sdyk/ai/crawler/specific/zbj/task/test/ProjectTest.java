package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.snapshot.ProjectSnapshot;
import com.sdyk.ai.crawler.model.witkey.snapshot.ServiceProviderSnapshot;
import one.rewind.db.DaoManager;
import org.junit.Test;

public class ProjectTest {


	// https://ucenter.zbj.com/phone/getANumByTask
	/**
	 * 测试获取最优需求数据
	 */
	@Test
	public void test() throws Exception {



	}

	@Test
	public void testBuildSnapshot() throws Exception {
		Dao<Project, String> dao = DaoManager.getDao(Project.class);
		Project project = dao.queryForId("00756aa02825b3193077791ea1aa1675");
		ProjectSnapshot snapshot = new ProjectSnapshot(project);

		System.out.println(project.toJSON());
		System.err.println(snapshot.toJSON());
	}

	// 6d351586e7ed7a2ef29ee40b2e6f35b0
	@Test
	public void testBuildSnapshot1() throws Exception {
		Dao<ServiceProvider, String> dao = DaoManager.getDao(ServiceProvider.class);
		ServiceProvider serviceProvider = dao.queryForId("6d351586e7ed7a2ef29ee40b2e6f35b0");
		ServiceProviderSnapshot snapshot = new ServiceProviderSnapshot(serviceProvider);

		System.out.println(serviceProvider.toJSON());
		System.err.println(snapshot.toJSON());
	}
}
