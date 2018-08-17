package com.sdyk.ai.crawler.model.company;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.util.Date;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "company_information")
public class CompanyInformation extends Model {

	// 名称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String name;

	// 简称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String simple_name;

	// 英文名
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String english_name;

	// 法人
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String legal_person_name;

	// 电话
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String telephone;

	// 官网
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String website;

	// email
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String email;

	// 城市地址
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String location;

	// 详细地址
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String address;

	// 描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 类型
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	// 标签
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String tags;

	// logo
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String logo;

	// 注册资本
	@DatabaseField(dataType = DataType.DOUBLE)
	public transient double reg_capital;

	// 竞品公司ID列表，英文半角逗号分割
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String competing_company_ids;

	// 信息更新时间
	@DatabaseField(dataType = DataType.DATE)
	public Date info_update_time;

	// 公司状态
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String reg_status;

	// 行业
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String industry;

	// 工商注册号
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String reg_number;

	// 组织机构代码
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String org_number;

	// 统一信用码
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String credit_code;

	// 纳税人识别号
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tax_number;

	// 营业期限
	@DatabaseField(dataType = DataType.DATE)
	public Date operating_period;

	// 登记机关
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String reg_institute;

	// 注册地址
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String reg_location;

	// 经营范围
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String business_scope;

	// 核准日期
	@DatabaseField(dataType = DataType.DATE)
	public Date approved_time;

	// 成立日期
	@DatabaseField(dataType = DataType.DATE)
	public Date founded_time;

	// 公司规模
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String size;

	public CompanyInformation() {}

	public CompanyInformation(String url) {
		super(url);
	}

	public static CompanyInformation getCompanyInformationById (String id){

		try {

			Dao dao = DaoManager.getDao(CompanyInformation.class);
			CompanyInformation companyInformation = (CompanyInformation) dao.queryForId(id);
			return  companyInformation;
		} catch (Exception e) {
			logger.error("error for getCompanyInformationById : {{}}", id);
		}

		return null;
	}

}
