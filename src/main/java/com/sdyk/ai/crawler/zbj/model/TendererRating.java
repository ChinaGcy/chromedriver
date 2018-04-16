package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.util.Date;

/**
 * 服务商评价
 */
@DBName(value = "crawler")
@DatabaseTable(tableName = "tenderer_ratings")
public class TendererRating extends Model {

	// 雇主url
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String tenderer_url;

	// 服务商名称
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String facilitator_name;

	// 服务商url
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String facilitator_url;

	// 评价时间
	@DatabaseField(dataType = DataType.DATE)
	public Date maluation_time;

	// 评价标签内容
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String maluation_tag;

	// 评价内容
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String maluation;

	// 付款及时度(0-5)
	@DatabaseField(dataType = DataType.INTEGER)
	public int pay_timeliness_num;

	// 合作愉快度（0-5）
	@DatabaseField(dataType = DataType.INTEGER)
	public int work_happy_num;

	public TendererRating() {}

	public TendererRating(String url) {
		super(url);
	}
}



