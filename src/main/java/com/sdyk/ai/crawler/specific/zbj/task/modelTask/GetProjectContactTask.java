package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import com.sdyk.ai.crawler.specific.zbj.model.ProjectEval;
import one.rewind.db.DaoManager;

/**
 * 获取zbj甲方手机号
 */
public class GetProjectContactTask extends Task {

	static {
		/*// init_map_class
		init_map_class = ImmutableMap.of("user_id", String.class,"case_id", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("user_id", "0", "case_id", "0");
		// url_template
		url_template = "https://shop.zbj.com/{{user_id}}/sid-{{case_id}}.html";*/
		registerBuilder(
				GetProjectContactTask.class,
				"https://shop.zbj.com/{{user_id}}/sid-{{case_id}}.html",
				ImmutableMap.of("user_id", String.class,"case_id", String.class),
				ImmutableMap.of("user_id", "0", "case_id", "0")

		);
	}

	public Project project;

	public ProjectEval projectEval;

	public static GetProjectContactTask getTask(Project project) {

		try {

			Dao dao = DaoManager.getDao(ProjectEval.class);

			// A 赋值
			GetProjectContactTask task = new GetProjectContactTask(project.url);
			task.project = project;

			task.projectEval = (ProjectEval) dao.queryForId(project.id);

			/*Account account = AccountManager.getAccountByDomain("zbj.com", "select");*/
			/*task.addAction(new LoginWithGeetestAction(account));
			task.addAction(new RedirectAction(project.url));*/

			// B 添加动作
			task.addAction(new GetProjectContactAction(task.projectEval));

			// C 填写手机号
			task.addDoneCallback((t)-> {

				try {
					task.project.cellphone = task.getResponse().getVar("cellphone");
					task.projectEval.cellphone = task.project.cellphone;
					ServiceWrapper.logger.info("project {} update {} projectscore {}.",
							task.project.id, task.project.update(), task.projectEval.update());
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

		this.addDoneCallback((t) -> {


		});
	}

}
