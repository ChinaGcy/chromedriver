package org.tfelab.io.requester.chrome;

import org.tfelab.opencv.OpenCVUtil;
import com.sdyk.ai.crawler.zbj.model.Account;
import org.tfelab.simulator.mouse.Action;
import org.tfelab.simulator.mouse.MouseEventModeler;
import org.tfelab.simulator.mouse.MouseEventSimulator;
import com.typesafe.config.Config;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.RequestFilter;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.mitm.manager.ImpersonatingMitmManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.tfelab.common.Configs;
import org.tfelab.io.requester.Requester;
import org.tfelab.io.requester.Task;
import org.tfelab.io.requester.chrome.action.ChromeDriverAction;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;
import org.tfelab.txt.StringUtil;
import org.tfelab.util.EnvUtil;
import org.tfelab.util.FileUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.security.Security;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import static org.tfelab.io.requester.chrome.ChromeDriverUbuntuUtil.checkXvfb;
import static org.tfelab.io.requester.chrome.ChromeDriverUbuntuUtil.getPid;
import static org.tfelab.io.requester.chrome.ChromeDriverUbuntuUtil.killProcessByPid;

/**
 * Chrome请求器
 * karajan@2017.9.17
 */
public class ChromeDriverAgent extends DefaultEventExecutor {

	public static final Logger logger = LogManager.getLogger(ChromeDriverAgent.class.getName());

	// 连接超时时间
	private static int CONNECT_TIMEOUT;

	// 读取超时时间
	private static int READ_TIMEOUT;

	// 获取元素超时时间
	private static int GET_ELEMENT_TIMEOUT = 10;

	// 配置设定
	static {

		// A. 读取配置文件
		Config ioConfig = Configs.getConfig(Requester.class);
		CONNECT_TIMEOUT = ioConfig.getInt("connectTimeout");
		READ_TIMEOUT = ioConfig.getInt("readTimeout");

		// B. 设定chromedriver executable
		if (EnvUtil.isHostLinux()) {
			System.setProperty("webdriver.chrome.driver", Configs.getConfig(Requester.class).getString("chromeDriver"));
			checkXvfb();
		} else {
			System.setProperty("webdriver.chrome.driver", Configs.getConfig(Requester.class).getString("chromeDriver") + ".exe");
		}

		// C. Set log file path
		System.setProperty("webdriver.chrome.logfile", "webdriver.chrome.log");

		// D. Add BouncyCastleProvider 接受特定环境的HTTPS策略
		Security.addProvider(new BouncyCastleProvider());
	}

	// 所有实例的列表
	public static final List<ChromeDriverAgent> instances = new ArrayList<ChromeDriverAgent>();

	// 参数标签
	private Set<Flag> flags;

	// 代理地址
	private InetSocketAddress proxy;

	// 存储用于启动 ChromeDriver 的 capabilities
	private DesiredCapabilities capabilities;

	// ChromeDriver 句柄
	private ChromeDriver driver;

	// Linux chrome 进程 pid, 用于控制chrome强制退出
	public int pid = 0;

	// 代理服务器
	public BrowserMobProxyServer bmProxy;

	// 启动后的初始化脚本
	private List<AutoScript> autoScripts = new ArrayList<>();

	public enum Status {
		NEW,
		BUSY,
		IDLE
	}

	public class StatusException extends Exception {};

	/**
	 * 启动标签类
	 */
	public static enum Flag {
		MITM
	}

	/**
	 * 自动执行脚本
	 */
	public class AutoScript implements Runnable, JSONable<AutoScript> {

		transient boolean success = false;

		public AutoScript() {}

		@Override
		public void run() {
			success = true;
		}

		@Override
		public String toJSON() {
			return JSON.toJson(this);
		}
	}

	/**
	 * 登录脚本
	 */
	public class LoginScript extends AutoScript {

		String url = "https://login.zbj.com/login";

		Account account;

		String usernameCssPath = "#username";
		String passwordCssPath = "#password";
		String loginButtonCssPath = "#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button";

		String errorMsgReg = "账号或密码错误";

		transient boolean success = false;

		public LoginScript() {}

		/**
		 *
		 * @return
		 */
		boolean fillUsernameAndPassword() {

			try {
				getUrl(url);

				Thread.sleep(5000);

				waitPageLoad(url);

				// 输入账号
				WebElement usernameInput = getElementWait(usernameCssPath);
				usernameInput.sendKeys(account.getUsername());

				// 输入密码
				WebElement passwordInput = getElementWait(passwordCssPath);
				passwordInput.sendKeys(account.getPassword());

				Thread.sleep(1000);
				return true;

			} catch (Exception e) {

				logger.error(e);
				return false;
			}
		}

