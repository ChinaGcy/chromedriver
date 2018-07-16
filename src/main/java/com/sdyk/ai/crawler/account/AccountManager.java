package com.sdyk.ai.crawler.account;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.account.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 账户管理器
 */
public class AccountManager {

	public static final Logger logger = LogManager.getLogger(AccountManager.class.getName());

	protected static AccountManager instance;

	/**
	 *
	 * @return
	 */
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
	 * 通过Id获取账号
	 * TODO check 使用场景
	 * @param Id
	 * @return
	 * @throws Exception
	 */
	public synchronized Account getAccountById(String Id) throws Exception {

		Dao<AccountImpl, String> dao = DaoManager.getDao(AccountImpl.class);

		AccountImpl account = dao.queryBuilder().limit(1L).
				where().eq("id", Id)
				.queryForFirst();

		if(account != null) {

			account.status = Account.Status.Occupied;
			account.update_time = new Date();
			dao.update(account);
			return account;
		}

		return null;
	}

	/**
	 * 根据domain获取一个 可用的 且状态为Free 的账号
	 * @param domain
	 * @return
	 * @throws Exception
	 */
	public synchronized Account getAccountByDomain(String domain) throws Exception {

		Dao<AccountImpl, String> dao = DaoManager.getDao(AccountImpl.class);

		AccountImpl account = dao.queryBuilder().limit(1L).
				where().eq("domain", domain)
				.and().eq("status", Account.Status.Free)
				.and().eq("enabled", true)
				.queryForFirst();

		if(account != null) {

			account.status = Account.Status.Occupied;
			account.update_time = new Date();
			dao.update(account);
			return account;
		}

		return null;
	}

	/**
	 * 根据 domain / group 获取一个 可用的 且状态为Free 的账号
	 * @param domain
	 * @param group
	 * @return
	 * @throws Exception
	 */
	public synchronized Account getAccountsByDomain(String domain, String group) throws Exception {

		Dao<AccountImpl, String> dao = DaoManager.getDao(AccountImpl.class);

		AccountImpl account = dao.queryBuilder().limit(1L).
				where().eq("domain", domain)
				.and().eq("group", group)
				.and().eq("status", Account.Status.Free)
				.and().eq("enabled", true)
				.queryForFirst();

		if(account != null) {

			account.status = Account.Status.Occupied;
			account.update_time = new Date();
			dao.update(account);
			return account;
		}

		return null;
	}

	/**
	 * 设定所有Account 的状态为Free
	 */
	public void setAllAccountFree() throws Exception {

		List<AccountImpl> accounts = DaoManager.getDao(AccountImpl.class).queryForAll();
		for(AccountImpl account : accounts) {
			account.status = Account.Status.Free;
			account.update();
		}
	}
}
