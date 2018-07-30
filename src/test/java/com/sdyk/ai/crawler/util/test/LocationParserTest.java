package com.sdyk.ai.crawler.util.test;

import com.sdyk.ai.crawler.util.LocationParser;
import one.rewind.json.JSON;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class LocationParserTest {

	LocationParser parser;
	Map<String, String> map;

	@Before
	public void init() {
		try {
			parser = LocationParser.getInstance();

			map = new HashMap<>();

			map.put("青海海南", "青海省,海南藏族自治州");
			map.put("北京朝阳", "北京市,朝阳区");
			map.put("吉林省 朝阳区", "吉林省,长春市,朝阳区");
			map.put("广西壮族自治区", "广西壮族自治区");
			map.put("长春", "吉林省,长春市");
			map.put("大兴安岭地区", "黑龙江省,大兴安岭地区");
			map.put("鄂尔多斯市", "内蒙古自治区,鄂尔多斯市");
			map.put("兴城", "辽宁省,葫芦岛市,兴城市");
			map.put("惠山区", "江苏省,无锡市,惠山区");
			map.put("红河哈尼族彝族自治州", "云南省,红河哈尼族彝族自治州");
			map.put("珠海市", "广东省,珠海市");
			map.put("玄武区", "江苏省,南京市,玄武区");
			map.put("江苏秦淮", "江苏省,南京市,秦淮区");
			map.put("岳阳市", "湖南省,岳阳市");
			map.put("河北", "河北省");
			map.put("中卫市", "宁夏回族自治区,中卫市");
			map.put("上海", "上海市");
			map.put("南京市", "江苏省,南京市");
			map.put("襄阳市", "湖北省,襄阳市");
			map.put("积石山保安族东乡族撒拉族自治县", "甘肃省,临夏回族自治州,积石山保安族东乡族撒拉族自治县");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void tryParse() {

		for( String s : map.keySet() ){
			Assert.assertEquals(parser.matchLocation(s).get(0).toString(), map.get(s));
		}
	}

	@Test
	public void parallelTest() throws InterruptedException {

		ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue());

		long t1 = System.currentTimeMillis();
		int cycle = 100;
		CountDownLatch countDownLatch = new CountDownLatch(map.keySet().size() * cycle);

		for(int i=0; i<cycle; i++) {
			for (String s : map.keySet()) {
				executor.submit(()-> {
					LocationParser.logger.info("{} --> {}", parser.matchLocation(s).get(0).toString(), map.get(s));
					countDownLatch.countDown();
				});
			}
		}

		countDownLatch.await();
		LocationParser.logger.info(System.currentTimeMillis() - t1);
	}
}
