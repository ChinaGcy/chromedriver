package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.TendererRating;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererRatingTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000;

	static {
		registerBuilder(
				TendererRatingTask.class,
				"https://www.mihuashi.com/users/{{tenderer_id}}?role=employer&rating=true",
				ImmutableMap.of("tenderer_id", String.class),
				ImmutableMap.of("tenderer_id", "")
		);
	}


	public String moreRatingPath = "#vue-comments-app > div:nth-child(2) > a";

	public String ratingPath = "#users-show > div.container-fluid > div.profile__container > main > header > ul > li:nth-child(2) > a";


	public TendererRatingTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

    	super(url);

        this.setPriority(Priority.MEDIUM);

		this.setNoFetchImages();

		this.addAction(new ClickAction(ratingPath));
		this.addAction(new LoadMoreContentAction(moreRatingPath));

        this.addDoneCallback((t)->{

        	Document doc = getResponse().getDoc();

        	//执行抓取任务
            crawlawJob(doc);
        });
    }

	public void crawlawJob(Document doc){

		//雇主url
		Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
		Matcher matcher = pattern.matcher(getUrl());
		String web=null;
		while(matcher.find()) {
			try {
				web = URLDecoder.decode("https://www.mihuashi.com/users/"+matcher.group("username"), "UTF-8")+"?role=employer";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		//抓取评价
		Elements elements = doc.getElementsByClass("profile__comment-cell");
		int i =1;
		for(Element element : elements){
			TendererRating tendererRating = new TendererRating(getUrl()+"&num="+i);
			i++;

			//雇主id
			tendererRating.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(web));

			//服务商名称
			tendererRating.service_provider_name = element.getElementsByClass("name").text();

			String allStr = element.toString();
			Pattern pattern1 = Pattern.compile("(?<=/projects/)\\d+");
			Matcher matcher1 = pattern1.matcher(allStr);
			while (matcher1.find()) {
				try {
					String projectUrl = "https://www.mihuashi.com/projects/" + matcher1.group();

					//项目ID
					tendererRating.project_id = one.rewind.txt.StringUtil.byteArrayToHex(
							one.rewind.txt.StringUtil.uuid(projectUrl));

				} catch (Exception e) {
					logger.error(e);
				}
			}

			//项目名称
			Elements elementsName = element.getElementsByClass("name");
			tendererRating.project_name = elementsName.get(0).text();

			//服务商ID
			tendererRating.service_provider_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(
					elementsName.get(1).attr("href")
			));

			//服务商名字
			tendererRating.service_provider_name = elementsName.get(1).text();

			//评价
			tendererRating.content = element.getElementsByClass("content").text();

			//评价时间
			String time = element.getElementsByClass("commented-time").text();
			try {
				tendererRating.pubdate = DateFormatUtil.parseTime(time);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			//合作愉快度
			Elements happy = element.getElementsByClass("fa fa-star selected");
			int happyNum = happy.size();
			tendererRating.rating = (happyNum/3);

			tendererRating.insert();
		}
	}
}
