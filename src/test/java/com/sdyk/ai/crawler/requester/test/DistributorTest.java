package com.sdyk.ai.crawler.requester.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask;
import one.rewind.db.RedissonAdapter;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;
import org.redisson.api.RMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DistributorTest {

	@Test
	public void testDistributor(){

		try {

			ChromeDriverAgent agent = new ChromeDriverAgent();

			//agent.start();

			ChromeDriverDistributor.getInstance().addAgent(agent);

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("project_id", "2d07c7455c407582");

			Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask");

			//生成holder
			/*ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			//提交任务
			Distributor.getInstance().submit(holder);*/

		} catch ( Exception e) {

			e.printStackTrace();
		}

	}

	@Test
	public void testGet() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

		/*Class clazz = Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask");

		long min_interval = Long.valueOf(clazz.getField("MIN_INTERVAL").getLong(clazz));

		System.out.println(min_interval);*/

		Distributor.URL_VISITS.clear();

		RMap<String, Long> URL_VISITS = RedissonAdapter.redisson.getMap("URL-Visits");

		if(URL_VISITS.get("hash") == null){
			System.out.println("111");
		}



		/*Field f = clazz.getField("MIN_INTERVAL");

		long o = f.getLong(clazz);

		System.out.println(o);*/

	}

}
