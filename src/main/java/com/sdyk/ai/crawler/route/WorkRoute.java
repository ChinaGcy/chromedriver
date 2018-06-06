package com.sdyk.ai.crawler.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.Work;
import one.rewind.io.server.Msg;
import spark.*;

import java.util.List;

import static com.sdyk.ai.crawler.model.Model.rewriteBinaryUrl;

/**
 * 案例路由
 */
public class WorkRoute {

	/**
	 * 通过id查询案例
	 */
	public static Route getWorkById = (Request request, Response response ) -> {

		String id = request.params(":id");

		Work k = (Work) Model.daoMap.get(Work.class.getSimpleName()).queryForId(id);

		//k.description = rewriteBinaryUrl(k.description);

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<Work>(Msg.SUCCESS, k);
	};

	/**
	 * 案例列表
	 */
	public static Route getWorks = (Request request, Response response) -> {

		String serviceSupplier = request.params(":id");

		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<Work, String> dao = Model.daoMap.get(Work.class.getSimpleName());

		QueryBuilder<Work, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		ServiceWrapper.logger.info(qb.prepareStatementString());

		List<Work> ws = qb.where().eq("user_id",serviceSupplier).query();

		for(Work w : ws) {
		//	w.description = rewriteBinaryUrl(w.description);
		}

		response.header("Access-Control-Allow-Origin", "*");
		return new Msg<List<Work>>(Msg.SUCCESS, ws);
	};
}
