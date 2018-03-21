package com.sdyk.ai.crawler.zbj.proxypool;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.*;
import com.aliyuncs.profile.DefaultProfile;

import java.util.ArrayList;
import java.util.List;

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

		// 发起请求
		try {
			CreateInstanceResponse response = client.getAcsResponse(createInstance);
			String serviceid = response.getInstanceId();
			getIP(serviceid);
			return serviceid;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 设置公网ip
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
	 * 云助手，设置需要执行的命令,生成的文件在/root目录下
	 * @return 命令的id
	 */
	public static String setCommand() {
		//设置参数
		CreateCommandRequest createCommand = new CreateCommandRequest();
		createCommand.setName("squid");
		// 设置linux环境的shell脚本
		createCommand.setType("RunShellScript");
		// 命令 Base64 编码后的内容
		createCommand.setCommandContent("c3VkbyBhcHQtZ2V0IC15IGluc3RhbGwgc3F1aWQgYXBhY2hlMi11dGlscwogCnN1ZG8gY3AgL2V0Yy9zcXVpZC9zcXVpZC5jb25mIC9ldGMvc3F1aWQvc3F1aWQuY29uZi5kZWZhdWx0ICYmIHN1ZG8gdG91Y2ggL2V0Yy9zcXVpZC9zcXVpZF9wYXNzd2QgJiYgc3VkbyBjaG93biBwcm94eSAvZXRjL3NxdWlkL3NxdWlkX3Bhc3N3ZCAmJiBzdWRvIGh0cGFzc3dkIC1iIC1jIC9ldGMvc3F1aWQvc3F1aWRfcGFzc3dkIHRmZWxhYiBUZmVMQUIyQDE1CiAKc3VkbyBzZWQgLWkgIjFpXGF1dGhfcGFyYW0gYmFzaWMgcHJvZ3JhbSAvdXNyL2xpYi9zcXVpZC9iYXNpY19uY3NhX2F1dGggL2V0Yy9zcXVpZC9zcXVpZF9wYXNzd2QgXG4gXAphY2wgbmNzYV91c2VycyBwcm94eV9hdXRoIFJFUVVJUkVEIFxuIFwKaHR0cF9hY2Nlc3MgYWxsb3cgbmNzYV91c2VycyBcbiBcCmNhY2hlIGRlbnkgYWxsIFxuIiAvZXRjL3NxdWlkL3NxdWlkLmNvbmYKIApzdWRvIHNlZCAtaSAicy8uKmh0dHBfcG9ydCAzMTI4LiovaHR0cF9wb3J0IDU5OTk4LyIgL2V0Yy9zcXVpZC9zcXVpZC5jb25mCiAKc3VkbyBzZWQgLWkgJyRhXGZvcndhcmRlZF9mb3Igb2ZmIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIEFsbG93IGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBBdXRob3JpemF0aW9uIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBXV1ctQXV0aGVudGljYXRlIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBQcm94eS1BdXRob3JpemF0aW9uIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBQcm94eS1BdXRoZW50aWNhdGUgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIENhY2hlLUNvbnRyb2wgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIENvbnRlbnQtRW5jb2RpbmcgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIENvbnRlbnQtTGVuZ3RoIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBDb250ZW50LVR5cGUgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIERhdGUgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIEV4cGlyZXMgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIEhvc3QgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIElmLU1vZGlmaWVkLVNpbmNlIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBMYXN0LU1vZGlmaWVkIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBMb2NhdGlvbiBhbGxvdyBhbGwgXApyZXF1ZXN0X2hlYWRlcl9hY2Nlc3MgUHJhZ21hIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBBY2NlcHQgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIEFjY2VwdC1DaGFyc2V0IGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBBY2NlcHQtRW5jb2RpbmcgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIEFjY2VwdC1MYW5ndWFnZSBhbGxvdyBhbGwgXApyZXF1ZXN0X2hlYWRlcl9hY2Nlc3MgQ29udGVudC1MYW5ndWFnZSBhbGxvdyBhbGwgXApyZXF1ZXN0X2hlYWRlcl9hY2Nlc3MgTWltZS1WZXJzaW9uIGFsbG93IGFsbCBcCnJlcXVlc3RfaGVhZGVyX2FjY2VzcyBSZXRyeS1BZnRlciBhbGxvdyBhbGwgXApyZXF1ZXN0X2hlYWRlcl9hY2Nlc3MgVGl0bGUgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIENvbm5lY3Rpb24gYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIFByb3h5LUNvbm5lY3Rpb24gYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIFVzZXItQWdlbnQgYWxsb3cgYWxsIFwKcmVxdWVzdF9oZWFkZXJfYWNjZXNzIENvb2tpZSBhbGxvdyBhbGwgXApyZXF1ZXN0X2hlYWRlcl9hY2Nlc3MgQWxsIGRlbnkgYWxsXG4nIC9ldGMvc3F1aWQvc3F1aWQuY29uZgogCnN1ZG8gc2VydmljZSBzcXVpZCByZXN0YXJ0");
		createCommand.setRegionId(regionId);

		// 发起请求
		try {
			CreateCommandResponse response = client.getAcsResponse(createCommand);
			return response.getCommandId();
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 云助手，执行命令
	 * @param commandId 任务id
	 * @param serviceId 服务器id
	 */
	public static void invokeCommand (String commandId, String serviceId) {
		//设置参数
		InvokeCommandRequest invokeCommand = new InvokeCommandRequest();

		//设置复杂类型参数
		List<String> instanceIdList = new ArrayList<>();
		instanceIdList.add(serviceId);
		invokeCommand.setInstanceIds(instanceIdList);

		invokeCommand.setRegionId(regionId);
		invokeCommand.setCommandId(commandId);

		// 发起请求
		try {
			InvokeCommandResponse response = client.getAcsResponse(invokeCommand);
			System.out.println("OK!");
		}catch (Exception e) {
			e.printStackTrace();
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
