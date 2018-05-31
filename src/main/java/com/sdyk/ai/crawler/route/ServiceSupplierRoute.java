package com.sdyk.ai.crawler.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.ServiceSupplier;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

/**
 * 服务商路由
 */
public class ServiceSupplierRoute {

	/**
	 * 通过id查询服务商
	 */
	public static Route getServiceById = (Request request, Response response ) -> {

		String id = request.params(":id");

		ServiceSupplier p = (ServiceSupplier) Model.daoMap.get(ServiceSupplier.class.getSimpleName()).queryForId(id);

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<ServiceSupplier>(Msg.SUCCESS, p);
	};

	/**
	 *服务商列表
	 */
	public static Route getServiceSuppliers = (Request request, Response response) -> {

		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<ServiceSupplier, String> dao = Model.daoMap.get(ServiceSupplier.class.getSimpleName());

		QueryBuilder<ServiceSupplier, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		ServiceWrapper.logger.info(qb.prepareStatementString());

		List<ServiceSupplier> ps = qb.query();

		response.header("Access-Control-Allow-Origin", "*");
		return new Msg<List<ServiceSupplier>>(Msg.SUCCESS, ps);
	};
}
