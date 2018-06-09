package com.sdyk.ai.crawler.model.snapshot;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.model.ServiceProvider;
import com.sdyk.ai.crawler.model.Tenderer;
import one.rewind.db.DBName;
import one.rewind.txt.StringUtil;

import java.lang.reflect.Field;

@DBName(value = "sdyk_raw_snapshot")
@DatabaseTable(tableName = "service_provider_snapshots")
public class ServiceProviderSnapshot extends ServiceProvider{

	@DatabaseField(dataType = DataType.STRING, width = 32, unique = true)
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
				StringUtil.uuid(serviceProvider.url + " " + serviceProvider.insert_time.getTime())
		);

		this.id_ = serviceProvider.id;
	}
}
