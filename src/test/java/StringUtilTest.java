import com.sdyk.ai.crawler.zbj.util.StringUtil;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilTest {

	String text_1 = "<div class=\"det-middle-content\">\n" +
			"                            <ul class=\"middle-content-ul\">\n" +
			"                                                                    <li class=\"middle-content-head\"><strong>客户名称：</strong>李先生</li>\n" +
			"                                \n" +
			"                                                                    <li class=\"middle-content-head\">\n" +
			"                                        <strong>类型：</strong>\n" +
			"                                                                                    企业宣传片                                                                            </li>\n" +
			"                                                            </ul>\n" +
			"                                                    </div>";
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

		String str = StringUtil.cleanContent(text_1,null, null);
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

	@Test
	public void testcleanContent() throws Exception {
		String s = "";

		Set<String> img_urls = new HashSet<>();
		Set<String> img_urls_a = new HashSet<>();

		org.tfelab.io.requester.Task t = new org.tfelab.io.requester.Task("http://task.zbj.com/12913633/");

		org.tfelab.io.requester.BasicRequester.getInstance().fetch(t);

		Pattern p = Pattern.compile("(?s)<div class=\'describe.+?<div class=\'img-item\'>");

		Matcher m = p.matcher(t.getResponse().getText());

		if(m.find()) {
			s = StringUtil.cleanContent(m.group(), img_urls, img_urls_a);
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
