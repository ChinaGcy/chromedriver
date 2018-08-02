package com.sdyk.ai.crawler.binary.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ServiceScanTask;
import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static one.rewind.util.FileUtil.readFileByLines;

public class TestBinary {

	/**
	 * 测试附件下载
	 * @throws Exception
	 */
	@Test
	public void testAccessory() throws Exception {

		ChromeDriverDistributor.instance = new Distributor();

		ProxyManager.getInstance().proxyDomainBannedMap.clear();
		Distributor.URL_VISITS.clear();

		/**
		 * 第一次下载附件 -> 附件全部下载
		 *
		 * 手动删除一条记录并修改对应任务的attachment_ids，模拟附件增加场景，查看被删除的附件是否重新下载，attachment_ids是否增加 ->
		 * -> 只生成被删除附件的下载任务，更新attachment_ids
		 */

		AccountManager.getInstance().setAllAccountFree();

		// 设置agent
		ChromeDriverAgent agent = new ChromeDriverAgent(ProxyManager.getInstance().getProxyById("53"));

		agent.start();

		// 生成登陆任务
		LoginTask t = LoginTask.buildFromJson(readFileByLines("login_tasks/clouderwork.com.json"));

		// 添加账号
		Account a = AccountManager.getInstance().getAccountByDomain("clouderwork.com");
		((LoginAction)t.getActions().get(t.getActions().size() - 1)).setAccount(a);

		// 提交登陆方法
		agent.submit(t);

		// 生成附件任务
		ServiceProviderTask serviceProviderTask = new ServiceProviderTask("https://www.clouderwork.com/freelancers/4bdf608d6ede95ea");
		serviceProviderTask.setNoFetchImages();

		/*ServiceScanTask serviceProviderTask = new ServiceScanTask("https://www.clouderwork.com/api/v2/freelancers/search?pagesize=10&pagenum=1");*/
		// 提交附件任务
		agent.submit(serviceProviderTask);

		//ProjectTask projectTask = new ProjectTask("https://www.clouderwork.com/jobs/ddbe955eba6c6a9f");

		//agent.submit(projectTask);

		Thread.sleep(10000000);

	}

	/**
	 * 测试描述图片下载
	 * @throws Exception
	 */
	@Test
	public void testContent() throws Exception {

		/**
		 * 第一次下载 -> 图片全部下载
		 *
		 * 手动删除一张图片，并修改contain字段 ->
		 */

		AccountManager.getInstance().setAllAccountFree();

		// 设置agent
		ChromeDriverAgent agent = new ChromeDriverAgent(ProxyManager.getInstance().getProxyById("53"));

		agent.start();

		// 生成登陆任务
		LoginTask t = LoginTask.buildFromJson(readFileByLines("login_tasks/shichangbu.com.json"));

		// 添加账号
		Account a = AccountManager.getInstance().getAccountByDomain("shichangbu.com");
		((LoginAction)t.getActions().get(t.getActions().size() - 1)).setAccount(a);

		// 提交登陆方法
		agent.submit(t);

		// 生成描述任务
		CaseTask caseTask = new CaseTask("http://www.shichangbu.com/portal.php?mod=product&op=view&id=2244");
		caseTask.setNoFetchImages();

		// 提交描述任务
		agent.submit(caseTask);

		Thread.sleep(10000000);
	}

	@Test
	public void testArrayLength() throws UnsupportedEncodingException {

		String src = "asdfgh";
		System.out.println(src.getBytes("UTF-8").length);

	}

}
