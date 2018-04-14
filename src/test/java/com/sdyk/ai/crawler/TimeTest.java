package com.sdyk.ai.crawler;

import org.junit.Test;

public class TimeTest {

	@Test
	public void time () {
		long start = System.currentTimeMillis();

		while(true) {
			if (System.currentTimeMillis() - start < 60*1000) {
				System.err.println("111111");
			}
			else {
				System.out.println("222222");
				try {
					Thread.sleep(1000*60);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				start = System.currentTimeMillis();

			}
		}

	}
}
