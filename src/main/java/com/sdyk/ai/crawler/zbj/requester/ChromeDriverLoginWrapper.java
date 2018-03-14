package com.sdyk.ai.crawler.zbj.requester;

import com.sdyk.ai.crawler.zbj.model.Account;
import com.sdyk.ai.crawler.zbj.model.Proxy;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.concurrent.PriorityBlockingQueue;

import static com.sdyk.ai.crawler.zbj.requester.ChromeRequester.urls;


public class ChromeDriverLoginWrapper extends Thread {

	public ChromeDriverAgent agent = new ChromeDriverAgent();

	private static final Logger logger = LogManager.getLogger(ChromeDriverLoginWrapper.class.getName());

	public static PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();

	public volatile boolean done = false;

	public Proxy proxy;

	/**
	 *
	 * @param domain
	 */
	public ChromeDriverLoginWrapper(String domain) {}

	/**
	 *
	 * @param pw
	 * @return
	 * @throws Exception
	 */
	public ChromeDriverAgent login(Account account, Proxy pw) throws Exception {

		return login(account, pw,true);
	}

	/**
	 *
	 * @param pw
	 * @param automaticByPassGeeTest
	 * @return
	 * @throws Exception
	 */
	public ChromeDriverAgent login(Account account, Proxy pw, boolean automaticByPassGeeTest) throws Exception {

		// A.打开网页
		org.tfelab.io.requester.Task t = new org.tfelab.io.requester.Task("http://"+account.getDomain());
		t.setProxyWrapper(pw);
		agent.fetch(t);

		// B.点击登录链接
		WebElement w = agent.getElementWait("#headerTopWarpV1 > div > div > ul > li.item.J_user-login-status > div > span.text-highlight > a:nth-child(1)");
		w.click();

		// C.输入账号密码
		WebElement usernameInput = agent.getElementWait("#username");
		usernameInput.sendKeys(account.getUsername());

		WebElement passwordInput = agent.getElementWait("#password");
		passwordInput.sendKeys(account.getPassword());
		Thread.sleep(1000);


		// D.操作验证码
		if (agent.getDriver().getPageSource().contains("geetest_radar_tip_content")) {

			// D1
			// 点击识别框
			agent.getElementWait(".geetest_radar_tip_content").click();

			/*Thread.sleep(3000);

			// 截图1
			FileUtil.writeBytesToFile(
					agent.shoot(".geetest_window"),
					"geetest/geetest1.png"
			);

			// 点击滑块
			new Actions(agent.getDriver()).dragAndDropBy(agent.getElementWait(".geetest_slider_button"), 5, 0).build().perform();

			Thread.sleep(5000);
			// 截图2
			FileUtil.writeBytesToFile(
					agent.shoot(".geetest_window"),
					"geetest/geetest2.png"
			);

			// 生成位移
			int offset = OpenCVUtil.getOffset("geetest/geetest1.png","geetest/geetest2.png");

			Robot bot = new Robot();
			*//*GeeTestUtil.mouseGlide(bot, 0, 0, 926, 552, 10, 10);*//*

			MouseEventTracker tracker = null;

			if(automaticByPassGeeTest) {

				// 移动滑块
				// TODO 寻找更好的模拟方法
				GeeTestUtil.mouseGlide(bot, 0, 0, 926, 552, 1000, 1000);
				bot.mousePress(InputEvent.BUTTON1_MASK);
				GeeTestUtil.mouseGlide(bot, 926, 552, 926 + offset, 552, 1000, 1000);
				bot.mouseRelease(InputEvent.BUTTON1_MASK);

				// 不识别
				*//*new Actions(agent.getDriver())
						.dragAndDropBy(agent.getElementWait(".geetest_slider_button"), offset, 0)
						.build().perform();*//*

				// 鼠标点击事件
				*//*int xOff = 920;
				robot.mouseMove(xOff, 550);

				Thread.sleep(1000);

				robot.mousePress(InputEvent.BUTTON1_MASK);

				Thread.sleep(1000);
				for(int index = 1; index <= offset; index++) {
					robot.mouseMove(++xOff, 550);
					Thread.sleep(10);
				}
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);*//*

			} else {

				tracker = new MouseEventTracker();

			}*/

			// 等待识别
			WebDriverWait wait = new WebDriverWait(agent.getDriver(), 60);
			wait.until(ExpectedConditions.or(
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
			));

			/*if(tracker != null) {
				tracker.serializeMovements();
			}*/

		}

		agent.getElementWait("#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button").click();
		Thread.sleep(1000);

		return agent;
	}

	/**
	 *运行线程
	 */
	public void run() {

		while (!done) {

			Task t = null;
			try {
				t = taskQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 判断是否任务重复执行
			if (!urls.contains(t.getUrl())) {

				try {
					// 执行任务
					agent.fetch(t);
					// 每分钟执行的Task数
					StatManager.getInstance().count();

					for (Task t_ : t.postProc(agent.getDriver())) {
						// 添加任务
						ChromeRequester.getInstance().distribute(t_);
					}

					urls.add(t.getUrl());

				} catch (Exception e) {
					logger.error("Exception while fetch task. ", e);
					taskQueue.add(t);
				}

			}

		}
	}
}
