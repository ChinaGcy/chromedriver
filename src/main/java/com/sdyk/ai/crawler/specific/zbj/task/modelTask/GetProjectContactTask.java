package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.model.ProjectEval;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;

/**
 * 获取zbj甲方手机号
 */
public class GetProjectContactTask extends com.sdyk.ai.crawler.specific.zbj.task.Task {

	public Project project;

	public ProjectEval evalProjects;

	public static GetProjectContactTask getTask(Project project) {

		try {

			// A 赋值
			GetProjectContactTask task = new GetProjectContactTask(project.url);
			task.project = project;
			task.evalProjects.id = project.id;
			task.evalProjects.url = project.url;

			/*Account account = AccountManager.getAccountByDomain("zbj.com", "select");*/
			/*task.addAction(new LoginWithGeetestAction(account));
			task.addAction(new RedirectAction(project.url));*/

			// B 添加动作
			task.addAction(new GetProjectContactAction(task.evalProjects));

			// C 填写手机号
			task.addDoneCallback(()-> {

				try {
					task.project.cellphone = task.getResponse().getVar("cellphone");
					task.evalProjects.cellphone = task.project.cellphone;
					ServiceWrapper.logger.info("project {} update {} projectscore {}.",
							task.project.id, task.project.update(), task.evalProjects.update());
				} catch (Exception e) {
					logger.error("Error update project:{} cellphone.", task.project.id, e);
				}

			});

			// D 监听异步请求
			task.setResponseFilter((res, contents, messageInfo) -> {

				// D1 通过返回数据获取手机号
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
