package com.sdyk.ai.crawler.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Case;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

import static com.sdyk.ai.crawler.model.Model.rewriteBinaryUrl;

/**
 * 服务路由
 */
public class CaseRoute {

	public static Route getCaseById = (Request request, Response response ) -> {

		String id = request.params(":id");

		Case c = (Case) Model.daoMap.get(Case.class.getSimpleName()).queryForId(id);

		c.content = rewriteBinaryUrl(c.content);

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<Case>(Msg.SUCCESS, c);
	};

	/**
	 *
	 */
	public static Route getCases = (Request request, Response response) -> {

		String user_id = request.params(":id");

		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<Case, String> dao = Model.daoMap.get(Case.class.getSimpleName());

		QueryBuilder<Case, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		ServiceWrapper.logger.info(qb.prepareStatementString());

		List<Case> cs = qb.where().eq("user_id", user_id).query();

		for(Case c : cs) {
			c.content = rewriteBinaryUrl(c.content);
		}

		response.header("Access-Control-Allow-Origin", "*");
		return new Msg<List<Case>>(Msg.SUCCESS, cs);
	};
}
