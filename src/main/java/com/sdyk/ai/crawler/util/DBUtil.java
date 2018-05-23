package com.sdyk.ai.crawler.util;

import com.sdyk.ai.crawler.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.rewind.db.Refacter;

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
