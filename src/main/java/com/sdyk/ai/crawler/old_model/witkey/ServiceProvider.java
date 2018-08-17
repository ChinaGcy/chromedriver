package com.sdyk.ai.crawler.old_model.witkey;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.old_model.Model;
import com.sdyk.ai.crawler.old_model.witkey.snapshot.ServiceProviderSnapshot;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@DBName(value = "sdyk_raw_")
@DatabaseTable(tableName = "service_providers")
public class ServiceProvider extends Model {

	// 来源网站
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String origin_id;

	// 名字
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String name;

	// 头像
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String head_portrait;

	// 平台认证
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String platform_certification;

	// 公司
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String company_name;

	// 公司地址
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String company_address;

	// 公司网址
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String company_website;

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
	public String cellphone = "";

	// 固定电话
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String telephone = "";

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
	public String position;

	// 存放图片
	@DatabaseField(dataType = DataType.STRING, columnDefinition = "TEXT")
	public String cover_images;

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
	public int negative_num;

	// 附件
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String attachment_ids;

	// 原网站 domain
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int domain_id;

	public List<Work> works = new ArrayList<>();

	public List<Case> cases = new ArrayList<>();

	public List<Resume> resumes = new ArrayList<>();

	public ServiceProvider() {}

	public ServiceProvider(String url) {
		super(url);
	}

	/**
	 *
	 */
	public void fullfill() {

		try {

			Dao dao_case = DaoManager.getDao(Case.class);
			Dao dao_work = DaoManager.getDao(Work.class);
			Dao dao_resume = DaoManager.getDao(Resume.class);

			this.cases.addAll((Collection<? extends Case>) dao_case.queryBuilder().where().eq("user_id", this.id).query());
			this.works.addAll((Collection<? extends Work>) dao_work.queryBuilder().where().eq("user_id", this.id).query());
			this.resumes.addAll((Collection<? extends Resume>) dao_resume.queryBuilder().where().eq("user_id", this.id).query());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param oldVersion
	 * @throws Exception
	 */
	public void createSnapshot(Model oldVersion) throws Exception {

		// 生成快照
		ServiceProviderSnapshot snapshot = new ServiceProviderSnapshot((ServiceProvider) oldVersion);

		// 保存快照
		snapshot.insert();
	}

	/**
	 * 通过ID查找数据
	 */
	public static ServiceProvider selectById(String id ){
		Dao dao = daoMap.get(ServiceProvider.class.getSimpleName());

		try {
			return (ServiceProvider) dao.queryForId(id);
		} catch (SQLException e) {
			return null;
		}
	}
}
