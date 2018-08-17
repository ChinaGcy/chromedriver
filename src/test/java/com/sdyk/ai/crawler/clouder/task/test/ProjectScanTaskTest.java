package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.clouderwork.util.ClouderWorkLogin;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.Task;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProjectScanTaskTest {

    @Test
    public void testProjectScanTask() throws Exception {

	    ChromeDriverDistributor.instance = new Distributor();

	    Distributor.URL_VISITS.clear();

	    //ProxyImpl proxy = (ProxyImpl) ProxyManager.getInstance().getProxyById("8");

	    ChromeDriverAgent agent = new ChromeDriverAgent();

	    ((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

	    //设置参数
	    Map<String, Object> init_map = new HashMap<>();
	    init_map.put("page", "1");
	    init_map.put("max_page", "");

	    Class clazz = Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask");

	    //生成holder
	    //ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map, null, 1, Task.Priority.HIGH);

	    //提交任务
	   // ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		Thread.sleep(1000000);

    }
}
