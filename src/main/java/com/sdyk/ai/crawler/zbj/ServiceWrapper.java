package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.route.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.get;
import static spark.Spark.port;

public class ServiceWrapper {

	public static final Logger logger = LogManager.getLogger(ServiceWrapper.class.getName());

	private static ServiceWrapper instance;

	public static ServiceWrapper getInstance() {

		synchronized (ServiceWrapper.class) {
			if (instance == null) {
				instance = new ServiceWrapper();
			}
		}

		return instance;
	}

	private int port = 80;

	public ServiceWrapper() {

		port(port);

		// 需求
		get("/project/:id", ProjectRoute.getProjectById, new ModelMsgTransformer());

		get("/projects/:page", ProjectRoute.getProjects, new ModelMsgTransformer());

		// 下载文件
		get("/binarys/:id", BinaryRoute.getBinaryForId, new ModelMsgTransformer());

		// 雇主
		get("/tenderer/:id", TendererRoute.getTendererById, new ModelMsgTransformer());

		get("/tenderers/:page", TendererRoute.getTenderers, new ModelMsgTransformer());

		// 雇主评价
		get("/tenderer/:tendererid/rating/:page", TendererRatingRoute.getTendererRatings, new ModelMsgTransformer());

		// 服务商
		get("/servicesupplier/:id", ServiceSupplierRoute.getServiceById, new ModelMsgTransformer());

		get("/servicesuppliers/:page", ServiceSupplierRoute.getServiceSuppliers, new ModelMsgTransformer());

		// 服务商案例
		get("/servicesupplier/case/:id", CaseRoute.getCaseById, new ModelMsgTransformer());

		get("/servicesupplier/:userid/cases/:page", CaseRoute.getCases, new ModelMsgTransformer());

		// 服务商服务
		get("/servicesupplier/work/:id", WorkRoute.getWorkById, new ModelMsgTransformer());

		get("/servicesupplier/:userid/works/:page", WorkRoute.getWorks, new ModelMsgTransformer());

		// 服务商评价
		get("/servicesupplier/:servicesupplierid/rating/:page", TendererRatingRoute.getTendererRatings, new ModelMsgTransformer());

		get("/system/queue", SystemRoute.getQueueSize, new ModelMsgTransformer());

	}

	public static void main(String[] args) {
		ServiceWrapper.getInstance();
	}
}
