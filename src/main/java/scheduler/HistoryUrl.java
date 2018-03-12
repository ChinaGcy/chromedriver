package scheduler;

import com.sdyk.ai.crawler.zbj.requester.ChromeRequester;
import com.sdyk.ai.crawler.zbj.task.scanTask.ServiceScanTask;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取
 */
public class HistoryUrl {

	/**
	 * 需求标签类型地址
	 * @return
	 */
	public static List<String> getTaskUrl() {

		List<String> list_task = new ArrayList<>();

		/*ChromeDriverAgent agent = new ChromeDriverAgent();
		// A. 获取所有地址
		agent.getDriver().get("http://task.zbj.com/xuqiu/");

		// A1 需求
		// http://task.zbj.com/t-pxfw/
		List<WebElement> li = agent.getDriver().findElement(By.cssSelector("body > div.grid.list-category-nav > form > div.ui-dropdown.ui-dropdown-level1 > ul"))
				.findElements(By.tagName("li"));
		for (WebElement w : li) {
			for (WebElement ww : w.findElements(By.tagName("a"))) {
				list_task.add(ww.getAttribute("href").split("/")[3]);
			}
		}*/
		list_task.add("t-yxtg");
		list_task.add("t-rlzy");
		list_task.add("t-rcsc");
		list_task.add("t-zrfw");
		list_task.add("t-wxptkf");
		list_task.add("t-xswbzbj");
		list_task.add("t-yxgjrj");
		list_task.add("t-paperwork");
		list_task.add("t-sign");
		list_task.add("t-xxtg");

		return list_task;
	}

	/**
	 * 服务商标签类型地址
	 * @return
	 */
	public static List<String> getServiceUrl() {

		List<String> list_service = new ArrayList<>();

		/*ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.getDriver().get("http://www.zbj.com/home/p.html");
		List<WebElement> li1 = agent.getDriver().findElement(By.cssSelector("#utopia_widget_5 > div.clearfix.category-list > ul"))
				.findElements(By.tagName("li"));
		for (WebElement w : li1) {
			list_service.add(w.findElement(By.tagName("a")).getAttribute("href").split("/")[3]);
		}*/
		list_service.add("yxtg");
		list_service.add("paperwork");
		list_service.add("rcsc");
		list_service.add("sign");
		list_service.add("xxtg");
		list_service.add("rlzy");
		list_service.add("wxptkf");
		list_service.add("yxgjrj");
		list_service.add("zrfw");
		list_service.add("xswbzbj");






		return list_service;
	}
}