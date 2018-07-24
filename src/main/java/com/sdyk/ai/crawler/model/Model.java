package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.sdyk.ai.crawler.es.ESTransportClientAdapter;
import com.sdyk.ai.crawler.model.witkey.Project;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import one.rewind.db.DaoManager;
import one.rewind.json.JSON;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public abstract class Model implements ESIndex {

	public static final Logger logger = LogManager.getLogger(Model.class.getName());

	public static Map<String, Dao> daoMap = new HashMap<>();

	static {

		for (Class<?> clazz : getModelClasses()) {
			try {
				Dao dao = DaoManager.getDao(clazz);

				logger.info("Add {} dao to daoMap.", clazz.getSimpleName());

				daoMap.put(clazz.getSimpleName(), dao);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Set<Class<? extends Model>> getModelClasses() {

		Reflections reflections = new Reflections(Model.class.getPackage().getName());

		Set<Class<? extends Model>> allClasses =
				reflections.getSubTypesOf(Model.class);

		return allClasses;

	}

	// UUID
	@DatabaseField(dataType = DataType.STRING, width = 32, id = true)
	public String id;

	// 当前网站url
	@DatabaseField(dataType = DataType.STRING, width = 1024, index = true)
	public String url;

	// 插入时间
	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	// 更新时间
	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();

	public Model() {}

	/**
	 * 
	 * @param url
	 */
	public Model(String url) {
		this.url = url;
		this.id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(url));
	}

	/**
	 *
	 * @return
	 */
	public String toJSON() {
		return JSON.toJson(this);
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean insert() {

		Dao dao = daoMap.get(this.getClass().getSimpleName());

		try {

			dao.create(this);

			if(ESTransportClientAdapter.Enable_ES) ESTransportClientAdapter.insertOne(this);

			return true;

		} catch (SQLException e) {

			// 数据库中已经存在记录
			if(e.getCause().getMessage().contains("Duplicate")) {

				try {

					this.update();

					if(ESTransportClientAdapter.Enable_ES) ESTransportClientAdapter.updateOne(this.id, this);

					return true;

				} catch (Exception ex) {
					logger.error("Update error {}", ex);
					return false;
				}
			}
			// 可能是采集数据本事存在问题
			else {
				logger.error("Model {} Insert ERROR. ", this.toJSON(), e);
				return false;
			}
		}
	}

	/**
	 *
	 * @return
	 */
	public boolean update(){

		Dao dao = daoMap.get(this.getClass().getSimpleName());

		try {
			dao.update(this);
			return true;
		} catch (SQLException e) {
			logger.error("Insert Update error {}", e);
			return false;
		}
	}

	/**
	 *
	 * @param src
	 * @return
	 */
	public static String rewriteBinaryUrl(String src) {

		if (src == null) {
			return src;
		}

		Pattern p = Pattern.compile("(?<=href=\")[\\w\\d]{32}");
		Matcher m = p.matcher(src);

		while(m.find()) {
			src = src.replace(m.group(), "/binary/" + m.group());
		}

		return src;
	}

	public String getId() {
		return this.id;
	}

	public void fullfill() {}

	/**
	 * 将新的数据赋值给已有数据
	 * @param model
	 */
	public void copy(Model model) throws Exception {

		if(!model.getClass().equals(this.getClass())) {
			throw new Exception("Can't copy fields from different model class.");
		}

		Field[] fieldList = model.getClass().getDeclaredFields();

		for(Field f : fieldList) {

			try {
				if (f.get(model) != null || f.get(model) != null && f.get(model) instanceof String && !f.get(model).equals("")) {

					Field f_ = this.getClass().getField(f.getName());
					f_.set(this, f.get(model));
				}
			} catch (Exception e) {
				logger.error("Error copy model field:{}. ", f.getName(), e);
			}
		}

	}
}
