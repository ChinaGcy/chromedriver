package com.sdyk.ai.crawler.route;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.GetProjectContactTask;
import one.rewind.db.DaoManager;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

public class ZbjRoute {

	public static Route getContactByProjectId = (Request request, Response response ) -> {

		String id = request.params(":id");

		GetProjectContactTask task = GetProjectContactTask.getTask(id);

		task.setResponseFilter((res, contents, messageInfo) -> {

			if(messageInfo.getOriginalUrl().contains("getANumByTask")) {

				ServiceWrapper.logger.info(messageInfo.getOriginalUrl());

				if(contents != null) {
					String src = new String(contents.getBinaryContents());
					System.err.println(src);

					task.getResponse().setVar("cellphone", src);
				}

			}
		});

		task.addDoneCallback(()-> {
			task.project.cellphone = task.getResponse().getVar("cellphone");
			task.project.update();
		});

		boolean result = AuthorizedRequester.getInstance().submit_(task);

		return new Msg<Boolean> (Msg.SUCCESS, result);
	};
}
