package com.sdyk.ai.crawler.redisTest;

import one.rewind.db.RedissonAdapter;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class RedissionSub {

	public static void subTask(){

		RTopic<String> topicSub = RedissonAdapter.redisson.getTopic("clouderWorkTopic");

		//订阅task
		topicSub.addListener(new MessageListener<String>() {
			@Override
			public void onMessage(String channel, String message) {

				System.out.println("topicSub : " + message);

			}
		});

	}

	/**
	 * 反序列化
	 * @param bytes
	 * @return
	 */
	public static Object unserialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
		}
		return null;
	}

}
