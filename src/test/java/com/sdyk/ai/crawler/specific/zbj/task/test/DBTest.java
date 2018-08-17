package com.sdyk.ai.crawler.specific.zbj.task.test;


import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.model.TaskInitializer;
import com.sdyk.ai.crawler.model.WebDirverCount;
import com.sdyk.ai.crawler.model.company.CompanyInformation;
import com.sdyk.ai.crawler.model.witkey.Case;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.model.witkey.snapshot.ProjectSnapshot;
import com.sdyk.ai.crawler.model.witkey.snapshot.ServiceProviderSnapshot;
import com.sdyk.ai.crawler.model.witkey.snapshot.TendererSnapshot;
import com.sdyk.ai.crawler.util.DBUtil;
import com.sdyk.ai.crawler.util.LocationParser;
import one.rewind.db.DaoManager;
import one.rewind.db.Refacter;
import org.apache.tools.ant.taskdefs.optional.extension.LibFileSet;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class DBTest {

	@Test
	public void testInitDao() {
		DBUtil.createTables();
	}

	@Test
	public void Con() throws Exception {

		//Refacter.dropTable(Project.class);
		Refacter.createTable(CompanyInformation.class);
		/*try {

			DockerHostImpl host = new DockerHostImpl("10.0.0.62", 22, "root");
			host.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}
*/

	}

	/**
	 * 测试多值字段正确性
	 * @throws Exception
	 */
	@Test
	public void query() throws Exception {
		Dao dao = DaoManager.getDao(Project.class);

		List<Project> list = dao.queryForAll();

		for (Project project : list) {
			System.err.println(project.tags.size());
		}
	}

	/**
	 * 数据转移更新
	 * @throws Exception
	 */
	@Test
	public void convertData() throws Exception {

		DBUtil.convertData();

	}

}
