package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "service_provider_ratings")
public class ServiceProviderRating extends Model{

	// 服务商id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String service_provider_id;

	// 项目id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String project_id;

	// 项目name
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String project_name;

	// 雇主id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_id;

	// 雇主name
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_name;

	// 项目花费
	@DatabaseField(dataType = DataType.DOUBLE)
	public double price;

	// 描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 评价标签
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String  tags;

	// 客户评价时间
	@DatabaseField(dataType = DataType.DATE)
	public Date pubdate;

	// 评分
	@DatabaseField(dataType = DataType.DOUBLE)
	public double rating;

	public ServiceProviderRating() {}

	public ServiceProviderRating(String url) {
		super(url);
	}
}
