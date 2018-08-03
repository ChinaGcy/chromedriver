package com.sdyk.ai.crawler.route;

import com.google.gson.Gson;
import com.sdyk.ai.crawler.util.LocationParser;
import one.rewind.io.server.Msg;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;
import java.util.stream.Collectors;

public class LocationParserRoute {

	public static Route parseLocation = (Request request, Response response ) -> {

		String localSrc = request.queryParams("q");

		List<String> locations = LocationParser.getInstance()
				.matchLocation(localSrc).stream().map(l -> ((LocationParser.Location) l).toString()).collect(Collectors.toList());

		if( locations.size() > 0 ) {

			return new Msg<List<String>>(Msg.SUCCESS, locations);

		} else {
			return new Msg<>(Msg.OBJECT_NOT_FOUND);
		}
	};
}
