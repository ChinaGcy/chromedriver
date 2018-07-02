/*
package com.sdyk.ai.crawler.specific.mihuashi.action;

import one.rewind.io.requester.chrome.action.ChromeAction;
import org.openqa.selenium.WebElement;

import java.net.SocketException;

public class TendererRatingActive extends ChromeAction {

    public String url;

    public String morePath = "#vue-comments-app > div:nth-child(2) > a";

    public String ratingPath = "#users-show > div.container-fluid > div.profile__container > main > header > ul > li:nth-child(2) > a";

    public TendererRatingActive(String url){
        this.url = url;
    }

    public boolean getmore(){
        Boolean clickRating = true;
        try {
            //获取页面
            this.agent.getUrl(this.url);
            this.agent.waitPageLoad(this.url);
            //跳至评价页
            WebElement rating = this.agent.getElementWait(ratingPath);
            rating.click();
            WebElement clc;
            do{
                try{
                    Thread.sleep(1000);
                    clc = this.agent.getElementWait(morePath);
                    clc.click();
                }catch (Exception e){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    break;
                }
            }while (clc!=null);
        } catch (SocketException e) {
            logger.error("error on clickRating",e);
            return  false;
        }
        return clickRating;
    }

    public void run() {
        this.getmore();
    }
}
*/
