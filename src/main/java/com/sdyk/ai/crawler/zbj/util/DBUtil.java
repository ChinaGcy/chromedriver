package com.sdyk.ai.crawler.zbj.util;

import com.sdyk.ai.crawler.zbj.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import one.rewind.db.Refacter;

import java.util.HashSet;
import java.util.Set;

public class DBUtil {

	private static final Logger logger = LogManager.getLogger(Model.class.getName());

	/**
	 *
	 */
	public static void createTables() {

		for (Class clzss : Model.getModelClasses()) {

			try {
				Refacter.dropTable(clzss);
				Refacter.createTable(clzss);
			} catch (Exception e) {
				logger.error("Error create table, ", e);
			}
		}
	}

}
