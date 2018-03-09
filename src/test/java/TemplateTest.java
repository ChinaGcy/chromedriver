import com.sdyk.ai.crawler.zbj.util.StringUtil;
import org.junit.Test;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.Task;
import org.tfelab.util.FileUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateTest {
	@Test
	public void testCleanContent() throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {

		String s = "";

		Set<String> img_urls = new HashSet<>();

		Set<String> img_urlsa = new HashSet<>();

		List<String> fileName_a = new ArrayList<>();

		Task t = new Task("http://task.zbj.com/12913633/");

		BasicRequester.getInstance().fetch(t);

		Pattern p = Pattern.compile("(?s)<div class=\'describe.+?<div class=\'img-item\'>");

		Matcher m = p.matcher(t.getResponse().getText());

		if(m.find()) {
			s = StringUtil.cleanContent(m.group(), img_urls, img_urlsa, fileName_a);
		}

		//System.out.println(s.replaceAll("//rms\\.zhubajie.com//resource/redirect\\?key=homesite/task/%E7%BD%91%E7%AB%99%E5%BC%80%E5%8F%91-1_206.xlsx/origine/f76f7652-1708-47e3-8e2b-caacecf365b9",""));

		for(String url : img_urlsa) {

			String suffix = url.split("key")[1].split("\\.")[1].split("/")[0];

			Task t_ = new Task("https:"+url);
			BasicRequester.getInstance().fetch(t_);
			s = s.replaceAll("<a .+?>", "<a href=" + t_.getId()+"."+ suffix + ">");
			byte[] src = t_.getResponse().getSrc();
			String contentType = "";
			String fileName = null;
			System.err.println(t_.getResponse().getHeader());

			if(t_.getResponse().getHeader() != null) {
				for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

					if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
						contentType = entry.getValue().toString();
					}

					if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

						System.err.println(entry.getValue().toString());

						fileName = entry.getValue().toString()
								.replaceAll("^.*?filename\\*=utf-8' '", "")
								.replaceAll("\\].*?$", "");
						fileName = java.net.URLDecoder.decode(fileName, "UTF-8");

						if(fileName == null || fileName.length() == 0) {

							fileName = entry.getValue().toString()
									.replaceAll("^.*?\"", "")
									.replaceAll("\".*$", "");
						}

					}
				}
			}

			if(fileName == null) {
				fileName = t_.getUrl().replaceAll("^.+/", "")+"."+ suffix;
			}

			FileUtil.writeBytesToFile(src, fileName);

		}
	}
}
