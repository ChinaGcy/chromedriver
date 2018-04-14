package com.sdyk.ai.crawler;

import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import one.rewind.io.SshManager;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
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
}
