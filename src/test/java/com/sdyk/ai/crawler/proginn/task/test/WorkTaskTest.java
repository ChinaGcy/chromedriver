package com.sdyk.ai.crawler.proginn.task.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.ServiceWrapper;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class WorkTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverDistributor.instance = new Distributor();

		ChromeDriverAgent agent = new ChromeDriverAgent();

		ChromeDriverDistributor.getInstance().addAgent(agent);

		try {

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("work_id", "/w/10158");
			init_map.put("like_num", "2");


			Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proginn.task.modelTask.WorkTask");

			//生成holder
			ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			//提交任务
			((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		} catch ( Exception e) {

			e.printStackTrace();
		}

		Thread.sleep(1000000);
	}
}
