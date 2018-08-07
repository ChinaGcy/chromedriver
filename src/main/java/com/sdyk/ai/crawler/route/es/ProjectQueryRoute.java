package com.sdyk.ai.crawler.route.es;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.es.ProjectAdapter;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过ES搜索project
 */
public class ProjectQueryRoute {

	/**
	 * 关键词搜索需求信息
	 * 默认 每页20条数据
	 */
	public static Route query = (Request request, Response response) -> {

		Map<String, String> map = new HashMap<>();
		map.put("begin_time", request.queryParams("begin_time"));
		map.put("end_time", request.queryParams("end_time"));
		map.put("status", request.queryParams("status"));
		map.put("budget_min", request.queryParams("budget_min"));
		map.put("budget_max", request.queryParams("budget_max"));
		map.put("platform", request.queryParams("platform"));
		map.put("categorys", request.queryParams("categorys"));
		map.put("tags", request.queryParams("tags"));
		map.put("location", request.queryParams("location"));

		Integer page = 1;
		Integer length = 20;

		String query = request.queryParams("_q");

		try {
			page = Integer.parseInt(request.queryParams("page"));
			length = Integer.parseInt(request.queryParams("length"));
		}catch (Exception e) {

			ServiceWrapper.logger.error("Error parse page / length parameter. ");
		}

		Map<String, Object> data = ProjectAdapter.projectRecommend(map, query, page, length);

		return new Msg<>(Msg.SUCCESS,data);

	};
}
