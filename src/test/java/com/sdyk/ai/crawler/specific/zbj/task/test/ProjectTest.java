package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.model.snapshot.ProjectSnapshot;
import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.GetProjectContactTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import org.junit.Test;

public class ProjectTest {


	// https://ucenter.zbj.com/phone/getANumByTask
	/**
	 * 测试获取最优需求数据
	 */
	@Test
	public void test() throws Exception {

		Account account = AccountManager.getAccountByDomain("zbj.com");

		//ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress());

		com.sdyk.ai.crawler.specific.zbj.task.Task task = new com.sdyk.ai.crawler.specific.zbj.task.Task("zbj.com");

		task.addAction(new LoginWithGeetestAction(account));

		ChromeDriverAgent agent = new ChromeDriverAgent();

		AuthorizedRequester.getInstance().addAgent(agent);

		agent.start();

		AuthorizedRequester.getInstance().submit(task);

		ProjectTask task1 = new ProjectTask("https://task.zbj.com/13501948/");



		GetProjectContactTask task2 = GetProjectContactTask.getTask(new Project());
		task.addAction(new GetProjectContactAction(task2.evalProjects));

		AuthorizedRequester.getInstance().submit(task1);

		Thread.sleep(10000000);

	}

	@Test
	public void testBuildSnapshot() throws Exception {
		Dao<Project, String> dao = DaoManager.getDao(Project.class);
		Project project = dao.queryForId("0d2c6b93bcbbc7bd58e286438364868b");
		ProjectSnapshot snapshot = new ProjectSnapshot(project);
		System.err.println(snapshot.toJSON());
	}
}
