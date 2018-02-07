package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.json.JSONable;

import java.sql.SQLException;
import java.util.Date;
/*
作品描述
 */
@DBName(value = "crawler")
@DatabaseTable(tableName = "works")
public class Work implements JSONable<Work> {

	@DatabaseField(id = true, width = 32)
	public String id;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String user_id;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String name;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String tenderer_name;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String field;

	@DatabaseField(dataType = DataType.STRING, width = 10240)
	public String description;

	@DatabaseField(dataType = DataType.STRING, width = 10240)
	public String content;

	@DatabaseField(dataType = DataType.INTEGER)
	public int fav_num;

	@DatabaseField(dataType = DataType.INTEGER)
	public int like_sum;

	@DatabaseField(dataType = DataType.INTEGER)
	public int view_sum;

	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String tags;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double pricee;

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();

	public Work() { }


	public boolean insert() throws Exception {

		Dao<Work, String> dao = OrmLiteDaoManager.getDao(Work.class);
		try {
			if (dao.create(this) == 1) {
				return true;
			}

		}catch (SQLException e) {
			dao.update(this);
		}

		return false;
	}

	@Override
	public String toJSON() {
		return null;
	}
}
