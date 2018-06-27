package com.sdyk.ai.crawler.util;

import com.google.gson.Gson;
import com.sdyk.ai.crawler.model.Tenderer;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.task.ChromeTask;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Map;

public class URLUtil {

	/**
	 * 生成task的url地址
	 * @param clazz
	 * @param username
	 * @param map
	 * @param step
	 * @param domain
	 * @param needLogin
	 * @param getBasePriority
	 * @param cron
	 * @return
	 */
	public static void PostTask(Class clazz, String username, Map<String, String> map, Integer step, String domain, Boolean needLogin, String getBasePriority, String cron) throws ClassNotFoundException {

		String classname_ = "class_name="+clazz.getName();

		String init_map = "";

		if (map != null) {
			init_map = new Gson().toJson(map);
		}
		/*if (key != null && value != null && key.length > 0) {
			String start = "&init_map={";
			String end = "}";
			String s="";
			for (int i = 0; i< key.length; i++ ) {
				if (key[i] != null) {
					if (i>0) {
						s = s +","+ "\"" + key[i] + "\":\"" + value[i] + "\"";
					} else {
						s = "\"" + key[i] + "\":\"" + value[i] + "\"";
					}
				}

			}
			init_map = start + s + end;
		}*/

		String step_ = "";
		if (step!=null) {
			step_ = "&step=" + step;
		}

		String needLogin_ = "";
		if (needLogin != null) {
			needLogin_ = "&needLogin=" + needLogin;
		}
		if (domain !=null && domain!="") {
			domain = "&domain=" + domain;
		} else {
			domain ="";
		}
		if (getBasePriority != null && getBasePriority != "") {
			getBasePriority = "&getBasePriority=" + getBasePriority;
		} else {
			getBasePriority = "";
		}
		if (cron != null && cron!= "") {
			try {
				cron = URLEncoder.encode(cron,"utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			cron = "&cron=" + cron;
		} else {
			cron = "";
		}
		if (username != null && username!= "") {
			username = "&username=" + username;
		} else {
			username ="";
		}
		String url = "http://localhost/task?"+classname_+username+init_map+step_+domain+needLogin_+getBasePriority+cron;

		Class.forName(clazz.getName());

		postURL(url);
	}

	/**
	 * 取消周期任务URL
	 * @param id
	 * @return
	 */
	public static void postUnschdeuleTask(String id) {

		postURL("http://localhost/task/unschedule/" + id);

	}

	/**
	 * post提交
	 * @param url
	 */
	private static void postURL(String url) {

		ChromeTask task = null;
		try {
			task = new ChromeTask(url);
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}

		task.setPost();

		BasicRequester.getInstance().submit(task);
	}
}
