package org.tfelab.io.requester.chrome;

import com.sdyk.ai.crawler.zbj.model.Account;
import com.sdyk.ai.crawler.zbj.requester.ChromeDriverLoginWrapper;
import com.typesafe.config.Config;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.tfelab.common.Configs;
import org.tfelab.io.requester.Requester;
import org.tfelab.io.requester.Task;
import org.tfelab.io.requester.chrome.action.ChromeDriverAction;
import org.tfelab.io.requester.chrome.action.ClickAction;
import org.tfelab.io.requester.cookie.CookiesHolderManager;
import org.tfelab.io.requester.proxy.ProxyWrapper;
import org.tfelab.io.requester.util.DocumentSettleCondition;
import org.tfelab.txt.URLUtil;
import org.tfelab.util.EnvUtil;

import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.security.Security;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

/**
 * Chrome请求器
 * karajan@2017.9.17
 */
public class ChromeDriverAgent {

	public static final Logger logger = LogManager.getLogger(ChromeDriverAgent.class.getName());

	public static int CONNECT_TIMEOUT;
	public static int READ_TIMEOUT;

	/**
	 * 配置设定
	 */
	static {

		Config ioConfig = Configs.getConfig(Requester.class);
		CONNECT_TIMEOUT = ioConfig.getInt("connectTimeout");
		READ_TIMEOUT = ioConfig.getInt("readTimeout");

		/**
		 * 设定chromedriver executable
		 */
		if (EnvUtil.isHostLinux()) {
			System.setProperty("webdriver.chrome.driver", Configs.getConfig(Requester.class).getString("chromeDriver"));
			checkXvfb();
		} else {
			System.setProperty("webdriver.chrome.driver", Configs.getConfig(Requester.class).getString("chromeDriver") + ".exe");
		}

		/**
		 * Set Log file
		 */
		System.setProperty("webdriver.chrome.logfile", "webdriver.chrome.log");

		/**
		 * Add BouncyCastleProvider, BouncyCastleProvider 主要用于接受特定环境的HTTPS策略
		 */
		Security.addProvider(new BouncyCastleProvider());
	}

	static int RETRY_LIMIT = 3; // 任务重试次数
	static int USAGE_COUNT_LIMIT = 2000; // 单个chrome的使用次数上限

	static int upstreamProxyPort = 59019; // 公用upstreaming代理端口号
	static HttpProxyServer upstreamProxy; // 公用upstreaming代理 由于 BrowserMobProxyServer 的特殊设置, 对没有代理的任务, 指定此代理

	static List<ChromeDriverAgent> instances = new ArrayList<ChromeDriverAgent>();

	private ChromeDriver driver;
	private File userDir;
	private int pid = 0; // 记录UNIX系统下的pid, 用于强制退出
	
	private int usageCount = 0; // 使用次数记录

	private BrowserMobProxyServer bmProxy; // 代理出口

	private Account account;
	private com.sdyk.ai.crawler.zbj.model.Proxy proxy;

	/**
	 *
	 */
	public ChromeDriverAgent() {
		init();
	}

	/**
	 * 获得运行中的chrome pid列表
	 * @return
	 */
	private static List<Integer> getAllRunningPids() {
		
		List<Integer> pids = new ArrayList<Integer>();
		for(ChromeDriverAgent agent : instances) {
			pids.add(agent.pid);
		}
		
		return pids;
	}

	public void addProxyRequestFilter(RequestFilter requestFilter) {
		bmProxy.addRequestFilter(requestFilter);
	}

	public void addProxyResponseFilter(ResponseFilter responseFilter) {
		bmProxy.addResponseFilter(responseFilter);
	}

