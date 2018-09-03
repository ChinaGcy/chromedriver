package com.sdyk.ai.crawler.model.witkey;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.util.JSONableListPersister;
import one.rewind.db.DBName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String project_name;

	// 发布时间
	@DatabaseField(dataType = DataType.DATE)
	public Date pubdate;

	// 评价标签内容
	@DatabaseField(persisterClass = JSONableListPersister.class)
	public List<String> tags;

	// 评价内容
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 付款及时度(0-5)
	@DatabaseField(dataType = DataType.DOUBLE)
	public double payment_rating;

	// 合作愉快度（0-5）
	@DatabaseField(dataType = DataType.DOUBLE)
	public double coop_rating;

	// 评分，五分制
	@DatabaseField(dataType = DataType.DOUBLE)
	public double rating;

	public TendererRating() {}

	public TendererRating(String url) {
		super(url);
	}

	/**
	 * 添加tags
	 * @param tag
	 * @return
	 */
	public TendererRating addTag(String... tag) {

		if (tags == null) {
			tags = new ArrayList<>();
		}

		for (String tag_ : tag) {
			tags.add(tag_);
		}
		return this;
	}
}



