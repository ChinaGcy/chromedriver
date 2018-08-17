package com.sdyk.ai.crawler.util;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.model.*;

import com.sdyk.ai.crawler.model.company.CompanyFinancing;
import com.sdyk.ai.crawler.model.company.CompanyInformation;
import com.sdyk.ai.crawler.model.witkey.*;
import com.sdyk.ai.crawler.model.witkey.snapshot.ProjectSnapshot;
import com.sdyk.ai.crawler.model.witkey.snapshot.ServiceProviderSnapshot;
import com.sdyk.ai.crawler.model.witkey.snapshot.TendererSnapshot;
import one.rewind.db.DaoManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.rewind.db.Refacter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class DBUtil {

	private static final Logger logger = LogManager.getLogger(Model.class.getName());

	/**
	 * 重新构建数据库
	 */
	public static void createTables() {

		for (Class clzss : Model.getModelClasses()) {

			try {
				Refacter.dropTable(clzss);
				Refacter.createTable(clzss);
			} catch (Exception e) {
				logger.error("Error create table, ", e);
			}
		}
	}

	public static void convertData() throws Exception {

		// old model -> new model
		Map<Class<? extends Model>, Class<? extends Model>> classMap = new HashMap();

		for(Class<? extends Model> clazz : classMap.keySet()){

			Dao oldModelDao = DaoManager.getDao(clazz);
			Dao newModelDao = DaoManager.getDao(classMap.get(clazz));

			List<Model> oldModels = oldModelDao.queryForAll();

			for(Model model : oldModels) {

				Map<String, Object> map = ReflectModelUtil.toMap(model);
				Map<String, Object> newMap = new HashMap<>();
				newMap.putAll(map);

				for(String key : map.keySet()) {
					if(key.matches("tags|attachment_ids")) {
						newMap.put(key, StringUtil.strToList((String) map.get(key)));
					}
				}



			}

		}




	}

	/**
	 * project 数据信息重构
	 *
	 * @throws Exception
	 */
	public static void projectConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.Project.class);
		Dao dao1 = DaoManager.getDao(Project.class);

		List<com.sdyk.ai.crawler.old_model.witkey.Project> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.witkey.Project project_old : list_old) {

			Project project = new Project(project_old.url);

			Field[] fieldList = project_old.getClass().getDeclaredFields();

			for(Field f : fieldList) {

				Field f_ = project.getClass().getField(f.getName());
				if (f.getName().contains("tags")){

					try {
						f_.set(project, StringUtil.strToList( (String) f.get(project_old) ));
					} catch (NullPointerException e) {
						continue;
					}

				}
				else if (f.getName().contains("attachment_ids")) {

					try {

						f_.set(project, StringUtil.strToList( (String) f.get(project_old) ));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else {
					f_.set(project, f.get(project_old));
				}
			}

			dao1.create(project);
		}
	}

	/**
	 *
	 */
	public static void tendererConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.Tenderer.class);
		Dao dao1 = DaoManager.getDao(Tenderer.class);

		List<com.sdyk.ai.crawler.old_model.witkey.Tenderer> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.witkey.Tenderer tenderer_old : list_old) {

			Tenderer tenderer = new Tenderer(tenderer_old.url);

			Field[] fieldList = tenderer_old.getClass().getDeclaredFields();

			for(Field f : fieldList) {

				Field f_ = tenderer.getClass().getField(f.getName());

				if (f.getName().contains("platform_certification")){

					try {
						f_.set(tenderer, StringUtil.strToList( (String) f.get(tenderer_old) ));
					}catch (NullPointerException e) {
						continue;
					}

				}
				else if (f.getName().contains("location")) {
					LocationParser parser = LocationParser.getInstance();

					try {
						String location = parser.matchLocation((String) f.get(tenderer_old)).size() > 0 ? parser.matchLocation((String) f.get(tenderer_old)).get(0).toString() : null;
						f_.set(tenderer, location);

					} catch (NullPointerException e) {
						continue;
					}
				}
				else if (f.getName().contains("attachment_ids")) {

					try {
						f_.set(tenderer, StringUtil.strToList( (String) f.get(tenderer_old) ));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else {
					f_.set(tenderer, f.get(tenderer_old));
				}
			}

			dao1.create(tenderer);
		}

	}

	/**
	 *
	 */
	public static void serviceproviderConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.ServiceProvider.class);
		Dao dao1 = DaoManager.getDao(ServiceProvider.class);

		List<com.sdyk.ai.crawler.old_model.witkey.ServiceProvider> list_old = dao.queryForAll();

		// 61 category 需要清洗
		for (com.sdyk.ai.crawler.old_model.witkey.ServiceProvider serviceProvider_old : list_old) {

			ServiceProvider serviceProvider = new ServiceProvider(serviceProvider_old.url);

			Field[] fieldList = serviceProvider_old.getClass().getDeclaredFields();

			// 遍历字段
			for(Field f : fieldList) {

				Field f_ = serviceProvider.getClass().getField(f.getName());

				if (f.getName().contains("tags")){

					try {
						f_.set(serviceProvider, StringUtil.strToList(( (String) f.get(serviceProvider_old)).replace(" ", "") ));
					}catch (NullPointerException e) {
						continue;
					}

				}
				else if (f.getName().contains("attachment_ids")) {

					try {

						f_.set(serviceProvider, StringUtil.strToList( (String) f.get(serviceProvider_old) ));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else if (f.getName().contains("cover_images")) {

					try {

						f_.set(serviceProvider, StringUtil.strToList( (String) f.get(serviceProvider_old) ));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else if (f.getName().contains("platform_certification")) {

					try {
						f_.set(serviceProvider, StringUtil.strToList( (String) f.get(serviceProvider_old) ));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else if (f.getName().contains("catgory")) {

					try {
						f_.set(serviceProvider, ((String) f.get(serviceProvider_old)).replace(" ", ""));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else {
					f_.set(serviceProvider, f.get(serviceProvider_old));
				}
			}

			dao1.create(serviceProvider);
		}

	}

	/**
	 *
	 */
	public static void serviceProviderRatingConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.ServiceProviderRating.class);
		Dao dao1 = DaoManager.getDao(ServiceProviderRating.class);

		List<com.sdyk.ai.crawler.old_model.witkey.ServiceProviderRating> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.witkey.ServiceProviderRating serviceProviderRating_old : list_old) {

			ServiceProviderRating serviceProviderRating = new ServiceProviderRating(serviceProviderRating_old.url);

			Field[] fieldList = serviceProviderRating_old.getClass().getDeclaredFields();

			// 遍历字段
			for(Field f : fieldList) {

				Field f_ = serviceProviderRating.getClass().getField(f.getName());

				// 62 为“ ” ，61需修改
				if (f.getName().contains("tags")){

					try {
						f_.set(serviceProviderRating, StringUtil.strToList( (String) f.get(serviceProviderRating_old) ));
					}catch (NullPointerException e) {
						continue;
					}

				}
				else {
					f_.set(serviceProviderRating, f.get(serviceProviderRating_old));
				}
			}

			dao1.create(serviceProviderRating);
		}
	}

	/**
	 *
	 */
	public static void tendererRatingConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.TendererRating.class);
		Dao dao1 = DaoManager.getDao(TendererRating.class);

		List<com.sdyk.ai.crawler.old_model.witkey.TendererRating> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.witkey.TendererRating TendererRating_old : list_old) {

			TendererRating tendererRating = new TendererRating(TendererRating_old.url);

			Field[] fieldList = TendererRating_old.getClass().getDeclaredFields();

			// 遍历字段
			for(Field f : fieldList) {

				Field f_ = tendererRating.getClass().getField(f.getName());

				// 62 为“ ” ，61需修改
				if (f.getName().contains("tags")){

					try {
						f_.set(tendererRating, StringUtil.strToList( (String) f.get(TendererRating_old) ));
					}catch (NullPointerException e) {
						continue;
					}

				}
				else {
					f_.set(tendererRating, f.get(TendererRating_old));
				}
			}

			dao1.create(tendererRating);
		}
	}

	/**
	 *
	 */
	public static void CaseConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.Case.class);
		Dao dao1 = DaoManager.getDao(Case.class);

		List<com.sdyk.ai.crawler.old_model.witkey.Case> list_old = dao.queryForAll();
		int i = 0;

		for (com.sdyk.ai.crawler.old_model.witkey.Case case_old : list_old) {

			logger.info("sum: {} , new: {}", list_old.size(), ++i);
			Case case_ = new Case(case_old.url);

			Field[] fieldList = case_old.getClass().getDeclaredFields();

			// 遍历字段
			for (Field f : fieldList) {

				Field f_ = case_.getClass().getField(f.getName());

				// 62 为“ ” ，61需修改
				if (f.getName().contains("tags")) {

					try {
						f_.set(case_, StringUtil.strToList( (String) f.get(case_old) ));
					} catch (NullPointerException e) {
						continue;
					}

				}
				else if (f.getName().contains("attachment_ids")) {

					try {
						f_.set(case_, StringUtil.strToList( (String) f.get(case_old) ));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else {
					f_.set(case_, f.get(case_old));
				}
			}

			dao1.create(case_);
		}
	}

	/**
	 *
	 */
	public static void workConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.Work.class);
		Dao dao1 = DaoManager.getDao(Work.class);
		int i = 0;
		List<com.sdyk.ai.crawler.old_model.witkey.Work> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.witkey.Work work_old : list_old) {

			logger.info("sum: {} , new: {}", list_old.size(), ++i);

			Work work = new Work(work_old.url);

			Field[] fieldList = work_old.getClass().getDeclaredFields();

			// 遍历字段
			for (Field f : fieldList) {

				Field f_ = work.getClass().getField(f.getName());

				// 62 为“ ” ，61需修改
				if (f.getName().contains("tags")) {

					try {
						f_.set(work, StringUtil.strToList( (String) f.get(work_old) ));
					} catch (NullPointerException e) {
						continue;
					}

				}
				else if (f.getName().contains("attachment_ids")) {

					try {

						f_.set(work, StringUtil.strToList( (String) f.get(work_old) ));
					}catch (NullPointerException e) {
						continue;
					}
				}
				else {
					f_.set(work, f.get(work_old));
				}
			}

			dao1.create(work);
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public static void projectSnapshotConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.snapshot.ProjectSnapshot.class);
		Dao dao1 = DaoManager.getDao(ProjectSnapshot.class);

		LocationParser parser = LocationParser.getInstance();

		int i = 0;
		List<com.sdyk.ai.crawler.old_model.witkey.snapshot.ProjectSnapshot> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.witkey.snapshot.ProjectSnapshot projectSnapshot_old : list_old) {

			logger.info("sum: {} , new: {}", list_old.size(), ++i);
			ProjectSnapshot projectSnapshot = new ProjectSnapshot();

			projectSnapshot.id = projectSnapshot_old.id;
			projectSnapshot.url = projectSnapshot_old.url;
			projectSnapshot.id_ = projectSnapshot_old.id_;
			projectSnapshot.domain_id = projectSnapshot_old.domain_id;
			projectSnapshot.title = projectSnapshot_old.title;
			projectSnapshot.origin_id = projectSnapshot_old.origin_id;
			try {
				projectSnapshot.location = parser.matchLocation(projectSnapshot_old.location).size() > 0 ? parser.matchLocation(projectSnapshot_old.location).get(0).toString() : null;
			} catch (NullPointerException e) {}
			projectSnapshot.origin_from = projectSnapshot_old.origin_from;
			projectSnapshot.category = projectSnapshot_old.category;
			try {
				projectSnapshot.tags = StringUtil.strToList( projectSnapshot_old.tags );
			} catch (NullPointerException e) {
			}
			projectSnapshot.content = projectSnapshot_old.content;
			projectSnapshot.budget_ub = projectSnapshot_old.budget_ub;
			projectSnapshot.budget_lb = projectSnapshot_old.budget_lb;
			projectSnapshot.time_limit = projectSnapshot_old.time_limit;
			projectSnapshot.trade_type = projectSnapshot_old.trade_type;
			projectSnapshot.due_time = projectSnapshot_old.due_time;
			projectSnapshot.pubdate = projectSnapshot_old.pubdate;
			projectSnapshot.bidder_total_num = projectSnapshot_old.bidder_total_num;
			projectSnapshot.bids_available = projectSnapshot_old.bids_available;
			projectSnapshot.bids_num = projectSnapshot_old.bids_num;
			projectSnapshot.tenderer_id = projectSnapshot_old.tenderer_id;
			projectSnapshot.tenderer_name = projectSnapshot_old.tenderer_name;
			projectSnapshot.status = projectSnapshot_old.status;
			projectSnapshot.type = projectSnapshot_old.type;
			projectSnapshot.rate_num = projectSnapshot_old.rate_num;
			projectSnapshot.reward_type = projectSnapshot_old.reward_type;
			projectSnapshot.view_num = projectSnapshot_old.view_num;
			projectSnapshot.fav_num = projectSnapshot_old.fav_num;
			projectSnapshot.like_num = projectSnapshot_old.like_num;
			projectSnapshot.cellphone = projectSnapshot_old.cellphone;
			projectSnapshot.rcmd_num = projectSnapshot_old.rcmd_num;
			projectSnapshot.delivery_steps = projectSnapshot_old.delivery_steps;
			try {
				projectSnapshot.attachment_ids = StringUtil.strToList( projectSnapshot_old.attachment_ids );
			} catch (NullPointerException e) {
			}
			projectSnapshot.insert_time = projectSnapshot_old.insert_time;
			projectSnapshot.update_time = projectSnapshot_old.update_time;

			dao1.create(projectSnapshot);
		}
	}


	/**
	 *
	 * @throws Exception
	 */
	public static void serviceProviderSnapshotConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.snapshot.ServiceProviderSnapshot.class);
		Dao dao1 = DaoManager.getDao(ServiceProviderSnapshot.class);

		LocationParser parser = LocationParser.getInstance();

		List<com.sdyk.ai.crawler.old_model.witkey.snapshot.ServiceProviderSnapshot> list_old = dao.queryForAll();

		int i = 0;
		for (com.sdyk.ai.crawler.old_model.witkey.snapshot.ServiceProviderSnapshot serviceProviderSnapshot_old : list_old) {
			logger.info("sum: {} , new: {}", list_old.size(), ++i);
			ServiceProviderSnapshot serviceProviderSnapshot = new ServiceProviderSnapshot();

			serviceProviderSnapshot.id = serviceProviderSnapshot_old.id;
			serviceProviderSnapshot.url = serviceProviderSnapshot_old.url;
			serviceProviderSnapshot.id_ = serviceProviderSnapshot_old.id_;
			serviceProviderSnapshot.origin_id = serviceProviderSnapshot_old.origin_id;
			serviceProviderSnapshot.name = serviceProviderSnapshot_old.name;
			serviceProviderSnapshot.head_portrait = serviceProviderSnapshot_old.head_portrait;
			try{
				serviceProviderSnapshot.platform_certification = StringUtil.strToList( serviceProviderSnapshot_old.platform_certification );
			} catch (NullPointerException e) {}
			serviceProviderSnapshot.company_name = serviceProviderSnapshot_old.company_name;
			serviceProviderSnapshot.company_address = serviceProviderSnapshot_old.company_address;
			serviceProviderSnapshot.company_website = serviceProviderSnapshot_old.company_website;
			serviceProviderSnapshot.type = serviceProviderSnapshot_old.type;
			try {
				serviceProviderSnapshot.location = parser.matchLocation(serviceProviderSnapshot_old.location).size() > 0 ? parser.matchLocation(serviceProviderSnapshot_old.location).get(0).toString() : null;
			} catch (NullPointerException e) {}
			serviceProviderSnapshot.content = serviceProviderSnapshot_old.content;
			serviceProviderSnapshot.team_size = serviceProviderSnapshot_old.team_size;
			serviceProviderSnapshot.work_experience = serviceProviderSnapshot_old.work_experience;
			try {
				serviceProviderSnapshot.category = serviceProviderSnapshot_old.category.replace(" ", "");
			} catch (NullPointerException e) {}
			try {
				serviceProviderSnapshot.tags = StringUtil.strToList( serviceProviderSnapshot_old.platform_certification );
			} catch (NullPointerException e) {}
			serviceProviderSnapshot.service_attitude = serviceProviderSnapshot_old.service_attitude;
			serviceProviderSnapshot.service_speed = serviceProviderSnapshot_old.service_speed;
			serviceProviderSnapshot.service_quality = serviceProviderSnapshot_old.service_quality;
			serviceProviderSnapshot.project_num = serviceProviderSnapshot_old.project_num;
			serviceProviderSnapshot.success_ratio = serviceProviderSnapshot_old.success_ratio;
			serviceProviderSnapshot.income = serviceProviderSnapshot_old.income;
			serviceProviderSnapshot.pending_txn = serviceProviderSnapshot_old.pending_txn;
			serviceProviderSnapshot.register_time = serviceProviderSnapshot_old.register_time;
			serviceProviderSnapshot.grade = serviceProviderSnapshot_old.grade;
			serviceProviderSnapshot.credit = serviceProviderSnapshot_old.credit;
			serviceProviderSnapshot.cellphone = serviceProviderSnapshot_old.cellphone;
			serviceProviderSnapshot.telephone = serviceProviderSnapshot_old.telephone;
			serviceProviderSnapshot.qq = serviceProviderSnapshot_old.qq;
			serviceProviderSnapshot.weixin = serviceProviderSnapshot_old.weixin;
			serviceProviderSnapshot.email = serviceProviderSnapshot_old.email;
			serviceProviderSnapshot.weibo = serviceProviderSnapshot_old.weibo;
			serviceProviderSnapshot.wangwang = serviceProviderSnapshot_old.wangwang;
			serviceProviderSnapshot.price_per_day = serviceProviderSnapshot_old.price_per_day;
			serviceProviderSnapshot.price_per_project = serviceProviderSnapshot_old.price_per_project;
			serviceProviderSnapshot.fav_num = serviceProviderSnapshot_old.fav_num;
			serviceProviderSnapshot.fan_num = serviceProviderSnapshot_old.fan_num;
			serviceProviderSnapshot.like_num = serviceProviderSnapshot_old.like_num;
			serviceProviderSnapshot.view_num = serviceProviderSnapshot_old.view_num;
			serviceProviderSnapshot.position = serviceProviderSnapshot_old.position;
			try {
				serviceProviderSnapshot.cover_images = StringUtil.strToList( serviceProviderSnapshot_old.cover_images );
		} catch (NullPointerException e) {}
			serviceProviderSnapshot.rating = serviceProviderSnapshot_old.rating;
			serviceProviderSnapshot.rcmd_num = serviceProviderSnapshot_old.rcmd_num;
			serviceProviderSnapshot.rating_num = serviceProviderSnapshot_old.rating_num;
			serviceProviderSnapshot.praise_num = serviceProviderSnapshot_old.praise_num;
			serviceProviderSnapshot.negative_num = serviceProviderSnapshot_old.negative_num;
			try {
				serviceProviderSnapshot.attachment_ids = StringUtil.strToList( serviceProviderSnapshot_old.attachment_ids );
			} catch (NullPointerException e) {}
			serviceProviderSnapshot.domain_id = serviceProviderSnapshot_old.domain_id;
			serviceProviderSnapshot.insert_time = serviceProviderSnapshot_old.insert_time;
			serviceProviderSnapshot.update_time = serviceProviderSnapshot_old.update_time;


			dao1.create(serviceProviderSnapshot);
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public static void tendererSnapshotConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.witkey.snapshot.TendererSnapshot.class);
		Dao dao1 = DaoManager.getDao(TendererSnapshot.class);

		LocationParser parser = LocationParser.getInstance();

		List<com.sdyk.ai.crawler.old_model.witkey.snapshot.TendererSnapshot> list_old = dao.queryForAll();

		int i = 0;
		for (com.sdyk.ai.crawler.old_model.witkey.snapshot.TendererSnapshot tendererSnapshot_old : list_old) {
			logger.info("sum: {} , new: {}", list_old.size(), ++i);
			TendererSnapshot tendererSnapshot = new TendererSnapshot();

			tendererSnapshot.id = tendererSnapshot_old.id;
			tendererSnapshot.url = tendererSnapshot_old.url;
			tendererSnapshot.id_ = tendererSnapshot_old.id_;
			tendererSnapshot.origin_id = tendererSnapshot_old.origin_id;
			tendererSnapshot.name = tendererSnapshot_old.name;
			tendererSnapshot.head_portrait = tendererSnapshot_old.head_portrait;
			try {
				tendererSnapshot.location = parser.matchLocation(tendererSnapshot_old.location).size() > 0 ? parser.matchLocation(tendererSnapshot_old.location).get(0).toString() : null;
			} catch (NullPointerException e) {}
			tendererSnapshot.login_time = tendererSnapshot_old.login_time;
			tendererSnapshot.trade_num = tendererSnapshot_old.trade_num;
			tendererSnapshot.category = tendererSnapshot_old.category;
			tendererSnapshot.tender_type = tendererSnapshot_old.tender_type;
			tendererSnapshot.company_scale = tendererSnapshot_old.company_scale;
			tendererSnapshot.content = tendererSnapshot_old.content;
			tendererSnapshot.req_forecast = tendererSnapshot_old.req_forecast;
			tendererSnapshot.total_spending = tendererSnapshot_old.total_spending;
			tendererSnapshot.total_project_num = tendererSnapshot_old.total_project_num;
			tendererSnapshot.total_employees = tendererSnapshot_old.total_employees;
			tendererSnapshot.register_time = tendererSnapshot_old.register_time;
			tendererSnapshot.praise_num = tendererSnapshot_old.praise_num;
			tendererSnapshot.rating_num = tendererSnapshot_old.rating_num;
			tendererSnapshot.selection_ratio = tendererSnapshot_old.selection_ratio;
			tendererSnapshot.success_ratio = tendererSnapshot_old.success_ratio;
			tendererSnapshot.grade = tendererSnapshot_old.grade;
			tendererSnapshot.company_name = tendererSnapshot_old.company_name;
			try {
				tendererSnapshot.platform_certification = StringUtil.strToList( tendererSnapshot_old.platform_certification );
			} catch (NullPointerException e) {}
			tendererSnapshot.credit = tendererSnapshot_old.credit;
			tendererSnapshot.insert_time = tendererSnapshot_old.insert_time;
			tendererSnapshot.update_time = tendererSnapshot_old.update_time;
			try {
				tendererSnapshot.attachment_ids = StringUtil.strToList( tendererSnapshot_old.attachment_ids );
			} catch (NullPointerException e) {}
			tendererSnapshot.domain_id = tendererSnapshot_old.domain_id;


			dao1.create(tendererSnapshot);
		}
	}

	/**
	 *  61 执行
	 * @throws Exception
	 */
	public static void companyFinancingConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.company.CompanyFinancing.class);
		Dao dao1 = DaoManager.getDao(CompanyFinancing.class);

		int i = 0;
		List<com.sdyk.ai.crawler.old_model.company.CompanyFinancing> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.company.CompanyFinancing companyFinancing_old : list_old) {

			logger.info("sum: {} , new: {}", list_old.size(), ++i);

			CompanyFinancing companyFinancing = new CompanyFinancing(companyFinancing_old.url);

			Field[] fieldList = companyFinancing_old.getClass().getDeclaredFields();

			// 遍历字段
			for (Field f : fieldList) {

				Field f_ = companyFinancing.getClass().getField(f.getName());

				if (f.getName().contains("name")){

					try {
						f_.set(companyFinancing, StringUtil.strToList( (String) f.get(companyFinancing_old)));
					}catch (NullPointerException e) {
						continue;
					}

				}
				else {
					f_.set(companyFinancing, f.get(companyFinancing_old));
				}
			}

			dao1.create(companyFinancing);
		}
	}

	/**
	 *  61 执行
	 * @throws Exception
	 */
	public static void companyInformationConvert() throws Exception {

		Dao dao = DaoManager.getDao(com.sdyk.ai.crawler.old_model.company.CompanyInformation.class);
		Dao dao1 = DaoManager.getDao(CompanyInformation.class);

		int i = 0;

		List<com.sdyk.ai.crawler.old_model.company.CompanyInformation> list_old = dao.queryForAll();

		for (com.sdyk.ai.crawler.old_model.company.CompanyInformation companyInformation_old : list_old) {

			logger.info("sum: {} , new: {}", list_old.size(), ++i);

			CompanyInformation companyInformation = new CompanyInformation(companyInformation_old.url);

			Field[] fieldList = companyInformation_old.getClass().getDeclaredFields();

			// 遍历字段
			for (Field f : fieldList) {

				Field f_ = companyInformation.getClass().getField(f.getName());

				if (f.getName().contains("tags")){

					try {

						f_.set(companyInformation, StringUtil.strToList( (String) f.get(companyInformation_old)));
					}catch (NullPointerException e) {
						continue;
					}

				}
				else if (f.getName().contains("competing_company_ids")){

					try {
						f_.set(companyInformation, StringUtil.strToList( (String) f.get(companyInformation_old)));
					}catch (NullPointerException e) {
						continue;
					}

				}
				else {
					f_.set(companyInformation, f.get(companyInformation_old));
				}
			}

			dao1.create(companyInformation);
		}
	}

}
