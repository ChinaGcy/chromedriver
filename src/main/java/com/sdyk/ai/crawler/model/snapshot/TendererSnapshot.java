package com.sdyk.ai.crawler.model.snapshot;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.Tenderer;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.txt.StringUtil;

import java.lang.reflect.Field;

@DBName(value = "sdyk_raw_snapshot")
@DatabaseTable(tableName = "tenderer_snapshots")
public class TendererSnapshot extends Tenderer {

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32, unique = true)
	public String id_;

	/*@DatabaseField(dataType = DataType.STRING, width = 32)
	public String hash_id;*/

	public TendererSnapshot() {};

	/**
	 * 反射获取父类的属性值
	 * @param tenderer
	 */
	public TendererSnapshot(Tenderer tenderer) throws NoSuchFieldException, IllegalAccessException {

		Field[] fieldList = tenderer.getClass().getDeclaredFields();

		for(Field f : fieldList) {

			Field f_ = this.getClass().getField(f.getName());
			f_.set(this, f.get(tenderer));
		}

		this.id = StringUtil.byteArrayToHex(
				StringUtil.uuid(tenderer.url + " " + System.currentTimeMillis())
		);

		this.id_ = tenderer.id;

		this.url = tenderer.url;
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
				logger.error("TendererSnapShot update error", e1);
				return false;
			}

		}
	}
}
