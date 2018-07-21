package com.sdyk.ai.crawler.util.test;

import com.sdyk.ai.crawler.model.TaskInitializer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TaskInitializerTest {

	@Test
	public void testTaskInitializer(){

		/*TaskInitializer.getAll().stream().forEach( t -> {
			System.out.println(t.class_name);
		});*/

		System.out.println(TaskInitializer.getAll().size());

	}

}
