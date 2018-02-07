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

/**
 * 服务商评价
 */
@DBName(value = "crawler")
@DatabaseTable(tableName = "tenderer_ratings")
public class TendererRating implements JSONable<TendererRating> {

	@DatabaseField(generatedId = true)
	public int id;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_url;

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String facilitator_name; //服务商名称

	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String facilitator_url; //服务商url

	@DatabaseField(dataType = DataType.DATE)
	public Date maluation_time; //评价时间

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String maluation_tag; //评价标签内容

	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String maluation; //评价内容

	@DatabaseField(dataType = DataType.INTEGER)
	public int pay_timeliness_num; //付款及时度(0-5)

	@DatabaseField(dataType = DataType.INTEGER)
	public int work_happy_num; //合作愉快度（0-5）

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date(); //采集入库时间

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date(); //采集更新时间



	public TendererRating() { }


	public boolean insert() throws Exception {

		Dao<TendererRating, String> dao = OrmLiteDaoManager.getDao(TendererRating.class);
		try {
			if (dao.create(this) == 1) {
				return true;
			}

		}catch (SQLException e) {
			dao.update(this);
		}

		return false;
	}

	@Override
	public String toJSON() {
		return JSON.toPrettyJson(this);
	}

}



