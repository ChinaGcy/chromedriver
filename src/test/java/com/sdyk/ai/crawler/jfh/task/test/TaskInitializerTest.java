package com.sdyk.ai.crawler.jfh.task.test;

import com.sdyk.ai.crawler.model.TaskInitializer;
import org.junit.Test;

import java.util.Date;

public class TaskInitializerTest {

	@Test
	public void test(){

		TaskInitializer.getAll().stream().filter(t -> {
			return t.enable == true;
		}).forEach( t ->{

			try {

				if( t.cron == null ){

					System.out.println(t.class_name + t.init_map_json);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		});

	}
}
