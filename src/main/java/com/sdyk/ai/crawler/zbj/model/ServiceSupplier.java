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
@DatabaseTable(tableName = "service_suppliers")
public class ServiceSupplier extends Model {

	// 来源网站
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String website_id;

	// 名字
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String name;

	// 团队/个人
	@DatabaseField(dataType = DataType.STRING, width = 16)
	public String type;

	// 位置
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String location;

	// 描述
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String description;

	// 团队成员数量
	@DatabaseField(dataType = DataType.INTEGER)
	public int member_num;

	// 工作经验
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String work_experience;

	// 擅长领域
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String expertise;

	// 擅长技能
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String skills;

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
	@DatabaseField(dataType = DataType.INTEGER)
	public int project_num;

	// 项目成功率
	@DatabaseField(dataType = DataType.FLOAT)
	public float success_ratio;

	// 收入
	@DatabaseField(dataType = DataType.DOUBLE)
	public double revenue;

	// 进行中的交易金额
	@DatabaseField(dataType = DataType.DOUBLE)
	public double transact;

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
	public double charge_single;

	// 每日
	@DatabaseField(dataType = DataType.DOUBLE)
	public double charge_daily;

	// 关注度
	@DatabaseField(dataType = DataType.INTEGER)
	public int attention_num;

	// 人气值
	@DatabaseField(dataType = DataType.INTEGER)
	public int popularity_num;

	// 目前职位
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String position_now;

	// 客户评分
	@DatabaseField(dataType = DataType.FLOAT)
	public float rating;

	// 雇主推荐
	@DatabaseField(dataType = DataType.INTEGER)
	public int recommendation_num;

	// 收藏量
	@DatabaseField(dataType = DataType.INTEGER)
	public int collection_num;

	// 评价数
	@DatabaseField(dataType = DataType.INTEGER)
	public int rating_num;

	// 好评
	@DatabaseField(dataType = DataType.INTEGER)
	public int good_rating_num;

	// 差评
	@DatabaseField(dataType = DataType.INTEGER)
	public int bad_rating_num;

	public ServiceSupplier() {}

	public ServiceSupplier(String url) {
		super(url);
	}
}
