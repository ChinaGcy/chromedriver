package com.sdyk.ai.crawler.old_model.witkey.snapshot;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.old_model.witkey.ServiceProvider;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.txt.StringUtil;

import java.lang.reflect.Field;

@DBName(value = "sdyk_raw_snapshot_")
@DatabaseTable(tableName = "service_providers")
public class ServiceProviderSnapshot extends ServiceProvider {

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String id_;

	public ServiceProviderSnapshot() {}

	/**
	 * 反射获取父类的属性值
	 * @param serviceProvider
	 */
	public ServiceProviderSnapshot(ServiceProvider serviceProvider) throws NoSuchFieldException, IllegalAccessException {

		Field[] fieldList = serviceProvider.getClass().getDeclaredFields();

		for(Field f : fieldList) {
			Field f_ = this.getClass().getField(f.getName());
			f_.set(this, f.get(serviceProvider));
		}

		this.id = StringUtil.byteArrayToHex(
				StringUtil.uuid(serviceProvider.url + " " + System.currentTimeMillis())
		);

		this.id_ = serviceProvider.id;

		this.url = serviceProvider.url;

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
