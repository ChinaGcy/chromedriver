package com.sdyk.ai.crawler.scheduler.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.Scheduler;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.model.TaskInitializer;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.route.ChromeTaskRoute;
import one.rewind.json.JSON;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpTaskPosterTest {

	@Test
	public void test() throws Exception {

		Scheduler.Flags = new ArrayList<>();
		Scheduler.getInstance();

		//Thread.sleep(40000);

		//Gson gson = new Gson();

		TaskInitializer.getAll().stream().filter(t -> {
			return t.enable == true;
		}).forEach( t ->{


			/*Map<String, Object> map = new HashMap<>();
			map = gson.fromJson(t.init_map_json, map.getClass());*/

			/*ObjectMapper mapper = new ObjectMapper();
			TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};
			try {
				Map<String, Object> init_map = (Map)mapper.readValue(t.init_map_json, typeRef);
				System.err.println(init_map);
			} catch (IOException e) {
				e.printStackTrace();
			}*/

			try {
				t.scheduled_task_id = HttpTaskPoster.getInstance().submit(t.class_name,null, t.init_map_json, 0, t.cron);

				t.start_time = new Date();

				t.update();

			} catch (Exception e) {
				e.printStackTrace();
			}

		});


		/*TaskInitializer.getAll().stream().forEach( t ->{

			Map<String, String> map = new HashMap<String, String>();
			map = gson.fromJson(t.init_map_json, map.getClass());

			try {
				HttpTaskPoster.getInstance().submit(t.class_name, null, map, 0, t.cron );
			} catch (Exception e) {
				e.printStackTrace();
			}

		} );*/

		Thread.sleep(1000000000);

	}

	@Test
	public void testAccount() throws Exception {
		Account account = AccountManager.getInstance().getAccountById("2");
		System.err.println(JSON.toPrettyJson(account));


		Proxy proxy = ProxyManager.getInstance().getProxyById("51");
		System.err.println(JSON.toPrettyJson(proxy));
	}

}
