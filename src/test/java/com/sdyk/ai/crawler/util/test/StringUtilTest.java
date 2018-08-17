package com.sdyk.ai.crawler.util.test;

import com.sdyk.ai.crawler.util.StringUtil;

import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试StringUtil相关功能
 * @Author
 * @Date
 */
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

	/**
	 *
	 */
	@Test
	public void testcleanHTML() {

		String str = StringUtil.cleanContent(text_1,null);
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

		s = StringUtil.cleanContent(text_1, img_urls);
		System.out.println(s);
		for (String ss : img_urls
				) {
			System.out.println("1--"+ss);
		}
	}

	/**
	 * 猪八戒测试
	 * @throws Exception
	 */
	@Test
	public void testCleanContent() throws Exception {
		String s = "";

		Set<String> img_urls = new HashSet<>();

		ChromeDriverAgent agent = null;

		ChromeTask t = new ChromeTask("http://task.zbj.com/12913633/");

		agent.submit(t);

		Pattern p = Pattern.compile("(?s)<div class=\'describe.+?<div class=\'img-item\'>");

		Matcher m = p.matcher(t.getResponse().getText());

		if(m.find()) {
			s = StringUtil.cleanContent(m.group(), img_urls);
		}

		System.out.println(s);
		for (String ss : img_urls) {
			System.out.println(ss);
		}
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
	public void dateTest() {
		Date date = new Date(System.currentTimeMillis());
		Date date1 = new Date(System.currentTimeMillis() + 60*1000*60);

		System.err.println(date1);
	}

	@Test
	public void headTest() {
		String s = "<img src=\"https://avatar.zbjimg.com/007/07/45/200x200_avatar_95.jpg!big\" border=\"0\" onerror=\"this.onerror=null;this.src='//t4.zbjimg.com/r/p/task/200.gif'\" alt=\"光荣网络光荣文创\">";

		Set<String> head_img = new HashSet<>();

		StringUtil.cleanContent(s, head_img);

		for ( String ss : head_img) {
			System.err.println(ss);
		}

	}
	@Test
	public void Test() {
		List<String> list = new ArrayList();

		list.add("123456");
		list.add("123456");

		System.err.println(list.toString());
	}
}
