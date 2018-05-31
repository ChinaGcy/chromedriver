package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.txt.URLUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "selected_project")
public class SelectedProject extends Model{

	// 原网站 domain
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String domain;

	// 名称
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String title;

	// 需求号
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String req_no;

	// 地点
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String area;

	// 来自
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String origin;

	// 定位到栏目大类
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String category;

	// 项目描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String description;

	// 预算下限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_lb;

	// 预算上限
	@DatabaseField(dataType = DataType.DOUBLE)
	public double budget_up;

	// 交易模式
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String trade_type;

	// 当前状态
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String current_status;

	// 剩余时间
	@DatabaseField(dataType = DataType.DATE)
	public Date remaining_time;

	// 发布时间
	@DatabaseField(dataType = DataType.DATE)
	public Date pubdate;

	// 可接受投标数
	@DatabaseField(dataType = DataType.INTEGER)
	public int bidder_total_num = 0;

	// 采集时刻可投标人数
	@DatabaseField(dataType = DataType.INTEGER)
	public int bids_available = 0;

	// 采集时刻已经投标数
	@DatabaseField(dataType = DataType.INTEGER)
	public int bids_num;

	// 招标人ID
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_id;

	// 招标人名称
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String tenderer_name;

	// 赏金分配
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String reward_distribution;

	// 项目评分
	@DatabaseField(dataType = DataType.DOUBLE)
	public double marking;

	// 联系方式
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String phone;

	public SelectedProject() {}

	public SelectedProject(String url) throws MalformedURLException, URISyntaxException {
		super(url);
		this.domain = URLUtil.getRootDomainName(URLUtil.getDomainName(url));
		this.req_no = url.split("/")[3];
	}
}
