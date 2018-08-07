package com.sdyk.ai.crawler.versions;

import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Date;

public class StringTest {

	@Test
	public void test(){

		int a = 1;
		int b = 2;

		Date c = new Date();
		Date d = new Date();

		System.out.println(String.valueOf(d).equals(String.valueOf(c)));
		System.out.println(String.valueOf(c));

	}

	@Test
	public void trstField(){

		Project project = new Project("http://www.baidu.com");

		Field[] fieldList = project.getClass().getDeclaredFields();

		for(Field f : fieldList) {

			System.out.println(f.getName());
		}

	}

	/*@Test
	public void getTenderById() throws Exception {

		Tenderer tenderer = Tenderer.getTenderById("25448fdb02c62d3834ae179a2b028d89");

		System.out.println(tenderer.toJSON());

	}*/


}
