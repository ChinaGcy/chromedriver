package com.sdyk.ai.crawler.specific.mihuashi.action;

import one.rewind.io.requester.chrome.action.ChromeAction;
import org.openqa.selenium.WebElement;

import java.net.SocketException;

/**
 *
 */
public class TendererProjectActive extends ChromeAction {

    public String url;

    public TendererProjectActive(String url){
        this.url = url;
    }

    public void getMore(){

        try {

            //获取页面
            this.agent.getUrl(this.url);
            this.agent.waitPageLoad(this.url);
            WebElement clc;

            do {

                try{
                    Thread.sleep(1000);
                    clc = this.agent.getElementWait("#artworks > div > section > div:nth-child(2) > a");
                    clc.click();
                }
                catch (Exception e){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }

            } while (clc!=null);

        } catch (SocketException e) {

            e.printStackTrace();
        }
    }

    public void run() {
        this.getMore();
    }
}
