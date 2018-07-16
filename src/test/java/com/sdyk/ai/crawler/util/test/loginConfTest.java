package com.sdyk.ai.crawler.util.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.util.Configs;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class loginConfTest {

	@Test
	public void test() throws ClassNotFoundException {

		Config base = ConfigFactory.load();
		InputStream stream = Configs.class.getClassLoader().getResourceAsStream("conf/LogPath.conf");
		Config config = ConfigFactory.parseReader(new InputStreamReader(stream)).withFallback(base);

		System.out.println(config.getConfig("clouderwork.com").getString("url"));

		/*Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.MihuashiLoginTask");*/


	}

}
