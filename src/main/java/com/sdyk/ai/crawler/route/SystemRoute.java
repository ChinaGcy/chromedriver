package com.sdyk.ai.crawler.route;

import com.sdyk.ai.crawler.Distributor;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;

import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.*;

/**
 * 系统信息路由
 */
public class SystemRoute {

	/**
	 * 获取队列未执行任务数量 & 空闲ChromeDriverAgent
	 */
	public static Route getQueueSize = (Request request, Response response ) -> {

		try {

			System.out.println("getQueueSize");

			Map<String, Object> data = new TreeMap<>();

			data.put("taskQueueNum", ChromeDriverDistributor.getInstance().queues.size());

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
			return new Msg<Map<String, Integer>>(Msg.SUCCESS, Distributor.taskQueueStat);
		} catch (Exception e) {
			return new Msg<>(Msg.KERNEL_FAILURE);
		}

	};

	/**
	 * 简单统计agent信息
	 */
	public static Route getAgentInformation = (Request request, Response response ) -> {

		Map<String, List<String>> resurt = new HashMap<>();

		for(ChromeDriverAgent a : ((Distributor)ChromeDriverDistributor.getInstance()).queues.keySet()){

			List<String> domains = new ArrayList<>();

			for( String d : a.accounts.keySet() ){
				domains.add(d);
				System.out.println(d);
			}

			resurt.put(a.name, domains);
		}

		try{
			return new Msg<Map<String, List<String>>>(Msg.SUCCESS, resurt);
		} catch (Exception e) {
			return new Msg<>(Msg.KERNEL_FAILURE);
		}

	};

}
