package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;

import java.sql.SQLException;
import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "service_rating")
public class SupplierRating implements JSONable<SupplierRating>{

	@DatabaseField(id = true, width = 16)
	public String id;

	// 服务商id
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String service_supplier_id;

	// 雇主姓名
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_name;

	// 雇主id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_url;

	// 项目地址
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String project_url;

	//雇主id
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String tenderer_id;

	//项目花费
	@DatabaseField(dataType = DataType.DOUBLE)
	public double spend;

	//描述
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String descaption;

	// 评价标签
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String  tags;

	// 客户评价时间
	@DatabaseField(dataType = DataType.DATE)
	public Date rating_time;

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();


	/**
	 * 插入
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception{

		Dao<SupplierRating, String> dao = OrmLiteDaoManager.getDao(SupplierRating.class);
		try {
			if (dao.create(this) == 1) {
				return true;
			}

		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	@Override
	public String toJSON() {
		return JSON.toPrettyJson(this);
	}
}