	/**
	 * 测试 Xvfb 服务是否运行
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

	/**
	 * 初始化
	 */
	public synchronized void init() {
			
		logger.info("Init...");
		
		usageCount = 0;
		driver = null;

		/**
		 *
		 */
		synchronized(instances) {
        	
        	this.userDir = new File("chrome_user_dir/" + this.hashCode());

			logger.info("User dir: {}", userDir.getAbsolutePath());
			instances.add(this);

			/**
			 * Create Default Upstreaming proxy
			 */
			if(upstreamProxy == null) {
				upstreamProxy = DefaultHttpProxyServer.bootstrap()
					.withPort(upstreamProxyPort)
					.withName("ChromeDriverUpstreamProxy")
					.withConnectTimeout(CONNECT_TIMEOUT)
					.start();
			}
		
			/**
			 * Create BrowserMobProxy
			 */
			bmProxy = new BrowserMobProxyServer();
			bmProxy.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
			bmProxy.setRequestTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);
			bmProxy.setChainedProxy(upstreamProxy.getListenAddress());
			bmProxy.setTrustAllServers(true);
			bmProxy.setMitmManager(ImpersonatingMitmManager.builder().trustAllServers(true).build());
			bmProxy.start(0); // Use any free port

			/**
			 * Set up chrome capabilities
			 */
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setPlatform(Platform.WIN8);

			// Set no loading images
			/*Map<String, Object> contentSettings = new HashMap<String, Object>();
			contentSettings.put("images", 2);

			Map<String, Object> preferences = new HashMap<String, Object>();
			preferences.put("profile.default_content_settings", contentSettings);

			capabilities.setCapability("chrome.prefs", preferences);*/
			
			LoggingPreferences logPrefs = new LoggingPreferences();
			logPrefs.enable(LogType.PERFORMANCE, Level.INFO);
			capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);
			
			/*Map<String, Object> perfLogPrefs = new HashMap<String, Object>();
			perfLogPrefs.put("traceCategories", "browser,devtools.timeline,devtools"); // comma-separated trace categories*/
			
			Proxy seleniumProxy = ClientUtil.createSeleniumProxy(bmProxy);
		    capabilities.setCapability("proxy", seleniumProxy);
		    capabilities.setCapability("recreateChromeDriverSessions", true);
		    capabilities.setCapability("newCommandTimeout", 120);
		    
		    Map<String, Object> prefs = new HashMap<String, Object>();
		    prefs.put("profile.default_content_setting_values.notifications", 2);
		    
			ChromeOptions options = new ChromeOptions();
			/*options.setExperimentalOption("perfLoggingPrefs", perfLogPrefs);
			options.addArguments("user-data-dir=" + userDir.getAbsolutePath());*/
			options.addArguments("--no-sandbox");
			/*options.addArguments("--start-maximized");*/
			options.addArguments("--dns-prefetch-disable");
			options.addArguments("--disable-gpu-watchdog");
			options.addArguments("--disable-gpu-program-cache");
			options.addArguments("--disk-cache-dir=/dev/null");
			options.addArguments("--disk-cache-size=1");
			// 解决Selenium最大化报错问题
			options.addArguments("--start-maximized");

			options.setExperimentalOption("prefs", prefs);
			options.setExperimentalOption("detach", true);

			/**
			 * 加载禁用图片插件
			 */
			/*File block_image_crx = new File("chrome_ext/Block-image_v1.0.crx");
			if (block_image_crx.exists()) {
				options.addExtensions(new File("chrome_ext/Block-image_v1.0.crx"));
			}*/
			
			capabilities.setCapability(ChromeOptions.CAPABILITY, options);
			
			List<Integer> pids = getAllRunningPids();

			/**
			 * 循环验证启动Chrome
			 */
			while(driver == null) {

				final ExecutorService executor = Executors.newSingleThreadExecutor();
				final Future<?> future = executor.submit(() -> {

					/**
					 * 此处代码可能hang forever
					 */
					try {
						driver = new ChromeDriver(capabilities);
						logger.info("Create chromedriver done.");
					} catch (Exception e) {
						logger.error("Error create chromedriver", e);
					}

					/**
					 * 极端情况处理，2s之后chromedriver仍未正常启动，则进行pid的再次获取
					 */
					if(EnvUtil.isHostLinux()) {

						pid = getPid();

						if(pid == 0) {
							pid = getPid();

							if(pid == 0) {
								logger.warn("No valid chromedriver pid dectected, but chromedriver looks fine.");
							}
						}
					}
				});

				executor.shutdown();
				
				try {
					future.get(30000, TimeUnit.MILLISECONDS);
				}
				catch (TimeoutException | InterruptedException | ExecutionException e){
					
					logger.error("{}", Thread.currentThread().getName(), e);
					future.cancel(true);
					
					if(EnvUtil.isHostLinux()) {
						/**
						 * 在某种特定情况下，new ChromeDriver hang forever
						 * 但chromedriver及相关chrome进程却正常启动了
						 * 需要终止这些无法控制的进程
						 */
						pid = getPid();
						if(pid != 0) {
							killProcessByPid(pid);
						}
					}
					driver = null;
				}
				
				if (!executor.isTerminated()){
					executor.shutdownNow();
				}
			}
			
