package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.io.requester.account.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@DBName(value = "crawler")
@DatabaseTable(tableName = "accounts")
public class AccountImpl extends Account {

	public static final Logger logger = LogManager.getLogger(AccountImpl.class.getName());

	// 需要添加一个无参构造，否则会抛出
	// java.lang.IllegalArgumentException: Can't find a no-arg constructor for class com.sdyk.ai.crawler.zbj.model.AccountImpl
	public AccountImpl() {}

	public AccountImpl(String domain, String username, String password) {

		super(domain, username, password);
	}
}
