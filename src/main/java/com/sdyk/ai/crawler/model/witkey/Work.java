package com.sdyk.ai.crawler.model.witkey;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.util.JSONableListPersister;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	@DatabaseField(persisterClass = JSONableListPersister.class)
	public List<String> tags;

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
	@DatabaseField(persisterClass = JSONableListPersister.class)
	public List<String> attachment_ids;

	// 发布时间
	@DatabaseField(dataType = DataType.DATE)
	public Date pubdate;

	// 截至时间
	@DatabaseField(dataType = DataType.DATE)
	public Date duedate ;

	// 周期
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String time_limit;

	public Work() {}

	public Work(String url) {
		super(url);
	}

	/**
	 * 插入ES
	 * @return
	 */
	public boolean insert() {

		super.insert();

		try {

			ServiceProvider serviceProvider = (ServiceProvider) getById(ServiceProvider.class, this.user_id);
			serviceProvider.fullfill();
			serviceProvider.updateES();

			return true;
		} catch (Exception e) {

			logger.error("Can not find ServiceProvider: {}", this.user_id, e);
			return false;
		}
	}

	/**
	 * 添加tags
	 * @param tag
	 * @return
	 */
	public Work addTag(String... tag) {

		if (tags == null) {
			tags = new ArrayList<>();
		}

		for (String tag_ : tag) {
			tags.add(tag_);
		}
		return this;
	}
}
