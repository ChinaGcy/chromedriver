package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

/**
 * 雇主
 */
@DBName(value = "crawler")
@DatabaseTable(tableName = "tenderers")
public class Tenderer implements JSONable<Tenderer> {

	@DatabaseField(id = true, width = 32)
	public String id;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String website_id; // 原网站id

	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String url; //原网站链接

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String name; //名字

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String area; //地点

	@DatabaseField(dataType = DataType.DATE)
	public Date login_time; //最近登录时间

	@DatabaseField(dataType = DataType.INTEGER)
	public int trade_num; //交易次数

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String industry; //所在行业

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String tender_type; //雇主类型

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String enterprise_size; //企业规模

	@DatabaseField(dataType = DataType.STRING, width = 10240)
	public String description; //雇主介绍描述

	@DatabaseField(dataType = DataType.STRING, width = 10240)
	public String demand_forecast; //需求预测

	@DatabaseField(dataType = DataType.DOUBLE)
	public double total_spending; //总消费

	@DatabaseField(dataType = DataType.INTEGER)
	public int total_project_num; //总项目数

	@DatabaseField(dataType = DataType.INTEGER)
	public int total_hires; //总雇佣人数

	@DatabaseField(dataType = DataType.DATE)
	public Date register_time; //注册时间

	@DatabaseField(dataType = DataType.INTEGER)
	public int good_rating_num; //好评数

	@DatabaseField(dataType = DataType.INTEGER)
	public int rating_num; //评价数

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String grade; //等级

	@DatabaseField(dataType = DataType.INTEGER)
	public int credit; //用户积分

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date(); //采集入库时间

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date(); //采集更新时间

	public Tenderer() {

	}

	public Tenderer(String url) {
		this.url = url.split("\\?")[0];

	}

	public boolean insert() throws Exception {

		Dao<Tenderer, String> dao = OrmLiteDaoManager.getDao(Tenderer.class);
		try {
			if (dao.create(this) == 1) {
				return true;
			}

		}catch (SQLException e) {
			e.printStackTrace();
			//dao.update(this);
		}

		return false;
	}

	@Override
	public String toJSON() {
		return JSON.toPrettyJson(this);
	}
}



