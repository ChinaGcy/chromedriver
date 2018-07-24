package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.es.ESTransportClientAdapter;
import one.rewind.db.DBName;

import java.sql.SQLException;

/**
 * 二进制文件
 * 网页下载资源 （文档/图片）
 */
@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "binaries")
public class Binary extends Model {

	// 资源名称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String file_name;

	// 资源的二进制数据
	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "MEDIUMBLOB")
	public byte[] src;

	// 资源格式
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String content_type;

	public Binary () {}

	public Binary(String url) {
		super(url);
	}

	/**
	 * 插入数据库
	 * @return
	 * @throws Exception
	 */
	public boolean insert() {

		Dao dao = daoMap.get(this.getClass().getSimpleName());

		try {

			dao.create(this);

			//if(ESTransportClientAdapter.Enable_ES) ESTransportClientAdapter.insertOne(this);

			return true;

		}  catch (SQLException e) {

			// 数据库中已经存在记录
			if(e.getCause().getMessage().contains("Duplicate")) {

				try {

					this.update();

					//if(ESTransportClientAdapter.Enable_ES) ESTransportClientAdapter.updateOne(this.id, this);

					return true;

				} catch (Exception ex) {
					logger.error("Update error {}", ex);
				}
			}
			// 可能是采集数据本事存在问题
			else {
				logger.error("Model {} Insert ERROR. ", this.toJSON(), e);
			}
		}
		return false;
	}
}
