package com.sdyk.ai.crawler.model.snapshot;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Project;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.txt.StringUtil;

import java.lang.reflect.Field;

@DBName(value = "sdyk_raw_snapshot")
@DatabaseTable(tableName = "project_snapshots")
public class ProjectSnapshot extends Project {

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32, unique = true)
	public String id_;

	// project信息hash值
	/*@DatabaseField(dataType = DataType.STRING, width = 32)
	public String hash_id;*/

	public ProjectSnapshot() {};

	/**
	 * 反射获取父类的属性值
	 * @param project
	 */
	public ProjectSnapshot(Project project) throws NoSuchFieldException, IllegalAccessException {

		Field[] fieldList = project.getClass().getDeclaredFields();

		for(Field f : fieldList) {
			Field f_ = this.getClass().getField(f.getName());
			f_.set(this, f.get(project));
		}

		this.id = StringUtil.byteArrayToHex(
			StringUtil.uuid(project.url + " " + System.currentTimeMillis())
		);

		this.id_ = project.id;
		this.url = project.url;

	}

	public boolean insert() {

		Dao dao = null;
		try {
			dao = DaoManager.getDao(this.getClass());
			dao.create(this);
			return true;

		} catch (Exception e) {
			try {
				dao.update(this);
				return true;
			} catch (Exception e1) {
				logger.error("ProjectSnapShot update error", e1);
				return false;
			}

		}
	}
}
