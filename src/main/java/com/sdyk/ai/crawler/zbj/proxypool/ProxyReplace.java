package com.sdyk.ai.crawler.zbj.proxypool;

import com.sdyk.ai.crawler.zbj.exception.IpException;
import com.sdyk.ai.crawler.zbj.model.Proxy;
import com.sdyk.ai.crawler.zbj.requester.ChromeRequester;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.tfelab.io.requester.proxy.ProxyWrapper;

import java.util.HashMap;
import java.util.Map;

public class ProxyReplace {

	public static Map<String, String> map = new HashMap<>();

	/**
	 * 更换代理
	 * @param task
	 * @return
	 * @throws Exception
	 */
	public static void replace(Task task) {

		try {
			Proxy proxy = Proxy.getValidProxy("aliyun");
			task.setProxyWrapper(proxy);
			task.agent.setzbjProxy(proxy);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// TODO 直接生成代理,存到库中后在拿
		task.getAgent().fetch(task);

		try {
			for (Task t : task.postProc(task.getAgent().getDriver())) {
				ChromeRequester.getInstance().distribute(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断是否被禁
	 * @param src
	 * @param task
	 * @throws Exception
	 */
	public static void proxyWork(String src, Task task) throws IpException {

		if (src.contains("您的访问存在异常-猪八戒网")) {

			ProxyWrapper proxyWapper = task.getProxyWrapper();

			String ip = proxyWapper.getHost();

			// 停止被禁IP服务器
			if (map.containsKey(ip)) {
				AliyunService.deleteService(map.get(ip));
			}
			ZBJProxyWrapper.tag = 1;

			throw new IpException("IP stop work");

		}
	}
}