		boolean clickSubmitButton() {

			try {
				// 点击登录框
				getElementWait(loginButtonCssPath).click();

				Thread.sleep(5000);
				waitPageLoad(url);

			} catch (Exception e) {
				logger.error(e);
				return false;
			}

			if (getDriver().getPageSource().matches(errorMsgReg)) {
				return false;
			} else {
				return true;
			}
		}

		@Override
		public void run() {

			if(!fillUsernameAndPassword()) return;

			if(clickSubmitButton()) {
				this.success = true;
			}
		}

		@Override
		public String toJSON() {
			return JSON.toJson(this);
		}
	}

	/**
	 * 登录脚本 bypass GeeTest
	 */
	public class LoginWithGeetestScript extends LoginScript {

		String geetestContentCssPath = ".geetest_radar_tip";

		String geetestWindowCssPath = ".geetest_window";
		String geetestSliderButtonCssPath = ".geetest_slider_button";
		String geetestSuccessMsgCssPath = ".geetest_success_radar_tip_content";

		String geetestResetTipCssPath = ".geetest_reset_tip_content";
		String geetestRefreshButtonCssPath = "a.geetest_refresh_1";
		String geetestRefreshTooManyErrorCssPath = "span.geetest_radar_error_code";

		transient int geetest_retry_count = 0;

		public LoginWithGeetestScript() {}

		public int getOffset() throws IOException, InterruptedException {

			String ts = System.currentTimeMillis() + "-" + StringUtil.uuid();
			String img_1_path = "tmp/geetest/geetest-1-" + ts + ".png";
			String img_2_path = "tmp/geetest/geetest-2-" + ts + ".png";

			// 拖拽前截图
			FileUtil.writeBytesToFile(shoot(geetestWindowCssPath), img_1_path);

			// 点击滑块，向右拖5px
			// TODO 此方法在未来可能被GeeTest屏蔽
			new Actions(ChromeDriverAgent.this.getDriver())
					.dragAndDropBy(getElementWait(geetestSliderButtonCssPath), 5, 0)
					.build().perform();

			// 等待图片加载
			Thread.sleep(5000);

			// 简单拖拽后截图，截图中会包含目标拖拽位置
			FileUtil.writeBytesToFile(shoot(geetestWindowCssPath), img_2_path);

			// 生成位移
			return OpenCVUtil.getOffset(img_1_path, img_2_path);
		}

		/**
		 * 鼠标操作
		 * @param offset 像素差
		 * @param sys_error_x 误差
		 * @throws Exception
		 */
		void mouseManipulate(int offset, int sys_error_x) throws Exception {

			int x = getElementWait(geetestSliderButtonCssPath).getLocation().x + 30;
			int y = getElementWait(geetestSliderButtonCssPath).getLocation().y + 135 ;

			Robot bot = new Robot();
			bot.mouseMove(x, y);

			List<Action> actions = MouseEventModeler.getInstance().getActions(offset + sys_error_x);

			MouseEventSimulator simulator = new MouseEventSimulator(actions);

			simulator.procActions();
		}

		/**
		 * 滑块验证
		 * @throws Exception
		 */
		void bypass() throws Exception {

			// 第一次
			if(geetest_retry_count == 0) {
				// 点击识别框
				try {
					ChromeDriverAgent.this.getElementWait(geetestContentCssPath).click();
				}
				// 如果页面上没有GeeTest识别框，直接返回
				catch (Exception e) {
					return;
				}
			}
			// 第 2 - N 次
			else {

				// 验证DIV未关闭
				if(getDriver().getPageSource().contains(
						geetestSliderButtonCssPath.substring(1, geetestSliderButtonCssPath.length())
				)
						) {
					// 点击刷新按钮
					getElementWait(geetestRefreshButtonCssPath).click();
				}
				// 验证DIV已经关闭
				else {

					// 主页面出现 尝试过多 请点击重试 01
					if(getElementWait(geetestRefreshTooManyErrorCssPath).getText().equals("01")) {
						// 点击重试连接
						getElementWait(geetestResetTipCssPath).click();
					}
					else {
						//TODO
					}
				}
			}

			Thread.sleep(3000);

			// 此时验证DIV已经打开
			mouseManipulate(getOffset(), 0);

			geetest_retry_count++;

			try {
				// 识别成功
				ChromeDriverAgent.this.getElementWait(geetestSuccessMsgCssPath);
			} catch (org.openqa.selenium.TimeoutException e) {
				// 重试
				if(geetest_retry_count < 5) {
					bypass();
				} else {
					throw new ByPassErrorException();
				}
			}
		}

