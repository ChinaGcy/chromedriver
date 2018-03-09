package com.sdyk.ai.crawler.zbj.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.zbj.ServiceWrapper;
import com.sdyk.ai.crawler.zbj.model.Model;
import com.sdyk.ai.crawler.zbj.model.Tenderer;
import org.tfelab.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

public class TendererRoute {

	public static Route getTendererById = (Request request, Response response ) -> {

		String id = request.params(":id");

		Tenderer p = (Tenderer) Model.daoMap.get(Tenderer.class.getSimpleName()).queryForId(id);

		System.err.println(p.toJSON());
		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<Tenderer>(Msg.SUCCESS, p);
	};

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
