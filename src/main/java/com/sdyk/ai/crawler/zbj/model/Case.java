package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;

import java.sql.SQLException;
import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "cases")
public class Case implements JSONable<Case> {

	@DatabaseField(id = true)
	public String id;

	//展示链接
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String url;

	//服务商id
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String user_id;

	//名称
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String title;

	//类型 视频/文档
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	//开始时间
	@DatabaseField(dataType = DataType.DATE)
	public Date sd;

	//结束时间
	@DatabaseField(dataType = DataType.DATE)
	public Date ed;

	//是否在进行中
	@DatabaseField(dataType = DataType.INTEGER)
	public int ongoing;

	//标签
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String tags;

	//描述
	@DatabaseField(dataType = DataType.STRING, width = 10240)
	public String description;

	//周期
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String cycle;

	//响应时间
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String response_time;

	//服务态度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_attitude;

	//工作速度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double work_speed;

	//完成质量
	@DatabaseField(dataType = DataType.DOUBLE)
	public double complete_quality;

	//购买人数
	@DatabaseField(dataType = DataType.INTEGER)
	public int purchase_num;

	//预算上限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_up;

	//预算下限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_lb;

	//客户评分
	@DatabaseField(dataType = DataType.FLOAT)
	public float rating;

	//客户评价数
	@DatabaseField(dataType = DataType.INTEGER)
	public int rate_num;

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();


	public boolean insert() throws Exception {
		Dao<Case, String> dao = OrmLiteDaoManager.getDao(Case.class);
		try {
			if (dao.create(this) == 1) {
				return true;
			}

		} catch (Exception e) {
			dao.update(this);
		}
		return  false;
	}

	@Override
	public String toJSON() {
		return JSON.toPrettyJson(this);
	}
}
