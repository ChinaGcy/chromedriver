package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.sdyk.ai.crawler.es.ESTransportClientAdapter;
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
public abstract class Model {

	public static final Logger logger = LogManager.getLogger(Model.class.getName());

	public static Map<String, Dao> daoMap = new HashMap<>();

	public static boolean ES_Index = false;

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

	/**
	 *
	 * @return
	 */
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

			Model oldVersion = (Model) dao.queryForId(id);

			// 没有旧版本
			if(oldVersion == null) {
				dao.create(this);
				createSnapshot(this);
				insertES();
			}
			// 存在旧版本
			else {

				if(diff(oldVersion)) {

					oldVersion.copy(this);
					oldVersion.update();

					createSnapshot(oldVersion);
					oldVersion.updateES();
				}
			}

			return true;
		}
		catch (SQLException e) {
			logger.error("Model {} insert error. ", this.toJSON(), e);
		}
		catch (Exception e) {
			logger.error(e);
		}

		return false;
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
	 */
	public void insertES() {

		this.fullfill();

		if(ESTransportClientAdapter.Enable_ES && ESTransportClientAdapter.clazzes.contains(this.getClass())){
			ESTransportClientAdapter.insertOne(this);
		}
	}

	/**
	 *
	 */
	public void updateES() {

		this.fullfill();

		if(ESTransportClientAdapter.Enable_ES && ESTransportClientAdapter.clazzes.contains(this.getClass())){
			ESTransportClientAdapter.updateOne(this);
		}
	}

	public void fullfill() {

	}

	public void createSnapshot(Model oldVersion) throws Exception {
	}

	/**
	 * 将HTML类型内容字段中 图片/附件标签的src属性 转换为本地路由
	 * @author gcy116149@gmail.com
	 * @date 2018/7/25
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

	/**
	 *
	 * @param model
	 * @return
	 * @throws Exception
	 */
	public boolean diff(Model model) throws Exception {

		if(!model.getClass().equals(this.getClass())) {
			throw new Exception("Model class is not equal.");
		}

		Field[] fieldList = model.getClass().getDeclaredFields();

		for(Field f : fieldList) {

			try {
				if ((f.get(model) != null || f.get(model) != null && f.get(model) instanceof String && !f.get(model).equals(""))
						&& !f.getName().equals("insert_time")
						&& !f.getName().equals("update_time")
						&& !f.getName().equals("budget")) {

					if( !String.valueOf(f.get(model)).equals(String.valueOf(f.get(this))) ){
						return true;
					}
				}
			} catch (Exception e) {
				logger.error("Error copy model field:{}. ", f.getName(), e);
			}
		}

		return false;
	}

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
