package com.sdyk.ai.crawler.util.test;

import com.sdyk.ai.crawler.task.LoginTask;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.util.Configs;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

public class LoginConfTest {

	@Test
	public void test() throws ClassNotFoundException {

		File file = new File("login_tasks");
		File[] tempList = file.listFiles();

		for( File f : tempList ){

			String mappingFilePath = "login_tasks/" + f.getName();

			one.rewind.util.FileUtil.readFileByLines(mappingFilePath);

			System.out.println(one.rewind.util.FileUtil.readFileByLines(mappingFilePath));

			System.out.println("/////////////////");

		}

	}

	@Test
	public void testFileToTAsk() throws URISyntaxException, MalformedURLException, ClassNotFoundException, ProxyException.Failed {

		Map<String, LoginTask> loginMap = new HashedMap();

		File file = new File("login_tasks");
		File[] tempList = file.listFiles();

		for( File f : tempList ){

			loginMap.put(
					f.getName().replace(".json",""),
					LoginTask.buildFromJson(one.rewind.util.FileUtil.readFileByLines("login_tasks/" + f.getName())) );

		}

		for( String s : loginMap.keySet() ){

			Account account = new AccountImpl("baidu","111","222");

			LoginTask loginTask = loginMap.get(s);
			((LoginAction) loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

			System.out.println(loginTask.toJSON());

		}

	}

}
