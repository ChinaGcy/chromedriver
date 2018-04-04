package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;

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
public abstract class Model implements JSONable<Model> {

	private static final Logger logger = LogManager.getLogger(Model.class.getName());

	public static Map<String, Dao> daoMap = new HashMap<>();

	static {

		for (Class<?> clazz : getModelClasses()) {
			try {
				Dao dao = OrmLiteDaoManager.getDao(clazz);
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
	@DatabaseField(dataType = DataType.DATE, index = true)
	public Date update_time = new Date();

	public Model() {}

	/**
	 * 
	 * @param url
	 */
	public Model(String url) {
		this.url = url;
		this.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(url));
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
	public boolean insert(){

		Dao dao = daoMap.get(this.getClass().getSimpleName());

		try {
			dao.create(this);
			return true;
		} catch (SQLException e) {
			System.out.println("update data !");
			try {
				dao.update(this);
				System.out.println("OK!");
				return true;
			} catch (SQLException e1) {
				logger.error("insert update error {}", e1);
				return false;
			}
		}
	}

	public static String rewriteBinaryUrl(String src) {

		Pattern p = Pattern.compile("(?<=href=\")[\\w\\d]{32}");
		Matcher m = p.matcher(src);

		while(m.find()) {
			src = src.replace(m.group(), "/binary/" + m.group());
		}

		return src;
	}

}
