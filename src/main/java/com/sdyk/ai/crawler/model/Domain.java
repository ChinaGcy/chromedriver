package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "domains")
public class Domain {

	// id
	@DatabaseField(generatedId = true)
	public int id;

	// domain
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String domain;

	// domain_name
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String domain_name;

	// insert
	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	// update
	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();

	public Domain () {}

}
