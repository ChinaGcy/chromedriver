package com.sdyk.ai.crawler.specific.proginn.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.proginn.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.AccountException;
import org.jsoup.nodes.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WorkTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				WorkTask.class,
				"https://www.proginn.com{{work_id}}",
				ImmutableMap.of("work_id", String.class, "like_num", String.class),
				ImmutableMap.of("work_id", "", "like_num", "1")
		);
	}

	public WorkTask(String url) throws Exception {

		super(url);

		this.setPriority(Priority.MEDIUM);

		this.setNoFetchImages();

		// 检测异常
		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("手机登陆") && src.contains("忘记密码") ){

				throw new AccountException.Failed(a.accounts.get(t.getDomain()));
			}
		});

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String uId = doc.select("div.nickname > a").attr("href");

			//String like_num  = t.getStringFromInitMap("like_num");

			//crawler(doc, uId, like_num);

		});

	}

	public void crawler( Document doc, String uId, String like_num ){

		Work work = new Work(getUrl());

		work.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(uId));

		work.like_num = Integer.valueOf(like_num);

		//外部链接
		work.external_url = doc.select("a.link").attr("href");

		//大类
		work.category = doc.select("p.sub-title").text().replace("-", ",");

		//标签
		work.tags = Arrays.asList(doc.select("p.functions").text(), ",");

		//标题
		work.title = doc.select("p.title").text().replace("查看更多作品 >","");

		//内容
		Set<String> urls = new HashSet<>();
		String content = StringUtil.cleanContent(doc.select("div.desc").text(), urls);
		work.content = BinaryDownloader.download(content, urls, getUrl());


		try {
			work.insert();
		} catch (Exception e) {
			logger.error("error for work.insert", e);
		}
	}

}
