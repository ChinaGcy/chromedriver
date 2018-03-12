package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.requester.ChromeDriverLoginWrapper;
import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import db.Refacter;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Queue;

public class ProjectTaskTest {


	// 页面格式2
	@Test
	public void projectTest() {

		ChromeDriverAgent agent = null;
		try {
			agent = new ChromeDriverLoginWrapper("zbj.com").login();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Task task = null;
		try {
			// 页面格式2 ： http://task.zbj.com/12909258/

			task = new ProjectTask("http://task.zbj.com/12913633/");
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}

		Queue<Task> taskQueue = new LinkedList<>();
		taskQueue.add(task);
		while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			if(t != null) {
				try {
					agent.fetch(t);
					for (Task t_ : t.postProc(agent.getDriver())) {
						taskQueue.add(t_);
						//agent.fetch(t_);
					}

				} catch (Exception e) {

					taskQueue.add(t);
				}
			}
		}
	}
	// 页面格式1
	@Test
	public void pageOneTest() {

		ChromeDriverAgent agent = null;
		try {
			agent = new ChromeDriverLoginWrapper("zbj.com").login();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Task task = null;

		try {
			task = new ProjectTask("http://task.zbj.com/12897928/");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		agent.fetch(task);
		try {
			task.postProc(agent.getDriver());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void dummyTest() throws Exception {

		Refacter.dropTable(Project.class);
		Refacter.createTable(Project.class);

		Project p = new Project("http://www.baidu.com/test");
		p.insert();
	}
}
