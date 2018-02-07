package com.sdyk.ai.crawler.zbj;

import com.google.common.collect.ImmutableMap;
import org.tfelab.txt.NumberFormatUtil;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil extends Helper {

	/**
	 * 时间转换工具
	 * TODO 半个月半年无法处理
	 * @param src
	 * @return
	 */
	public static String detectTimeSpanString(String src) {

		String timeSpanStr = null;

		String[] patterns = {".+?(周期|工期).{0,6} *(:|：)? *(?<T1>\\d+ *个?(日|天|星期|礼拜|周|月|季度|年)?).+?",
				".+?(?<T2>\\d+ *个?(日|天|星期|礼拜|周|月|季度|年)?) *(周期|工期).+?"};

		String patternStr = "";

		for(String p : patterns) {
			patternStr += p + "|";
		}

		patternStr = patternStr.substring(0, patternStr.length() - 1);

		Pattern pattern = Pattern.compile(patternStr);

		Matcher matcher = pattern.matcher(
				org.tfelab.txt.StringUtil.removeHTML(src));

		if(matcher.matches()) {
			timeSpanStr = matcher.group("T1");

		}
		if (timeSpanStr != null) {
			if (timeSpanStr.contains("个")) {
				return timeSpanStr.replaceAll("个", "");
			}
			return timeSpanStr;
		}
		return "";
	}

	/**
	 *
	 * @param src
	 * @return
	 */
	public static int getTimeSpan(String src) {

		Map<String, Integer> mapper = ImmutableMap.of(
				"天", 1,
				"日", 1,
				"周", 7,
				"星期", 7,
				"礼拜", 7

		);
		Map<String, Integer> mapper1 = ImmutableMap.of(
				"月", 30,
				"季度", 90,
				"年", 365
		);
		Pattern pattern;
		Matcher matcher;

		Set<String> set = mapper.keySet();
		Set<String> set1 = mapper1.keySet();
		for (String t: set
			 ) {
			if (src.contains(t)) {
				return Integer.parseInt(src.replaceAll(t,"").replaceAll(" ","")) * mapper.get(t);
			}
		}
		for (String t: set1
				) {
			if (src.contains(t)) {
				return Integer.parseInt(src.replaceAll(t,"").replaceAll(" ","")) * mapper1.get(t);
			}
		}
		return 0;
	}


	public static double detectBudget(String src) {

		String patternStr = ".*预算.{0,6} *(:|：)? *(?<T>\\d+ *(亿|千万|百万|万|千|百|w|W|k|K)?)(元|块)?.*";

		Pattern pattern = Pattern.compile(patternStr);

		Matcher matcher = pattern.matcher(
				org.tfelab.txt.StringUtil.removeHTML(src));

		if(matcher.matches()) {

			return NumberFormatUtil.parseDouble(matcher.group("T"));
		}

		return 0;
	}


}
