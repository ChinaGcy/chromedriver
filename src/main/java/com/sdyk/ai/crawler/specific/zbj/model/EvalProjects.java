package com.sdyk.ai.crawler.specific.zbj.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Model;
import one.rewind.db.DBName;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@DBName(value = "crawler")
@DatabaseTable(tableName = "eval_projects")
public class EvalProjects extends Model{

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String tags;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String cellphone;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double rating;

	@DatabaseField(dataType = DataType.DOUBLE)
	public double cost;

	public EvalProjects() {}

	public EvalProjects(String url) {
		super(url);
	}
}
