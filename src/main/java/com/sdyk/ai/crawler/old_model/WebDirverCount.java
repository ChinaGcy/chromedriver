package com.sdyk.ai.crawler.old_model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.util.List;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "web_dirver_count")
public class WebDirverCount {

	// id
	@DatabaseField(generatedId = true)
	public int id;

	// domain
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String domain;

	// dirvercount
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int dirver_count;

	public WebDirverCount(){}

	public List<WebDirverCount> getAll() {

		Dao dao = null;

		try {

			dao = DaoManager.getDao(this.getClass());
			return dao.queryForAll();

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