		class ByPassErrorException extends Exception {}

		@Override
		public void run() {

			if(!fillUsernameAndPassword()) return;

			try {
				bypass();
			} catch (Exception e) {
				logger.error("GeeTest bypass error, ", e);
				return;
			}

			if(clickSubmitButton()) {
				this.success = true;
			}
		}

		@Override
		public String toJSON() {
			return JSON.toJson(this);
		}
	}

	/**
	 * 初始化类
	 */
	public class Init implements Callable<Boolean> {

		@Override
		public Boolean call() {

			logger.info("Init...");

			buildCapabilities();

			driver = null;

			File userDir = new File("chrome_user_dir/" + this.hashCode());
			logger.info("User dir: {}", userDir.getAbsolutePath());

			synchronized (instances) {

				instances.add(ChromeDriverAgent.this);

				// 启动 Chrome
				try {
					driver = new ChromeDriver(capabilities);
					logger.info("Create chromedriver done.");
				} catch (Exception e) {
					logger.error("Error create chromedriver", e);
					return false;
				}

				// 极端情况处理，2s之后chromedriver仍未正常启动，则进行pid的再次获取
				if (EnvUtil.isHostLinux()) {

					pid = getPid();

					if (pid == 0) {
						pid = getPid();

						if (pid == 0) {
							// Chrome 貌似成功启动，但是没法正常获得pid
							logger.warn("No valid chromedriver pid detected, but chromedriver looks fine.");
						}
					}
				}
			}

			logger.info("New chromedriver pid:{}", pid);

			// 设置脚本运行超时参数
			driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);

			// 设置等待超时参数
			driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

			// 这个值要设置的比较大
			// 否则会出现 org.openqa.selenium.TimeoutException: timeout: cannot determine loading status
			// karajan 2018/4/4
			driver.manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);

			logger.info("ChromeDriver Init done, Execute auto scripts.");

			boolean execute_success = true;
			for(AutoScript script : ChromeDriverAgent.this.autoScripts) {
				script.run();
				execute_success = execute_success && script.success;
			}

			logger.info("Auto scripts execute {}.", execute_success? "succeed" : "failed" );

