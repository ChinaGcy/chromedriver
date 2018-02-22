package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.model.Account;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.util.FileUtil;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ChromeDriverWithLogin extends Thread {

	public ChromeDriverAgent agent = new ChromeDriverAgent();

	private static final Logger logger = LogManager.getLogger(ChromeDriverWithLogin.class.getName());

	public static BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

	public volatile boolean done = false;

	public static Set<String> set = new HashSet<>();

	public String domain = "zbj.com";

	public Account account;

	public ChromeDriverWithLogin(String domain) {

		this.domain = domain;

		try {

			account = Account.getAccountByDomain(domain);
			account.status = Account.Status.Occupied;
			// TODO update account

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public ChromeDriverAgent login() throws Exception {

		// A.打开网页
		agent.getDriver().get("http://"+account.getDomain());

		// B. 点击登录链接
		WebElement w = agent.getElementWait("#headerTopWarpV1 > div > div > ul > li.item.J_user-login-status > div > span.text-highlight > a:nth-child(1)");
		w.click();

		// C.输入账号密码
		WebElement usernameInput = agent.getElementWait("#username");
		usernameInput.sendKeys(account.getUsername());

		WebElement passwordInput = agent.getElementWait("#password");
		passwordInput.sendKeys(account.getPassword());

		Thread.sleep(2000);

		// D.等待10S 操作验证码
		if (agent.getDriver().getPageSource().contains("geetest_radar_tip_content")) {

			// D1
			agent.getElementWait(".geetest_radar_tip_content").click();

			new Actions(agent.getDriver()).dragAndDropBy(agent.getElementWait(".geetest_slider_button"), 5, 0).build().perform();

			Thread.sleep(1000);

			FileUtil.writeBytesToFile(
					agent.shoot(".geetest_window"),
					"geetest.png"
			);

			WebDriverWait wait = new WebDriverWait(agent.getDriver(), 60);
			wait.until(ExpectedConditions.or(
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
			));
		}

		agent.getElementWait("#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button").click();

		return agent;
	}

	/**
	 *
	 */
	public void run() {

		while (!done) {

			Task t = null;
			try {
				t = taskQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (!set.contains(t.getUrl())) {
				set.add(t.getUrl());
				try {
					agent.fetch(t);
					for (Task t_ : t.postProc(agent.getDriver())) {
						ChromeRequester.getInstance().distribute(t_);
					}
				} catch (Exception e) {
					logger.error("Exception while fetch task. ", e);
					taskQueue.add(t);
				}

			}
			/*while (!queue.isEmpty()) {
				Task t = queue.poll();
				agent.fetch(t);
				for (Task tt : t.postProc(agent.getDriver())) {
					queue.add(tt);
				}

			}*/

		}
	}

	/**
	 * 测试
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		ChromeDriverAgent agent = (new ChromeDriverWithLogin("zbj.com")).login();

	}
}
