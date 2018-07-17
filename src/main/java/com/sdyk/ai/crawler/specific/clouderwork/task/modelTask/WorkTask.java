package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import one.rewind.io.requester.task.ChromeTask;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Pattern;

public class WorkTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				WorkTask.class,
				"https://www.clouderwork.com{{work_id}}",
				ImmutableMap.of("work_id", String.class),
				ImmutableMap.of("work_id", "")
		);
	}

	public static String domain() {
		return "clouderwork";
	}


	public WorkTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.MEDIUM);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawlerJob(doc);

		});

	}

	public void crawlerJob(Document doc){

		String useUrl = "https://www.clouderwork.com" + doc.select("div.p-item.project > a").attr("href");

		Pattern pattern = Pattern.compile("[0-9]*");
		String url = getUrl();
		Work workinfor = new Work(url);

		//乙方ID
		workinfor.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(useUrl));

		//项目名称
		String title = doc.getElementsByClass("p-title").text();
		if(title!=null&&!"".equals(title)){
			workinfor.title = title;
		}

		String all = doc.getElementsByClass("info").text().replace("类型：","");
		if(all!=null&&!"".equals(all)){

			//抓取类型
			String[] all1 = all.split("行业：");
			if(all1[0]!=null&&!"".equals(all1[0])){
				workinfor.category = all1[0];
			}

			//抓取行业
			String[] all2 = all1[1].split("工期：");
			if(all2[0]!=null&&!"".equals(all2[0])){
				workinfor.tags = all2[0];
			}

			String[] all3 = all2[1].split("报价：");

			//抓取报价
			if( all3[1]!=null && !"".equals(all3[1] )) {
				String budge = all3[1].replace(",","").replace("¥","");
				if(budge!=null&!"".equals(budge)&&pattern.matcher(budge).matches()){
					try {
						workinfor.price = Double.valueOf(budge);
					} catch (Exception e) {
						logger.error("error on String to Integer", e);
					}
				}
			}
		}

		//抓取描述
		String descreption = doc.getElementsByClass("desc simditor-content").html();
		if(descreption!=null&&!"".equals(descreption)){
			workinfor.content = descreption;
		}

		try {
			workinfor.insert();
		} catch (Exception e) {
			logger.error("error on insert work", e);
		}
	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}

}
