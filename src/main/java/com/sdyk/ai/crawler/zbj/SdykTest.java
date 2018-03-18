package com.sdyk.ai.crawler.zbj;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.tfelab.io.requester.Task;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

public class SdykTest {

	public static void main(String[] args) throws InterruptedException {

		ChromeDriverAgent agent_1 = new ChromeDriverAgent();

		agent_1.getDriver().get("http://www.315free.com");

		Thread.sleep(2000);

		agent_1.getElementWait("#index > section.body > div > button:nth-child(2)").click();

		Thread.sleep(2000);

		agent_1.getDriver().executeScript("window.open('');");

		System.err.println(agent_1.getDriver().getWindowHandles());

//		agent_1.getDriver().switchTo().window();

		/*Point point = new Point(0, 0);
		Dimension dimension = new Dimension(0,0);

		agent_1.getDriver().manage().window().setPosition(point);
		agent_1.getDriver().manage().window().setSize(dimension);

		Thread.sleep(2000);

		ChromeDriverAgent agent_2 = new ChromeDriverAgent();
		agent_2.getDriver().get("http://www.315free.com");

		Thread.sleep(2000);

		agent_1.getDriver().switchTo().alert().accept();*/

		//agent_1.getDriver().manage().window().fullscreen();

	}

}
