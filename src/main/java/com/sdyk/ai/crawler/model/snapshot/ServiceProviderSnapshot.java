package com.sdyk.ai.crawler.model.snapshot;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.ServiceProvider;
import com.sdyk.ai.crawler.model.Tenderer;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.txt.StringUtil;

import java.lang.reflect.Field;

@DBName(value = "sdyk_raw_snapshot")
@DatabaseTable(tableName = "service_provider_snapshots")
public class ServiceProviderSnapshot extends ServiceProvider{

	// 原网站id
	@DatabaseField(dataType = DataType.STRING, width = 32, unique = true)
	public String id_;

	/*@DatabaseField(dataType = DataType.STRING, width = 32, unique = true)
	public String hash_id;*/

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
				logger.error("ServiceProviderSnapShot update error", e1);
				return false;
			}

		}
	}
}
