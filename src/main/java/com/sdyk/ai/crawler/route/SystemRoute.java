package com.sdyk.ai.crawler.route;

import com.sdyk.ai.crawler.Requester;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

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

	/**
	 * 简单统计未执行任务类型接口
	 */
	public static Route getTaskStat = (Request request, Response response ) -> {

		try{
			return new Msg<Map<String, Integer>>(Msg.SUCCESS, Requester.taskStat);
		} catch (Exception e) {
			return new Msg<>(Msg.KERNEL_FAILURE);
		}

	};
}
