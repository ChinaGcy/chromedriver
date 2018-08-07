package com.sdyk.ai.crawler.redisTest;

import one.rewind.db.RedissonAdapter;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class RedissionPub {

	public static void pubTask() {

		RTopic<String> topicPub = RedissonAdapter.redisson.getTopic("clouderWorkTopic");

		String message = "测试1";

		topicPub.publish(message);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		topicPub.publish("测试2");

	}

	/**
	 * 序列化
	 * @param object
	 * @return
	 */
	public static byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			byte[] bytes = baos.toByteArray();
			return bytes;
		} catch (Exception e) {
		}
		return null;
	}

}
