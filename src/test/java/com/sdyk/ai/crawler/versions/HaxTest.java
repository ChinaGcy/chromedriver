package com.sdyk.ai.crawler.versions;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.model.witkey.Project;
import one.rewind.db.DaoManager;
import org.junit.Test;

import java.util.Date;

public class HaxTest {

	@Test
	public void test() throws Exception {

		Dao dao = DaoManager.getDao(Project.class);

		Project oldproject = (Project) dao.queryForId("bfb9d5e5f496f45970790ce239505c6c");

		Project newProject = (Project) dao.queryForId("bfb9d5e5f496f45970790ce239505c6c");

		if( one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(oldproject.toJSON()))
				.equals( one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(newProject.toJSON())) ) ){
			System.out.println("实例相同");
		}
		else {
			System.out.println("实例不同");
		}

	}

	@Test
	public void testVersion() throws Exception {


		Dao dao = DaoManager.getDao(Project.class);

		Project newProject = (Project) dao.queryForId("bfb9d5e5f496f45970790ce239505c6c");

		newProject.insert_time = new Date();
		newProject.update_time = new Date();

		// 测试当数据未发生变化时，不做操作

		// 测试数据改变时，进行project的更新操作，ProjectSnapshot的插入操作
		//newProject.bidder_total_num = 10;
		//newProject.budget_ub = 50000;

		// 测试状态为已完成时，更新状态
		//newProject.status = "已完成";

		newProject.insert();

		Thread.sleep(100000000);

	}

}
