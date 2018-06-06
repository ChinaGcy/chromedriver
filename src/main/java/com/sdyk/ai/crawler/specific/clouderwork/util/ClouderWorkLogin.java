package com.sdyk.ai.crawler.specific.clouderwork.util;

import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static one.rewind.simulator.mouse.MouseEventModeler.logger;

public class ClouderWorkLogin {

    public static void login(ChromeDriverAgent agent) throws InterruptedException {

        Task task = null;
        try {
            task = new Task("https://passport.clouderwork.com/signin");
        } catch (MalformedURLException e) {
            logger.info("error on creat task",e);
        } catch (URISyntaxException e) {
            logger.info("error on creat task",e);
        }
        task.setBuildDom();
        try {
            agent.submit(task);
        } catch (ChromeDriverException.IllegalStatusException e) {
            logger.info("error on agent submit task",e);
        }
        WebDriver webDriver = agent.getDriver();
        WebElement usernameInput = webDriver.findElement(By.cssSelector("#app > div > div > div > section > dl > dd:nth-child(1) > input[category=\"text\"]"));
        usernameInput.sendKeys("17600485107");
        WebElement passwordInput = webDriver.findElement(By.cssSelector("#app > div > div > div > section > dl > dd:nth-child(2) > input[category=\"password\"]"));
        passwordInput.sendKeys("123456");
        WebElement click = webDriver.findElement(By.cssSelector("#app > div > div > div > section > button:nth-child(3)"));
        click.click();
        Thread.sleep(3000);

    }

}
