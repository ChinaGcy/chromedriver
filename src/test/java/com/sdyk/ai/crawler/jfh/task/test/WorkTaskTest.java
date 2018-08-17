package com.sdyk.ai.crawler.jfh.task.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.PostAction;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class WorkTaskTest {

	@Test
	public void testPostRequest() throws Exception {

		ChromeDriverDistributor.instance = new Distributor();

		ChromeDriverAgent agent = new ChromeDriverAgent();
		//agent.start();
		ChromeDriverDistributor.getInstance().addAgent(agent);

		//

		String url = "https://www.jfh.com/jfhrm/buinfo/showbucaseinfo";
		Map<String, String> data = ImmutableMap.of("uuidSecret", "=NDkwMTs2Ng==");



		ChromeTask task = new ChromeTask(url);
		task.addAction(new PostAction(url, data));
		agent.submit(task);
	}

	@Test
	public void test() throws Exception{


		ChromeDriverDistributor.instance = new Distributor();

		ChromeDriverAgent agent = new ChromeDriverAgent();

		ChromeDriverDistributor.getInstance().addAgent(agent);

		try {

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("uuidSecret", "NDkwMTs2Ng==");
			init_map.put("uId", "111");

			Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName(
					"com.sdyk.ai.crawler.specific.jfh.task.modelTask.WorkTask");

			//生成holder
			//ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			//提交任务
			//((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		} catch ( Exception e) {

			e.printStackTrace();
		}

		Thread.sleep(1000000);
	}

}
