package com.sdyk.ai.crawler.util;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Document;
import one.rewind.txt.NumberFormatUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtil {

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
				one.rewind.txt.StringUtil.removeHTML(src));

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
	 * @param doc
	 * @param path
	 * @return
	 */
	public static int getBidderTotalNum(Document doc, String path) {
		try {
			return Integer.parseInt(doc.select(path).text());
		}catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 *
	 * @param doc
	 * @param path
	 * @return
	 */
	public static int getBidderNum(Document doc, String path) {
		try {
			if (doc.select(path).size() > 1) {
				return Integer.parseInt(doc.select(path).get(1).text());
			} else if (doc.select(path).size() == 1) {
				return Integer.parseInt(doc.select(path).get(0).text());
			} else {
				return 0;
			}
		}catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 *获取预算
	 * @param path
	 * @param des_src
	 * @return
	 */
	public static double[] budget_all(Document doc, String path, String des_src) {

		// 1.描述中拿预算
		double budget_all = StringUtil.detectBudget(des_src);
		// 2.判断拿到的预算是否为0，如果拿不到就从预算栏中获取预算；拿到预算直接赋值。
		if (budget_all == 0 || budget_all > Integer.MAX_VALUE) {

			// 3.拿到预算栏中的数据
			String budget = doc.select(path).text();

			// 4.判断格式
			if (budget.equals("可议价") || budget.equals("待设置")) {
				return new double[] {0.00, 0.00};
			}
			else if (budget.contains("￥") && budget.contains("-")) {

				String pr = budget.split("￥")[1];
				String price[] = pr.split("-");
				return new double[] {Double.parseDouble(price[0]), Double.parseDouble(price[1])};
			}
			else if (!budget.contains("￥") && budget.contains("-")) {

				String price[] = budget.split("-");
				return new double[] {Double.parseDouble(price[0]), Double.parseDouble(price[1])};
			}
			else {
				try {
					String price = budget.replace("￥", "");
					return new double[]{Double.parseDouble(price), Double.parseDouble(price)};
				} catch (Exception e) {
					return new double[] {0.00, 0.00};
				}
			}
		}
		else {

			return new double[] {budget_all, budget_all};
		}
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

	/**
	 *
	 * @param src
	 * @return
	 */
	public static double detectBudget(String src) {

		String patternStr = ".*预算.{0,6} *(:|：)? *(?<T>\\d+ *(亿|千万|百万|万|千|百|w|W|k|K)?)(元|块)?.*";

		Pattern pattern = Pattern.compile(patternStr);

		Matcher matcher = pattern.matcher(
				one.rewind.txt.StringUtil.removeHTML(src));

		if(matcher.matches()) {

			return NumberFormatUtil.parseDouble(matcher.group("T"));
		}

		return 0;
	}

	/**
	 * 清洗数据，获得a，img地址，并且保留干净的a，img 标签
	 * @param in
	 * @param extraUrls_img
	 * @return
	 */
	public static String cleanContent(String in, Set<String> extraUrls_img){

		in = in.replace("(?i)^<.+?>", "");
		in = in.replace("(?i)</.+?>$", "");

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

		// 去掉i标签
		out = out.replaceAll("<i.*?></i>", "");

		// 去掉<a>标签
		out = out.replaceAll("(?si)<a.*?>|</a>", "");

		// 删除javascript标记
		// TODO 此行代码应该下移
		out = out.replaceAll("javascript:.+?(?=['\" ])", "");

		// *****************************
		// 处理图片，获取需要下载的连接
		//    <img src="{binaryId}">
		//    <img src="data:image/...">

		// 清洗 img 标签，只保留 src 属性
		Matcher matcher = Pattern.compile("(?si)<img.*?>").matcher(out);

		List<String> imgs = new ArrayList<>();
		while(matcher.find()){

			// 获取 src属性
			String imgSrc = matcher.group().replaceAll("^.*?src=['\"]?", "")
					.replaceAll("[ \"'>].*?$", "");

			// 图片是Base64 Encode形式
			if(imgSrc.matches("^data:image/.+?")) {
				imgs.add("<img src=\"" + imgSrc + "\">");
			}
			// 如果 img 的 src长度小于等于10 则认为该图片无效
			else if(imgSrc.length() > 10) {
				extraUrls_img.add(imgSrc);
				imgs.add("<img src=\"" + imgSrc + "\">");
			}
			else {
				imgs.add("");
			}
		}

		matcher = Pattern.compile("(?si)<img.*?>").matcher(out);
		int i = 0;
		while(matcher.find()){
			out = out.replace(matcher.group(), imgs.get(i));
			i++;
		}

		// 清洗 a 标签，只保留 href 属性
		// TODO 应该只清洗附件类型
		// TODO 附件改名 a 标签中的title属性
		// 下载有可能有多种形式，应该能处理这些形式
		/**
		 *  TODO 只清洗描述，不含单独a标签
		 */
		/*matcher = Pattern.compile("(?si)<a.*?>下载").matcher(out);
		List<String> as = new ArrayList<>();

		while(matcher.find()){

			String attachmentUrl = matcher.group()
					.replaceAll("^.*?href=['\"]?", "")
					.replaceAll("[ \"'>].*?$", "");

			if(attachmentUrl.length() > 10) {
				extraUrls_a.add(attachmentUrl);
				as.add("<a href=\"" + attachmentUrl + "\">下载");
			} else {
				as.add("");
			}
		}*/

		// 去除<a><img></img></a> 这种格式的a标签
		/**
		 * 只用于清洗描述，只含有img标签
		 */
		/*matcher = Pattern.compile("(?si)<a.*?><img src").matcher(out);
		int a = 0;
		while (matcher.find()) {
			out = out.replace(matcher.group(), "<img src").replace("></a>", ">");
			a++;
		}

		// 获取 title的值给fileName
		matcher = Pattern.compile("(?si)<a.*?title=\"(?<T>.+?)\".*?>下载").matcher(out);
		while (matcher.find()) {
			fileNames.add(matcher.group("T"));
		}

		// 将a标签替换
		matcher = Pattern.compile("(?si)<a.*?>下载").matcher(out);
		int b = 0;
		while (matcher.find()) {
			out = out.replace(matcher.group(), as.get(b));
			b++;
		}*/


		// 去掉分页特殊文字
		out = out.replaceAll("(?i)(上一页(\\d)*)|(下一页)", "");

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

		/*out = out.replaceAll("(?<=[\u4E00-\u9FA5]) (?=[\u4E00-\u9FA5])", ""); // 中文间空格转化为逗号
		out = out.replaceAll(" (?=[\u4E00-\u9FA5])", ""); // 去掉中文前空格
		out = out.replaceAll("<p>[^\u4E00-\u9FA5]+?</p>", ""); // 删除没有中文的段落*/

		// <p> </p> 标记的补全
		out = out.replaceAll("(?<=[\u4E00-\u9FA5：])<p>(?=[\u4E00-\u9FA5])", "</p><p>"); // 段落标记的补全
		out = out.replaceAll("(?<=[\u4E00-\u9FA5：])</p>(?=[\u4E00-\u9FA5])", "</p><p>"); // 段落标记的补全

		// 去掉特殊无意义字符
		out = out.replaceAll("[—§№☆★○●◎⊙◇◆□■△▲※→←]", "");

		return out;
	}

	public static List<String> strToList(String src) {

		if(src == null) return null;
		List list =  Arrays.asList(src.split(",")).stream().map(el -> el.trim()).filter(el -> !el.equals(" ") && !el.equals("")).collect(Collectors.toList());
		if(list.size() == 0) list = null;
		return list;
	}

}
