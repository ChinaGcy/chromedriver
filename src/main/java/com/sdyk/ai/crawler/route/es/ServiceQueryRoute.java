package com.sdyk.ai.crawler.route.es;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.es.ServiceAdapter;
import com.sdyk.ai.crawler.es.TendererAdapter;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public class ServiceQueryRoute {

	public static Route query = (Request request, Response response) -> {

		Map<String, String> map = new HashMap<>();
		map.put("type", request.queryParams("type"));
		map.put("location", request.queryParams("location"));
		map.put("position", request.queryParams("position"));

		String query = request.queryParams("_q");

		int page = 1;
		int length = 20;

		try {
			page = Integer.parseInt(request.queryParams("page"));
			length = Integer.parseInt(request.queryParams("length"));
		}catch (Exception e) {
			ServiceWrapper.logger.error("Error parse page / length parameter. ");
		}

		Map<String, Object> data = ServiceAdapter.serviceQuery(map, query, page, length);

		return new Msg<>(Msg.SUCCESS,data);
	};
}
