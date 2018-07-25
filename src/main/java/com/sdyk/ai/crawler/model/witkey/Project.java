package com.sdyk.ai.crawler.model.witkey;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.es.ESTransportClientAdapter;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.witkey.snapshot.ProjectSnapshot;
import com.sdyk.ai.crawler.util.Range;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "projects")
public class Project extends Model{

	// 原网站 domain
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int domain_id;

	// 名称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String title;

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String origin_id;

	// 地点
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String location;

	// 来自
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String origin_from;

	// 定位到栏目大类
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String category;

	// 小标题
	@DatabaseField(dataType = DataType.STRING, width = 256)
	public String tags;

	// 项目描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 预算下限
	@DatabaseField(dataType = DataType.DOUBLE)
	public transient double budget_lb;

	// 预算上限
	@DatabaseField(dataType = DataType.DOUBLE)
	public transient double budget_ub;

	// 工期
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int time_limit;

	// 交易模式
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String trade_type;

	// 截止日期
	@DatabaseField(dataType = DataType.DATE)
	public Date due_time;

	// 发布时间
	@DatabaseField(dataType = DataType.DATE)
	public Date pubdate;

	// 可接受投标数
	@DatabaseField(dataType = DataType.INTEGER, width = 32)
	public int bidder_total_num = 0;

	// 采集时刻可投标人数
	@DatabaseField(dataType = DataType.INTEGER, width = 32)
	public int bids_available = 0;

	// 采集时刻已经投标数
	@DatabaseField(dataType = DataType.INTEGER, width = 32)
	public int bids_num;

	// 招标人ID
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_id;

	// 招标人名称
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_name;

	// 项目状态：招募，开发中，已交付
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String status;

	// 类型
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	// 客户评价数
	@DatabaseField(dataType = DataType.INTEGER)
	public int rate_num;

	// 赏金分配
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String reward_type;

	// 浏览次数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int view_num;

	// 收藏人数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int fav_num;

	// 点赞人数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int like_num;

	// 电话
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String cellphone;

	// 推荐人数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int rcmd_num;

	// 交付步骤付款占比
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String delivery_steps;

	// 附件
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String attachment_ids;

	public Range budget;

	public Project() {}

	public Project(String url) {
		super(url);
	}

	public void fullfill() {
		this.budget = new Range(this.budget_lb, this.budget_ub, true);
	}

	/**
	 *
	 * @param oldVersion
	 * @throws Exception
	 */
	public void createSnapshot(Model oldVersion) throws Exception {
		// 生成快照
		ProjectSnapshot projectSnapshot = new ProjectSnapshot((Project) oldVersion);

		// 保存快照
		projectSnapshot.insert();
	}

}
