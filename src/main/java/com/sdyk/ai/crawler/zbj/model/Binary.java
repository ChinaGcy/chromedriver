package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.db.Refacter;

import java.sql.SQLException;
import java.util.Date;

/**
 * 网页下载资源 （文档/图片）
 */
@DBName(value = "crawler")
@DatabaseTable(tableName = "binaries")
public class Binary {

	// id
	@DatabaseField(dataType = DataType.STRING, width = 32)
	public String id;

	//当前网站url
	@DatabaseField(dataType = DataType.STRING, width = 64)
	public String url;

	//资源名称
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String file_name;

	//资源的二进制数据
	@DatabaseField(dataType = DataType.BYTE_ARRAY, columnDefinition = "LONGBLOB")
	public byte[] src;

	//资源格式
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String content_type;

	//插入时间
	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	//更新时间
	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();




	/**
	 * 插入
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception{

		Dao<Binary, String> dao = OrmLiteDaoManager.getDao(Binary.class);
		try {
			if (dao.create(this) == 1) {
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			dao.update(this);
		}

		return false;
	}

	/*
	测试;数据库
	 */
	public static void main(String[] agrs) throws Exception {
		Refacter.dropTable(Binary.class);
		Refacter.createTable(Binary.class);
	}


}
