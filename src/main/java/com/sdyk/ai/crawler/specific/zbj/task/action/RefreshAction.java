package com.sdyk.ai.crawler.specific.zbj.task.action;

import one.rewind.io.requester.chrome.action.ChromeAction;

/**
 *
 */
public class RefreshAction extends ChromeAction {

	public RefreshAction () {}

	public void run() {

		String src = this.agent.getDriver().getPageSource();

		if (src.contains("操作失败请稍后重试") || src.contains("很抱歉，此页面内部错误！")) {
			this.agent.getDriver().navigate().refresh();
		}
	}
}
