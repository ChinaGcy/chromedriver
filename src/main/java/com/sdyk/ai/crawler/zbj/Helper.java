package com.sdyk.ai.crawler.zbj;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.db.Refacter;
import org.tfelab.txt.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {

	private static final Logger logger = LogManager.getLogger(Helper.class.getName());

	/**
	 * 谨慎使用
	 */
	public static void initDB() {

		logger.info("Init db tables...");

		try {

			Refacter.dropTables("ocom.sdyk.ai.crawler.zbj.model");
			Refacter.createTables("com.sdyk.ai.crawler.zbj.model");

			logger.info("Create db tables done.");

		} catch (Exception e) {
			logger.error("Error create tables.", e);
		}
	}

	public static int getTimeSpan(String src) {

		Pattern pattern;
		Matcher matcher;

		if(src.matches("\\d+天")) {
			pattern = Pattern.compile("(?<T>\\d+)天");
			matcher = pattern.matcher(src);
			if(matcher.matches()) {
				return Integer.parseInt(matcher.group("T"));
			}
		}
		else if (true) {

		}


		return 0;
	}

	public static String cleanContent(String in, Set<String> extraUrls){

		String out = "<p>" + in + "</p>";

		// 去HTML注释
		out = out.replaceAll("<!--.*?-->", "");
		out = out.replaceAll("<?xml.*?>", "");
		out = out.replaceAll("(?i)<!--[if !IE]>.*?<![endif]-->", "");

		// 去掉<span>标签
		out = out.replaceAll("(?is)<span([^>]*)>|</span>|<wbr>", "");

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
		out = out.replaceAll("(?si)<a.*?>|</a>", "");

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
				extraUrls.add(imgUrl);
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


	public static void main(String[] args) throws Exception {
		//initDB();

		String src = "<div><p class=\"task-extend-item\" data-a=\"0\"><strong>需求号：12844519</strong></p><p><strong>通过手机客户端发布的需求</strong></p><div class=\"task_content task-extend-item\" work-map=\"work-short\"><h3>具体要求：</h3><p class=\"demand-txt overflow\" style=\"\">我需要网站测试，要求：安全测试，熟悉Web，检测漏洞的或者渗透的联系我在线等急急急<br>周期：1个礼拜<br>预算：5000元（找到漏洞并修复漏洞）<br>沟通方式：电话＋微信（和电话同号）<br></p><a class=\"check-all-btn\" href=\"javascript:;\" style=\"display: block;\">查看全部</a></div><p></p></div>";
		src = src.replaceAll("<a.+?>查看全部</a>", "");
		Set<String> img_urls = new HashSet<>();
		src = cleanContent(src, img_urls);
		System.err.println(src);

		for(String url : img_urls) {
			System.err.println(url);
		}

	}
}
