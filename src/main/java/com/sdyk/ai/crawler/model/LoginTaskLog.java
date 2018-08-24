package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.task.LoginTask;
import com.sdyk.ai.crawler.util.JSONableListPersister;
import one.rewind.db.DBName;

import java.util.Date;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "login_task_logs")
public class LoginTaskLog {

	@DatabaseField(generatedId = true)
	public int id;

	// domain
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String domain;

	@DatabaseField(dataType = DataType.INTEGER, index = true)
	public int login_task_id;

	@DatabaseField(dataType = DataType.BOOLEAN)
	public boolean success = false;

	@DatabaseField(dataType = DataType.STRING, width = 10204)
	public String exceptions;

	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "MEDIUMBLOB") // TODO check
	public byte[] screen_shot;

	// 插入时间
	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	// 更新时间
	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();

}
