package com.sdyk.ai.crawler.zbj.account;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.zbj.account.model.AccountImpl;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.account.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;

public class AccountManager {

	public static final Logger logger = LogManager.getLogger(AccountManager.class.getName());

	protected static AccountManager instance;

	public static AccountManager getInstance() {

		if (instance == null) {
			synchronized (AccountManager.class) {
				if (instance == null) {
					instance = new AccountManager();
				}
			}
		}

		return instance;
	}

	public AccountManager() {}


	/**
	 *
	 * @param domain
	 * @return
	 * @throws Exception
	 */
	public synchronized static Account getAccountByDomain(String domain) throws Exception {

		Dao<AccountImpl, String> dao = DaoManager.getDao(AccountImpl.class);

		AccountImpl account = dao.queryBuilder().limit(1L).
				where().eq("domain", domain)
				.and().eq("status", Account.Status.Free)
				.queryForFirst();

		if(account != null) {

			account.status = Account.Status.Occupied;
			account.update_time = new Date();
			dao.update(account);
			return account;
		}

		return null;
	}

	public synchronized static List<AccountImpl> getAccountByDomain(String domain, long num) throws Exception {

		Dao<AccountImpl, String> dao = DaoManager.getDao(AccountImpl.class);

		List<AccountImpl> accounts = dao.queryBuilder().limit(num).
				where().eq("domain", domain)
				.and().eq("status", Account.Status.Free)
				.query();

		logger.info("Get {} {} accounts.", domain, accounts.size());

		return accounts;
	}
}
