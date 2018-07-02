package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.ServiceProviderRating;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import com.sdyk.ai.crawler.task.Task;
import com.sdyk.ai.crawler.util.URLUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceRatingTask extends Task {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("service_id", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("q", "ip");
		// url_template
		url_template = "https://www.mihuashi.com/users/{{service_id}}?role=painter&rating=true";
	}

	public String workFilePath = "#users-show > div.container-fluid > div.profile__container > main > header > ul > li:nth-child(2) > a";

	public String morePath = "#vue-comments-app > div:nth-child(2) > a";

    public ServiceRatingTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        this.setPriority(Priority.MEDIUM);

	    this.addAction(new ClickAction( workFilePath ));
	    this.addAction(new LoadMoreContentAction(morePath));

        this.addDoneCallback((t)->{

            Document doc = getResponse().getDoc();

            //执行抓取任务
            crawlawJob(doc);
        });
    }

	public void crawlawJob(Document doc){

		List<Task> tasks = new ArrayList();
		Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=painter");
		Matcher matcher = pattern.matcher(getUrl());
		String web=null;
		while(matcher.find()) {
			try {
				web = URLDecoder.decode(matcher.group("username"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		Elements elements = doc.getElementsByClass("profile__comment-cell");
		Pattern pattern1 = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
		int i = 0;

		for(Element element : elements){
			ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl()+"&num="+i);
			i++;

			//服务商ID
			serviceProviderRating.service_provider_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(getUrl()));

			//甲方名字
			Elements name = element.getElementsByClass("name");
			serviceProviderRating.tenderer_name = name.get(1).text();
			try {
				String url = URLDecoder.decode(name.get(1).attr("href"), "UTF-8");

				//甲方ID
				serviceProviderRating.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid(url));

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			//项目名称
			serviceProviderRating.project_name = name.get(0).text();

			//描述
			serviceProviderRating.content = element.getElementsByClass("content").text();

			//发布时间
			String time = element.getElementsByClass("commented-time").text();
			try {
				serviceProviderRating.pubdate = DateFormatUtil.parseTime(time);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			//项目URL
			String proUrl = element.toString();
			Pattern pattern2 = Pattern.compile("/projects/\\d+");
			Matcher matcher2 = pattern2.matcher(proUrl);
			while (matcher2.find()) {

				String project_id = matcher2.group().replace("/projects/","");
				try {
					URLUtil.PostTask(ProjectTask.class,
							null,
							ImmutableMap.of("project_id", project_id),
							null,
							null,
							null,
							null,
							null);

				} catch (Exception e) {
					logger.error("error for URLUtil.PostTask ProjectTask.class", e);
				}
			}

			serviceProviderRating.insert();

		}

	}

}