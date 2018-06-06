package com.sdyk.ai.crawler.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "service_providers")
public class ServiceProvider extends Model {

	// 来源网站
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String origin_id;

	// 名字
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String name;

	// 公司
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String company_name;

	// 类型
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	// 位置
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String location;

	// 描述
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String content;

	// 团队成员数量
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int team_size;

	// 工作经验
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int work_experience;

	// 擅长领域
	@DatabaseField(dataType = DataType.STRING, width = 256)
	public String category;

	// 擅长技能
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String tags;

	//服务质量
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_quality;

	// 服务速度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_speed;

	// 服务态度
	@DatabaseField(dataType = DataType.DOUBLE)
	public double service_attitude;

	// 平台项目数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int project_num;

	// 项目成功率
	@DatabaseField(dataType = DataType.FLOAT)
	public float success_ratio;

	// 收入
	@DatabaseField(dataType = DataType.DOUBLE)
	public double income;

	// 进行中的交易金额
	@DatabaseField(dataType = DataType.DOUBLE)
	public double pending_txn;

	// 注册时间
	@DatabaseField(dataType = DataType.DATE)
	public Date register_time;

	// 等级
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String grade;

	// 积分
	@DatabaseField(dataType = DataType.FLOAT)
	public float credit;

	// 电话
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String cellphone;

	// qq
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String qq;

	// 微信
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String weixin;

	// 邮箱
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String email;

	// 微博
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String weibo;

	// 旺旺
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String wangwang;

	// 单次
	@DatabaseField(dataType = DataType.DOUBLE)
	public double price_per_project;

	// 每日
	@DatabaseField(dataType = DataType.DOUBLE)
	public double price_per_day;

	// 关注数/收藏数
	@DatabaseField(dataType = DataType.INTEGER)
	public int fav_num;

	// 粉丝数
	@DatabaseField(dataType = DataType.INTEGER)
	public int fan_num;

	// 点赞数
	@DatabaseField(dataType = DataType.INTEGER)
	public int like_num;

	// 人气值/浏览数
	@DatabaseField(dataType = DataType.INTEGER)
	public int view_num;

	// 目前职位
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String occupation;

	// 客户评分
	@DatabaseField(dataType = DataType.FLOAT)
	public float rating;

	// 雇主推荐
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int rcmd_num;

	// 评价数
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int rating_num;

	// 好评
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int praise_num;

	// 差评
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int bad_review_num;

	public ServiceProvider() {}

	public ServiceProvider(String url) {
		super(url);
	}
}
