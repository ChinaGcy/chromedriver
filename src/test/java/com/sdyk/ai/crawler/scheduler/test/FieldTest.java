package com.sdyk.ai.crawler.scheduler.test;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.junit.Test;

public class FieldTest {

	@Test
	void testField() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

		Class clazz = Class.forName("");

		long min_interval = Long.valueOf(clazz.getField("MIN_INTERVAL").getLong(clazz));

	}

}
