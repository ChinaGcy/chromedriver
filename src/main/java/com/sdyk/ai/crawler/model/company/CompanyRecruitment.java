package com.sdyk.ai.crawler.model.company;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "company_recruitments")
public class CompanyRecruitment extends Model {

	// 公司ID
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String company_id;

	// 职务类型
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String position_type;

	// 职务名称
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String position_name;

	// 学历
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String educational;

	// 薪资下限
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int payroll_lb;

	// 薪资上线
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int payroll_ub;

	// 经验下线
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int experience_lb;

	// 经验上线
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int experience_ub;

	// 内容
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	public CompanyRecruitment() {}

	public CompanyRecruitment(String url) {
		super(url);
	}
}
