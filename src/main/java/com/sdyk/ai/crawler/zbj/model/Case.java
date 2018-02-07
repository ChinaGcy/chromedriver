package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.json.JSONable;

import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "cases")
public class Case implements JSONable<Case> {

	@DatabaseField(id = true)
	public String id;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String user_id;

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String title;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	@DatabaseField(dataType = DataType.DATE)
	public Date sd;

	@DatabaseField(dataType = DataType.DATE)
	public Date ed;

	@DatabaseField(dataType = DataType.INTEGER)
	public int ongoing;

	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String tags;

	@DatabaseField(dataType = DataType.STRING, width = 10240)
	public String description;

	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String url;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String cycle;

	@DatabaseField(dataType = DataType.INTEGER)
	public int purchase_num;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget;

	@DatabaseField(dataType = DataType.FLOAT)
	public float rating;

	@DatabaseField(dataType = DataType.INTEGER)
	public int rate_num;

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time;

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time;


	public boolean insert() {
		try {
			Dao<Case, String> dao = OrmLiteDaoManager.getDao(Case.class);
			if (dao.create(this) == 1) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return  false;
	}

	@Override
	public String toJSON() {
		return null;
	}
}
