package com.sdyk.ai.crawler.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.json.JSON;
import one.rewind.json.JSONable;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoginTask extends ChromeTask implements JSONable<LoginTask> {

	public LoginTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.setValidator((a, t) -> {

			/*String text = t.getResponse().getText();

			if( (text.contains("ip") || text.contains("IP")) && text.contains("封禁") ){
				throw new ProxyException.Failed(a.proxy);
			}*/

		});

	}

	/**
	 * 将任务转换成json数据
	 * @return
	 */
	public static String toJSON(LoginTask loginTask) {

		List<Map> actions = new ArrayList<>();

		loginTask.getActions().stream().forEach( a-> {

			LinkedHashMap action_map = new LinkedHashMap();

			action_map.put("className", a.getClass().getName());
			action_map.put("content", a);

			actions.add(action_map);

		});

		LinkedHashMap task_map = new LinkedHashMap();
		task_map.put("url", loginTask.getUrl());
		task_map.put("actions", actions);

		return JSON.toPrettyJson(task_map);
	}

	/**
	 * 将json数据转换成登陆任务
	 * @param json
	 * @return
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws ProxyException.Failed
	 */
	public static LoginTask buildFromJson(String json) throws ClassNotFoundException, MalformedURLException, URISyntaxException, ProxyException.Failed {

		JsonParser parser = new JsonParser();

		JsonObject task_json = parser.parse(json).getAsJsonObject();

		String url = task_json.get("url").getAsString();

		LoginTask task = new LoginTask(url);

		JsonArray actions_json = task_json.get("actions").getAsJsonArray();

		for(JsonElement action_json : actions_json) {

			String className = action_json.getAsJsonObject().get("className").getAsString();

			String content = action_json.getAsJsonObject().get("content").toString();

			ChromeAction action = JSON.fromJson(content, (Class<ChromeAction>) Class.forName(className));

			task.addAction(action);
		}

		return task;
	}

}
