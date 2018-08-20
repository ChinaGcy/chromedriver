//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package one.rewind.io.requester.task;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.Task.Priority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChromeTaskFactory {
	private static ChromeTaskFactory instance;
	private static final Logger logger = LogManager.getLogger(ChromeTaskFactory.class.getName());
	public Map<Class<? extends ChromeTask>, TaskBuilder> builders = new HashMap();

	public static ChromeTaskFactory getInstance() {
		if (instance == null) {
			Class var0 = ChromeDriverDistributor.class;
			synchronized(ChromeDriverDistributor.class) {
				if (instance == null) {
					instance = new ChromeTaskFactory();
				}
			}
		}

		return instance;
	}

	private ChromeTaskFactory() {
	}

	public Priority getBasePriority(Class<? extends ChromeTask> clazz) {
		TaskBuilder builder = (TaskBuilder)this.builders.get(clazz);
		return builder != null ? builder.base_priority : Priority.MEDIUM;
	}

	public ChromeTask buildTask(TaskHolder holder) throws Exception {
		Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName(holder.class_name);
		TaskBuilder builder = (TaskBuilder)this.builders.get(clazz);
		if (builder == null) {
			throw new Exception("Builder not exist for " + clazz.getName());
		} else {
			Constructor<?> cons = clazz.getConstructor(String.class);
			ChromeTask task = (ChromeTask)cons.newInstance(holder.url);
			if (builder.need_login) {
				task.setLoginTask();
			}

			task.holder = holder;
			task.setUsername(holder.username);
			task.setStep(holder.step);
			task.setPriority(holder.priority);
			task.setId(holder.id);
			return task;
		}
	}

	public TaskHolder newHolder(TaskHolder holder) {
		List<String> trace = holder.trace;
		if (trace == null) {
			trace = new ArrayList();
		}

		((List)trace).add(holder.id);
		return new TaskHolder(holder.class_name, holder.domain, holder.vars, holder.url, holder.need_login, holder.username, holder.step, holder.priority, holder.id, holder.scheduled_task_id, (List)trace);
	}

	public TaskHolder newHolder(TaskHolder holder, Class<? extends ChromeTask> clazz, Map<String, Object> init_map, String username, int step, Priority priority) throws Exception {
		TaskBuilder builder = (TaskBuilder)this.builders.get(clazz);
		if (builder == null) {
			throw new Exception(clazz.getName() + " builder not exist.");
		} else {
			Map<String, Object> vars = builder.validateInitMap(init_map);
			String url = builder.url_template;

			String key;
			for(Iterator var10 = vars.keySet().iterator(); var10.hasNext(); url = url.replace("{{" + key + "}}", String.valueOf(vars.get(key)))) {
				key = (String)var10.next();
			}

			if (priority == null) {
				priority = builder.base_priority;
			}

			List<String> trace = holder.trace;
			if (trace == null) {
				trace = new ArrayList();
			}

			((List)trace).add(holder.id);
			return new TaskHolder(clazz.getName(), builder.domain, vars, url, builder.need_login, username, step, priority, holder.id, holder.scheduled_task_id, (List)trace);
		}
	}

	public TaskHolder newHolder(Class<? extends ChromeTask> clazz, Map<String, Object> init_map, String username, int step, Priority priority) throws Exception {
		TaskBuilder builder = (TaskBuilder)this.builders.get(clazz);
		if (builder == null) {
			throw new Exception(clazz.getName() + " builder not exist.");
		} else {
			Map<String, Object> vars = builder.validateInitMap(init_map);
			String url = builder.url_template;

			String key;
			for(Iterator var9 = vars.keySet().iterator(); var9.hasNext(); url = url.replace("{{" + key + "}}", String.valueOf(vars.get(key)))) {
				key = (String)var9.next();
			}

			if (priority == null) {
				priority = builder.base_priority;
			}

			return new TaskHolder(clazz.getName(), builder.domain, vars, url, builder.need_login, username, step, priority);
		}
	}

	public TaskHolder newHolder(Class<? extends ChromeTask> clazz, Map<String, Object> init_map, String username, int step) throws Exception {
		return this.newHolder(clazz, init_map, username, step, (Priority)null);
	}

	public TaskHolder newHolder(Class<? extends ChromeTask> clazz, String username, Map<String, Object> init_map) throws Exception {
		return this.newHolder(clazz, init_map, username, 0);
	}

	public TaskHolder newHolder(Class<? extends ChromeTask> clazz, Map<String, Object> init_map, int step) throws Exception {
		return this.newHolder(clazz, init_map, (String)null, step);
	}

	public TaskHolder newHolder(Class<? extends ChromeTask> clazz, Map<String, Object> init_map) throws Exception {
		return this.newHolder(clazz, init_map, (String)null, 0);
	}
}
