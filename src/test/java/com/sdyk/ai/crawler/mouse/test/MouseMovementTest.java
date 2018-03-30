package com.sdyk.ai.crawler.mouse.test;

import com.sdyk.ai.crawler.zbj.mouse.Action;
import com.sdyk.ai.crawler.zbj.mouse.MouseEventModeler;
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
	public void transformData() {

		File folder = new File(MouseEventTracker.serPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile()) {

				System.err.println("File: " + listOfFiles[i].getPath());

				String src = FileUtil.readFileByLines(listOfFiles[i].getPath());

				src = src.replaceAll("(?s)\\{.+?actions\": ", "");

				src = src.replaceAll("(?s)\\r?\\n\\}$","");

				src = src.replaceAll("(?<=\\n)  ", "");

				FileUtil.writeBytesToFile(src.getBytes(), listOfFiles[i].getPath());

			} else if (listOfFiles[i].isDirectory()) {
				System.err.println("Directory: " + listOfFiles[i].getName());
			}
		}
	}


	@Test
	public void generateData() {

		List<List<Action>> actions = MouseEventModeler.loadData();

		String all = "{";

		for(List<Action> as : actions) {

			String output = "{";

			int y0 = 0;
			long t0 = 0;

			for(Action a : as) {

				if(y0 == 0) y0=a.y;
				if(t0 == 0) t0=a.time;
				output += "{" + (a.time-t0) + ", " + (a.y-y0) +"}, ";
			}

			output = output.substring(0,output.length()-2) + "}";

			all += output + ", ";
		}

		all = all.substring(0,all.length()-2) + "}";

		System.err.println(all);
	}

	@Test
	public void SimulateActions() throws AWTException {


		new MouseEventSimulator(
				MouseEventModeler.loadData("mouse_movements/1521357776022_6a62fed3-55ad-44d6-8ce8-205c6514dc9b.txt")
		).simulate(100);
	}

}
