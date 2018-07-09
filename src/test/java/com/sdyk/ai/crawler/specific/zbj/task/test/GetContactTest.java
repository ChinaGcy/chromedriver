package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.Scheduler;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.GetProjectContactTask;
import one.rewind.db.DaoManager;
import org.junit.Test;

public class GetContactTest {

	@Test
	public void getGetContact() throws Exception {

		int num = 0;
		Scheduler scheduler = new Scheduler("zbj.com", num);
		scheduler.initAuthorizedRequester();

		String project_id = "55cfaf039c82a3064f584f9252639678";

		Project project = DaoManager.getDao(Project.class).queryForId(project_id);

		GetProjectContactTask task = GetProjectContactTask.getTask(project);

		boolean result = AuthorizedRequester.getInstance().submit_(task);

		Thread.sleep(1000000);
	}
}
