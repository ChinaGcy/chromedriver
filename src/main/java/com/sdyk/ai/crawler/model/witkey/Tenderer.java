package com.sdyk.ai.crawler.model.witkey;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.es.ESTransportClientAdapter;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.witkey.snapshot.ServiceProviderSnapshot;
import com.sdyk.ai.crawler.model.witkey.snapshot.TendererSnapshot;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.sql.SQLException;
import java.util.Date;

/**
 * 雇主
 */
@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "tenderers")
public class Tenderer extends Model {

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String origin_id;

	// 名字
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String name;

	// 头像
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String head_portrait;

	// 地点
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String location;

	// 最近登录时间
	@DatabaseField(dataType = DataType.DATE)
	public Date login_time;

	// 交易次数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int trade_num;

	// 所在行业
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String category;

	// 雇主类型
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String tender_type;

	// 企业规模
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String company_scale;

	// 雇主介绍描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 需求预测
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String req_forecast;

	// 总消费
	@DatabaseField(dataType = DataType.DOUBLE)
	public double total_spending;

	// 总项目数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int total_project_num;

	// 总雇佣人数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int total_employees;

	// 注册时间
	@DatabaseField(dataType = DataType.DATE)
	public Date register_time;

	// 好评数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int praise_num;

	// 评价数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int rating_num;

	// 需求选定率
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int selection_ratio;

	// 需求成功率
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int success_ratio;

	// 等级
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String grade;

	// 公司名称
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String company_name;

	// 平台认证
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String platform_certification;

	// 用户积分
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int credit;

	// 附件
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String attachment_ids;

	public Tenderer() {}

	public Tenderer(String url) {
		super(url);
	}

	/**
	 *
	 * @param oldVersion
	 * @throws Exception
	 */
	public void createSnapshot(Model oldVersion) throws Exception {
		// 生成快照
		TendererSnapshot snapshot = new TendererSnapshot((Tenderer) oldVersion);

		// 保存快照
		snapshot.insert();
	}

}



