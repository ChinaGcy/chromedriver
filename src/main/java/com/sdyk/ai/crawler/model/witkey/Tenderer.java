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

	// 版本号
	@DatabaseField(dataType = DataType.INTEGER, width = 4)
	public int version_num;

	public Tenderer() {}

	public Tenderer(String url) {
		super(url);
	}

	/**
	 *
	 * @return
	 */
	public boolean insert() {

		try {

			Dao dao = DaoManager.getDao(this.getClass());

			this.version_num = 1;

			dao.create(this);

			if(ESTransportClientAdapter.Enable_ES) ESTransportClientAdapter.updateOne(this.id, this);

			return true;
		}
		catch (SQLException e) {

			// 数据库中已经存在记录
			if(e.getCause().getMessage().contains("Duplicate")) {

				try {

					Dao dao = DaoManager.getDao(this.getClass());

					Tenderer tenderer = (Tenderer) dao.queryForId(this.id);

					// 数据发生变化
					if( !judgeEquale(tenderer, this) ){

						this.insert_time = new Date();

						this.update_time = new Date();

						this.version_num = tenderer.version_num + 1;

						TendererSnapshot tendererSnapshot = new TendererSnapshot(this);

						tendererSnapshot.insert();

						this.update();

					}

					return true;
				} catch (Exception ex) {
					logger.error("Error insert snapshot. ", ex);
				}

				return false;
			}
			// 可能是采集数据本事存在问题
			else {
				logger.error("Model {} Insert ERROR. ", this.toJSON(), e);
				return false;
			}
		}
		// 数据库连接问题
		catch (Exception e) {
			logger.error("Model {} Insert ERROR. ", this.toJSON(), e);
			return false;
		}
	}

	public boolean judgeEquale(Tenderer oldTenderer, Tenderer newTenderer) {

		newTenderer.insert_time = oldTenderer.insert_time;
		newTenderer.update_time = oldTenderer.update_time;
		newTenderer.version_num = oldTenderer.version_num;

		return one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(oldTenderer.toJSON()))
				.equals( one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid(newTenderer.toJSON())) );
	}
}



