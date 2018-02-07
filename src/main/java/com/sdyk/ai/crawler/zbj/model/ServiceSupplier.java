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

@DBName(value = "crawler")
@DatabaseTable(tableName = "service_suppliers")
public class ServiceSupplier implements JSONable<ServiceSupplier> {

	@DatabaseField(id = true, width = 32)
	public String id;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String website_id;

	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String url;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String name;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String location;

	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String description;

	@DatabaseField(dataType = DataType.INTEGER)
	public int member_num;

	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String work_experience;

	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String expertises;

	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String skills;

	@DatabaseField(dataType = DataType.INTEGER)
	public int project_num;

	@DatabaseField(dataType = DataType.FLOAT)
	public float success_ratio;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double revenue;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double transact;

	@DatabaseField(dataType = DataType.DATE)
	public Date register_time;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String grade;

	@DatabaseField(dataType = DataType.FLOAT)
	public float credit;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String cellphone;

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String qq;

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String weixin;

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String email;

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String weibo;

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String wangwang;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double charge_single;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double charge_daily;

	@DatabaseField(dataType = DataType.INTEGER)
	public int attention_num;

	@DatabaseField(dataType = DataType.INTEGER)
	public int popularity_num;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String position_now;

	@DatabaseField(dataType = DataType.FLOAT)
	public float rating;

	@DatabaseField(dataType = DataType.INTEGER)
	public int rating_num;

	@DatabaseField(dataType = DataType.INTEGER)
	public int good_rating_num;

	@DatabaseField(dataType = DataType.INTEGER)
	public int bad_rating_num;

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();

	public ServiceSupplier() { }


	public boolean insert() throws Exception {

		Dao<ServiceSupplier, String> dao = OrmLiteDaoManager.getDao(ServiceSupplier.class);
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
