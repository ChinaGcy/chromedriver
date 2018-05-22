package com.sdyk.ai.crawler.zbj.route;

import com.sdyk.ai.crawler.zbj.model.Binary;
import com.sdyk.ai.crawler.zbj.model.Model;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Map;
import java.util.TreeMap;

/**
 *二进制路由
 */
public class SystemRoute {

	public static Route getQueueSize = (Request request, Response response ) -> {

		try {

			Map<String, Object> data = new TreeMap<>();

			data.put("taskQueueNum", ChromeDriverRequester.getInstance().queue.size());
			data.put("idleAgentNum", ChromeDriverRequester.getInstance().idleAgentQueue.size());

			return new Msg<Map<String, Object>>(Msg.SUCCESS, data);
		} catch (Exception e) {
			return new Msg<>(Msg.KERNEL_FAILURE);
		}
	};

}
