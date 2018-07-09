package com.sdyk.ai.crawler.model.company;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;

import java.util.Date;

/**
 * 公司融资情况
 */
@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "company_financings")
public class CompanyFinancing extends Model {

	// 公司ID
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String company_id;

	// 投资方名称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String name;

	// 轮次
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String financing_round;

	// 金额
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String financing_amount;

	// 新闻
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String financing_news;

	// 投资时间 TODO 没什么没有使用到？
	@DatabaseField(dataType = DataType.DATE)
	public Date financing_time;

	public CompanyFinancing() {}

	public CompanyFinancing(String url) {
		super(url);
	}

}
