package com.sdyk.ai.crawler.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

/**
 * 雇主路由
 */
public class TendererRoute {

	/**
	 * 通过id查询雇主
	 */
	public static Route getTendererById = (Request request, Response response ) -> {

		String id = request.params(":id");

		Tenderer p = (Tenderer) Model.daoMap.get(Tenderer.class.getSimpleName()).queryForId(id);

		System.err.println(p.toJSON());
		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<Tenderer>(Msg.SUCCESS, p);
	};

	/**
	 * 雇主列表
	 */
	public static Route getTenderers = (Request request, Response response) -> {

		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<Tenderer, String> dao = Model.daoMap.get(Tenderer.class.getSimpleName());

		QueryBuilder<Tenderer, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		ServiceWrapper.logger.info(qb.prepareStatementString());

		List<Tenderer> ps = qb.query();
		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<List<Tenderer>>(Msg.SUCCESS, ps);
	};
}
