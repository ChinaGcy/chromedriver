package com.sdyk.ai.crawler.util.test;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class stringToMap {

	@Test
	public void testStringToMap(){

		String str = "{1:a,2:b}";

		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();
		map = gson.fromJson(str, map.getClass());

		System.out.println(map.toString());

	}

}
