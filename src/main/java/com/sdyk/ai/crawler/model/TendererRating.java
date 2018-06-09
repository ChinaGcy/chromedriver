package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.util.Date;

/**
 * 服务商评价
 */
@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "tenderer_ratings")
public class TendererRating extends Model {

	// 雇主id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String user_id;

	// 服务商id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String service_provider_id;

	// 服务商name
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String service_provider_name;

	// 项目id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String project_id;

	// 项目name
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String project_name;

	// 发布时间
	@DatabaseField(dataType = DataType.DATE)
	public Date pubdate;

	// 评价标签内容
	@DatabaseField(dataType = DataType.STRING, width = 512)
	public String tags;

	// 评价内容
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 付款及时度(0-5)
	@DatabaseField(dataType = DataType.DOUBLE)
	public double payment_rating;

	// 合作愉快度（0-5）
	@DatabaseField(dataType = DataType.DOUBLE)
	public double coop_rating;

	public TendererRating() {}

	public TendererRating(String url) {
		super(url);
	}
}