			return true;
		}
	}

	/**
	 * 关闭当前 ProxyServer / chromeDriver
	 * 并尝试删除 chromedriver / chrome pid
	 */
	public class Close implements Callable<Boolean> {

		public Boolean call() {

			if(driver == null) return false;

			logger.info("Closing chromedriver ... pid:{}.", pid);

			// 关闭 ProxyServer
			try {

				if(bmProxy != null && !bmProxy.isStopped()) {
					bmProxy.stop();
				}

				bmProxy = null;

			} catch (Exception e){

				logger.error("Close BMProxy Error, ", e);
			}

			// 关闭ChromeDriver
			try {

				for (String handle : driver.getWindowHandles()) {
					driver.switchTo().window(handle);
				}

				driver.quit();
			}
			catch (Exception e){
				logger.error("Close ChromeDriver error, ", e);
			}
			finally {
				// 直接kill pid
				if(EnvUtil.isHostLinux()) {
					killProcessByPid(pid);
				}
				driver = null;
			}

			try {
				synchronized(instances) {
					instances.remove(this);
				}
			} catch (Exception e){
				logger.error(e);
			}

			logger.info("ChromeDriver pid:{} closed.", pid);

			return true;
		}
	}

	/**
	 * 任务封装
	 * @author karajan@tfelab.org
	 * 2017年3月22日 上午10:04:11
	 */
	class Wrapper implements Callable<Task> {

		Task task;

		/**
		 * @param task 采集任务
		 */
		Wrapper(Task task) {
			this.task = task;
			this.task.setStartTime();
			this.task.setException(null);
		}

		/**
		 * 主要任务执行方法
		 */
		public Task call() {

			boolean needRestart = false;

			logger.info("{}", task.getUrl());

			try {

				ChromeDriverAgent.this.getUrl(task.getUrl());
				ChromeDriverAgent.this.waitPageLoad(task.getUrl());

				// 正常解析到页面
				if(!driver.getCurrentUrl().matches("^data:.+?")) {

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
				}
			}
			// 需要重启
			catch (NoSuchWindowException e) {
				logger.error(e);
				task.setException(e);
				needRestart = true;
			}
			catch (UnreachableBrowserException e) {
				logger.error(e);
				task.setException(e);
				needRestart = true;
			}
			// 操作超时，譬如判断页面是否成功加载时候，会抛这个异常
			catch (org.openqa.selenium.TimeoutException e) {
				/*logger.error(e);
				task.setException(e);
				needRestart = true;*/
			}
			catch (NoSuchSessionException e) {
				logger.error(e);
				task.setException(e);
				needRestart = true;
			}
			catch (WebDriverException e) {
				logger.error(e);
				task.setException(e);
				needRestart = true;
				if(e.getMessage().contains("chrome not reachable")
						|| e.getMessage().contains("session deleted")
						|| e.getMessage().contains("unknown error")
						|| e.getMessage().contains("unexpectedly died")
						) {

				}
			}
			catch (Exception e) {
				logger.error("Unknown Error.", e);
				task.setException(e);
			}
			finally {
				task.setDuration();
				// 停止页面加载
				driver.executeScript("window.stop()");
			}

			if(needRestart) {
				// TODO 执行重启
			}

			return task;
		}
	}


	/**
	 *
	 */
	public ChromeDriverAgent(InetSocketAddress proxy, Flag... flags) {

		this.proxy = proxy;

		this.capabilities = this.buildCapabilities();

		this.flags = new HashSet<Flag>(Arrays.asList(flags));

		init();
	}

	/**
	 * 生成 Capabilities
	 * 执行时间点：任意
	 * @return
	 */
	private DesiredCapabilities buildCapabilities() {

		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		capabilities.setPlatform(Platform.WIN8);

		// Set no loading images
		// 此处代码可能没有效果
		/*Map<String, Object> contentSettings = new HashMap<String, Object>();
		contentSettings.put("images", 2);

		Map<String, Object> preferences = new HashMap<String, Object>();
		preferences.put("profile.default_content_settings", contentSettings);

		capabilities.setCapability("chrome.prefs", preferences);*/

		// 设置 chromedriver.exe 日志级别
		LoggingPreferences logPrefs = new LoggingPreferences();
		logPrefs.enable(LogType.PERFORMANCE, Level.INFO);

		// 设置 Capabilities
		capabilities.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

		/*Map<String, Object> perfLogPrefs = new HashMap<String, Object>();
		perfLogPrefs.put("traceCategories", "browser,devtools.timeline,devtools"); // comma-separated trace categories*/

		// 设定 Chrome 代理
		if(proxy != null) {

			Proxy seleniumProxy;

			if (this.flags.contains(Flag.MITM)) {

				bmProxy = buildBMProxy(proxy);
				seleniumProxy = ClientUtil.createSeleniumProxy(bmProxy);

			} else {
				seleniumProxy = new Proxy();
				seleniumProxy.setHttpProxy(proxy.toString());
			}

			capabilities.setCapability("proxy", seleniumProxy);
		}

		// 设定Session超时后重启动
		capabilities.setCapability("recreateChromeDriverSessions", true);
		capabilities.setCapability("newCommandTimeout", 120);

		// 只加载html的DOM，不会加载js
		// https://stackoverflow.com/questions/43734797/page-load-strategy-for-chrome-driver
		capabilities.setCapability("pageLoadStrategy", "none");

		// 禁用页面提示信息
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("profile.default_content_setting_values.notifications", 2);

		// ChromeOptions 设定 增强Chrome稳定性
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

		// 加载禁用图片插件
		/*File block_image_crx = new File("chrome_ext/Block-image_v1.0.crx");
		if (block_image_crx.exists()) {
			options.addExtensions(new File("chrome_ext/Block-image_v1.0.crx"));
		}*/
		capabilities.setCapability(ChromeOptions.CAPABILITY, options);

		return capabilities;
	}

	/**
	 * 初始化一个 BrowserMobProxyServer
	 * 执行时间点：任意
	 * @param proxy upstream proxy address
	 * @return BrowserMobProxyServer
	 */
	public static BrowserMobProxyServer buildBMProxy(InetSocketAddress proxy) {

		BrowserMobProxyServer bmProxy = new BrowserMobProxyServer();
		bmProxy.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
		bmProxy.setRequestTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS);

		/**
		 * 设定上游代理地址
		 */
		if(proxy != null) {
			bmProxy.setChainedProxy(proxy);
		}

		bmProxy.setTrustAllServers(true);
		bmProxy.setMitmManager(ImpersonatingMitmManager.builder().trustAllServers(true).build());
		bmProxy.start(0); // Use any free port

		return bmProxy;
	}

	/**
	 * MITM 监听
	 * 对请求信息进行过滤监听
	 * @param requestFilter 请求过滤器
	 */
	public void addProxyRequestFilter(RequestFilter requestFilter) {

		if(bmProxy != null)
			bmProxy.addRequestFilter(requestFilter);
	}

	/**
	 * MITM 监听
	 * 对请求信息进行过滤监听
	 * @param responseFilter 响应过滤器
	 */
	public void addProxyResponseFilter(ResponseFilter responseFilter) {
		if(bmProxy != null)
			bmProxy.addResponseFilter(responseFilter);
	}

	/**
	 * 添加自运行脚本
	 * @param json
	 * @param className
	 * @throws Exception
	 */
	public void addAutoScript(String json, String className) throws Exception {

		Class<LoginScript> clazz = (Class<LoginScript>) Class.forName(className);
		this.autoScripts.add(JSON.fromJson(json, clazz));
	}

	/**
	 *
	 * @param dimension
	 */
    public void setSize(Dimension dimension) {

		if(driver != null) {
			// Dimension dimension = new Dimension(1024, 600);
			driver.manage().window().setSize(dimension);
		}
	}

	/**
	 *
	 * @param startPoint
	 */
	public void setPosition(Point startPoint) {

		if(driver != null) {
			/*Random r = new Random();
			Point startPoint = new Point(60 * r.nextInt(10), 40 * r.nextInt(10));*/
			driver.manage().window().setPosition(startPoint);
		}
	}

	/**
	 * 找到特定元素
	 * @param path
	 * @return
	 */
	public WebElement getElementWait(String path) {
		WebDriverWait wait = new WebDriverWait(driver, GET_ELEMENT_TIMEOUT);
	    return wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(path)));
	}
	
	/**
	 * 在特定元素上执行JavaScript脚本
	 * Really should only be used when the web driver is sucking at exposing
	 * functionality natively
	 * @param script The script to execute
	 * @param element The target of the script, referenced as arguments[0]
	 */
	public void trigger(String script, WebElement element) {
	    ((JavascriptExecutor) driver).executeScript(script, element);
	}

	/**
	 * 执行JavaScript脚本
	 * @note Really should only be used when the web driver is sucking at exposing
	 * functionality natively
	 * @param script The script to execute
	 */
	public Object trigger(String script) {
	    return ((JavascriptExecutor) driver).executeScript(script);
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
	 * @param imgPath 图片的CSS路径
	 * @return 图片byte数组
	 * @throws IOException ImageIO异常
	 */
	public byte[] shoot(String imgPath) throws IOException {
		
		WebElement element = getElementWait(imgPath);
		
		File screen = driver.getScreenshotAs(OutputType.FILE);

		Point p = element.getLocation();

		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();

		Rectangle rect = new Rectangle(width, height);

		// 先整体截图
		BufferedImage img = null;
		img = ImageIO.read(screen);

		// 再根据元素相对位置抠图
		BufferedImage dest = img.getSubimage(p.getX(), p.getY(), rect.width, rect.height);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(dest, "png", baos);
		
		return baos.toByteArray();
	}

	/**
	 * @return ChromeDriver 对象
	 */
	public synchronized ChromeDriver getDriver() {

		return this.driver;
	}

	/**
	 * 解析URL
	 * @param url 需要访问的URL地址
	 * @throws InterruptedException
	 * @throws SocketException
	 */
	public void getUrl(String url) throws InterruptedException, SocketException {

		driver.get(url);

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
	}

	/**
	 * 等待页面加载
	 * @param url
	 * @throws Exception
	 */
	private void waitPageLoad(String url) {

		/*DocumentSettleCondition<WebElement> settleCondition = new DocumentSettleCondition<WebElement>(
			ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body")));

		new FluentWait<WebDriver>(driver)
			.withTimeout(10, TimeUnit.SECONDS)
			.pollingEvery(settleCondition.getSettleTime(), TimeUnit.MILLISECONDS)
			.ignoring(WebDriverException.class)
			.until(settleCondition);*/

		String readyState = driver.executeScript("return document.readyState").toString();
		logger.info("{} page ready: {}", url, readyState.equals("complete"));
	}

	/**
	 * 合并 iframe 中的源码
	 * TODO 遍历所有iframe
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


	/**
	 * 同步执行任务
	 * @param task
	 */
	public synchronized void fetch(Task task) {

		task.setStartTime();
		
		Wrapper wrapper = new Wrapper(task);
		task.setException(null);
		wrapper.run();

		if(wrapper.needRestart) {
			this.close();
			// 重启chromedriver
			try {
				this.init();
			} catch (ChromeInitException e) {
				logger.error("Cann't init chrome, exit.", e);
				System.exit(-1);
			}
		}

		wrapper = null;
	}
	
	/**
	 * 同步执行任务 可以设定超时时间
	 * @param task
	 * @param timeout
	 */
	public synchronized void fetch(Task task, long timeout) {

		Wrapper wrapper = new Wrapper(task);

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

		//

		wrapper = null;
	}
}