package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import one.rewind.db.DaoManager;
import one.rewind.db.RedissonAdapter;
import one.rewind.io.requester.task.ChromeTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RBlockingQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * 获取zbj甲方手机号
 */
public class GetProjectContactTask extends ChromeTask {

	static {
		registerBuilder(
				GetProjectContactTask.class,
				"https://task.zbj.com/{{project_id}}/",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "0"),
				true,
				Priority.MEDIUM
		);
	}

	public static RBlockingQueue SdykCrawlerProjectPhoneUpdates = RedissonAdapter.redisson.getBlockingQueue("Sdyk-Crawler-Project-Contact-Updates");

	public static final Logger logger = LogManager.getLogger(GetProjectContactTask.class.getName());

	public Project project;

	public GetProjectContactTask(String url) throws Exception {

		super(url);
		// 设置优先级
		this.setPriority(Priority.HIGH);

		this.addAction(new GetProjectContactAction());

		// D 监听异步请求
		this.setResponseFilter((res, contents, messageInfo) -> {

			// D1 通过返回数据获取手机号
			if(messageInfo.getOriginalUrl().contains("getANumByTask")) {

				ServiceWrapper.logger.info(messageInfo.getOriginalUrl());

				if(contents != null) {
					String src = new String(contents.getBinaryContents());
					String cellphone = src.replaceAll("^.+?\"data\":\"", "")
							.replaceAll("\"}", "");

					ServiceWrapper.logger.info(cellphone);
					getResponse().setVar("cellphone", cellphone);
				} else {
					logger.info("NOT Find Cellphone");
				}
			}
		});

		this.addDoneCallback((t) -> {

			try {

				String project_id = t.getStringFromVars("project_id");

				project = DaoManager.getDao(Project.class).queryForId(project_id);

				project.cellphone = getResponse().getVar("cellphone");

				ServiceWrapper.logger.info("project {} update {}.",
						project.id, project.update());

				Map map = new HashMap();

				map.put("status", "1");
				map.put("project_id", project_id);
				map.put("phone", project.cellphone);
				map.put("id", this.id);

				SdykCrawlerProjectPhoneUpdates.add(map);

			} catch (Exception e) {

				Map map = new HashMap();
				map.put("status", "0");
				map.put("project_id", t.getStringFromVars("project_id"));
				map.put("id", this.id);

				SdykCrawlerProjectPhoneUpdates.put(map);
				logger.error("Error update project:{} cellphone.", project.id, e);
			}

		});
	}

}
