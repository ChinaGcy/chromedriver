package com.sdyk.ai.crawler.old_model.company;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.old_model.Model;
import one.rewind.db.DBName;

@DBName(value = "sdyk_raw_")
@DatabaseTable(tableName = "company_staffs")
public class CompanyStaff extends Model {

	// 公司ID
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String company_id;

	// 姓名
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String name;

	// 职位
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String position;

	// 介绍
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	public CompanyStaff() {}

	public CompanyStaff(String url) {
		super(url);
	}
}
