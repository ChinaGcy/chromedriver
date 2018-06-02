package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.chrome.action.RedirectAction;

import java.util.logging.Logger;

public class GetProjectContactTask extends com.sdyk.ai.crawler.specific.zbj.task.Task {

	public Project project;

	public static GetProjectContactTask getTask(Project project) {

		try {

			GetProjectContactTask task = new GetProjectContactTask(project.url);
			task.project = project;

			/*Account account = AccountManager.getAccountByDomain("zbj.com", "select");*/
			/*task.addAction(new LoginWithGeetestAction(account));
			task.addAction(new RedirectAction(project.url));*/

			task.addAction(new GetProjectContactAction(project));

			task.addDoneCallback(()-> {

				try {
					task.project.cellphone = task.getResponse().getVar("cellphone");
					ServiceWrapper.logger.info("project {} update {}.",
							task.project.id, task.project.update());
				} catch (Exception e) {
					logger.error("Error update project:{} cellphone.", task.project.id, e);
				}

			});

			task.setResponseFilter((res, contents, messageInfo) -> {

				if(messageInfo.getOriginalUrl().contains("getANumByTask")) {

					ServiceWrapper.logger.info(messageInfo.getOriginalUrl());

					if(contents != null) {
						String src = new String(contents.getBinaryContents());
						String cellphone = src.replaceAll("^.+?\"data\":\"", "")
								.replaceAll("\"}", "");

						ServiceWrapper.logger.info(cellphone);
						task.getResponse().setVar("cellphone", cellphone);
					} else {
						logger.info("NOT Find Cellphone");
					}
				}
			});

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

		this.addDoneCallback(() -> {


		});
	}

}
