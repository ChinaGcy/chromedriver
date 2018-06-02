package com.sdyk.ai.crawler.route;

import com.google.gson.Gson;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.GetProjectContactTask;
import one.rewind.db.DaoManager;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZbjRoute {

	public static Route getContactByProjectId = (Request request, Response response ) -> {

		String id = request.params(":id");

		// 获取精选需求
		Project project = DaoManager.getDao(Project.class).queryForId(id);

		// 生成任务
		GetProjectContactTask task = GetProjectContactTask.getTask(project);

		boolean result = AuthorizedRequester.getInstance().submit_(task);

		return new Msg<Boolean> (Msg.SUCCESS, result);
	};
}
