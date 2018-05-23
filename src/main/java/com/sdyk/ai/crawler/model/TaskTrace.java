package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.json.JSON;
import one.rewind.json.JSONable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * 记录ScanTask翻页记录
 */
@DatabaseTable(tableName = "task_traces")
@DBName(value = "crawler")
public class TaskTrace implements JSONable {

	private static final Logger logger = LogManager.getLogger(TaskTrace.class.getName());

	@DatabaseField(generatedId = true)
	private transient Long id;

	@DatabaseField(dataType = DataType.STRING, width = 64, canBeNull = false, uniqueCombo = true)
	public String type;

	@DatabaseField(dataType = DataType.STRING, width = 64, canBeNull = false, uniqueCombo = true)
	public String channel;

	@DatabaseField(dataType = DataType.STRING, width = 11, canBeNull = false, uniqueCombo = true)
	public String page;

	@DatabaseField(dataType = DataType.DATE, canBeNull = false)
	public Date insert_time = new Date();

	public TaskTrace() {};

	public TaskTrace(Class clazz, String channel, String page) {
		this.type = clazz.getSimpleName();
		this.channel = channel;
		this.page = page;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception {

		Dao<TaskTrace, String> dao = DaoManager.getDao(TaskTrace.class);

		if (dao.create(this) == 1) {
			return true;
		}

		return false;
	}

	/**
	 *
	 * @param code
	 * @param clazz
	 * @param page
	 * @return
	 * @throws Exception
	 */
	public static TaskTrace getTaskTrace(String code, Class clazz, String page) throws Exception {
		Dao<TaskTrace, String> dao = DaoManager.getDao(TaskTrace.class);
		return dao.queryBuilder().where().eq("url", code)
				.and().eq("type", clazz.getSimpleName())
				.and().eq("page", page).queryForFirst();

	}

	@Override
	public String toJSON() {
		return JSON.toJson(this);
	}

}
