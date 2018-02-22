package com.sdyk.ai.crawler.zbj;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.tfelab.txt.NumberFormatUtil;

import java.util.ArrayList;
import java.util.List;
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

	/**
	 * 清洗数据，获得a，img地址，并且保留干净的a，img 标签
	 * @param in
	 * @param extraUrls_img
	 * @param extraUrls_a
	 * @return
	 */
	public static String cleanContent(String in, Set<String> extraUrls_img, Set<String> extraUrls_a){

		String out = "<p>" + in + "</p>";

		// 去HTML注释
		out = out.replaceAll("<!--.*?-->", "");
		out = out.replaceAll("<?xml.*?>", "");
		out = out.replaceAll("(?i)<!--[if !IE]>.*?<![endif]-->", "");

		// 去掉<span>标签
		out = out.replaceAll("(?is)<span([^>]*)>|</span>|<wbr>", "");

		out = out.replaceAll("(?is)<div class=\"cnblogs_code\">.+?</div>", "");

		// 去掉隐藏的HTML对象
		out = out.replaceAll("<[^>]*?display:none.*?>.*?</.*?>", "");

		// 特殊转义符替换
		out = out.replaceAll("(&nbsp;?)+", " ");
		out = out.replaceAll("　+|	+| +| +", " "); // 将特殊空格转换为标准空格
		out = out.replaceAll("[\uE000-\uF8FF]", " "); // 将UTF-8编码的特殊空白符号转化为标准空格
		out = out.replaceAll("&lt;+", "<");
		out = out.replaceAll("&gt;+", ">");
		out = out.replaceAll("\\\\", "/");

		// 去掉特殊控制符
		char c1 = (char) 0;
		char c2 = (char) 31;
		out = out.replaceAll("[" + c1 + "-" + c2 + "]+", "");

		// 去掉多余的空白符
		out = out.replaceAll(">\\s+", ">");
		out = out.replaceAll("\\s+<", "<");

		// 去掉<a>标签
		//out = out.replaceAll("(?si)<a.*?>|</a>", "");

		// 删除javascript标记
		// TODO 此行代码应该下移
		out = out.replaceAll("javascript:.+?(?=['\" ])", "");

		String out1 = out;

		// 清洗 img 标签，只保留 src 属性
		Matcher matcher = Pattern.compile("(?si)<img.*?>").matcher(out);
		List<String> imgs = new ArrayList<String>();
		while(matcher.find()){

			String imgUrl = matcher.group().replaceAll("^.*?src=['\"]?", "").replaceAll("[ \"'>].*?$", "");
			if(imgUrl.length()>10) {
				extraUrls_img.add(imgUrl);
				imgs.add("<img src=\"" + imgUrl + "\">");
			} else {
				imgs.add("");
			}
		}

		matcher = Pattern.compile("(?si)<img.*?>").matcher(out);
		int i = 0;
		while(matcher.find()){
			out1 = out1.replace(matcher.group(), imgs.get(i));
			i++;
		}

		// 清洗 a 标签，只保留 href 属性
		matcher = Pattern.compile("(?si)<a.*?>").matcher(out);
		List<String> as = new ArrayList<String>();
		while(matcher.find()){

			String imgUrl = matcher.group().replaceAll("^.*?href=['\"]?", "").replaceAll("[ \"'>].*?$", "");
			if(imgUrl.length()>10) {
				extraUrls_a.add(imgUrl);
				as.add("<a href=\"" + imgUrl + "\">");
			} else {
				as.add("");
			}
		}

		matcher = Pattern.compile("(?si)<a.*?>").matcher(out);
		int a = 0;
		while(matcher.find()){
			out1 = out1.replace(matcher.group(), as.get(a));
			a++;
		}

		// 去掉分页特殊文字
		out = out1.replaceAll("(?i)(上一页(\\d)*)|(下一页)", "");

		// 去掉图片题注
		out = out.replaceAll("(?i)<p([^>]*)>图\\s*\\d+.*?</p>", "");

		// 去掉数据资料来源说明
		out = out.replaceAll(
				"(?i)<p([^>]*)>(数据来源|资料来源).*?</p>", "");

		// 去掉无法识别的HTML标签
		out = out.replaceAll("(?si)document.write\\(.*?\\);?", ""); // zhuhuihua 2016/06/09
		out = out.replaceAll("(?si)<link.*?>.*?</link>", "");
		out = out.replaceAll("(?si)<script.*?>.*?</script>", "");
		out = out.replaceAll("(?si)<script.*?>|</script>", "");
		out = out.replaceAll("(?si)<style.*?>.*?</style>", "");
		out = out.replaceAll("(?si)<iframe.*?>.*?</iframe>", "");
		out = out.replaceAll("(?si)<form.*?>.*?</form>", "");
		out = out.replaceAll("(?si)<select.*?>.*?</select>", "");
		out = out.replaceAll("(?si)<input.*?>", "");
		out = out.replaceAll("(?si)<object.*?>", "");

		out = out.replaceAll("(?si)<div.*?>|</div>", "");
		out = out.replaceAll("(?si)<font.*?>|</font>", "");
		out = out.replaceAll("(?si)<center>|</center>", "");
		out = out.replaceAll("(?si)<section.*?>|</section>|</fieldset>|<fieldset.*?>", "");
		out = out.replaceAll("(?i)<o:p.*?>|</o:p.*?>", "");

		// 对 dd dl dt的处理
		out = out.replaceAll("(?si)<dd.*?>", "<dd>");
		out = out.replaceAll("(?si)<dl.*?>", "<dl>");
		out = out.replaceAll("(?si)<dt.*?>", "<dt>");
		out = out.replaceAll("(?si)<ul.*?>", "<ul>");
		out = out.replaceAll("(?si)<li.*?>", "<li>");
		out = out.replaceAll("(?si)<ol.*?>", "<ol>");

		out = out.replaceAll("(?si)<h1.*?>", "<h1>");
		out = out.replaceAll("(?si)<h2.*?>", "<h2>");
		out = out.replaceAll("(?si)<h3.*?>", "<h3>");
		out = out.replaceAll("(?si)<h4.*?>", "<h4>");
		out = out.replaceAll("(?si)<h5.*?>", "<h5>");
		out = out.replaceAll("(?si)<h6.*?>", "<h6>");
		out = out.replaceAll("(?si)<h7.*?>", "<h7>");

		out = out.replaceAll("(?si)</h1>", "</h1>");
		out = out.replaceAll("(?si)</h2>", "</h2>");
		out = out.replaceAll("(?si)</h3>", "</h3>");
		out = out.replaceAll("(?si)</h4>", "</h4>");
		out = out.replaceAll("(?si)</h5>", "</h5>");
		out = out.replaceAll("(?si)</h6>", "</h6>");
		out = out.replaceAll("(?si)</h7>", "</h7>");


		out = out.replaceAll("(?si)</?em>", "");

		//

		out = out.replaceAll("。+", "。");

		out = out.replaceAll("(?i)<wbr.*?>|<br.*?>", "</p><p>");

		// 去掉Table和p的样式 & 大小写转换
		out = out.replaceAll("(?si)<table.*?>", "<table>");
		out = out.replaceAll("(?si)</table>", "</table>");

		out = out.replaceAll("(?si)<tbody.*?>", "<tbody>");
		out = out.replaceAll("(?si)</tbody>", "</tbody>");

		out = out.replaceAll("(?si)<thead.*?>", "<thead>");
		out = out.replaceAll("(?si)</thead>", "</thead>");

		out = out.replaceAll("(?si)<col.*?>", "<col>");
		out = out.replaceAll("(?si)</col>", "</col>");

		out = out.replaceAll("(?si)<colgroup.*?>", "<colgroup>");
		out = out.replaceAll("(?si)</colgroup>", "</colgroup>");

		out = out.replaceAll("(?si)<tr.*?>", "<tr>");
		out = out.replaceAll("(?si)</tr>", "</tr>");

		out = out.replaceAll("(?si)<td.*?>", "<td>");
		out = out.replaceAll("(?si)</td>", "</td>");

		out = out.replaceAll("(?si)<th.*?>", "<th>");
		out = out.replaceAll("(?si)</th>", "</th>");

		out = out.replaceAll("(?si)<p.*?>", "<p>");
		out = out.replaceAll("(?si)</p>", "</p>");

		// 删除 qqmusic
		out = out.replaceAll("(?si)<qqmusic.*?>|</qqmusic>", "");


		// 去掉开头结尾的空白
		out = out.replaceAll("^ +| +$", "");

		// 合并嵌套<p> <b>标记
		out = out.replaceAll("(?si)(<p>[\\r\\n\\s ]*)+<p>", "<p>");
		out = out.replaceAll("(?si)(</p>[\\r\\n\\s ]*)+</p>", "</p>");

		out = out.replaceAll("(?si)(<b.*?>)+", "<b>");
		out = out.replaceAll("(?si)(</b>)+", "</b>");

		// 保留strong标签，标签大小写统一
		out = out.replaceAll("(?si)(<strong.*?>)+", "<strong>");
		out = out.replaceAll("(?i)<strong>", "<strong>");
		out = out.replaceAll("(?i)</strong>", "</strong>");

		// 特殊标点转换，全角 -> 半角
		out = out.replaceAll("．", ".");
		out = out.replaceAll("％", "%");
		out = out.replaceAll("﹐", "，");
		out = out.replaceAll("﹔", "；");
		out = out.replaceAll("。、", "。");

		// unicode编码字符
		out = out.replaceAll("&#[0-9]+;", "");

		// 中文段落中解决符号乱用
		out = out.replaceAll("(?<=[\u4E00-\u9FA5]) *& *(?=[\u4E00-\u9FA5])", "和"); // 解决&乱用
		out = out.replaceAll("(?<=[\u4E00-\u9FA5]) *[:：] *(?=[\u4E00-\u9FA5])", "："); // 冒号转换:->：
		out = out.replaceAll("(?<=[\u4E00-\u9FA5]) *[;；] *", "；");
		out = out.replaceAll("(?<=[\u4E00-\u9FA5]) *[\\?？] *", "？");
		out = StringEscapeUtils.unescapeHtml4(out);

		// 去掉空<p>标记
		out = out.replaceAll("<\\w+>[\\r\\n\\s ]*</\\w+>", "");
		out = out.replaceAll("<\\w+>[\\r\\n\\s ]*</\\w+>", "");
		out = out.replaceAll("<\\w+>[\\r\\n\\s ]*</\\w+>", "");

//		out = out.replaceAll("(?<=[\u4E00-\u9FA5]) (?=[\u4E00-\u9FA5])", ""); // 中文间空格转化为逗号
//		out = out.replaceAll(" (?=[\u4E00-\u9FA5])", ""); // 去掉中文前空格
//		out = out.replaceAll("<p>[^\u4E00-\u9FA5]+?</p>", ""); // 删除没有中文的段落

		// <p> </p> 标记的补全
		out = out.replaceAll("(?<=[\u4E00-\u9FA5：])<p>(?=[\u4E00-\u9FA5])", "</p><p>"); // 段落标记的补全
		out = out.replaceAll("(?<=[\u4E00-\u9FA5：])</p>(?=[\u4E00-\u9FA5])", "</p><p>"); // 段落标记的补全

		//out = out.replaceAll("-{2,}", "：");

//		out = out.replaceAll(
//				"(http|ftp|https)://([\\w-]+.)+[\\w-]+(/[\\w- \\./\\?%&=]*)?", ""); // 去掉URL

		// 去掉特殊无意义字符
		out = out.replaceAll("[—§№☆★○●◎⊙◇◆□■△▲※→←]", "");
//		out = out.replaceAll("[\\[【]", "“");
//		out = out.replaceAll("[\\]】]", "”");

		//out = out.replaceAll("<p>[\\(（][^<]{0,18}[）\\)]</p>", "<p></p>");

		//out = out.replaceAll("0%0%", "");
		//out = out.replaceAll("(\\.)+", ".");

		return out;
	}

}
