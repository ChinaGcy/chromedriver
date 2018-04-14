package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.OrmLiteDaoManager;
import one.rewind.json.JSON;
import one.rewind.json.JSONable;

import java.sql.SQLException;
import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "service_ratings")
public class SupplierRating extends Model{

	// 服务商id
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String service_supplier_id;

	// 雇主姓名
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String tenderer_name;

	// 雇主id
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String tenderer_url;

	// 项目地址
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String project_url;

	// 雇主id
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String tenderer_id;

	// 项目花费
	@DatabaseField(dataType = DataType.DOUBLE)
	public double spend;

	// 描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String description;

	// 评价标签
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String  tags;

	// 客户评价时间
	@DatabaseField(dataType = DataType.DATE)
	public Date rating_time;

	public SupplierRating() {}

	public SupplierRating(String url) {
		super(url);
	}
}
