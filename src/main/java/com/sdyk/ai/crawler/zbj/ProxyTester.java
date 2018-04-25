package com.sdyk.ai.crawler.zbj;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.zbj.proxy.model.ProxyImpl;
import one.rewind.db.DaoManager;

import java.util.List;

public class ProxyTester {

	public static void main(String[] args) throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);

		List<ProxyImpl> proxies = dao.queryForAll();

		for (ProxyImpl proxy : proxies) {
			proxy.validate();
		}
	}
}
