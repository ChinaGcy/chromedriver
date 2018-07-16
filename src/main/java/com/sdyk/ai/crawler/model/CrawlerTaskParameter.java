package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.util.List;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "crawler_task_parameter")
public class CrawlerTaskParameter {

	// id
	@DatabaseField(generatedId = true)
	public int id;

	// domain
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String class_name;

	// dirvercount
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String parameter;

	// dirvercount
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String status;

	public CrawlerTaskParameter(){}

	public List<CrawlerTaskParameter > getAll() {

		Dao dao = null;

		try {

			dao = DaoManager.getDao(this.getClass());
			return (List<CrawlerTaskParameter>) dao.queryBuilder().where().eq(this.status,"run");

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
