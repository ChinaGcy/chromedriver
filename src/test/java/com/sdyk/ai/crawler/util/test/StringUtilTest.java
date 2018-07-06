package com.sdyk.ai.crawler.util.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.util.StringUtil;

import one.rewind.json.JSON;
import org.junit.Test;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilTest {

	String text_1 ="<div class=\"task-detail wrapper\">\n" +
			"<div class=\"task-detail-content content\">\n" +
			"<p class=\"title\">具体要求：</p>\n" +
			"\n" +
			"\n" +
			"\n" +
			"<p class=\"task-content\">\n" +
			"\n" +
			"我需要找一名兼职的设计师，设计过瓷砖、马赛克、石材背景墙等相关的，负责新图案的开发设计，出具立面图、效果图等。熟练操作AutoCAD,3Dmax, Photoshop,CoreloDRAW等专业电脑软件。价格可以按量计算也可以按设计师要求而定。附件是效果图与实物图。<br><br>周期 预算：商议\n" +
			"</p>\n" +
			"\n" +
			"\n" +
			"\n" +
			"<p class=\"title\">附件:</p>\n" +
			"\n" +
			"<p>\n" +
			"<span class=\"download-file-name\">201407100932275937566.jpg</span>\n" +
			"<a class=\"download-link\" target=\"_blank\" href=\"//rms.zhubajie.com/resource/redirect?key=mobile/newwap/201407100932275937566.jpg/origine/bce26c57-08ee-4eb2-a820-665a367f9376\">下载</a>\n" +
			"</p>\n" +
			"\n" +
			"<p>\n" +
			"<span class=\"download-file-name\">AE-K001-2.jpg</span>\n" +
			"<a class=\"download-link\" target=\"_blank\" href=\"//rms.zhubajie.com/resource/redirect?key=mobile/newwap/AE-K001-2.jpg/origine/84524609-5166-49a2-a530-5e6f9c7a1cfd\">下载</a>\n" +
			"</p>\n" +
			"\n" +
			"\n" +
			"\n" +
			"\n" +
			"\n" +
			"\n" +
			"\n" +
			"\n" +
			"<div class=\"more-info\">\n" +
			"\n" +
			"\n" +
			"<span>\n" +
			"<i class=\"lbs\"></i>上海-上海-青浦\n" +
			"</span>\n" +
			"\n" +
			"<span>\n" +
			"<i></i>来自：猪八戒\n" +
			"</span>\n" +
			"\n" +
			"</div>\n" +
			"</div>\n" +
			"\n" +
			"<div class=\"open-close\">\n" +
			"\n" +
			"<a class=\"close-item\" href=\"javascript:;\">收起 ∧</a>\n" +
			"<a class=\"open-item hide\" href=\"javascript:;\">显示全部 ∨</a>\n" +
			"</div>\n" +
			"\n" +
			"</div>";
	/**
	 * 测试清洗HTML标签
	 */
	@Test
	public void testRemoveHTML() {

		String str = one.rewind.txt.StringUtil.removeHTML(text_1);
		System.err.println(str);
	}

	@Test
	public void testcleanHTML() {

		String str = StringUtil.cleanContent(text_1,null, null, null);
		System.err.println(str);
	}

	/**
	 * 测试解析时间段
	 */
	@Test
	public void testDetectTimeSpan() {

		String timeSpanStr = StringUtil.detectTimeSpanString(text_1);
		System.err.println(timeSpanStr);

		int span = StringUtil.getTimeSpan(timeSpanStr);
		System.err.print(span);
	}

	/**
	 *
	 */
	@Test
	public void testDetectBudget() {

		double budget = StringUtil.detectBudget(text_1);
		System.err.println(budget);
	}

	/**
	 * 本地测试
	 */
	@Test
	public void localCleanContent() {
		String s = "";
		Set<String> img_urls = new HashSet<>();
		Set<String> img_urls_a = new HashSet<>();
		List<String> fileName = new ArrayList<>();

		s = StringUtil.cleanContent(text_1, img_urls, img_urls_a, fileName);
		System.out.println(s);
		for (String ss : img_urls
				) {
			System.out.println("1--"+ss);
		}

		for (String ss : img_urls_a
				) {
			System.out.println("2--"+ss);
		}

		for (String ss : fileName
				) {
			System.out.println("3--"+ss);
		}
	}

	/**
	 * 猪八戒测试
	 * @throws Exception
	 */
	@Test
	public void testcleanContent() throws Exception {
		/*String s = "";

		Set<String> img_urls = new HashSet<>();
		Set<String> img_urls_a = new HashSet<>();
		List<String> fileName = new ArrayList<>();

		ChromeDriverAgent agent = null;

		one.rewind.io.requester.Task t = new one.rewind.io.requester.Task("http://task.zbj.com/12913633/");

		agent.submit(t);

		Pattern p = Pattern.compile("(?s)<div class=\'describe.+?<div class=\'img-item\'>");

		Matcher m = p.matcher(t.getResponse().getText());

		if(m.find()) {
			s = StringUtil.cleanContent(m.group(), img_urls, img_urls_a, fileName);
		}

		System.out.println(s);
		for (String ss : img_urls
			 ) {
			System.out.println(ss);
		}
		for (String ss : img_urls_a
				) {
			System.out.println(ss);
		}*/
	}
	@Test
	public void systemTest() {
		System.err.println(System.nanoTime());
	}

	@Test
	public void md5Test() {
		System.err.println(one.rewind.txt.StringUtil.MD5("http://task.zbj.com/5190852/"));
	}

	@Test
	public void ttest() {
		Date date = new Date(System.currentTimeMillis());
		Date date1 = new Date(System.currentTimeMillis() + 60*1000*60);

		System.err.println(date1);

	}

	@Test
	public void headTest() {
		String s = "<img src=\"https://avatar.zbjimg.com/007/07/45/200x200_avatar_95.jpg!big\" border=\"0\" onerror=\"this.onerror=null;this.src='//t4.zbjimg.com/r/p/task/200.gif'\" alt=\"光荣网络光荣文创\">";

		Set<String> head_img = new HashSet<>();

		StringUtil.cleanContent(s, head_img, null, null);

		for ( String ss : head_img) {
			System.err.println(ss);
		}

	}

	@Test
	public void test3() {
		String userId = null;
		int page = 0;

		String s = "https://home.zbj.com/20400617/?ep=2";
		Pattern pattern = Pattern.compile("https://home.zbj.com/(?<userId>\\d+)/\\?ep=(?<page>\\d+)");

		Matcher matcher = pattern.matcher(s);

		if (matcher.find()) {
			userId = matcher.group("userId");
			page = Integer.parseInt(matcher.group("page"));

			System.err.println(userId +"----------"+ page);
		}
	}

	/**
	 * 测试httpTaskPoster
	 */
	@Test
	public void httpTaskPosterTest() throws ClassNotFoundException, MalformedURLException, UnsupportedEncodingException, URISyntaxException {
		HttpTaskPoster.getInstance().submit(ProjectTask.class, new HashMap<>());
		HttpTaskPoster.getInstance().submit(ProjectTask.class,"" , new HashMap<>(), 1, "", "", "");
	}

	@Test
	public void gsonTest() throws IOException {
		Map<String, String> map = ImmutableMap.of("user_id", "20400617", "page", "1");

		String init_map_str = JSON.toJson(map);

		ObjectMapper mapper = new ObjectMapper();

		TypeReference<HashMap<String, Object>> typeRef
				= new TypeReference<HashMap<String, Object>>() {};

		Map<String, Object> init_map = mapper.readValue(init_map_str, typeRef);

		String init_map_json = JSON.toJson(init_map);

		System.err.println(init_map_json);
	}


	@Test
	public void testHsot() {
		String os = System.getProperty("os.name");

		System.err.println(os);
	}
}
