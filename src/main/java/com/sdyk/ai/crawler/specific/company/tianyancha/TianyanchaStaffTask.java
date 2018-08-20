package com.sdyk.ai.crawler.specific.company.tianyancha;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.company.CompanyStaff;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.TaskHolder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TianyanchaStaffTask extends Task {

	public static long MIN_INTERVAL = 1000L;

	static {
		registerBuilder(
				TianyanchaStaffTask.class,
				"https://www.tianyancha.com/pagination/teamMember.xhtml?ps=5&name={{company_name}}&pn={{page}}&uId={{uId}}",
				ImmutableMap.of("company_name", String.class, "page",String.class, "uId",String.class),
				ImmutableMap.of("company_name", "","page","","uId",""),
				true,
				Priority.HIGHEST
		);
	}

	public TianyanchaStaffTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGHEST);

		//this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc);

		});

	}

	public void crawler(Document doc){

		Elements elements = doc.select("div.card-team");
		int i = 0;

		String companyId = null;

		Pattern pattern_url = Pattern.compile("uId=(?<uId>.+)");
		Matcher matcher_url = pattern_url.matcher(getUrl());
		if (matcher_url.find()) {
			companyId = matcher_url.group("uId");
		}

		String company_name = null;

		Pattern pattern_name = Pattern.compile("name=(?<name>.+?)&pn");
		Matcher matcher_name = pattern_name.matcher(getUrl());
		if (matcher_name.find()) {
			company_name = matcher_name.group("name");
		}

		int page = 0;

		Pattern pattern_page = Pattern.compile("pn=(?<page>.+?)&u");
		Matcher matcher_page = pattern_page.matcher(getUrl());
		if (matcher_page.find()) {
			page = Integer.valueOf( matcher_page.group("page"));
		}


		for(Element element : elements){
			i++;
			CompanyStaff companyStaff = new CompanyStaff(getUrl() + "&i=" + i);
			companyStaff.company_id = companyId;
			companyStaff.position = element.select("div.title").text();
			companyStaff.name = element.select("div.name").text();
			companyStaff.content = element.select("div.right > p").toString();

			try {
				companyStaff.insert();
			} catch (Exception e) {
				logger.error("error for companyStaff.insert", e);
			}
		}

		if( elements.size() >4 ){
			int nextPage = page +1;

			// 提交天眼查高管任务
			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("company_name", company_name);
				init_map.put("uId", companyId);
				init_map.put("page", String.valueOf(nextPage));

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.tianyancha.TianyanchaStaffTask");

				//生成holder
				TaskHolder holder = this.getHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit TianyanchaTask.class", e);
			}

		}

	}

}
