import com.sdyk.ai.crawler.zbj.requester.ChromeDriverLoginWrapper;
import com.sdyk.ai.crawler.zbj.util.StringUtil;

import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilTest {

	String text_1 ="<div class=\"det-left fl\">\n" +
			"                        <h2 class=\"det-left-title\">设计方案：<pre>宅大侠古风卡通形象设计</pre></h2>\n" +
			"\n" +
			"\n" +
			"                                                    <div class=\"case-file-container\">\n" +
			"                                                                    <div class=\"case-file pic\">\n" +
			"                                        <img data-original=\"http://homesitetask.zbjimg.com/homesite/task/宅大侠释义.jpg/origine/e514bc8f-768e-4796-875f-7359b31f049b?imageMogr2/format/webp\" src=\"http://homesitetask.zbjimg.com/homesite/task/宅大侠释义.jpg/origine/e514bc8f-768e-4796-875f-7359b31f049b?imageMogr2/format/webp\" class=\"det-left-img\" alt=\"宅大侠古风卡通形象设计1\" style=\"display: block;\">\n" +
			"                                    </div>\n" +
			"                                                                                                </div>\n" +
			"                        \n" +
			"                        <p class=\"det-left-foot\"></p>\n" +
			"                    </div>";
	/**
	 * 测试清洗HTML标签
	 */
	@Test
	public void testRemoveHTML() {

		String str = org.tfelab.txt.StringUtil.removeHTML(text_1);
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
			agent = new ChromeDriverLoginWrapper("zbj.com").login();
		} catch (Exception e) {
			e.printStackTrace();
		}

		org.tfelab.io.requester.Task t = new org.tfelab.io.requester.Task("http://task.zbj.com/12913633/");

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
