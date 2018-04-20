package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.zbj.Crawler;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import one.rewind.db.Refacter;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.sdyk.ai.crawler.zbj.model.DockerHost;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;

import java.net.URL;

public class DockerTest {

	@Test
	public void dockerChromedriver() throws Exception {

		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		/*ChromeOptions options = new ChromeOptions();
		options.addArguments("--start-maximized");*/

		ChromeDriver agent = new ChromeDriver();

		WebDriver driver = new RemoteWebDriver(new URL("http://10.0.0.62:4444/wd/hub"),capabilities);

		driver.manage().window().maximize();
		driver.get("http://zbj.com");

		String s = driver.findElement(By.cssSelector("#headerTopWarpV1 > div > ul > li:nth-child(3)")).getText();

		System.err.println(s);

		Thread.sleep(3000);
		driver.close();
	}

	@Test
	public void createDockerDB() throws Exception {
		Refacter.createTable(DockerHost.class);
	}


	@Test
	public void dockerRun() throws Exception {

		DockerHostManager dockerHostManager = DockerHostManager.getInstance();
		// 连接docker 创建容器
		// dockerHostManager.createDockerContainers("10.0.0.62", 5);

		// 删除所有容器
		dockerHostManager.delAllDockerContainers(dockerHostManager.getHostByIp("10.0.0.62"));
	}


	@Test
	public void crawlerTest() throws Exception {

		// 删除阿里云服务器
	/*	List<AliyunHost> all = AliyunHost.getAll();
		Thread.sleep(10000);

		for (AliyunHost a : all) {
			a.stop();
		}

		Thread.sleep(50000);
		for (AliyunHost a : all) {
			a.delete();
		}
*/
		// 执行登录操作
		Crawler crawler = new Crawler();

		Thread.sleep(300000);

	}

}
