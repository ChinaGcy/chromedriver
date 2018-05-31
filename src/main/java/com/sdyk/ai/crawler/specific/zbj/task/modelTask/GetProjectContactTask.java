package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import one.rewind.db.DaoManager;

public class GetProjectContactTask extends com.sdyk.ai.crawler.specific.zbj.task.Task {

	Project project;

	public static GetProjectContactTask getTask(String project_id) {

		try {
			Project project = DaoManager.getDao(Project.class).queryForId(project_id);
			GetProjectContactTask task = new GetProjectContactTask(project.url);
			task.addAction(new GetProjectContactAction(project));
			project.cellphone = task.getResponse().getVar("cellphone");
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
