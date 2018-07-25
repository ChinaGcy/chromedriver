package com.sdyk.ai.crawler.scheduler.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.Scheduler;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SchedulerTest {

	/**
	 * 测试代理不可用时回调方法
	 */
	@Test
	public void testProxyFailCallback() throws Exception {

		Scheduler.Flags = new ArrayList<>();

		Class clazz = Class.forName("com.sdyk.ai.crawler.scheduler.test.ProxyFailedTask");

		start(clazz);

		/**
		 * 方式 -> 更改数据库代理端口
         * 结果 -> 更换代理并停止阿里云主机 -> 若无可用代理，则创建阿里云代理           测试正确
         */
	}

	/**
	 * 测试代理被所有domain封禁的回调方法
	 */
	@Test
	public void testProxyAllDomainBanned() throws Exception {

		Scheduler.Flags = new ArrayList<>();

		Class clazz = Class.forName("com.sdyk.ai.crawler.scheduler.test.ProxyAllDomainBannedTask");

		start(clazz);

		/**
		 * proxy被所有domain封禁
		 * 日志：
		 *lambda$addAgent$1 - 39.108.162.15:59998 banned by all domain.
		 *stopAndDeleteAliyunHost: 39.108.162.15
		 *delete From Mysql.proxys: 39.108.162.15
		 *run - Test Alive --> 119.23.219.80:59998
		 *119.23.219.80:59998 ---> [OK]
		 *Proxy:48 119.23.219.80:59998 good.
		 *call - Change to 119.23.219.80:59998
		 */

	}

	/**
	 * 测试代理被封禁 无关联账号
	 */
	@Test
	public void testProxyDomainBannedWithoutAccount() throws Exception {

		Scheduler.Flags = new ArrayList<>();

		// 无其他可用于该domain的Agent
		//Scheduler.DefaultDriverCount = 1;

		// 有其他可用于该 domain 的Agent
		Scheduler.DefaultDriverCount = 2;

		Class clazz = Class.forName("com.sdyk.ai.crawler.scheduler.test.ProxyDomainBannedWithoutAccountTask");

		start(clazz);

		//todo 任务没有重试

		/**
		 * 当没有可用于该domain的已经初始化好的Agent时会新建Agent
		 * 日志
		 * lambda$addAgent$1 - proxy with account : null
		 * lambda$addAgent$1 - Agent without account and agent_new : null
		 * createChromeDriverDockerContainer - Use 10.0.0.61 port: 1
		 * addAgent container_new : com.sdyk.ai.crawler.docker.model.ChromeDriverDockerContainerImpl@1bfedf19 ,
		 * proxy_new : com.sdyk.ai.crawler.proxy.model.ProxyImpl@27185173
		 */

		/**
		 * 当有可用于该domain的已经初始化好的Agent时,不会会新建Agent
		 * 日志
		 * lambda$addAgent$1 - Proxy 47.106.107.155:59998 failed.
		 * run - Test Alive --> 47.106.107.155:59998
		 * validate - Proxy:50 47.106.107.155:59998 good
		 * lambda$addAgent$1 - proxy with account : null
		 * lambda$addAgent$1 - Agent without account and agent_new : one.rewind.io.requester.chrome.ChromeDriverAgent@45de5fe0
		 */


	}

	/**
	 * 测试代理被封禁有关联账号
	 */
	@Test
	public void testProxyDomainBannedWithAccountTask() throws Exception {

		// 无其他可用于该domain的Agent
		//Scheduler.DefaultDriverCount = 1;

		// 有其他可用于该domain的Agent
		Scheduler.DefaultDriverCount = 2;

		Class clazz = Class.forName("com.sdyk.ai.crawler.scheduler.test.ProxyDomainBannedWithAccountTask");

		start(clazz);

		// todo 当Agent处于阻塞状态时，不执行登陆任务
		/**
		 * 当没有可用于该domain的已经初始化好的Agent时会新建Agent
		 * 日志
		 * lambda$addAgent$1 - Proxy 47.106.107.155:59998 failed.
		 * run - Test Alive --> 47.106.107.155:59998
		 * validate - 47.106.107.155:59998 ---> [OK]
		 * lambda$addAgent$1 - proxy with account : com.sdyk.ai.crawler.account.model.AccountImpl@635f5c2d
		 * createChromeDriverDockerContainer - Use 10.0.0.61 port: 1
		 * lambda$addAgent$1 - Create New Agent with container ： com.sdyk.ai.crawler.docker.model.ChromeDriverDockerContainerImpl@32f0df9f
		 * and proxy : com.sdyk.ai.crawler.proxy.model.ProxyImpl@2e44fe97
		 */

	}

	/**
	 * 测试账号异常，无已初始化好的Agent
	 * @throws Exception
	 */
	@Test
	public void testAccountWithoutOtherAgent() throws Exception {

		Class clazz = Class.forName("com.sdyk.ai.crawler.scheduler.test.AccountWithoutOtherAgentTask");

		start(clazz);

	}




	/**
	 *  启动方法
	 */
	public void start(Class clazz) throws Exception {

		Scheduler scheduler = new Scheduler();

		// 异常任务
		Thread.sleep(30000);

		Map<String, Object> init_map = new HashMap<>();

		init_map.put("jobs", "jobs");

		ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map );

		// 测试代理封禁，有关联账号
		holder.login_task = true;

		((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		Thread.sleep(1000000000);

	}
}
