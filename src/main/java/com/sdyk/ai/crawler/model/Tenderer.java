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
	public String origin_id;

	// 名字
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String name;

	// 地点
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String location;

	// 最近登录时间
	@DatabaseField(dataType = DataType.DATE)
	public Date login_time;

	// 交易次数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int trade_num;

	// 所在行业
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String category;

	// 雇主类型
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String tender_type;

	// 企业规模
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String company_type;

	// 雇主介绍描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 需求预测
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String req_forecast;

	// 总消费
	@DatabaseField(dataType = DataType.DOUBLE)
	public double total_spending;

	// 总项目数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int total_project_num;

	// 总雇佣人数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int total_employees;

	// 注册时间
	@DatabaseField(dataType = DataType.DATE)
	public Date register_time;

	// 好评数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int praise_time;

	// 评价数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int rating_num;

	// 等级
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String grade;

	// 公司名称
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String company_name;

	// 平台认证
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String platform_certification;

	// 用户积分
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int credit;

	public Tenderer() {}

	public Tenderer(String url) {
		super(url);
	}
}



