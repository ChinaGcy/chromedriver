package com.sdyk.ai.crawler.specific.mihuashi.action;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.io.requester.chrome.action.ChromeAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.net.SocketException;

/**
 * 模拟点击评论评论，并加载更多
 *
 * @author zhangseng@315free.com
 * @data 2018/5/30
 */
public class LoadMoreContentAction extends Action {

   	String morePath ;

	ChromeDriverAgent agent;

	public LoadMoreContentAction(String morePath) {
		this.morePath = morePath;
	}

    public void run() {

	    //循环验证点击加载更多，加载全部页面
	    WebElement clc;

	    do {
		    try {
			    Thread.sleep(1000);
			    clc = this.agent.getDriver().findElement(By.cssSelector(morePath));
			    if( clc == null){
				    break;
			    }
			    clc.click();
		    }
		    catch (Exception e){
			    //logger.error("Error find morePath", e);
			    break;
		    }
	    } while ( clc != null );

    }
}
