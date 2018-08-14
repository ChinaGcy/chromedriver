package com.sdyk.ai.crawler.util.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ProxyException;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

import static one.rewind.util.FileUtil.readFileByLines;

public class LoginTaskTest {

	@Test
	public void test() throws Exception {

		Map<String, LoginTask> loginTasks = new HashedMap();

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		File file = new File("login_tasks");
		File[] tempList = file.listFiles();

		for( File f : tempList ){

			loginTasks.put(
					f.getName().replace(".json",""),
					LoginTask.buildFromJson(readFileByLines("login_tasks/" + f.getName())) );

		}

		System.out.println(Domain.getAll().size());

		Domain.getAll().stream()
				.map(d -> d.domain)
				.forEach(d -> {

					Account account = null;

					try {

						account = AccountManager.getInstance().getAccountByDomain(d);

						if(account != null) {

							System.out.println(d);

							// 根据 domain 获取 LoginTask
							LoginTask loginTask = loginTasks.get(d);

							// 设定账户
							((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);


							agent.submit(loginTask);

						} else {
							return;
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				});

		Thread.sleep(100000000);


	}



}