			logger.info("Chrome driver pid:{}", pid);

			// 设置脚本运行超时参数
			driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);

			// 设置等待超时参数
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			
			// 这个值要设置的比较大 否则会出现 org.openqa.selenium.TimeoutException: timeout: cannot determine loading status
			driver.manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);

			// Set Dimension
			//Dimension d = new Dimension(1024, 600);
			//Dimension d = new Dimension(380, 600);
			//driver.manage().window().setSize(d);
			// TODO 记录此处的报错信息 和 报错时的上下文信息

			//driver.manage().window().maximize();

			Random r = new Random();
			Point p = new Point(60 * r.nextInt(10), 40 * r.nextInt(10));
			//driver.manage().window().setPosition(p);
			
			logger.info("Init ChromeDriverAgent done.");
        }
    }

	/**
	 * 获取对应 chromedriver 进程 ID
	 * @return
	 */
	private int getPid() {
		
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
			List<Integer> pids = new ArrayList<Integer>();
			List<Integer> p_pids = this.getAllRunningPids();
			
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
			else if (pids.size() == 0) {
				
			}
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
	 * 向浏览器注入当前 host 下的 cookies
	 * @param host
	 * @param cookies
	 */
	private void injectCookie(String host, String cookies) {
		
		logger.trace(cookies);
		
		String[] cookie_items = cookies.split(";");
		
		for(String ci : cookie_items) {
			
			ci = ci.trim();
			String[] kv = ci.split("=", 2);
			
			if(kv.length > 1) {
				
				logger.trace("[{}]\t{}={}", host, kv[0], kv[1]);
				//Cookie cookie_ = new Cookie(kv[0], kv[1]);
				
				Cookie cookie_ = new Cookie(kv[0], kv[1], "." + URLUtil.getRootDomainName(host), "/", null);
				/**
				 * TODO
				 * 可能会出问题
				 * 当ChromeDriver未能完全加载时
				 */
				driver.manage().addCookie(cookie_);
			}
		}
	}

	/**
	 * 测试发送POST请求
	 * 其实完全没必要使用chromedriver发起post请求
	 * TODO  未测试
	 * @param url
	 * @param postData
	 */
	private void redirectPost(String url, String postData) {
		String html = "<form method=post action="+ url +">";
		
		String[] items = postData.split("&");
		for(String item : items) {
			String[] kv = item.split("=");
			if(kv.length > 1) {
				html += "<input type=hidden name=" + kv[0] + " value=" + kv[1] + ">";
			}
		}
		html += "<input id=Pnowl89Xac type=submit name=METHOD value=mysubmitbutton></form>";		
		
		String script = "var h1 = document.createElement('div'); " + "h1.innerHTML=\"" + html + "\"; document.body.appendChild(h1)";
		
		driver.executeScript(script);
		driver.findElementById("Pnowl89Xac").submit();
	}
	
	/**
	 * 找到特定元素
	 * @param driver
	 * @param path
	 * @return
	 */
	public static WebElement getElementWait(ChromeDriver driver, String path){
		WebDriverWait wait = new WebDriverWait(driver, 10);
	    return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(path)));
	}

	/**
	 * 找到特定元素
	 * @param path
	 * @return
	 */
	public WebElement getElementWait(String path) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
	    return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(path)));
	}
	
	/**
	 * Executes a script on an element
	 * @note Really should only be used when the web driver is sucking at exposing
	 * functionality natively
	 * @param script The script to execute
	 * @param element The target of the script, referenced as arguments[0]
	 */
	public void trigger(String script, WebElement element) {
	    ((JavascriptExecutor) driver).executeScript(script, element);
	}

	/** Executes a script
	 * @note Really should only be used when the web driver is sucking at exposing
	 * functionality natively
	 * @param script The script to execute
	 */
	public Object trigger(String script) {
	    return ((JavascriptExecutor)driver).executeScript(script);
	}

	/**
	 * Opens a new tab for the given URL
	 */
	/*public void openTab(String url) throws JavascriptException {
	    String script = "var d=document,a=d.createElement('a');a.target='_blank';a.href='%s';a.innerHTML='.';d.body.appendChild(a);return a";
	    Object element = trigger(String.format(script, url));
	    if (element instanceof WebElement) {
	        WebElement anchor = (WebElement) element; anchor.click();
	        trigger("var a=arguments[0];a.parentNode.removeChild(a);", anchor);
	    } else {
	        throw new org.openqa.selenium.JavascriptException("Unable to open tab");
	    }
	}*/
	
	/**
	 * 截图
	 * @param imgPath
	 * @return
	 * @throws IOException
	 */
	public byte[] shoot(String imgPath) throws IOException {
		
		WebElement element = getElementWait(driver, imgPath);
		
		File screen = driver.getScreenshotAs(OutputType.FILE);

		Point p = element.getLocation();

		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();

		Rectangle rect = new Rectangle(width, height);

		BufferedImage img = null;
		img = ImageIO.read(screen);

		BufferedImage dest = img.getSubimage(p.getX(), p.getY(), rect.width, rect.height);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(dest, "png", baos);
		
		return baos.toByteArray();
	}
	
	/**
	 * 任务封装
	 * @author karajan@tfelab.org
	 * 2017年3月22日 上午10:04:11
	 */
	public class Wrapper implements Runnable {
			
		Task task;
		boolean needRestart = false;

		int retryCount = 0;
		
		/**
		 * 
		 * @param task
		 */
		public Wrapper(Task task) {
			this.task = task;
		}
		
		/**
		 * 主要任务执行方法
		 */
		public void run() {

			this.needRestart = false;

			logger.info("{}", task.getUrl());
			
			try {
				
				ChromeDriverAgent.this.setProxy(task.getProxyWrapper());
				
				//String cookies = ChromeDriverAgent.this.handleCookie(task.getDomain(), task.getUrl(), task.getCookies());

				ChromeDriverAgent.this.getUrl(task.getUrl(), task.getPost_data());

				//ChromeDriverAgent.this.waitPageLoad(task.getUrl());

				// 正常解析到页面
				/*if(!driver.getCurrentUrl().matches("^data:.+?")) {

					boolean actionResult = true;
					for(ChromeDriverAction action : task.getActions()) {
						actionResult = actionResult && action.run(driver);
					}

					task.getResponse().setActionDone(actionResult);

					task.getResponse().setSrc(ChromeDriverAgent.this.getAllSrc().getBytes());
					task.getResponse().setText();


					if(task.isShoot_screen()) {
						task.getResponse().setScreenshot(driver.getScreenshotAs(OutputType.BYTES));
					}
				}*/
				
				// save cookies
				//String newCookies = ChromeDriverAgent.this.getNewCookies(task.getDomain(), cookies);
				//task.getResponse().setCookies(newCookies);

			}
			/**
			 * 需要重启
			 */
			catch (NoSuchWindowException e) {
				/*logger.error(e);
				task.setException(e);
				needRestart = true;*/
			}
			catch (UnreachableBrowserException e) {
				/*logger.error(e);
				task.setException(e);
				needRestart = true;*/
			}
			/**
			 * 操作超时，譬如判断页面是否成功加载时候，会抛这个异常
			 */
			catch (org.openqa.selenium.TimeoutException e) {
				/*logger.error(e);
				task.setException(e);
				needRestart = true;*/
			}
			catch (NoSuchSessionException e) {
				/*logger.error(e);
				task.setException(e);
				needRestart = true;*/
			}
			catch (WebDriverException e) {
				/*logger.error(e);
				task.setException(e);
				needRestart = true;
				if(e.getMessage().contains("chrome not reachable") 
					|| e.getMessage().contains("session deleted")
					|| e.getMessage().contains("unknown error")
					|| e.getMessage().contains("unexpectedly died")
				) {

				}*/
			}
			catch (Exception e) {
				/*logger.error("Unknown Error.", e);
				task.setException(e);*/
			}
			finally {
				// TODO
				task.setDuration();
			}
		}
		
		/*public void close() {
			driver.executeScript("window.stop()");
		}*/
	}


	/**
	 * 同步执行任务
	 * @param task
	 */
	public synchronized void fetch(Task task) {

		task.setStartTime();
		
		Wrapper wrapper = new Wrapper(task);
		
		while(wrapper.needRestart && wrapper.retryCount < RETRY_LIMIT || wrapper.retryCount == 0) {
			
			if(wrapper.needRestart) {

				if (account == null) {
					try {
						account = Account.getAccountByDomain("zbj.com");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (proxy == null) {
					try {
						proxy = com.sdyk.ai.crawler.zbj.model.Proxy.getValidProxy("aliyun");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				this.close();
				// 重启chromedriver
				this.init();

				// 超时重新登录
				loginRestart();
			}
			
			task.setException(null);
			wrapper.run();
			
			if(task.getResponse().getSrc() == null && task.getResponse().getText() == null) {
				wrapper.needRestart = true;
			}
			wrapper.retryCount ++;
			this.usageCount ++;
		}
		
		if(this.usageCount > USAGE_COUNT_LIMIT) {
			this.close();
			this.init();
		}
		
		wrapper = null;
	}
	
	/**
	 * 同步执行任务 可以设定超时时间
	 * @param task
	 * @param timeout
	 */
	public synchronized void fetch(Task task, long timeout) {

		task.setStartTime();
		Wrapper wrapper = new Wrapper(task);
		
		while(wrapper.needRestart && wrapper.retryCount < RETRY_LIMIT || wrapper.retryCount == 0) {
			
			if(wrapper.needRestart) {
				
				this.close();
				this.init();
			}
			task.setException(null);
			
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			final Future<?> future = executor.submit(wrapper);
			executor.shutdown();
			
			try {
				future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (TimeoutException | InterruptedException | ExecutionException e){
				task.setException(e);
				
				// Task 将抛出 java.io.IOException: Stream closed
				future.cancel(true);
			} finally {
				task.setDuration();
			}
			
			if (!executor.isTerminated()){
				executor.shutdownNow();
			}

			if(task.getResponse().getSrc() == null && task.getResponse().getText() == null) {
				wrapper.needRestart = true;
			}
			
			wrapper.retryCount ++;
			this.usageCount ++;
		}
		
		if(this.usageCount > USAGE_COUNT_LIMIT) {
			this.close();
			this.init();
		}
		
		wrapper = null;
	}

	/**
	 * 返回 ChromeDriver 对象
	 * @return
	 */
	public synchronized ChromeDriver getDriver() {
	
		this.usageCount ++;
		if(this.usageCount > USAGE_COUNT_LIMIT) {
			this.close();
			this.init();
		}

		return this.driver;
	}
	
	/**
	 * 关闭当前Agent
	 */
	public synchronized void close() {
		
		if(driver == null) return;
		
		logger.info("{} try to close chrome driver.", Thread.currentThread().getName());
		
		try {
			if(this.bmProxy != null && !this.bmProxy.isStopped()) bmProxy.stop();
		} catch (Exception e){
			logger.error("{}", Thread.currentThread().getName(), e);
		}
		
		try {
			
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			final Future<?> future = executor.submit(new Runnable(){
				public void run() {
					for (String handle : driver.getWindowHandles()) {
						driver.switchTo().window(handle);
					}
					driver.quit();
				}
			});
			executor.shutdown();
			
			try {
				future.get(10000, TimeUnit.MILLISECONDS);
			} catch (TimeoutException | InterruptedException | ExecutionException e){
				logger.error("{}", Thread.currentThread().getName(), e);
				future.cancel(true);
			}
			
			if (!executor.isTerminated()){
				executor.shutdownNow();
			}
			
		} 
		catch (Exception e){
			logger.error("{}", Thread.currentThread().getName(), e);
		}
		finally {
			
			if(EnvUtil.isHostLinux()) {
				killProcessByPid(pid);
			}
			driver = null;
		}
		
		try {
			synchronized(instances) {
				instances.remove(this);
				if(instances.size() == 0) {
					if(upstreamProxy != null) {
						upstreamProxy.stop();
						upstreamProxy = null;
					}
				}
			}
		} catch (Exception e){
			logger.error("{}", Thread.currentThread().getName(), e);
		}
		
		logger.info("{} quit.", Thread.currentThread().getName());
	}

	/**
	 * 根据pid终止chromedriver进程
	 * driver.quit() 有可能执行失败，通过这个方法强制chromedriver退出
	 * 释放资源
	 * @param pid
	 */
	private void killProcessByPid(int pid) {
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
	 * 设定代理
	 * @param pw
	 */
	public void setProxy(ProxyWrapper pw) {
		if(pw != null){
			InetSocketAddress upstreamProxyAddress = new InetSocketAddress(pw.getHost(), pw.getPort());
			bmProxy.setChainedProxy(upstreamProxyAddress);
			bmProxy.chainedProxyAuthorization(pw.getUsername(), pw.getPassword(), AuthType.BASIC);
		}
		// TODO 当Task没有代理设定时，不改变当前浏览器代理设定
		/*else {
			bmProxy.setChainedProxy(upstreamProxy.getListenAddress());
		}*/
	}

	/**
	 * 设置账户
	 * @param account
	 */
	public void setAccount(Account account) {
		this.account = account;
	}

	/**
	 *
	 * @return
	 */
	public Account getAccount() {
		return this.account;
	}

	/**
	 * 设置代理
	 * @param proxy
	 */
	public void setzbjProxy(com.sdyk.ai.crawler.zbj.model.Proxy proxy){
		this.proxy = proxy;
	}

	public com.sdyk.ai.crawler.zbj.model.Proxy getProxy() {
		return proxy;
	}

	/**
	 * 管理Cookie
	 * @param domain	域名
	 * @param url 		URL
	 * @param cookie	输入cookie
	 * @return 如果输入cookie为空，则从cookie池中选择一个合适的cookie返回
	 * @throws MalformedURLException	URL不合法
	 * @throws URISyntaxException		URL不合法
	 * @throws InterruptedException		中断异常
	 */
	public String handleCookie(String domain, String url, String cookie) throws MalformedURLException, URISyntaxException, InterruptedException {
		
		logger.trace("Begin handle cookies.");

		/**
		 * 如果需要注入cookie
		 */
		if (cookie != null) {
			
			/*openTab("http://www.baidu.com");*/
			driver.get("chrome://settings-frame/clearBrowserData");
			new ClickAction("#clear-browser-data-commit").run(driver);
			Thread.sleep(1000);

			driver.get(URLUtil.getProtocol(url) + "://" + URLUtil.getDomainName(url) + "/" + UUID.randomUUID().toString());
			driver.manage().deleteAllCookies();
			
			injectCookie(domain, cookie);
		}
		
		return cookie;
	}
	
	/**
	 * 生成一个新Cookie
	 * @param cookie
	 * @return
	 */
	public String getNewCookies(String domain, String cookie) {
		String newCookies = "";
		for(Cookie ci : driver.manage().getCookies() ){
			
			logger.trace("[{}]\t{}={}; ", ci.getDomain(), ci.getName(), ci.getValue());
			
			if(domain.contains(ci.getDomain().replaceAll("^.", ""))){
				if(!ci.getName().matches("(?i)Path|Expires|Domain"))
					newCookies += ci.getName() + "=" + ci.getValue() + "; ";
			}
		}
		
		newCookies = CookiesHolderManager.mergeCookies(cookie, newCookies);
		return newCookies;
	}
	
	/**
	 * 解析URL
	 * @param url
	 * @param postData
	 * @throws InterruptedException
	 * @throws SocketException
	 */
	public void getUrl(String url, String postData) throws InterruptedException, SocketException {

		try {
			driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
			driver.get(url);
		} catch (org.openqa.selenium.TimeoutException e) {
			System.out.println(12399658);
		}
		// Bypass 验证
		if(driver.getPageSource().contains("安全检查中")){
			Thread.sleep(10000);
		} else {
			Thread.sleep(2000);
		}
		
		if(driver.getPageSource().contains("Bad Gateway") || 
			driver.getPageSource().contains("Gateway Timeout"))
		{
			throw new SocketException("Connection to upstream server failed.");
		}
		
		if(postData != null && postData.length() > 0) {
			redirectPost(url, postData);
		}
	}
	
	/**
	 * 等待页面加载
	 * @param url
	 * @throws Exception
	 */
	public void waitPageLoad(String url) {
		
		DocumentSettleCondition<WebElement> settleCondition = new DocumentSettleCondition<WebElement>(
			ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));
		
		new FluentWait<WebDriver>(driver)
			.withTimeout(15, TimeUnit.SECONDS)
			.pollingEvery(settleCondition.getSettleTime(), TimeUnit.MILLISECONDS)
			.ignoring(WebDriverException.class)
			.until(settleCondition);
		
		String readyState = driver.executeScript("return document.readyState").toString();
		logger.info("{}, page ready: {}", url, readyState.equals("complete"));
	}
	
	/**
	 * 合并 iframe 中的源码
	 */
	public String getAllSrc() {
		String src = driver.getPageSource();

		/*List<WebElement> iframes = driver.findElements(By.tagName("iframe"));

		for(int i=0; i<iframes.size(); i++) {
			driver.switchTo().frame(iframes.get(i));
			src += driver.getPageSource();
			driver.switchTo().defaultContent();
		}*/
		return src;
	}

	public void loginRestart() {

		Task t = null;
		try {
			t = new Task("https://login.zbj.com/login");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		t.setProxyWrapper(proxy);
		this.fetch(t);

		// B.点击登录链接
		/*WebElement w = this.getElementWait("#headerTopWarpV1 > div > div > ul > li.item.J_user-login-status > div > span.text-highlight > a:nth-child(1)");
		w.click();*/

		// C.输入账号密码
		WebElement usernameInput = this.getElementWait("#username");
		usernameInput.sendKeys(account.getUsername());

		WebElement passwordInput = this.getElementWait("#password");
		passwordInput.sendKeys(account.getPassword());
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// D.操作验证码
		if (this.getDriver().getPageSource().contains("geetest_radar_tip_content")) {

			// D1
			// 点击识别框
			this.getElementWait(".geetest_radar_tip_content").click();

			/*Thread.sleep(3000);

			// 截图1
			FileUtil.writeBytesToFile(
					agent.shoot(".geetest_window"),
					"geetest/geetest1.png"
			);

			// 点击滑块
			new Actions(agent.getDriver()).dragAndDropBy(agent.getElementWait(".geetest_slider_button"), 5, 0).build().perform();

			Thread.sleep(5000);
			// 截图2
			FileUtil.writeBytesToFile(
					agent.shoot(".geetest_window"),
					"geetest/geetest2.png"
			);

			// 生成位移
			int offset = OpenCVUtil.getOffset("geetest/geetest1.png","geetest/geetest2.png");

			Robot bot = new Robot();
			GeeTestUtil.mouseGlide(bot, 0, 0, 926, 552, 10, 10);

			MouseEventTracker tracker = null;

			if(false) {

				// 移动滑块
				// TODO 寻找更好的模拟方法
				GeeTestUtil.mouseGlide(bot, 0, 0, 926, 552, 1000, 1000);
				bot.mousePress(InputEvent.BUTTON1_MASK);
				GeeTestUtil.mouseGlide(bot, 926, 552, 926 + offset, 552, 1000, 1000);
				bot.mouseRelease(InputEvent.BUTTON1_MASK);

				// 不识别
				new Actions(agent.getDriver())
						.dragAndDropBy(agent.getElementWait(".geetest_slider_button"), offset, 0)
						.build().perform();

				// 鼠标点击事件
				int xOff = 920;
				robot.mouseMove(xOff, 550);

				Thread.sleep(1000);

				robot.mousePress(InputEvent.BUTTON1_MASK);

				Thread.sleep(1000);
				for(int index = 1; index <= offset; index++) {
					robot.mouseMove(++xOff, 550);
					Thread.sleep(10);
				}
				robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

			} else {

				tracker = new MouseEventTracker();

			}*/

			// 等待识别
			WebDriverWait wait = new WebDriverWait(this.getDriver(), 60);
			wait.until(ExpectedConditions.or(
					ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
			));

			/*if(tracker != null) {
				tracker.serializeMovements();
			}*/

		}

		this.getElementWait("#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button").click();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 测试方法
	 * @param args
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws MalformedURLException, URISyntaxException, ParseException {


	}
}