package org.tfelab.io.requester.chrome;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.tfelab.io.requester.chrome.ChromeDriverAgent.logger;

public class ChromeDriverUbuntuUtil {

	/**
	 * 根据pid终止chromedriver进程
	 * driver.quit() 有可能执行失败，通过这个方法强制chromedriver退出
	 * 释放资源
	 * @param pid chromedriver进程pid
	 */
	public static void killProcessByPid(int pid) {

		try {

			if (pid > 0) {

				logger.info("{} Try to kill process:{} and forked processes.", Thread.currentThread().getName(), pid);
				String command = "pkill -9 -P " + pid;
				Process pro = Runtime.getRuntime().exec(command);

				BufferedReader in = new BufferedReader(
						new InputStreamReader(pro.getInputStream()));
				String line = null;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
				}

				pro.destroy();

				command = "kill -9 " + pid;
				Process pro_ = Runtime.getRuntime().exec(command);

				in = new BufferedReader(
						new InputStreamReader(pro_.getInputStream()));
				line = null;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
				}

				pro_.destroy();
			}

		} catch (IOException ex) {
			logger.error("{}", Thread.currentThread().getName(), ex);
		}
	}

	/**
	 * @return 获取当前 chromedriver 进程 ID
	 */
	public static int getPid() {

		int pid = 0;

		try {

			Thread.sleep(2000);

			Process p1 = Runtime.getRuntime().exec("ps aux");
			InputStream i1 = p1.getInputStream();

			Process p2 = Runtime.getRuntime().exec("grep chromedriver");
			OutputStream o2 = p2.getOutputStream();

			IOUtils.copy(i1, o2);
			o2.close();
			i1.close();

			List<String> result = IOUtils.readLines(p2.getInputStream());
			List<Integer> pids = new ArrayList<>();
			List<Integer> p_pids = getAllRunningPids();

			for(String str : result) {
				//logger.info(str);
				if(str.matches(".+?/opt/.+?/chromedriver.+?")) {
					pids.add(Integer.parseInt(str.split("\\s+")[1]));
				}
			}

			logger.info("Current chromedriver pid: {}", pids);
			logger.info("Previous chromedriver pid: {}", p_pids);

			pids.removeAll(p_pids);

			if(pids.size() == 1) {

				pid = pids.get(0);
			}
			// No new chromedriver process dectected
			/*else if (pids.size() == 0) {
			}*/
			// More than one new chromedriver process dectected
			else {
				pid = pids.get(0);
			}

			p1.destroy();
			p2.destroy();

		} catch (IOException | InterruptedException e) {
			logger.error(e.getMessage());
		}

		return pid;
	}

	/**
	 * 获得运行中的全部chrome pid列表
	 * @return pid 列表
	 */
	public static List<Integer> getAllRunningPids() {

		List<Integer> pids = new ArrayList<>();

		for(ChromeDriverAgent agent: ChromeDriverAgent.instances) {
			pids.add(agent.pid);
		}

		return pids;
	}

	/**
	 * 测试 Xvfb 服务是否运行
	 * Linux 环境适用
	 */
	public static void checkXvfb() {

		List<String> params = new ArrayList<String>();
		params.add("/etc/init.d/xvfb");
		params.add("start");

		ProcessBuilder pb = new ProcessBuilder(params);
		try {
			pb.start();
		} catch (Exception e) {
			logger.error("{} Error execute xvfb start script. {}", Thread.currentThread().getName(), e.getMessage());
		}
	}
}
