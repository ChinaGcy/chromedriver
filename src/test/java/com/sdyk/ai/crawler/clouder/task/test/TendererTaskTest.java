package com.sdyk.ai.crawler.clouder.task.test;


import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.TendererTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import sun.management.resources.agent;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TendererTaskTest {

    @Test
    public void testTendererTask() throws Exception {

    	Distributor.URL_VISITS.clear();

    	ChromeDriverDistributor.instance = new Distributor();

	    ChromeDriverAgent agent = new ChromeDriverAgent();

	    ((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

	    try {

		    //设置参数
		    Map<String, Object> init_map = new HashMap<>();
		    init_map.put("tenderer_id", "/clients/7fefc37381706a22");

		    Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.TendererTask");

		    //生成holder
		    TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

		    //提交任务
		    ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

	    } catch ( Exception e) {

	    }

	    Thread.sleep(10000000);

    }
}
