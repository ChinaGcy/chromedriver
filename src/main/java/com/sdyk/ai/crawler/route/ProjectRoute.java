package com.sdyk.ai.crawler.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.Project;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

import static com.sdyk.ai.crawler.model.Model.rewriteBinaryUrl;

/**
 * 项目路由
 */
public class ProjectRoute {

	/**
	 * 根据id查询
	 */
	public static Route getProjectById = (Request request, Response response ) -> {

		String id = request.params(":id");

		Project p = (Project) Model.daoMap.get(Project.class.getSimpleName()).queryForId(id);

		p.content = rewriteBinaryUrl(p.content);

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<Project>(Msg.SUCCESS, p);
	};

	/**
	 * 项目列表
	 */
	public static Route getProjects = (Request request, Response response) -> {

		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<Project, String> dao = Model.daoMap.get(Project.class.getSimpleName());

		QueryBuilder<Project, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		ServiceWrapper.logger.info(qb.prepareStatementString());

		List<Project> ps = qb.query();

		for(Project p : ps) {
			p.content = rewriteBinaryUrl(p.content);
		}

		response.header("Access-Control-Allow-Origin", "*");
		return new Msg<List<Project>>(Msg.SUCCESS, ps);
	};
}
