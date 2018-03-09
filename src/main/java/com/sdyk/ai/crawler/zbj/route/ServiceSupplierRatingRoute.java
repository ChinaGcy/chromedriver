package com.sdyk.ai.crawler.zbj.route;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.zbj.ServiceWrapper;
import com.sdyk.ai.crawler.zbj.model.Model;
import com.sdyk.ai.crawler.zbj.model.SupplierRating;
import org.tfelab.io.server.Msg;
import spark.*;

import java.util.List;

public class ServiceSupplierRatingRoute {

	public static Route getServiceSupplierRatings = (Request request, Response response) -> {

		int serviceSupplierId = Integer.parseInt(request.params(":servicesupplierid"));
		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<SupplierRating, String> dao = Model.daoMap.get(SupplierRating.class.getSimpleName());

		QueryBuilder<SupplierRating, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		// TODO 可能有问题
		List<SupplierRating> ps = qb.where().eq("service_supplier_id", serviceSupplierId).query();;

		ServiceWrapper.logger.info(qb.prepareStatementString());

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<List<SupplierRating>>(Msg.SUCCESS, ps);
	};

}
