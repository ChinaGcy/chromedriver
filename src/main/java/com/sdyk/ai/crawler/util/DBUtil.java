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

		classMap.put(com.sdyk.ai.crawler.old_model.witkey.Project.class, Project.class);

		classMap.put(com.sdyk.ai.crawler.old_model.witkey.Tenderer.class, Tenderer.class);
		classMap.put(com.sdyk.ai.crawler.old_model.witkey.ServiceProvider.class, ServiceProvider.class);
		classMap.put(com.sdyk.ai.crawler.old_model.witkey.TendererRating.class, TendererRating.class);
		classMap.put(com.sdyk.ai.crawler.old_model.witkey.ServiceProviderRating.class, ServiceProviderRating.class);

		//classMap.put(com.sdyk.ai.crawler.old_model.witkey.Work.class, Work.class);

		classMap.put(com.sdyk.ai.crawler.old_model.witkey.Case.class, Case.class);

		classMap.put(com.sdyk.ai.crawler.old_model.witkey.snapshot.TendererSnapshot.class, TendererSnapshot.class);
		classMap.put(com.sdyk.ai.crawler.old_model.witkey.snapshot.ProjectSnapshot.class, ProjectSnapshot.class);
		//classMap.put(com.sdyk.ai.crawler.old_model.witkey.snapshot.ServiceProviderSnapshot.class, ServiceProviderSnapshot.class);
		classMap.put(com.sdyk.ai.crawler.old_model.company.CompanyFinancing.class, CompanyFinancing.class);
		classMap.put(com.sdyk.ai.crawler.old_model.company.CompanyInformation.class, CompanyInformation.class);

		for(Class<? extends Model> clazz : classMap.keySet()){

			Dao oldModelDao = DaoManager.getDao(clazz);
			Dao newModelDao = DaoManager.getDao(classMap.get(clazz));

			List<Model> oldModels = oldModelDao.queryForAll();
			int i = 0;

			for(Model model : oldModels) {

				Map<String, Object> map = ReflectModelUtil.toMap(model);
				Map<String, Object> newMap = new HashMap<>();
				newMap.putAll(map);

				for(String key : map.keySet()) {

					if(key.matches("tags|attachment_ids|cover_images|investors|platform_certification")) {
						newMap.put(key, StringUtil.strToList((String) map.get(key)));
					}
					else if(key.matches("category")) {
						if(map.get(key) != null)
							newMap.put(key, ((String) map.get(key)).replaceAll(" ", ""));
					}
					else if(key.matches("location")) {

						if(map.get(key) != null) {

							List<? extends LocationParser.Location> new_locations = LocationParser.getInstance().matchLocation((String) map.get(key));

							if (new_locations.size() > 0) {
								newMap.put(key, new_locations.get(0).toString());
							}
						}
					}
				}

				Model newModel = (Model) ReflectModelUtil.toObj(newMap, classMap.get(clazz));
				newModelDao.create(newModel);
				logger.info("Class: {}, sum:{}, new:{}", model.getClass().getSimpleName(), oldModels.size(), ++i);

			}
		}

	}
}
