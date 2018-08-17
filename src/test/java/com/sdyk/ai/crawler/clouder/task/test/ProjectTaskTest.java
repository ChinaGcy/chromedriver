package com.sdyk.ai.crawler.clouder.task.test;


import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTaskTest {

    @Test
    public void testProjectTask() throws Exception {

	    ChromeDriverAgent agent = new ChromeDriverAgent();
	    agent.start();

	    // 不同类型页面有差异
	    // 驻场    https://www.clouderwork.com/jobs/0231ba693af8ea5b
	    // 非驻场  https://www.clouderwork.com/jobs/5322791db8d612b6

	    ProjectTask projectTask = new ProjectTask("https://www.clouderwork.com/jobs/06c1e2b17b221a85");

	    agent.submit(projectTask);

		Thread.sleep(1000000);
    }

    @Test
    public void schedullerTaskTest() throws Exception{


	    ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

	    ChromeDriverAgent agent = new ChromeDriverAgent();
	    distributor.addAgent(agent);

	    Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask");

	    //ChromeTaskHolder holder = ChromeTask.buildHolder(
			    //clazz, ImmutableMap.of("project_id", "da151689bd68bdb4"));

	   // distributor.submit(holder);

	    Thread.sleep(600000);
    }

    @Test
    public void getTags(){

    	String src = "{[getLevel(2,'IOS')]}";

	    Pattern pattern_1 = Pattern.compile("getLevel\\((?<t>.+?),'");
	    Matcher matcher_1 = pattern_1.matcher(src);
	    String lv = "1";

	    if( matcher_1.find() ){

	    	switch (matcher_1.group("t")){
			    case "1" : lv = "初级"; break;
			    case "2" : lv = "中极"; break;
			    case "3" : lv = "高级";
		    }
	    }

	    System.out.println(lv);

    }

}
