package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.chrome.action.RedirectAction;

public class GetProjectContactTask extends com.sdyk.ai.crawler.specific.zbj.task.Task {

	public Project project;

	public static GetProjectContactTask getTask(String project_id) {

		try {

			Project project = DaoManager.getDao(Project.class).queryForId(project_id);

			GetProjectContactTask task = new GetProjectContactTask(project.url);

			Account account = AccountManager.getAccountByDomain("zbj.com", "select");

			/*task.addAction(new LoginWithGeetestAction(account));
			task.addAction(new RedirectAction(project.url));*/
			task.addAction(new GetProjectContactAction(project));

			return task;

		} catch (Exception e) {
			logger.error(e);
		}

		return null;
	}

	public GetProjectContactTask(String url) throws Exception {

		super(url);
		// 设置优先级
		this.setPriority(Priority.HIGH);
	}

}
