package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.task.Task;
import org.openqa.selenium.WebDriver;

import java.util.HashSet;
import java.util.Set;

public class MonitorProjectZBJ {

	public String domain = "zbj.com";

	public ChromeDriverWithLogin agent = new ChromeDriverWithLogin(domain);

	Set<String> oldUrl = new HashSet<>();
	Set<String> newUrl = new HashSet<>();

	public Set<String> getNewUrl(ChromeDriverWithLogin agent) {


		return null;
	}
}
