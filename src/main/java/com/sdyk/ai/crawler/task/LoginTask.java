package com.sdyk.ai.crawler.task;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sdyk.ai.crawler.LoginInAction__;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.json.JSON;
import one.rewind.json.JSONable;
import one.rewind.util.Configs;
import org.openqa.selenium.interactions.SendKeysAction;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoginTask extends ChromeTask implements JSONable<LoginTask> {

	public LoginTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public String toJSON() {

		List<Map> actions = new ArrayList<>();

		this.getActions().stream().forEach( a-> {

			LinkedHashMap action_map = new LinkedHashMap();

			action_map.put("className", a.getClass().getName());
			action_map.put("content", a);

			actions.add(action_map);

		});

		LinkedHashMap task_map = new LinkedHashMap();
		task_map.put("url", getUrl());
		task_map.put("actions", actions);

		return JSON.toPrettyJson(task_map);
	}

	public static LoginTask buildFromJson(String json) throws ClassNotFoundException, MalformedURLException, URISyntaxException {

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

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, ClassNotFoundException, ChromeDriverException.IllegalStatusException, InterruptedException {

		/*ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();*/

		LoginTask task = new LoginTask("https://www.mihuashi.com/login");

		LoginAction a1 = new LoginAction();
		a1.url = "https://www.itjuzi.com/user/login";
		a1.usernameCssPath = "#create_account_email";
		a1.passwordCssPath = "#create_account_password";
		a1.loginButtonCssPath = "#login_btn";
		a1.errorMsgReg = "账号或密码错误";
		//a1.setAccount(new AccountImpl("mihuashi.com", "17152187084", "123456 "));


		//ClickAction a2 = new ClickAction("#tab-mobile",2000);

		//task.addAction(a2);
		task.addAction(a1);

		System.out.println(task.toJSON());

		/*LoginTask task1 = LoginTask.buildFromJson(task.toJSON());

		agent.submit(task1);

		Thread.sleep(1000);*/





	}

}
