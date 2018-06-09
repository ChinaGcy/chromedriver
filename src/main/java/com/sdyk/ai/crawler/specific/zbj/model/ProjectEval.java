package com.sdyk.ai.crawler.specific.zbj.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@DBName(value = "sdyk_raw_eval")
@DatabaseTable(tableName = "project_evals")
public class ProjectEval extends Model{

	// 标签
	@DatabaseField(dataType = DataType.STRING, width = 512)
	public String tags;

	// 联系方式
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String cellphone;

	// 综合标签打分
	@DatabaseField(dataType = DataType.DOUBLE)
	public double rating;

	// 原网站标签打分
	@DatabaseField(dataType = DataType.DOUBLE)
	public double domain_rating;

	// 价格
	@DatabaseField(dataType = DataType.DOUBLE)
	public double cost;

	public ProjectEval() {}

	public ProjectEval(String url) {
		super(url);
	}
}
