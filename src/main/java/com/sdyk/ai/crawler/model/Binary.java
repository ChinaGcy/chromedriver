package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;

import java.sql.SQLException;

/**
 * 二进制文件
 * 网页下载资源 （文档/图片）
 */
@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "binaries")
public class Binary extends com.sdyk.ai.crawler.model.Model {

	// 资源名称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String file_name;

	// 资源的二进制数据
	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "MEDIUMBLOB")
	public byte[] src;

	// 资源格式
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String content_type;

	// 文件大小
	@DatabaseField(dataType = DataType.DOUBLE)
	public transient double file_size;

	// FDFS 组信息
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String fdfs_group;

	// FDFS 路径信息
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String fdfs_filepath;

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

			return true;

		}  catch (SQLException e) {

			// 数据库中已经存在记录
			if(e.getCause().getMessage().contains("Duplicate")) {

				logger.error("date exist", e);
			}
			// 可能是采集数据本事存在问题
			else {
				logger.error("Model {} Insert ERROR. ", this.toJSON(), e);
			}
		}

		return false;
	}

	/**
	 * 通过Id获取数据
	 * @return
	 * @throws Exception
	 */
	public static Binary getBinaryById(String Id) {

		Dao dao = daoMap.get(Binary.class.getSimpleName());

		try {
			return (Binary) dao.queryForId(Id);
		} catch (SQLException e) {
			logger.error("error for getBinaryById", e);
			return null;
		}

	}
}
