package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;
import org.tfelab.txt.StringUtil;
import org.tfelab.txt.URLUtil;

import javax.xml.crypto.Data;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "projects")
public class Project implements JSONable<Project> {

	@DatabaseField(dataType = DataType.STRING, width = 32, id = true)
	public String id;

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String domain; // 原网站 domain

	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String url; //原网站链接

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String title; //名称

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String req_no; // 需求号

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String area; // 地点

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String from_; // 来自

	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String category; //定位到栏目大类

	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String tags; //tag 小标题

	@DatabaseField(dataType = DataType.STRING, width = 10240)
	public String description; //项目描述

	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_lb; //预算下限

	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_up; //预算上限

	@DatabaseField(dataType = DataType.INTEGER)
	public int time_limit; //工期

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String trade_type; //交易模式

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String current_status; //当前状态

	@DatabaseField(dataType = DataType.DATE)
	public Date remaining_time; //剩余时间

	@DatabaseField(dataType = DataType.DATE)
	public Date pubdate; //发布时间

	@DatabaseField(dataType = DataType.INTEGER)
	public int bidder_total_num; // 可接受投标数

	@DatabaseField(dataType = DataType.INTEGER)
	public int bidder_num; // 采集时刻投标人数

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_id; //招标人ID

	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_name; //招标人名称

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String status; // 项目状态：招募，开发中，已交付

	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type; //类型

	@DatabaseField(dataType = DataType.INTEGER)
	public int rate_num; //客户评价数

	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String reward_type; //赏金分配

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date(); //采集入库时间

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date(); //采集更新时间

	public Project() {}

	public Project(String url) throws MalformedURLException, URISyntaxException {
		this.id = StringUtil.byteArrayToHex(StringUtil.uuid(url));
		this.domain = URLUtil.getRootDomainName(URLUtil.getDomainName(url));
		this.url = url;
		this.req_no = url.split("/")[3];
	}

	/**
	 * 插入
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception{

		Dao<Project, String> dao = OrmLiteDaoManager.getDao(Project.class);
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
