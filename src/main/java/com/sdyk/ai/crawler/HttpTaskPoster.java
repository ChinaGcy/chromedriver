package com.sdyk.ai.crawler;

import com.google.gson.reflect.TypeToken;
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
import java.util.Map;

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
	 * @param clazz
	 * @param username
	 * @param map
	 * @param step
	 * @param cron
	 * @return
	 * @throws ClassNotFoundException
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public boolean submit(Class clazz, String username, Map<String, String> map, int step, String... cron) throws ClassNotFoundException, UnsupportedEncodingException, MalformedURLException, URISyntaxException {

		String params = "";

		params += "class_name=" + clazz.getName();

		params += "&init_map=" + JSON.toJson(map);

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

		Type type = new TypeToken<Msg<Map<String, Object>>>(){}.getType();
		Msg<Map<String, Object>> msg = JSON.fromJson(task.getResponse().getText(), type);

		logger.info(JSON.toPrettyJson(msg));

		return msg.code == Msg.SUCCESS;
	}

	/**
	 *
	 * @param clazz
	 * @param map
	 * @return
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public boolean submit(Class clazz, Map<String, String> map) throws ClassNotFoundException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		return submit(clazz, null, map, 0, null);
	}

	/**
	 *
	 * @param clazz
	 * @param username
	 * @param map
	 * @return
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public boolean submit(Class clazz, String username, Map<String, String> map) throws ClassNotFoundException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		return submit(clazz, username, map, 0, null);
	}

	/**
	 *
	 * @param clazz
	 * @param username
	 * @param map
	 * @param step
	 * @return
	 * @throws ClassNotFoundException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public boolean submit(Class clazz, String username, Map<String, String> map, int step) throws ClassNotFoundException, MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		return submit(clazz, username, map, step, null);
	}
}
