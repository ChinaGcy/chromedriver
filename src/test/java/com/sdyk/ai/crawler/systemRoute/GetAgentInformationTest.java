package com.sdyk.ai.crawler.systemRoute;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.Scheduler;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.json.JSON;
import org.junit.Test;

import java.util.*;

public class GetAgentInformationTest {

	@Test
	public void test() throws Exception {

		Scheduler.getInstance();

		Thread.sleep(30000);

		Map<String, List<String>> resurt = new HashMap<>();

		for(ChromeDriverAgent a : ((Distributor)ChromeDriverDistributor.getInstance()).queues.keySet()){

			List<String> domains = new ArrayList<>();

			for( String d : a.accounts.keySet() ){
				domains.add(d);
				System.out.println(d);
			}

			resurt.put(a.name, domains);
		}



		System.out.println(JSON.toPrettyJson(resurt));

		Thread.sleep(100000000);
	}


}
