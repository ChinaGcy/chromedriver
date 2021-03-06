package com.sdyk.ai.crawler.account.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.account.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "accounts")
public class AccountImpl extends Account {

	/*// domain
	@DatabaseField(dataType = DataType.STRING, width = 1024, indexName = "domain-group")
	public String domain;*/

	// 分组
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String group;

	public static final Logger logger = LogManager.getLogger(AccountImpl.class.getName());

	// 需要添加一个无参构造，否则会抛出
	// java.lang.IllegalArgumentException: Can't find a no-arg constructor for class com.sdyk.ai.proc.account.model.AccountImpl
	public AccountImpl() {}

	/**
	 *
	 * @param domain
	 * @param username
	 * @param password
	 */
	public AccountImpl(String domain, String username, String password) {

		super(domain, username, password);
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean update() throws Exception{

		update_time = new Date();

		Dao dao = DaoManager.getDao(this.getClass());

		if (dao.update(this) == 1) {
			return true;
		}

		return false;
	}
}
