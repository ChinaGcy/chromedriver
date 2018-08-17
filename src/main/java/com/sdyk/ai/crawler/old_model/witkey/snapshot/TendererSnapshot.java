package com.sdyk.ai.crawler.old_model.witkey.snapshot;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.old_model.witkey.Tenderer;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.txt.StringUtil;

import java.lang.reflect.Field;

@DBName(value = "sdyk_raw_snapshot_")
@DatabaseTable(tableName = "tenderers")
public class TendererSnapshot extends Tenderer {

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String id_;

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

	/**
	 * 插入数据库
	 * @return
	 */
	public boolean insert() {

		try {

			Dao dao = DaoManager.getDao(this.getClass());

			dao.create(this);

			//if(ESTransportClientAdapter.Enable_ES) ESTransportClientAdapter.updateOne(this.id, this);

			return true;
		}
		// 数据库连接问题
		catch (Exception e) {
			logger.error("Model {} Insert ERROR. ", this.toJSON(), e);
			return false;
		}
	}
}
