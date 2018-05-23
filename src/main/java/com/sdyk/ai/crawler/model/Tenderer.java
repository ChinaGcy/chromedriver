package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.util.Date;

/**
 * 雇主
 */
@DBName(value = "crawler")
@DatabaseTable(tableName = "tenderers")
public class Tenderer extends Model {

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String website_id;

	// 名字
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String name;

	// 地点
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String area;

	// 最近登录时间
	@DatabaseField(dataType = DataType.DATE)
	public Date login_time;

	// 交易次数
	@DatabaseField(dataType = DataType.INTEGER)
	public int trade_num;

	// 所在行业
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String industry;

	// 雇主类型
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String tender_type;

	// 企业规模
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String enterprise_size;

	// 雇主介绍描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String description;

	// 需求预测
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String demand_forecast;

	// 总消费
	@DatabaseField(dataType = DataType.DOUBLE)
	public double total_spending;

	// 总项目数
	@DatabaseField(dataType = DataType.INTEGER)
	public int total_project_num;

	// 总雇佣人数
	@DatabaseField(dataType = DataType.INTEGER)
	public int total_hires;

	// 注册时间
	@DatabaseField(dataType = DataType.DATE)
	public Date register_time;

	// 好评数
	@DatabaseField(dataType = DataType.INTEGER)
	public int good_rating_num;

	// 评价数
	@DatabaseField(dataType = DataType.INTEGER)
	public int rating_num;

	// 等级
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String grade;

	// 用户积分
	@DatabaseField(dataType = DataType.INTEGER)
	public int credit;

	public Tenderer() {}

	public Tenderer(String url) {
		super(url);
	}
}



