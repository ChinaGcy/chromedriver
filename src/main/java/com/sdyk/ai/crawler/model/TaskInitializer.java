package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.util.Date;
import java.util.List;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "task_initializers")
public class TaskInitializer {

	// id
	@DatabaseField(generatedId = true)
	public int id;

	// domain
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String class_name;

	// init_map
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String init_map_json;

	// cron
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String cron;

	// 插入时间
	@DatabaseField(dataType = DataType.DATE)
	public Date start_time = new Date();

	// scheduled_task_id
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String scheduled_task_id;

	// enable
	@DatabaseField(dataType = DataType.BOOLEAN, canBeNull = false, defaultValue = "true")
	public boolean enable;


	public TaskInitializer(){}

	/**
	 * 获取所有初始任务
	 * @return
	 */
	public static List<TaskInitializer > getAll() {

		try {

			Dao<TaskInitializer, String> dao = DaoManager.getDao(TaskInitializer.class);
			return dao.queryForEq("enable",1);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 更新数据库
	 */
	public boolean update() throws Exception {

		Dao dao = DaoManager.getDao(this.getClass());

		if (dao.update(this) == 1) {
			return true;
		}

		return false;
	}
}
