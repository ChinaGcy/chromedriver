package com.sdyk.ai.crawler;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import com.sdyk.ai.crawler.zbj.proxy.ProxyManager;
import com.sdyk.ai.crawler.zbj.proxy.model.ProxyImpl;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.proxy.Proxy;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyTest {

	@Test
	public void validateAll() throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);
		List<ProxyImpl> ps = dao.queryForAll();

		for(ProxyImpl p: ps) {
			try {
				p.validateAll();
				p.update_time = new Date();
				p.update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testSpeed() throws Exception {

		// String url = "https://www.baidu.com";
		String url = "https://www.zbj.com/";

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);
		List<ProxyImpl> ps = dao.queryForAll();

		Map<Proxy, Float> result = new HashMap<>();

		for(ProxyImpl proxy : ps) {
			result.put(proxy, proxy.testSpeed(url));
		}

		for(Proxy proxy : result.keySet()) {
			System.err.println("Proxy " + proxy.getInfo() + " to " + url + "\t" + result.get(proxy) + " KB/s.");
		}

		/*ProxyImpl proxy = (ProxyImpl) ProxyManager.getInstance().getProxyById("1");
		System.err.println("Proxy " + proxy.getInfo() + " to " + url + "\t" + proxy.testSpeed(url) + " KB/s.");*/
	}

	@Test
	public void queue() throws Exception {
		ProxyManager proxyManager =ProxyManager.getInstance();
		ProxyImpl proxy = proxyManager.getValidProxy("aliyun");

		System.err.println(proxy.toJSON());

	}
}
