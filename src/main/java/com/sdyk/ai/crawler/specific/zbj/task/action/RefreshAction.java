package com.sdyk.ai.crawler.specific.zbj.task.action;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;

/**
 *
 */
public class RefreshAction extends Action {

	public RefreshAction () {}

	public boolean run(ChromeDriverAgent agent) {

		String src = agent.getDriver().getPageSource();

		if (src.contains("操作失败请稍后重试") || src.contains("很抱歉，此页面内部错误！")) {
			agent.getDriver().navigate().refresh();
		}
		return true;
	}
}
