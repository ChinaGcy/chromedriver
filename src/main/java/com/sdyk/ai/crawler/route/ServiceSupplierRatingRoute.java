package com.sdyk.ai.crawler.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import one.rewind.io.server.Msg;
import spark.*;

import java.util.List;

/**
 * 服务商评价路由
 */
public class ServiceSupplierRatingRoute {

	/**
	 * 评价列表
	 */
	public static Route getServiceSupplierRatings = (Request request, Response response) -> {

		int serviceSupplierId = Integer.parseInt(request.params(":id"));
		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<ServiceProviderRating, String> dao = Model.daoMap.get(ServiceProviderRating.class.getSimpleName());

		QueryBuilder<ServiceProviderRating, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		// TODO 可能有问题
		List<ServiceProviderRating> ps = qb.where().eq("service_provider_id", serviceSupplierId).query();;

		ServiceWrapper.logger.info(qb.prepareStatementString());

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<List<ServiceProviderRating>>(Msg.SUCCESS, ps);
	};

}
