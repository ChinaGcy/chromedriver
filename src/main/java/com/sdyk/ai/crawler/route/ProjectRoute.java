package com.sdyk.ai.crawler.route;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.witkey.Project;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.route.ChromeTaskRoute;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.io.server.Msg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sdyk.ai.crawler.model.Model.rewriteBinaryUrl;

/**
 * 项目路由
 */
public class ProjectRoute extends ChromeTaskRoute{

	private static final Logger logger = LogManager.getLogger(ProjectRoute.class.getName());

	/**
	 * 根据id查询
	 */
	public static Route getProjectById = (Request request, Response response ) -> {

		String id = request.params(":id");

		Project p = (Project) Model.daoMap.get(Project.class.getSimpleName()).queryForId(id);

		p.content = rewriteBinaryUrl(p.content);

		response.header("Access-Control-Allow-Origin", "*");

		return new Msg<Project>(Msg.SUCCESS, p);
	};

	/**
	 * 项目列表
	 */
	public static Route getProjects = (Request request, Response response) -> {

		int page = Integer.parseInt(request.params(":page"));
		if(page < 1) page = 1;

		long length = 20;
		// TODO length 不起作用
		if(request.queryParams("length") != null) {
			length = Long.valueOf(request.queryParams("length"));
		}

		long offset = (page - 1) * length;

		Dao<Project, String> dao = Model.daoMap.get(Project.class.getSimpleName());

		QueryBuilder<Project, String> qb = dao.queryBuilder()
				.limit(length).offset(offset)
				.orderBy("update_time", false);

		ServiceWrapper.logger.info(qb.prepareStatementString());

		List<Project> ps = qb.query();

		for(Project p : ps) {
			p.content = rewriteBinaryUrl(p.content);
		}

		response.header("Access-Control-Allow-Origin", "*");
		return new Msg<List<Project>>(Msg.SUCCESS, ps);
	};

	// 执行任务，返回任务分派信息
	public static Route submit = (Request request, Response response) -> {

		try {

			// 任务类名
			String class_name = request.queryParams("class_name");
			Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName(class_name);

			// 初始参数
			String init_map_str = request.queryParams("vars");

			ObjectMapper mapper = new ObjectMapper();
			TypeReference<HashMap<String, Object>> typeRef
					= new TypeReference<HashMap<String, Object>>() {};

			Map<String, Object> init_map = mapper.readValue(init_map_str, typeRef);

			// 用户名
			String username = request.queryParams("username");

			if(filter != null)
				filter.run(class_name, username);

			// 步骤数
			int step = 0;
			if(request.queryParams("step") != null) {
				step = Integer.valueOf(request.queryParams("step"));
			}

			// Create Holder
			TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map, username, step);

			String[] cron = request.queryParamsValues("cron");

			Map<String, Object> info = null;

			// A 周期性任务
			// 加载到Scheduler
			if(cron != null) {
				if (cron.length ==1) {
					ScheduledChromeTask st = new ScheduledChromeTask(holder, cron[0]);
					info = ChromeTaskScheduler.getInstance().schedule(st);
				}
				else if (cron.length > 1) {
					ScheduledChromeTask st =  new ScheduledChromeTask(holder, Arrays.asList(cron));
					info = ChromeTaskScheduler.getInstance().schedule(st);
				}
			}
			// B 单步任务 Submit Holder
			else {
				holder.step = 1;
				info = ChromeDriverDistributor.getInstance().submit(holder);

			}

			// Return holder
			return new Msg<Map<String, Object>>(Msg.SUCCESS, info);

		}
		catch (Exception e) {

			logger.error("Error create/assign task. ", e);
			return new Msg<String>(Msg.FAILURE, e.getMessage());
		}
	};
}
