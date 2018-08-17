package com.sdyk.ai.crawler.proginn.task.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.specific.proginn.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.txt.DateFormatUtil;
import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class ServiceProviderTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverDistributor.instance = new Distributor();

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		/*ChromeDriverDistributor.getInstance().addAgent(agent);

		Map<String, Object> init_map = new HashMap<>();
		init_map.put("servicer_id", "138970");

		Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proginn.task.modelTask.ServiceProviderTask");

		//生成holder
		ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		//提交任务
		((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);*/

		ServiceProviderTask serviceProviderTask = new ServiceProviderTask("https://www.proginn.com/wo/67486");

		agent.submit(serviceProviderTask);

		Thread.sleep(10000000);
	}

	@Test
	public void test1() throws ParseException {

		String s = "2011-9 - 2015-7";
		System.out.println(DateFormatUtil.parseTime(s.split("-")[1].replace(" ", "")));
		System.out.println(DateFormatUtil.parseTime("2011-9-1"));

	}
}
