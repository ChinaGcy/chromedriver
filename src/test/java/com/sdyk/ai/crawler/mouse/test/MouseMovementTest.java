package com.sdyk.ai.crawler.mouse.test;

import com.sdyk.ai.crawler.zbj.mouse.MouseEventSimulator;
import com.sdyk.ai.crawler.zbj.mouse.MouseEventTracker;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.json.JSON;
import org.tfelab.util.FileUtil;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MouseMovementTest {

	@Test
	public void move() throws AWTException {
		Robot robot = new Robot();
		robot.mouseMove(920,550);
	}

	@Test
	public void mouseClick() throws AWTException, InterruptedException {

		new ChromeDriverAgent().getDriver().get("https://jqueryui.com/slider/");

		Robot robot = new Robot();
		int index = 440;
		robot.delay(1000);
		robot.mouseMove(index,480);

		robot.delay(100);
		robot.mousePress(InputEvent.BUTTON1_MASK); //按下左键

		for (int i = 0; i < 100; i++) {
			robot.mouseMove(++index, 480);
			Thread.sleep(100);
		}
		robot.delay(100);
		robot.mouseMove(600, 480);
		robot.delay(100);
		robot.mouseRelease(InputEvent.BUTTON1_MASK); //释放左键
	}

	@Test
	public void modifyRecord() throws InterruptedException {

		/*MouseEventTracker m = JSON.fromJson(
				FileUtil.readFileByLines("mouse_movements/1520069499746.txt"),
				MouseEventTracker.class
		);*/

		List<MouseEventTracker> trackerList = new ArrayList<>();

		File folder = new File("mouse_movements");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile()) {

				System.out.println("File " + listOfFiles[i].getPath());

				MouseEventTracker m = JSON.fromJson(
						FileUtil.readFileByLines(listOfFiles[i].getPath()),
						MouseEventTracker.class
				);

				trackerList.add(m);

			} else if (listOfFiles[i].isDirectory()) {
				// System.out.println("Directory " + listOfFiles[i].getName());
			}
		}

		MouseEventTracker.removePreMoveActions(trackerList);
	}

	@Test
	public void SimulateActions() throws AWTException {

		MouseEventTracker m = JSON.fromJson(
			FileUtil.readFileByLines("mouse_movements/1521357776022_6a62fed3-55ad-44d6-8ce8-205c6514dc9b.txt"),
			MouseEventTracker.class
		);

		new MouseEventSimulator(m).simulate(100);
	}

}
