import com.gargoylesoftware.htmlunit.javascript.host.Reflect;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.zbj.StringUtil;
import com.sdyk.ai.crawler.zbj.model.Account;

import db.OrmLiteDaoManager;
import db.Refacter;
import org.junit.Test;
import org.tfelab.txt.DateFormatUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Date;
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

}
