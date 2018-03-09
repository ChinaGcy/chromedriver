package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.model.Binary;
import com.sdyk.ai.crawler.zbj.model.TendererRating;
import com.sdyk.ai.crawler.zbj.model.Work;
import com.sdyk.ai.crawler.zbj.route.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.io.server.MsgTransformer;

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

		// project
		get("/project/:id", ProjectRoute.getProjectById, new ModelMsgTransformer());

		get("/projects/:page", ProjectRoute.getProjects, new ModelMsgTransformer());

		// binary
		get("/binarys/:id", BinaryRoute.getBinaryForId, new ModelMsgTransformer());

		// tenderer
		get("/tenderer/:id", TendererRoute.getTendererById, new ModelMsgTransformer());

		get("/tenderers/:page", TendererRoute.getTenderers, new ModelMsgTransformer());

		// tendererRating
		get("/tenderer/:tendererid/rating/:page", TendererRatingRoute.getTendererRatings, new ModelMsgTransformer());

		// serviceSupplier
		get("/servicesupplier/:id", ServiceSupplierRoute.getServiceById, new ModelMsgTransformer());

		get("/servicesuppliers/:page", ServiceSupplierRoute.getServiceSuppliers, new ModelMsgTransformer());

		// serviceSupplier_case
		get("/servicesupplier/case/:id", CaseRoute.getCaseById, new ModelMsgTransformer());

		get("/servicesupplier/:userid/cases/:page", CaseRoute.getCases, new ModelMsgTransformer());

		// serviceSupplier_work
		get("/servicesupplier/work/:id", WorkRoute.getWorkById, new ModelMsgTransformer());

		get("/servicesupplier/:userid/works/:page", WorkRoute.getWorks, new ModelMsgTransformer());

		// serviceSupplier_rating
		get("/servicesupplier/:servicesupplierid/rating/:page", TendererRatingRoute.getTendererRatings, new ModelMsgTransformer());

	}

	public static void main(String[] args) {
		ServiceWrapper.getInstance();
	}
}
