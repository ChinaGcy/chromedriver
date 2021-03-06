package com.sdyk.ai.crawler.companyTaskTest;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.chrome.action.PostAction;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.proxy.ProxyImpl;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static one.rewind.util.FileUtil.readFileByLines;

public class CompanyTaskTest {

	@Test
	public void test() throws Exception{

		AccountManager.getInstance().setAllAccountFree();

		ChromeDriverDistributor.instance = new Distributor();

		ChromeDriverAgent agent = new ChromeDriverAgent();

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/tianyancha.com.json"));
		((LoginAction)loginTask.getActions().get(loginTask.getActions().size() - 1)).setAccount(
				AccountManager.getInstance().getAccountByDomain("tianyancha.com")
		);

		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

		//设置参数
		Map<String, Object> init_map = new HashMap<>();
		init_map.put("company_name", "北京华维顿科技有限公司");

		Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.CompanyInformationTask");

		//生成holder
		TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

		//提交任务
		((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		Thread.sleep(1000000);

	}

	@Test
	public void lagouPostTesr() throws  Exception{

		String url = "https://www.lagou.com/jobs/positionAjax.json?needAddtionalResult=false";
		Map<String, String> data = new HashMap<>();
		data.put("first", "false");
		data.put("pn", "2");
		data.put("kd", "北京三点一刻科技有限公司");

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ChromeTask task = new ChromeTask(url);
		task.addAction(new PostAction(url, data));
		agent.submit(task);

		Thread.sleep(1000000);
	}


	@Test
	public void testProxyLoginTianyancha() throws Exception {

		AccountManager.getInstance().setAllAccountFree();

		ChromeDriverDistributor.instance = new Distributor();

		Proxy proxy = new ProxyImpl("118.190.44.184", 59998, "tfelab", "TfeLAB2@15");

		//ProxyImpl proxy1 = new ProxyImpl( "sdyk.red", 60202, "tfelab", "TfeLAB2@15");

		//ProxyImpl proxy = ProxyManager.getInstance().getValidProxy("aliyun-cn-shenzhen-squid");

		//System.out.println(proxy.host);

		ChromeDriverAgent agent = new ChromeDriverAgent(proxy, ChromeDriverAgent.Flag.MITM);

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/tianyancha.com.json"));
		((LoginAction)loginTask.getActions().get(loginTask.getActions().size() - 1)).setAccount(
				AccountManager.getInstance().getAccountByDomain("tianyancha.com")
		);

		((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);
		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		//((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		Thread.sleep(10000000);

	}


}
