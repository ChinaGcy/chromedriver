package com.sdyk.ai.crawler.zbj.route;

import com.sdyk.ai.crawler.zbj.model.Binary;
import com.sdyk.ai.crawler.zbj.model.Model;
import jodd.io.FileUtil;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 *二进制路由
 */
public class BinaryRoute {

	public static Route getBinaryForId = (Request request, Response response ) -> {

		String id = request.params(":id");

		Binary binary = (Binary) Model.daoMap.get(Binary.class.getSimpleName()).queryForId(id);

		if(binary != null) {

			response.type(binary.content_type.replaceAll("\\[|\\]", ""));
			response.header("Content-Disposition",
					"attachment; filename=\"" + binary.file_name + "\"; filename*=utf-8' '" + binary.file_name);
			response.header("Content-Transfer-Encoding", "binary");


			response.raw().setContentLength(binary.src.length);
			response.raw().getOutputStream().write(binary.src);

			return null;

		} else {
			return new Msg<>(Msg.OBJECT_NOT_FOUND);
		}

	};

}
