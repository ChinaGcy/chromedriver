package com.sdyk.ai.crawler.specific.company.linkedin;/*
package com.sdyk.ai.crawler.specific.company.linkedinTask;

import com.sdyk.ai.crawler.model.LinkedIn;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class LinkedInTask extends Task {

	LinkedIn linkedIn;

	public LinkedInTask(String url, String companyName) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback(() -> {

			Document doc = getResponse().getDoc();


			int randomNumber = (int) Math.round(Math.random()*10 + 5);

			try {
				Thread.sleep(randomNumber * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			crawler(doc, companyName);
		});
	}

	public void crawler(Document doc, String companyName){

		linkedIn = new LinkedIn(getUrl());

		linkedIn.company_nmae = companyName;

		linkedIn.position_name = doc.select("h1.jobs-details-top-card__job-title").text();
		linkedIn.location = doc.select("#ember979 > div > div.jobs-details-top-card__content-container.mt6.pb5 > h3 > span.jobs-details-top-card__bullet").text();

		linkedIn.req = doc.select("p.js-formatted-exp-body").text();

		Elements elements = doc.select("ul.js-formatted-industries-list > li");
		StringBuffer industry = new StringBuffer();
		for(Element element : elements){
			industry.append(element.text().replace("\"",""));
			industry.append(",");
		}
		linkedIn.industry = industry.substring(0, industry.length()-1);

		linkedIn.type = doc.select("p.js-formatted-employment-status-body").text();

		Elements categoryList = doc.select("ul.js-formatted-job-functions-list > li");
		StringBuffer category = new StringBuffer();
		for( Element element : categoryList ){
			category.append(element.text().replace("\"",""));
			category.append(",");
		}
		linkedIn.category = category.substring(0, category.length()-1);

		linkedIn.content = doc.select("#job-details").text();

		try{
			linkedIn.insert();
		} catch (Exception e) {
			logger.error("error for linkedin.insert", e);
		}

	}

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}
*/
