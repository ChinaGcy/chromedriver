package com.sdyk.ai.crawler.zbj.proxypool;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.*;
import com.aliyuncs.profile.DefaultProfile;

public class ProxyWapper {

	String serviceId;

	public ProxyWapper() {
		// 创建一个阿里云服务器
		//this.serviceId = AliyunService.getService();

	}

	public static void main(String[] args) {

		//ProxyWapper proxyWapper = new ProxyWapper();

		AliyunService.startService("i-wz925qgv6mdvnw9nghp8");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String IP = AliyunService.getIP("i-wz925qgv6mdvnw9nghp8");

		/*AliyunService.startService("i-wz98mciwctrhy5q6jlao");
		//c-05c09f76f52e49d1a376ebefd9fed5e6
		String commandId = AliyunService.setCommand();
		System.out.println(commandId);
		AliyunService.invokeCommand(commandId, "i-wz98mciwctrhy5q6jlao");*/
	}
}
