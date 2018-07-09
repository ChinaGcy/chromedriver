package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class ServiceSupplierTask extends com.sdyk.ai.crawler.task.Task {

	ServiceProvider serviceProvider;

	public ServiceSupplierTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);
		this.setPriority(Priority.HIGH);
		this.setBuildDom();
		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();
			String src = getResponse().getText();

			if( src.contains("迷路啦") || src.contains("非常抱歉") ) {
				return;
			}
			//页面正常
			else{
				try {
					crawlerJob(doc);
				} catch (ChromeDriverException.IllegalStatusException e) {
					logger.info("error on crawlerJob",e);
				}
			}

		});
	}

	public void crawlerJob(Document doc) throws ChromeDriverException.IllegalStatusException {

		serviceProvider = new ServiceProvider(getUrl());
		List<Task> tasks = new ArrayList<Task>();
		String[] url = getUrl().split("users");

		try {
			serviceProvider.origin_id = URLDecoder.decode(url[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("error for 字符集转换", e);
		}

		//名字
		String name = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5").text();
		String renzhang = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5 > span").text();
		serviceProvider.name = name.replace(renzhang,"");

		//介绍
		serviceProvider.content = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__summary-wrapper").html();

		//评论数
		String ratNum = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > section.credit > p")
				.text().replace("共","").replace("条评价","");
		ratNum = CrawlerAction.getNumbers(ratNum);
		if(ratNum!=null&&!"".equals(ratNum)){
			serviceProvider.rating_num = Integer.valueOf(ratNum);
		}

		//领域
		serviceProvider.tags = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__skill-wrapper").text();

		//项目数
		String projectNum = doc.select("#users-show > div.container-fluid > div.profile__container > main > header > ul > li.active > a > span").text();
		if( projectNum != null && !"".equals(projectNum) ){
			serviceProvider.project_num = Integer.valueOf(projectNum);
		}

		//服务质量
		String serviceQuality = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(1) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceQuality!=null && !"".equals(serviceQuality) ){
			serviceProvider.service_quality = Double.valueOf(serviceQuality) / 20;
		}

		//服务速度
		String serviceSpeed = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(2) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceSpeed!=null && !"".equals(serviceSpeed) ){
			serviceProvider.service_speed = Double.valueOf(serviceSpeed) /20;
		}

		//服务态度
		String serviceAttitude = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(3) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceAttitude!=null && !"".equals(serviceAttitude)){
			serviceProvider.service_attitude = Double.valueOf(serviceAttitude) / 20;
		}

		serviceProvider.insert();
	}

}
