package scheduler;

import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.requester.ChromeRequester;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ServiceScanTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Scheduler {

	/**
	 *历史数据
	 */
	public static void historyData() {

		ChromeRequester chromeRequester =  ChromeRequester.getInstance();

		// 需求
		for ( String url : HistoryUrl.getTaskUrl()) {

			chromeRequester.distribute(ProjectScanTask.generateTask(url, 1, null));
		}

		// 服务商
		for (String url : HistoryUrl.getServiceUrl()) {

			chromeRequester.distribute(ServiceScanTask.generateTask(url, 1, null));
		}



	}

	/**
	 * 监控数据
	 */
	public static void monitorData() {

		ChromeRequester chromeRequester =  ChromeRequester.getInstance();

		ProjectScanTask p = null;
		try {
			p = new ProjectScanTask("http://task.zbj.com/s5.html?o=7",1);

			// 设置不翻页
			p.backtrace = false;

		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}

		chromeRequester.distribute(p);

	}

	public static void main(String[] args) {

		historyData();

		monitorData();
	}

}
