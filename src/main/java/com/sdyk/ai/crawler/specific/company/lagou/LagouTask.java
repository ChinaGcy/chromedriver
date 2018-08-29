package com.sdyk.ai.crawler.specific.company.lagou;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.company.CompanyRecruitment;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.task.Task;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LagouTask extends Task {

	public static long MIN_INTERVAL = 1000L;

	static {
		registerBuilder(
				LagouTask.class,
				"https://www.lagou.com/jobs/{{jobUrl}}?uId={{uId}}",
				ImmutableMap.of("jobUrl", String.class,"uId",String.class),
				ImmutableMap.of("jobUrl", "4786442.html","uId", "0033f3652846db839a877d26e344a970"),
				true,
				Priority.HIGHEST
		);
	}

	public LagouTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGHEST);

		this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawsler(doc);

		});

	}

	public void crawsler( Document doc ){

		String uId = null;
		Pattern pattern_url = Pattern.compile("uId=(?<uId>.+)");
		Matcher matcher_url = pattern_url.matcher(getUrl());
		if (matcher_url.find()) {
			uId = matcher_url.group("uId");
		}

		CompanyRecruitment companyRecruitment = new CompanyRecruitment(getUrl());

		companyRecruitment.company_id = uId;

		companyRecruitment.position_name = doc.select("span.name").text().split(" ")[0];

		String detailInfo = doc.select("dd.job_request > p").text();
		String[] detailInfos = detailInfo.split("/");
		if( detailInfos.length > 0 ){

			//薪资
			String salary = detailInfos[0];
			if( salary.contains("-") ){
				String salarylb = CrawlerAction.getNumbers(salary.split("-")[0]);
				String salaryub = CrawlerAction.getNumbers(salary.split("-")[1]);
				if( !"".equals(salarylb) ){
					companyRecruitment.payroll_lb = Integer.valueOf(salarylb);
					companyRecruitment.payroll_ub = Integer.valueOf(salaryub);
				}
			}
			else {
				salary = CrawlerAction.getNumbers(salary);
				if( !"".equals(salary) ){
					companyRecruitment.payroll_ub = Integer.valueOf(salary);
					companyRecruitment.payroll_lb = Integer.valueOf(salary);
				}
			}

			//经验
			String exception = detailInfos[2];
			if( exception.contains("-") ){

				String[] exceptions = exception.split("-");
				String exceptionlb = CrawlerAction.getNumbers(exceptions[0]);
				String exceptionub = CrawlerAction.getNumbers(exceptions[1]);
				if( !"".equals(exceptionlb) ){
					companyRecruitment.experience_lb = Integer.valueOf(exceptionlb);
				}
				if( !"".equals(exceptionub) ){
					companyRecruitment.experience_ub = Integer.valueOf(exceptionub);
				}
			}
			else {
				exception = CrawlerAction.getNumbers(exception);
				if( !"".equals(exception) ){
					companyRecruitment.experience_ub = Integer.valueOf(exception);
					companyRecruitment.experience_lb = Integer.valueOf(exception);
				}
			}

			//学历
			companyRecruitment.educational = detailInfos[3];

			//类型
			companyRecruitment.position_type = detailInfos[4].split(" ")[1];

		}

		//内容
		companyRecruitment.content = StringUtil.cleanContent(doc.select("dd.job_bt").html(), new HashSet<>());

		try {
			companyRecruitment.insert();
		} catch ( Exception e ) {
			logger.error("error on companyRecruitment.insert", e);
		}

	}

}
