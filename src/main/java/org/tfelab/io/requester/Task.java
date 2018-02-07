package org.tfelab.io.requester;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.chrome.action.ChromeDriverAction;
import org.tfelab.io.requester.proxy.ProxyWrapper;
import org.tfelab.json.JSON;
import org.tfelab.txt.ChineseChar;
import org.tfelab.txt.NumberFormatUtil;
import org.tfelab.txt.StringUtil;
import org.tfelab.txt.URLUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

import static org.tfelab.io.requester.BasicRequester.autoDecode;

/**
 * HTTP 请求任务
 * @author karajan
 *
 */
@DatabaseTable(tableName = "tasks")
@DBName(value = "crawler")
public class Task {

	enum Priority {
		Normal, Prior
	}

	@DatabaseField(dataType = DataType.STRING, width = 32, id = true)
	private String id;

	@DatabaseField(dataType = DataType.ENUM_INTEGER, width = 2, canBeNull = false)
	private Priority priority = Priority.Normal;

	@DatabaseField(dataType = DataType.STRING, width = 4096)
	private String url;

	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<String, String> headers;

	@DatabaseField(dataType = DataType.LONG_STRING)
	private String post_data;

	@DatabaseField(dataType = DataType.STRING, width = 4096)
	private String cookies;

	@DatabaseField(dataType = DataType.STRING, width = 4096)
	private String ref;

	@DatabaseField(dataType = DataType.STRING, width = 256)
	private String domain;

	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private HashMap<String, Object> params = new HashMap<>();

	@DatabaseField(dataType = DataType.STRING, width = 256)
	private String requester_class = BasicRequester.class.getSimpleName();

	// 代理出口信息
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private AccountWrapper aw;
	// 账户信息
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private ProxyWrapper pw;

	// 执行动作列表
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private ArrayList<ChromeDriverAction> actions = new ArrayList<>();

	// 返回对象
	private transient Response response = new Response();

	// 控制参数
	@DatabaseField(dataType = DataType.BOOLEAN)
	private boolean pre_proc = false;

	@DatabaseField(dataType = DataType.BOOLEAN)
	private boolean shoot_screen = false;

	@DatabaseField(dataType = DataType.BOOLEAN)
	private boolean reset_agent = false;

	// 记录参数
	@DatabaseField(dataType = DataType.DATE)
	private Date start_time;

	@DatabaseField(dataType = DataType.LONG)
	private long duration = 0;

	@DatabaseField(dataType = DataType.INTEGER)
	private int retryCount = 0;

	// 运行时异常
	private transient Exception e;

	// 异常记录
	@DatabaseField(dataType = DataType.SERIALIZABLE)
	private ArrayList<String> exceptions = new ArrayList<>();

	@DatabaseField(dataType = DataType.DATE, canBeNull = false)
	private Date create_time = new Date();

	private Task() {
		this.response = new Response();
	}
	
