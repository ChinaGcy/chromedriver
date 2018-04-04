import com.sdyk.ai.crawler.zbj.proxy.proxyPool.AliyunService;
import com.sdyk.ai.crawler.zbj.proxy.proxyPool.ProxyReplace;
import com.sdyk.ai.crawler.zbj.proxy.proxyPool.ZBJProxyWrapper;
import org.junit.Test;

public class AliyunProxyTest {
	/**
	 * 代理服务器获取
	 * @throws InterruptedException
	 */
	@Test
	public void getProxy() throws InterruptedException {
		ZBJProxyWrapper proxyWapper = new ZBJProxyWrapper();
		Thread.sleep(5000);
		try {
			String[] a = proxyWapper.getProxy(proxyWapper);

			ProxyReplace.map.put(a[0], a[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取ESC
	 */
	@Test
	public void getESC() {
		String serviceId = AliyunService.getService();
		System.err.println(serviceId);
	}

	@Test
	public void startESC() {
		AliyunService.startService("i-wz94jj2dsbimcqgr6zgm");
	}

	@Test
	public void setIP() {
		AliyunService.getIP("i-wz94jj2dsbimcqgr6zgm");
	}

	public void stopESC() {
		AliyunService.deleteService("");
	}
}
