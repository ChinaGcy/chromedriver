package com.sdyk.ai.crawler.zbj.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sdyk.ai.crawler.zbj.model.Model;
import one.rewind.io.server.Msg;
import one.rewind.json.JSONable;
import spark.ResponseTransformer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelMsgTransformer implements ResponseTransformer {

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public String render(Object model) throws JsonProcessingException {

		if(model instanceof JSONable){
			return ((JSONable) model).toJSON();
		}
		else if (model instanceof ObjectNode) {
			return ((ObjectNode) model).toString();
		}
		else {
			return mapper.writeValueAsString(model);
		}
	}
}
