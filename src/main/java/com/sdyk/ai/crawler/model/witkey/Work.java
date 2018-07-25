package com.sdyk.ai.crawler.model.witkey;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;

/**
 * 作品描述
 * @author
 * @date
 */
@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "works")
public class Work extends Model {

	// 服务商id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String user_id;

	// 雇主名
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String tenderer_name;

	// 标签
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String tags;

	// 领域
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String category;

	// 内容
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 作品名称
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String title;

	// 职位
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String position;

	// 收藏
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int fav_num;

	// 点赞
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int like_num;

	// 浏览量
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int view_num;

	// 作品展示链接
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String external_url;

	// 价格
	@DatabaseField(dataType = DataType.DOUBLE)
	public double price;

	// 附件
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String attachment_ids;

	public Work() {}

	public Work(String url) {
		super(url);
	}
}
