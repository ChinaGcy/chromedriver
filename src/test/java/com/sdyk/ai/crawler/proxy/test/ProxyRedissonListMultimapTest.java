package com.sdyk.ai.crawler.proxy.test;

import one.rewind.db.RedissonAdapter;
import org.junit.Test;
import org.redisson.api.RMultimap;

public class ProxyRedissonListMultimapTest {

	@Test
	public void test(){

		RMultimap<String, String> proxyDomainBannedMap = RedissonAdapter.redisson.getListMultimap("proxy-domain-banned-map");

//		proxyDomainBannedMap.put("id","domain");
		if( proxyDomainBannedMap.get("id").contains("domain") ){
			System.out.println(proxyDomainBannedMap.get("id"));
		}


	}

}
