package com.sdyk.ai.crawler.old_model.company;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;

/**
 * 公司产品信息
 */
@DBName(value = "sdyk_raw_")
@DatabaseTable(tableName = "company_products")
public class CompanyProduct extends Model {

	// 公司ID
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String company_id;

	// 产品名称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String product_name;

	// 描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	public CompanyProduct() {}

	public CompanyProduct(String url) {
		super(url);
	}

}
