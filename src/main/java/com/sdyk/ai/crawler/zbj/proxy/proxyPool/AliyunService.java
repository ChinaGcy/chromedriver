package com.sdyk.ai.crawler.zbj.proxy.proxyPool;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.*;
import com.aliyuncs.profile.DefaultProfile;

public class AliyunService {

	public static String key = "LTAIhJIxxeqIxCZS";
	public static String secret = "tPemFWEvmggedGTFXhLOwivlXBDwPu";
	public static String regionId = "cn-shenzhen";
	// 初始化
	public static DefaultProfile profile = DefaultProfile.getProfile(regionId, key, secret);
	public static IAcsClient client = new DefaultAcsClient(profile);

	/**
	 * 增加服务器
	 */
	public static String getService () {
		//设置参数
		CreateInstanceRequest createInstance = new CreateInstanceRequest();
		// 地区
		createInstance.setRegionId("cn-shenzhen");
		// 操作系统
		createInstance.setImageId("ubuntu_16_0402_64_20G_alibase_20171227.vhd");
		// 服务器类型
		createInstance.setInstanceType("ecs.xn4.small");
		// 安全组 用于开放端口
		createInstance.setSecurityGroupId("sg-wz9ejq1i5n8kv5kp8sqo");
		// 账号密码
		createInstance.setHostName("aliyun-zbj");
		createInstance.setPassword("SDYK315pr");
		// 按时间计费
		createInstance.setInternetChargeType("PayByTraffic");
		// 带宽
		createInstance.setInternetMaxBandwidthOut(100);

		// 发起请求
		try {
			CreateInstanceResponse response = client.getAcsResponse(createInstance);
			String serviceid = response.getInstanceId();
			return serviceid;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 设置公网ip，运行中
	 * @param ServiceId
	 * @return
	 */
	public static String getIP(String ServiceId) {

		AllocatePublicIpAddressRequest allocatePublicIpAddress = new AllocatePublicIpAddressRequest();
		allocatePublicIpAddress.setInstanceId(ServiceId);
		// 设置公网ip， 默认自动生成
		//allocatePublicIpAddress.setIpAddress("");

		// 发起请求
		try {
			AllocatePublicIpAddressResponse response = client.getAcsResponse(allocatePublicIpAddress);
			System.out.println(response.getIpAddress());
			return response.getIpAddress();
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 开启服务器
	 * @param serviceId
	 */
	public static void startService(String serviceId) {

		//设置参数
		StartInstanceRequest startInstance = new StartInstanceRequest();
		startInstance.setInstanceId(serviceId);

		// 发起请求
		try {
			StartInstanceResponse response = client.getAcsResponse(startInstance);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 停止服务器
	 * @param serviceId 需要停止的服务器id
	 */
	public static void deleteService(String serviceId) {
		//设置参数
		StopInstanceRequest stopInstance = new StopInstanceRequest();
		stopInstance.setInstanceId(serviceId);
		stopInstance.setConfirmStop(true);
		//stopInstance.setForceStop(true);

		// 发起请求
		try {
			StopInstanceResponse response = client.getAcsResponse(stopInstance);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
