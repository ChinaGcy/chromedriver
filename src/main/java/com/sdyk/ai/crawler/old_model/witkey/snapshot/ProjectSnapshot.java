package com.sdyk.ai.crawler.old_model.witkey.snapshot;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.old_model.witkey.Project;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.txt.StringUtil;

import java.lang.reflect.Field;

@DBName(value = "sdyk_raw_snapshot_")
@DatabaseTable(tableName = "projects")
public class ProjectSnapshot extends Project {

	// 原数据 id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String id_;

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

	/**
	 * 插入数据库
	 * @return
	 */
	public boolean insert() {

		try {

			Dao dao = DaoManager.getDao(this.getClass());

			dao.create(this);

			return true;
		}
		// 数据库连接问题
		catch (Exception e) {
			logger.error("Model {} Insert ERROR. ", this.toJSON(), e);
			return false;
		}
	}
}
