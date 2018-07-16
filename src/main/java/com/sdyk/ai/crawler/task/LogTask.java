package com.sdyk.ai.crawler.task;

import com.sdyk.ai.crawler.LoginInAction;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.rewind.io.requester.account.Account;
import one.rewind.util.Configs;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class LogTask extends Task {


	public LogTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	/**
	 * 通过 domain 生成登陆操作
	 * @param domain
	 * @param account
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static LoginInAction getLoginActionByDomain(String domain, Account account) throws MalformedURLException, URISyntaxException {

		Config base = ConfigFactory.load();
		InputStream stream = Configs.class.getClassLoader().getResourceAsStream("conf/LogPath.conf");
		Config config = ConfigFactory.parseReader(new InputStreamReader(stream)).withFallback(base);

		String url = config.getConfig(domain).getString("url");
		String usernameParh = config.getConfig(domain).getString("usernamePath");
		String passwordPath = config.getConfig(domain).getString("passwordPath");
		String loginButtenPath = config.getConfig(domain).getString("loginButtenPath");
		String typePath = config.getConfig(domain).getString("typePath");
		if( typePath == null || typePath.length() < 2){
			typePath = null;
		}

		return new LoginInAction(url, usernameParh, passwordPath, loginButtenPath, typePath, account);
	}

}
