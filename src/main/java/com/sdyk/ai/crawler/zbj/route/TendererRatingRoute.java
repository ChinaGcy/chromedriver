package com.sdyk.ai.crawler.zbj.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.zbj.ServiceWrapper;
import com.sdyk.ai.crawler.zbj.model.Model;
import com.sdyk.ai.crawler.zbj.model.TendererRating;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

/**
 * 雇主评价列表
 */
public class TendererRatingRoute {

	public static Route getTendererRatings = (Request request, Response response) -> {

		int tendererId = Integer.parseInt(request.params(":tendererid"));
		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<TendererRating, String> dao = Model.daoMap.get(TendererRating.class.getSimpleName());

		QueryBuilder<TendererRating, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		// TODO 可能有问题
		List<TendererRating> ps = qb.where().eq("tenderer_url", "http://home.zbj.com/"+tendererId+"/").query();;

		ServiceWrapper.logger.info(qb.prepareStatementString());

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<List<TendererRating>>(Msg.SUCCESS, ps);
	};
}
