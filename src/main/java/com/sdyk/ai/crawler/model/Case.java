package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "cases")
public class Case extends Model {

	// 服务商id
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String user_id;

	// 名称
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String title;

	// 类型描述
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String type;

	// 开始时间
	@DatabaseField(dataType = DataType.DATE)
	public Date sd;

	// 结束时间
	@DatabaseField(dataType = DataType.DATE)
	public Date ed;

	// 是否在进行中
	@DatabaseField(dataType = DataType.INTEGER, width = 1)
	public int ongoing;

	// 标签
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String tags;

	// 描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String description;

	// 周期
	// TODO 应该以天数为单位，float类型
	// 1天 = 8小时
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String cycle;

	// 响应时间
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String response_time;

	// 服务态度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_attitude;

	// 工作速度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double work_speed;

	// 完成质量
	@DatabaseField(dataType = DataType.DOUBLE)
	public double complete_quality;

	// 购买人数
	@DatabaseField(dataType = DataType.INTEGER)
	public int purchase_num;

	// 预算上限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_up;

	// 预算下限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_lb;

	// 客户评分
	@DatabaseField(dataType = DataType.FLOAT)
	public float rating;

	// 客户评价数
	@DatabaseField(dataType = DataType.INTEGER)
	public int rate_num;

	public Case(){}

	public Case(String url) {
		super(url);
	}
}
