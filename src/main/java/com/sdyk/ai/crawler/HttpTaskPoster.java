package com.sdyk.ai.crawler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sdyk.ai.crawler.model.TaskInitializer;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.Task;
import one.rewind.io.server.Msg;
import one.rewind.json.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 任务提交器
 *
 * @Author
 * @Date
 */
public class HttpTaskPoster {

	// logger 日志
	public static final Logger logger = LogManager.getLogger(ChromeDriverDistributor.class.getName());

	//
	public static HttpTaskPoster instance;

	// 单例
	public static HttpTaskPoster getInstance() {

		if (instance == null) {
			synchronized (HttpTaskPoster.class) {
				if (instance == null) {
					instance = new HttpTaskPoster();
				}
			}
		}

		return instance;
	}

	// ip
	public String host = "127.0.0.1";
	// 端口
	public int port = 80;

	public HttpTaskPoster() {}

	public HttpTaskPoster(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * 
	 * @param class_name
	 * @param username
	 * @param map_json
	 * @param step
	 * @param cron
	 * @return
	 * @throws ClassNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public String submit(String class_name, String username, String map_json, int step, String... cron) throws ClassNotFoundException, UnsupportedEncodingException, MalformedURLException, URISyntaxException {

		String params = "";

		params += "class_name=" + class_name;

		params += "&init_map=" + map_json;

		params += "&step=" + step;

		if (cron != null){

			for(String c : cron) {
				params += "&cron=" + URLEncoder.encode(c,"utf-8");
			}
		}

		if (username != null && username.length() > 0) {
			params += "&username=" + username;
		}

		String url = "http://" + host + ":" + port + "/task?" + params;

		Task task = new Task(url);
		task.setPost();
		BasicRequester.getInstance().submit(task);

		String scheduled_task_id = "";

		Pattern pattern = Pattern.compile("\"id\":\"(?<Id>.+?)\"");
		Matcher matcher = pattern.matcher(task.getResponse().getText());
		if (matcher.find()) {
			scheduled_task_id = matcher.group("Id");
		}

		/*Type type = new TypeToken<Msg<Map<String, Object>>>(){}.getType();

		Msg<Map<String, Object>> msg = JSON.fromJson(task.getResponse().getText(), type);

		System.out.println(JSON.toPrettyJson(msg));*/

		return scheduled_task_id;

	}

	/**
	 *
	 * @param class_name
	 * @param map_json
	 * @return
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public String submit(String class_name, String map_json) throws ClassNotFoundException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		return submit(class_name, null, map_json, 0, null);
	}

	/**
	 *
	 * @param class_name
	 * @param username
	 * @param map_json
	 * @return
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public String submit(String class_name, String username, String map_json) throws ClassNotFoundException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		return submit(class_name, username, map_json, 0, null);
	}

	/**
	 *
	 * @param class_name
	 * @param username
	 * @param map_json
	 * @param step
	 * @return
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public String submit(String class_name, String username, String map_json, int step) throws ClassNotFoundException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		return submit(class_name, username, map_json, step, null);
	}

	public static void startAllInitializers() throws ClassNotFoundException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {

		for(TaskInitializer initializer : TaskInitializer.getAll()) {

			HttpTaskPoster.getInstance().submit(initializer.class_name,null, initializer.init_map_json, 0, initializer.cron);
		}
	}
}
