package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;

import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "accounts")
public class Account extends one.rewind.io.requester.account.Account {

	public Account(String domain, String username, String password) {

		super(domain, username, password);
	}

	/**
	 *
	 * @param domain
	 * @return
	 * @throws Exception
	 */
	public synchronized static Account getAccountByDomain(String domain) throws Exception {

		Dao<Account, String> dao = DaoManager.getDao(Account.class);

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
}
