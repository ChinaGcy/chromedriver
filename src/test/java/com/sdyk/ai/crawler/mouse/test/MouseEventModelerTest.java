package com.sdyk.ai.crawler.mouse.test;

import com.sdyk.ai.crawler.zbj.OpenCVUtil;
import com.sdyk.ai.crawler.zbj.mouse.Action;
import com.sdyk.ai.crawler.zbj.mouse.MouseEventModeler;
import com.sdyk.ai.crawler.zbj.mouse.MouseEventSimulator;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.util.FileUtil;

import java.awt.*;
import java.io.IOException;
import java.util.List;

import static com.sdyk.ai.crawler.zbj.mouse.MouseEventModeler.*;

public class MouseEventModelerTest {

	@Test
	public void singleBuildTest() throws Exception {

		List<Action> actions = loadData(
				"mouse_movements/1521775936826_85c3fd26-2211-4015-8367-2393c68e1ef2.txt");

		MouseEventModeler.Model model = new MouseEventModeler.Model(actions);

		String output = toMathematicaListStr(model.buildActions());

		FileUtil.writeBytesToFile(output.getBytes(), "original_actions.txt");

		model.morph(10);

		output = toMathematicaListStr(model.buildActions());

		FileUtil.writeBytesToFile(output.getBytes(), "new_actions.txt");
	}

	@Test
	public void modelerTest() throws Exception {

		MouseEventModeler modeler = new MouseEventModeler();

		for(int i=40; i<=200; i++) {

			try {
				MouseEventModeler.logger.info(i);
				String output = toMathematicaListStr(modeler.getActions(i));

				FileUtil.writeBytesToFile(output.getBytes(), "new_actions/" + i + ".txt");
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Test
	public void oneModelGenerationTestd() {

		try {

			int px = 135;
			List<Action> actions = MouseEventModeler.getInstance().getActions(px);

			MouseEventSimulator simulator = new MouseEventSimulator(actions);

			simulator.procActions();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void zbjloginTest() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.getDriver().get("https://login.zbj.com/login");
		Thread.sleep(2000);

		if (agent.getDriver().getPageSource().contains("geetest_radar_tip_content")) {

			// D1
			// 点击识别框
			agent.getElementWait(".geetest_radar_tip_content").click();

			Thread.sleep(3000);

			// 生成位移
			int offset = getPicOffset(agent);

			// 鼠标移动
			validatePass(agent, offset, 0);

			// 等待识别
			try {
				WebDriverWait wait = new WebDriverWait(agent.getDriver(), 5);
				wait.until(ExpectedConditions.or(
						ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
				));
			} catch (org.openqa.selenium.TimeoutException e) {

				login(agent);
			}

			agent.getElementWait("#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button").click();

			agent.close();
		}
	}

	/**
	 *
	 * @param agent
	 * @throws Exception
	 */
	public void login(ChromeDriverAgent agent) throws Exception {

		agent.getElementWait("body > div.geetest_fullpage_click.geetest_float.geetest_wind.geetest_slide3 > div.geetest_fullpage_click_wrap > div.geetest_fullpage_click_box > div > div.geetest_panel > div > a.geetest_refresh_1").click();

		try {
			agent.getElementWait("#password-captcha-box > div.geetest_holder.geetest_wind.geetest_radar_error > div.geetest_btn > div.geetest_radar_btn").click();

		}catch (org.openqa.selenium.TimeoutException e) {

			Thread.sleep(5000);

			int offset = getPicOffset(agent);

			validatePass(agent, offset, 0);

			// 等待识别
			try {
				WebDriverWait wait = new WebDriverWait(agent.getDriver(), 5);
				wait.until(ExpectedConditions.or(
						ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
				));
			} catch (org.openqa.selenium.TimeoutException e1) {

				login(agent);
			}
		}

	}

	/**
	 * 获取位移
	 * @param agent
	 * @return
	 * @throws Exception
	 */
	public int getPicOffset(ChromeDriverAgent agent) throws Exception {
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
	 * 鼠标点击移动
	 * @param agent
	 * @param offset 像素差
	 * @param error 误差
	 * @throws Exception
	 */
	public void validatePass( ChromeDriverAgent agent, int offset, int error) throws Exception {

		int x = agent.getElementWait(".geetest_slider_button").getLocation().x + 30;
		int y = agent.getElementWait(".geetest_slider_button").getLocation().y + 135 ;

		Robot bot = new Robot();
		bot.mouseMove(x, y);

		List<Action> actions = MouseEventModeler.getInstance().getActions(offset - error);

		MouseEventSimulator simulator = new MouseEventSimulator(actions);

		simulator.procActions();
	}
}
