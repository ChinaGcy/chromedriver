package com.sdyk.ai.crawler.route.es;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.es.ProjectAdapter;
import com.sdyk.ai.crawler.es.TendererAdapter;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public class TendererQueryRoute {

	public static Route query = (Request request, Response response) -> {

		Map<String, String> map = new HashMap<>();
		int page = 1;
		int length = 20;

		String query = request.queryParams("_q");
		map.put("type", request.queryParams("type"));
		map.put("source", request.queryParams("source"));

		try {
			page = Integer.parseInt(request.queryParams("page"));
			length = Integer.parseInt(request.queryParams("length"));
		}catch (Exception e) {
			ServiceWrapper.logger.error("Error parse page / length parameter. ");
		}

		Map<String, Object> data = TendererAdapter.tendererRecommend(map, query, page, length);

		return new Msg<>(Msg.SUCCESS,data);
	};
}