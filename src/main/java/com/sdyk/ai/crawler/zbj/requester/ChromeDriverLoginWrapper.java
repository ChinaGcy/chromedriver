package com.sdyk.ai.crawler.zbj.requester;

import one.rewind.opencv.OpenCVUtil;
import com.sdyk.ai.crawler.zbj.model.Account;
import com.sdyk.ai.crawler.zbj.model.Proxy;
import one.rewind.simulator.mouse.Action;
import one.rewind.simulator.mouse.MouseEventModeler;
import one.rewind.simulator.mouse.MouseEventSimulator;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.proxy.IpDetector;
import one.rewind.util.FileUtil;
import one.rewind.util.NetworkUtil;

import java.awt.*;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import static com.sdyk.ai.crawler.zbj.requester.ChromeRequester.urls;


public class ChromeDriverLoginWrapper extends Thread {

	public ChromeDriverAgent agent = new ChromeDriverAgent();

	public static String LOCAL_IP = IpDetector.getIp() + " :: " + NetworkUtil.getLocalIp();

	private static final Logger logger = LogManager.getLogger(ChromeDriverLoginWrapper.class.getName());

	public PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<>();

	public long startTime = System.currentTimeMillis();

	public volatile boolean done = false;

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
		one.rewind.io.requester.Task t = new one.rewind.io.requester.Task("https://login.zbj.com/login");
		t.setProxyWrapper(pw);
		agent.fetch(t);
		Thread.sleep(5000);

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

			Thread.sleep(3000);

			// D2 生成位移
			int offset = getPicOffset(agent);

			mouseManipulate(agent, offset, 0);

			// D3 等待识别
			try {
				WebDriverWait wait = new WebDriverWait(agent.getDriver(), 5);
				wait.until(ExpectedConditions.or(
						ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
				));
			} catch (org.openqa.selenium.TimeoutException e) {
				// D4 刷新继续验证
				verificationPass(agent);
			}
		}

		agent.getElementWait("#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button").click();
		Thread.sleep(3000);
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

			try {
				t.setAgent(agent);
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

	/**
	 * 滑块验证
	 * @param agent
	 * @throws Exception
	 */
	public static void verificationPass(ChromeDriverAgent agent) throws Exception {

		agent.getElementWait("body > div.geetest_fullpage_click.geetest_float.geetest_wind.geetest_slide3 > div.geetest_fullpage_click_wrap > div.geetest_fullpage_click_box > div > div.geetest_panel > div > a.geetest_refresh_1").click();

		try {
			agent.getElementWait("#password-captcha-box > div.geetest_holder.geetest_wind.geetest_radar_error > div.geetest_btn > div.geetest_radar_btn").click();

		}catch (org.openqa.selenium.TimeoutException e) {

			Thread.sleep(5000);

			int offset = getPicOffset(agent);

			mouseManipulate(agent, offset, 0);

			// 等待识别
			try {
				WebDriverWait wait = new WebDriverWait(agent.getDriver(), 5);
				wait.until(ExpectedConditions.or(
						ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
				));
			} catch (org.openqa.selenium.TimeoutException e1) {

				verificationPass(agent);
			}
		}
	}

	/**
	 * 获取位移
	 * @param agent
	 * @return
	 * @throws Exception
	 */
	public static int getPicOffset(ChromeDriverAgent agent) throws Exception {
		// 截图1
		FileUtil.writeBytesToFile(
				agent.shoot(".geetest_window"),
				"geetest/geetest1.png"
		);

		// 点击滑块
		new Actions(agent.getDriver()).dragAndDropBy(agent.getElementWait("body > div.geetest_fullpage_click.geetest_float.geetest_wind.geetest_slide3 > div.geetest_fullpage_click_wrap > div.geetest_fullpage_click_box > div > div.geetest_wrap > div.geetest_slider.geetest_ready > div.geetest_slider_button"), 5, 0).build().perform();

		Thread.sleep(5000);
		// 截图2
		FileUtil.writeBytesToFile(
				agent.shoot(".geetest_window"),
				"geetest/geetest2.png"
		);
		// 生成位移
		int offset = OpenCVUtil.getOffset("geetest/geetest1.png", "geetest/geetest2.png");
		return offset;
	}

	/**
	 * 鼠标点击移动操作
	 * @param agent
	 * @param offset 像素差
	 * @param error 误差
	 * @throws Exception
	 */
	public static void mouseManipulate( ChromeDriverAgent agent, int offset, int error) throws Exception {

		int x = agent.getElementWait(".geetest_slider_button").getLocation().x + 30;
		int y = agent.getElementWait(".geetest_slider_button").getLocation().y + 135 ;

		Robot bot = new Robot();
		bot.mouseMove(x, y);

		List<Action> actions = MouseEventModeler.getInstance().getActions(offset - error);

		MouseEventSimulator simulator = new MouseEventSimulator(actions);

		simulator.procActions();
	}

}
