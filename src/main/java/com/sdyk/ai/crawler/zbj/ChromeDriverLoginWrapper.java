package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.model.Account;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.tracking.MouseEventTracker;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.util.FileUtil;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;


public class ChromeDriverLoginWrapper extends Thread {

	public ChromeDriverAgent agent = new ChromeDriverAgent();

	public static StatManager statManager = StatManager.getInstance();

	private static final Logger logger = LogManager.getLogger(ChromeDriverLoginWrapper.class.getName());

	public static PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();

	public volatile boolean done = false;

	public static Set<String> set = new HashSet<>();

	public String domain = "zbj.com";

	public Account account;

	public ChromeDriverLoginWrapper(String domain) {

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
		return login(true);
	}

	public ChromeDriverAgent login(boolean automaticByPassGeeTest) throws Exception {

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
		Thread.sleep(1000);


		// D. 操作验证码
		if (agent.getDriver().getPageSource().contains("geetest_radar_tip_content")) {

			// D1
			// 点击识别框
			agent.getElementWait(".geetest_radar_tip_content").click();
			Thread.sleep(2000);

			// 截图1
			FileUtil.writeBytesToFile(
					agent.shoot(".geetest_window"),
					"geetest/geetest1.png"
			);

			// 点击滑块
			new Actions(agent.getDriver()).dragAndDropBy(agent.getElementWait(".geetest_slider_button"), 5, 0).build().perform();

			Thread.sleep(2000);
			// 截图2
			FileUtil.writeBytesToFile(
					agent.shoot(".geetest_window"),
					"geetest/geetest2.png"
			);

			// 生成位移
			int offset = OpenCVUtil.getOffset("geetest/geetest1.png","geetest/geetest2.png");

			Robot bot = new Robot();
			/*GeeTestUtil.mouseGlide(bot, 0, 0, 926, 552, 10, 10);*/

			MouseEventTracker tracker = null;

			if(automaticByPassGeeTest) {

				// 移动滑块
				// TODO 寻找更好的模拟方法
				GeeTestUtil.mouseGlide(bot, 0, 0, 926, 552, 1000, 1000);
				bot.mousePress(InputEvent.BUTTON1_MASK);
				GeeTestUtil.mouseGlide(bot, 926, 552, 926 + offset, 552, 1000, 1000);
				bot.mouseRelease(InputEvent.BUTTON1_MASK);

				// 不识别
				/*new Actions(agent.getDriver())
						.dragAndDropBy(agent.getElementWait(".geetest_slider_button"), offset, 0)
						.build().perform();*/

				// 鼠标点击事件
				/*int xOff = 920;
				robot.mouseMove(xOff, 550);

				Thread.sleep(1000);

				robot.mousePress(InputEvent.BUTTON1_MASK);

				Thread.sleep(1000);
				for(int index = 1; index <= offset; index++) {
					robot.mouseMove(++xOff, 550);
					Thread.sleep(10);
				}
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);*/

			} else {

				tracker = new MouseEventTracker();

			}

			// 等待识别
			WebDriverWait wait = new WebDriverWait(agent.getDriver(), 60);
			wait.until(ExpectedConditions.or(
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
			));

			if(tracker != null) {
				tracker.serializeMovements();
			}

		}

		agent.getElementWait("#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button").click();
		Thread.sleep(1000);

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
				statManager.count();
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

		}
	}

	/**
	 * 测试
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		ChromeDriverLoginWrapper driverWrapper = new ChromeDriverLoginWrapper("zbj.com");

		/*driverWrapper.agent.addProxyRequestFilter(new RequestFilter() {
			@Override
			public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {


				if (messageInfo.getOriginalUrl().endsWith("/some-endpoint-to-intercept")) {

					// retrieve the existing message contents as a String or, for binary contents, as a byte[]
					String messageContents = contents.getTextContents();

					// do some manipulation of the contents
					String newContents = messageContents.replaceAll("original-string", "my-modified-string");

					// replace the existing content by calling setTextContents() or setBinaryContents()
					contents.setTextContents(newContents);
				}

				// in the request filter, you can return an HttpResponse object to "short-circuit" the request
				return null;
			}
		});

		driverWrapper.agent.addProxyResponseFilter(new ResponseFilter() {
			@Override
			public void filterResponse(HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
				if (true) {
					contents.setTextContents("This message body will appear in all responses!");
				}
			}
		});*/

		ChromeDriverAgent agent = driverWrapper.login(false);

	}
}
