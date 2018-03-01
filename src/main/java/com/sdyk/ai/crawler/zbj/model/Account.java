package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;

import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "accounts")
public class Account {

	@DatabaseField(generatedId = true)
	public int id;

	//使用的网站
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String domain;

	//账户
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String username;

	//密码
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String password;

	//状态（是否可用）
	@DatabaseField(dataType = DataType.ENUM_STRING)
	public Status status = Status.Free;

	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time;

	@DatabaseField(dataType = DataType.DATE)
	public Date update_time;

	public enum Status {
		Free,
		Occupied,
		Broken
	}

	public Account() {

	}

	public Account(String domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;
	}

	public synchronized static Account getAccountByDomain(String domain) throws Exception {

		Dao<Account, String> dao = OrmLiteDaoManager.getDao(Account.class);

		Account account = dao.queryBuilder().limit(1L).
				where().eq("domain", domain)
				.and().eq("status", Status.Free)
				.queryForFirst();

		if(account != null) {
			account.status = Status.Occupied;
			account.update_time = new Date();
			dao.update(account);
			return account;
		}

		return null;
	}

	public String getDomain() {
		return domain;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public Status getStatus() {
		return status;
	}

	public Date getInsert_time() {
		return insert_time;
	}

	public Date getUpdate_time() {
		return update_time;
	}
}
