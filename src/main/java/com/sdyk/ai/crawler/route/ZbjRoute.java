package com.sdyk.ai.crawler.route;

import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.GetProjectContactTask;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

public class ZbjRoute {

	public static Route getContactByProjectId = (Request request, Response response ) -> {

		String id = request.params(":id");

		GetProjectContactTask task = GetProjectContactTask.getTask(id);

		task.setResponseFilter((res, contents, messageInfo) -> {
			if(messageInfo.getOriginalUrl().contains("https://ucenter.zbj.com/phone/getANumByTask")) {
				task.getResponse().setVar("cellphone", contents.getTextContents());
			}
		});

		boolean result = AuthorizedRequester.getInstance().submit_(task);

		return new Msg<Boolean>(Msg.SUCCESS, result);
	};
}
