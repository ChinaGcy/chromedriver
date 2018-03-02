import com.sdyk.ai.crawler.zbj.ChromeDriverWithLogin;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * 鼠标移动案例
 */
public class MouseMoveTest {
	Robot robot;

	@Test
	public void rb() throws AWTException {
		robot = new Robot();
		robot.mouseMove(920,550);
	}

	@Test
	public void mouseClick() throws AWTException, InterruptedException {

		new ChromeDriverAgent().getDriver().get("https://jqueryui.com/slider/");

		robot = new Robot();
		int index = 440;
		robot.delay(1000);
		robot.mouseMove(index,480);

		robot.delay(100);
		robot.mousePress(InputEvent.BUTTON1_MASK);//按下左键

		for (int i = 0; i < 100; i++) {
			robot.mouseMove(++index, 480);
			Thread.sleep(100);
		}
		robot.delay(100);
		robot.mouseMove(600, 480);
		robot.delay(100);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);//释放左键
	}



	/*public void mouseGlide(int x1, int y1, int x2, int y2, int t, int n) {
		try {
			Robot r = new Robot();
			double dx = (x2 - x1) / ((double) n);
			double dy = (y2 - y1) / ((double) n);
			double dt = t / ((double) n);
			for (int step = 1; step <= n; step++) {
				Thread.sleep((int) dt);
				r.mouseMove((int) (x1 + dx * step), (int) (y1 + dy * step));
			}
		} catch (AWTException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/

}
