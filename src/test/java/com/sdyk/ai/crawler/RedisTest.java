package com.sdyk.ai.crawler;

import redis.clients.jedis.Jedis;

public class RedisTest {
	public static void main(String[] args) {

		//连接本地的 Redis 服务
		Jedis jedis = new Jedis("localhost");
		jedis.auth("sdyk");
		System.out.println("连接成功");

		jedis.set("runoobkey", "www.runoob.com");
		jedis.set("runoobkey1", "www.runoob.com");
	}
}
