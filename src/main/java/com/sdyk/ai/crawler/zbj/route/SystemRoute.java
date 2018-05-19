package com.sdyk.ai.crawler.zbj.route;

import com.sdyk.ai.crawler.zbj.model.Binary;
import com.sdyk.ai.crawler.zbj.model.Model;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 *二进制路由
 */
public class SystemRoute {

	public static Route getQueueSize = (Request request, Response response ) -> {

		try {
			return new Msg<Integer>(Msg.SUCCESS, ChromeDriverRequester.getInstance().queue.size());
		} catch (Exception e) {
			return new Msg<>(Msg.KERNEL_FAILURE);
		}
	};

}
