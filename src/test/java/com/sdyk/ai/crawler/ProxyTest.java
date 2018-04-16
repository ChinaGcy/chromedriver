package com.sdyk.ai.crawler;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.zbj.model.Proxy;
import one.rewind.db.DaoManager;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class ProxyTest {

	@Test
	public void validateAll() throws Exception {
		Dao<Proxy, String> dao = DaoManager.getDao(Proxy.class);
		List<Proxy> ps = dao.queryForAll();

		for(Proxy p: ps) {
			try {
				p.validateAll();
				p.update_time = new Date();
				p.update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
