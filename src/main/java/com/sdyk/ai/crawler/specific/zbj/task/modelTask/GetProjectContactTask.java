package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import com.sdyk.ai.crawler.specific.zbj.model.ProjectEval;
import one.rewind.db.DaoManager;

/**
 * 获取zbj甲方手机号
 */
public class GetProjectContactTask extends Task {

	static {
		registerBuilder(
				ProjectTask.class,
				"https://task.zbj.com/{{project_id}}/",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "0"),
				true,
				Priority.MEDIUM
		);
	}

	public Project project;

	//public ProjectEval projectEval;

	public GetProjectContactTask(String url) throws Exception {

		super(url);
		// 设置优先级
		this.setPriority(Priority.HIGH);

		project = DaoManager.getDao(Project.class).queryForId(getUrl().split("/")[3]);

		//projectEval = DaoManager.getDao(ProjectEval.class).queryForId(getUrl().split("/")[3]);

		this.addAction(new GetProjectContactAction());

		// D 监听异步请求
		this.setResponseFilter((res, contents, messageInfo) -> {

			// D1 通过返回数据获取手机号
			if(messageInfo.getOriginalUrl().contains("getANumByTask")) {

				ServiceWrapper.logger.info(messageInfo.getOriginalUrl());

				if(contents != null) {
					String src = new String(contents.getBinaryContents());
					String cellphone = src.replaceAll("^.+?\"data\":\"", "")
							.replaceAll("\"}", "");

					ServiceWrapper.logger.info(cellphone);
					getResponse().setVar("cellphone", cellphone);
				} else {
					logger.info("NOT Find Cellphone");
				}
			}
		});

		this.addDoneCallback((t) -> {

			try {
				project.cellphone = getResponse().getVar("cellphone");
				ServiceWrapper.logger.info("project {} update {}.",
						project.id, project.update());
			} catch (Exception e) {
				logger.error("Error update project:{} cellphone.", project.id, e);
			}

		});
	}

}
