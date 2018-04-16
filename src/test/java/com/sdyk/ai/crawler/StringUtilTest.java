package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.zbj.util.StringUtil;

import org.junit.Test;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilTest {

	String text_1 ="<div class=\"taskinfo-title\"><i></i><span>任务需求</span></div>\n" +
			"                        <div class=\"task-info-content\">\n" +
			"                            <p>装修公司（高档整装）核心优势关注环保，品牌这两个字</p><p>（容易记忆，琅琅上口，接地气点最好，要么当下流行的词语或有关于重庆历史文化与重庆接地气的名字）</p><p><br></p><p>要求可以注册。</p>                        </div>\n" +
			"                                                                                                                        <div class=\"taskinfo-title\">\n" +
			"                                        <i></i>\n" +
			"                                        <span>需求补充</span>\n" +
			"                                                                                    <font class=\"font12 c396 f_l ml_10 mt_10 pt_5\">2018-03-27 10:52:31 客服审核通过</font>\n" +
			"                                                                            </div>\n" +
			"                                    <div class=\"clearfix pt_10 pb_10 font14 c666\">\n" +
			"                                        取名字要求.装修公司（高档整装）.核心优势工艺领先.关注环保.品牌就两个字（容易记忆.琅琅上口.针对全国市场.接地气点最好.要么当下流行的词语或有点历史的词语）                                    </div>\n" +
			"                                                                                                                                    <div class=\"task-info-tipsy mt_10\">温馨提示：请不要轻信需要交钱（报名费、抵押金之类）才能承接的任务。如有遇到请第一时间联系客服。</div>\n" +
			"                        <div class=\"clear\"></div>";
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
		String s = "";

		Set<String> img_urls = new HashSet<>();
		Set<String> img_urls_a = new HashSet<>();
		List<String> fileName = new ArrayList<>();

		ChromeDriverAgent agent = null;
		try {
			agent = new ChromeDriverLoginWrapper("zbj.com").login(null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		one.rewind.io.requester.Task t = new one.rewind.io.requester.Task("http://task.zbj.com/12913633/");

		agent.fetch(t);

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
		}
	}
	@Test
	public void systemTest() {
		System.err.println(System.nanoTime());
	}

}
