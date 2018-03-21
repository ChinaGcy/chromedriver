package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.requester.ChromeRequester;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ServiceScanTask;
import it.sauronsoftware.cron4j.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.tfelab.io.requester.proxy.IpDetector;
import org.tfelab.util.NetworkUtil;

import java.util.ArrayList;
import java.util.List;

import static spark.route.HttpMethod.get;

public class Crawler {

	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Crawler.class.getName());

	protected static Crawler instance;

	public static Crawler getInstance() {

		if (instance == null) {

			synchronized (Crawler.class) {
				if (instance == null) {
					instance = new Crawler();

				}
			}
		}

		return instance;
	}

	// 项目频道参数
	public String[] project_channels = {
			"t-yxtg",
			"t-rlzy",
			"t-rcsc",
			"t-zrfw",
			"t-wxptkf",
			"t-xswbzbj",
			"t-yxgjrj",
			"t-paperwork",
			"t-sign",
			"t-xxtg"
	};

	// 服务商频道参数
	public static String[] service_supplier_channels = {
			"yxtg",
			"paperwork",
			"rcsc",
			"sign",
			"xxtg",
			"rlzy",
			"wxptkf",
			"yxgjrj",
			"zrfw",
			"xswbzbj"
	};

	/**
	 *
	 * @param backtrace
	 * @return
	 */
	public List<ScanTask> getTask(boolean backtrace) {

		List<ScanTask> tasks = new ArrayList<>();

		for(String channel : project_channels) {
			ScanTask t = ProjectScanTask.generateTask(channel, 1, null);
			t.backtrace = backtrace;
			tasks.add(t);
			System.out.println("PROJECT:" + channel);
		}

		if (backtrace == true) {
			for (String channel : service_supplier_channels) {
				ScanTask t = ServiceScanTask.generateTask(channel, 1, null);
				t.backtrace = backtrace;
				tasks.add(t);
				System.out.println("SERVICE:" + channel);
			}
		}

		return tasks;
	}

	/**
	 * 获取历史数据
	 */
	public void getHistoricalData() {

		// 需求
		for (Task task : getTask(true)) {
			ChromeRequester.getInstance().distribute(task);
		}
	}

	/**
	 * 监控调度
	 */
	public void monitor() {

		try {

			Scheduler s = new Scheduler();

			// 每隔十分钟，生成实时扫描任务
			s.schedule("*/10 * * * *", new Runnable() {

				public void run() {

					for (Task task : getTask(false)) {
						ChromeRequester.getInstance().distribute(task);
					}
				}
			});

			s.start();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length == 1 && args[0].equals("H")){
			// 获取历史数据
			System.out.println("历史数据");
			Crawler.getInstance().getHistoricalData();
		}
		else {
			// 监控数据
			System.out.println("监控数据");
			Crawler.getInstance().monitor();
		}
	}

}