	/**
	 * 简单 GET 请求
	 * @param url url地址
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public Task(String url) throws MalformedURLException, URISyntaxException {
		this.url = url;
		domain = URLUtil.getDomainName(url);
		this.response = new Response();
		this.id = StringUtil.MD5(url + System.nanoTime());
	}
	
	/**
	 * 简单 POST 请求
	 * @param url url 地址
	 * @param post_data post data 字符串格式
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public Task(String url, String post_data) throws MalformedURLException, URISyntaxException {
		
		this.url = url;
		this.post_data = post_data;
		domain = URLUtil.getDomainName(url);
		
		this.response = new Response();
		this.id = StringUtil.MD5(url + post_data + System.nanoTime());
	}
	
	/**
	 * 需要 Cookie 的 POST 请求
	 * @param url
	 * @param post_data
	 * @param cookies
	 * @param ref
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public Task(String url, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		
		this.url = url;
		this.post_data = post_data;
		this.cookies = cookies;
		this.ref = ref;
		domain = URLUtil.getDomainName(url);
		
		this.response = new Response();
		this.id = StringUtil.MD5(url + post_data + cookies + System.nanoTime());
	}

	/**
	 * 完整参数请求
	 * @param url
	 * @param headers
	 * @param post_data
	 * @param cookies
	 * @param ref
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public Task(String url, HashMap<String, String> headers, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {

		this.url = url;
		this.headers = headers;
		this.post_data = post_data;
		this.cookies = cookies;
		this.ref = ref;
		domain = URLUtil.getDomainName(url);

		this.response = new Response();
		this.id = StringUtil.MD5(url + post_data + cookies + System.nanoTime());
	}

	public String getId() {
		return this.id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getPost_data() {
		return post_data;
	}

	public String getCookies() {
		return cookies;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getDomain() {
		return domain;
	}

	public ProxyWrapper getProxyWrapper() {
		return pw;
	}

	public void setProxyWrapper(ProxyWrapper pw) {
		this.pw = pw;
	}

	public AccountWrapper getAccountWrapper() {
		return this.aw;
	}

	public void setAccount(AccountWrapper aw) {
		this.aw = aw;
	}

	public List<ChromeDriverAction> getActions() {
		return actions;
	}

	public void addAction(ChromeDriverAction action) {
		this.actions.add(action);
	}

	public void setResponse() {
		response = new Response();
	}

	public Response getResponse() {
		return response;
	}

	public boolean isPre_proc() {
		return pre_proc;
	}

	public void setPre_proc(boolean pre_proc) {
		this.pre_proc = pre_proc;
	}

	public boolean isShoot_screen() {
		return shoot_screen;
	}

	public void setShoot_screen(boolean shoot_screen) {
		this.shoot_screen = shoot_screen;
	}

	public boolean isReset_agent() {
		return reset_agent;
	}

	public void setReset_agent(boolean reset_agent) {
		this.reset_agent = reset_agent;
	}

	public void setStartTime() {
		this.start_time = new Date();
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration() {
		this.duration = System.currentTimeMillis() - this.start_time.getTime();
	}

	public Exception getException() {
		return e;
	}

	public void setException(Exception e) {
		this.e = e;
	}


	public List<Task> postProc() throws Exception {
		return null;
	}

	public String toJSON() {
		return JSON.toJson(this);
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void addRetryCount() {
		this.retryCount ++;
	}

	public String getRequester_class() {
		return requester_class;
	}

	public void setRequester_class(String requester_class) {
		this.requester_class = requester_class;
	}

	public List<String> getExceptions() {
		return exceptions;
	}

	public void addExceptions(String exception) {
		this.exceptions.add(exception);
	}

	public String getParamString(String key) {
		if(params.get(key) == null) return null;

		return String.valueOf(params.get(key));
	}

	public int getParamInt(String key) {
		if(params.get(key) == null) return 0;
		//return Integer.valueOf((int) params.get(key));
		return NumberFormatUtil.parseInt(String.valueOf(params.get(key)));
	}

	public void setParam(String key, Object object) {
		this.params.put(key, object);
	}

	public boolean isPrior() {
		return priority == Priority.Prior;
	}

	public void setPrior() {
		this.priority = Priority.Prior;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception {

		Dao<Task, String> dao = OrmLiteDaoManager.getDao(Task.class);

		if (dao.create(this) == 1) {
			return true;
		}

		return false;
	}

	/**
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static Task getTask(String id) throws Exception {
		Dao<Task, String> dao = OrmLiteDaoManager.getDao(Task.class);
		return dao.queryForId(id);
	}

	/**
	 * 返回对象
	 * @author karajan
	 */
	public class Response {

		private Map<String, List<String>> header;
		private byte[] src;
		private String encoding;
		private String cookies;

		private boolean actionDone;

		private String text;

		private byte[] screenshot = null;

		private Exception e = null;

		public Map<String, List<String>> getHeader() {
			return header;
		}

		public void setHeader(Map<String, List<String>> header) {
			this.header = header;
		}

		public byte[] getSrc() {
			return src;
		}

		public void setSrc(byte[] src) {
			this.src = src;
		}

		public String getEncoding() {
			return encoding;
		}

		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		public String getCookies() {
			return cookies;
		}

		public void setCookies(String cookies) {
			this.cookies = cookies;
		}

		public boolean isActionDone() {
			return actionDone;
		}

		public void setActionDone(boolean actionDone) {
			this.actionDone = actionDone;
		}

		public String getText() {
			return text;
		}

		public byte[] getScreenshot() {
			return screenshot;
		}

		public void setScreenshot(byte[] screenshot) {
			this.screenshot = screenshot;
		}

		public Exception getException() {
			return e;
		}

		public void setException(Exception e) {
			this.e = e;
		}

		/**
		 * 判断 Response 是否为文本
		 * @return
		 */
		public boolean isText(){
			if(header == null) return true;
			if(header.get("Content-Type") != null){
				for(String item: header.get("Content-Type")){
					if((item.contains("application") && !item.contains("json") && !item.contains("xml") && !item.contains("x-javascript"))
						|| item.contains("video")
						|| item.contains("audio")
						|| item.contains("image")
					){
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 文本内容预处理
		 * @param input 原始文本
		 * @throws UnsupportedEncodingException
		 */
		public void setText(String input) throws UnsupportedEncodingException {

			this.text = input;

			if(isPre_proc()) {

				try {
					text = StringEscapeUtils.unescapeHtml4(text);
					text = ChineseChar.unicode2utf8(text);
				} catch (Exception e) {
					e.printStackTrace();
				}

				/* src = ChineseChar.unescape(src); */
				text = ChineseChar.toDBC(text);
				/* text = ChineseChar.toSimp(text); */
				text = text.replaceAll("　+|	+| +| +", " ");
				text = text.replaceAll(">\\s+", ">");
				text = text.replaceAll("\\s+<", "<");
				text = text.replaceAll("(\r?\n)+", "\n");
				/* src = src.replaceAll("<!\\[CDATA\\[|\\]\\]>", ""); */

				text = url + "\n" + post_data + "\n" + text;
			}
		}

		/**
		 * 文本内容预处理
		 * @throws UnsupportedEncodingException
		 */
		public void setText() throws UnsupportedEncodingException {
			String input = autoDecode(src, encoding);
			setText(input);
		}
	}
}
