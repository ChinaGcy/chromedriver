package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.model.Binary;
import com.sdyk.ai.crawler.zbj.model.Work;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.tfelab.db.Refacter;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.txt.StringUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 案例详情
 */
public class WorkTask extends Task {

	public WorkTask(String url, String user_id) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("user_id",user_id);
	}

	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();
		Work work = new Work();

		if (getUrl().contains("zbj")) {
			work.id = StringUtil.byteArrayToHex(StringUtil.uuid(getUrl()));
			work.name = driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-head.fl > div"))
					.getText();
			work.user_id = this.getParamString("user_id");

			String src_ = driver.findElement
					(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-content > ul")).getText() + " ";


			Pattern pattern = Pattern.compile(".*客户名称：(?<T>.+?)\\s+");
			Pattern pattern1 = Pattern.compile(".*类型.*： ?(?<T>.+?)\\s+");
			Pattern pattern2 = Pattern.compile(".*行业.*： ?(?<T>.+?)\\s+");
			Matcher matcher = pattern.matcher(src_);
			Matcher matcher1 = pattern1.matcher(src_);
			Matcher matcher2 = pattern2.matcher(src_);

			if (matcher.find()) {
				work.tenderer_name = matcher.group("T");
			}
			if (matcher1.find()) {
				work.type = matcher1.group("T");
			}
			if (matcher2.find()) {
				work.field = matcher2.group("T");
			}

			String description_src = driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-left.fl"))
					.getAttribute("innerHTML");
			Set<String> img_urls = new HashSet<>();
			Set<String> a_urls = new HashSet<>();

			String des_src = com.sdyk.ai.crawler.zbj.StringUtil.cleanContent(description_src, img_urls, a_urls);

			//处理图片
			for (String img : img_urls) {

				if (img.contains("https://t5.zbjimg.com/t5s/common/img/space.gif")) {
					continue;
				}
				try {
					org.tfelab.io.requester.Task t_ = new org.tfelab.io.requester.Task(img);
					BasicRequester.getInstance().fetch(t_);
					String fileName = null;
					Binary binary = new Binary();
					binary.src = t_.getResponse().getSrc();

					if (t_.getResponse().getHeader() != null) {
						for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

							if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
								binary.content_type = entry.getValue().toString();
							}

							if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

								System.err.println("1111");
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

						fileName = t_.getUrl().split("task/")[1].split("\\.")[0];
					}
					binary.file_name = fileName;
					binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(img));
					binary.url =getUrl();
					des_src = des_src.replace(img, binary.file_name).replaceAll("&s\\.w=\\d+&s\\.h=\\d+","");
					binary.insert();

				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

			//处理下载
			for (String a : a_urls) {

				if (!a.contains("key=")) {
					continue;
				}

				try {
					org.tfelab.io.requester.Task t_ =null;
					if (!a.contains("https")) {
						String a1 = a.replace("http", "https");
						t_ = new org.tfelab.io.requester.Task(a1);
					}else {
						t_ = new org.tfelab.io.requester.Task(a);
					}
					BasicRequester.getInstance().fetch(t_);
					String fileName = null;
					Binary binary = new Binary();
					binary.src = t_.getResponse().getSrc();

					if (t_.getResponse().getHeader() != null) {
						for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

							if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
								binary.content_type = entry.getValue().toString();
							}

							if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

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
						fileName = t_.getUrl().replaceAll("^.+/", "");
					}
					binary.file_name = fileName;
					binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(a));
					binary.url = getUrl();
					des_src = des_src.replace(a, binary.file_name);
					binary.insert();

				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

			work.description = des_src.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

			work.pricee = Double.parseDouble(driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content"))
					.getText().split("￥")[1]);

		}
		else {
			try {
				work.id = StringUtil.byteArrayToHex(StringUtil.uuid(getUrl()));
				work.user_id = this.getParamString("user_id");
				work.name = driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-title > h2"))
						.getText();
				work.tenderer_name = driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-customer > em"))
						.getText();
				work.pricee = Double.parseDouble(driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-amount > em"))
						.getText().replaceAll("¥", "").replaceAll(",", ""));
				work.tags = driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > ul")).getText();


				String description_src = driver.findElement(By.cssSelector("body > div.tp-works-bd > div > div.works-bd-content > div"))
						.getAttribute("innerHTML");
				Set<String> img_urls = new HashSet<>();
				Set<String> a_urls = new HashSet<>();

				String des_src = com.sdyk.ai.crawler.zbj.StringUtil.cleanContent(description_src, img_urls, a_urls);

				//处理图片
				for (String img : img_urls) {

					if (img.equals("https://t5.zbjimg.com/t5s/common/img/space.gif")) {
						continue;
					}
					try {
						org.tfelab.io.requester.Task t_ = new org.tfelab.io.requester.Task(img);
						BasicRequester.getInstance().fetch(t_);

						String fileName = null;
						Binary binary = new Binary();
						binary.src = t_.getResponse().getSrc();

						if (t_.getResponse().getHeader() != null) {
							for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
									binary.content_type = entry.getValue().toString();
								}

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

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
							fileName = t_.getUrl().split("task/")[1].split("\\.")[0];
						}
						binary.file_name = fileName;
						binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(img));
						binary.url = getUrl();
						des_src = des_src.replace(img, binary.file_name).replaceAll("&s\\.w=\\d+&s\\.h=\\d+","");
						binary.insert();

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

				//处理下载
				for (String a : a_urls) {

					if (!a.contains("key=")) {
						continue;
					}

					try {
						org.tfelab.io.requester.Task t_ =null;
						if (!a.contains("https")) {
							String a1 = a.replace("http", "https");
							t_ = new org.tfelab.io.requester.Task(a1);
						}else {
							t_ = new org.tfelab.io.requester.Task(a);
						}
						BasicRequester.getInstance().fetch(t_);

						String fileName = null;
						Binary binary = new Binary();
						binary.src = t_.getResponse().getSrc();

						if (t_.getResponse().getHeader() != null) {
							for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
									binary.content_type = entry.getValue().toString();
								}

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

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
							fileName = t_.getUrl().replaceAll("^.+/", "");
						}
						binary.file_name = fileName;
						binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(a));
						binary.url = getUrl();
						des_src = des_src.replace(a, binary.file_name);
						binary.insert();

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

				work.description = des_src.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

			}catch (Exception e) {
				e.printStackTrace();
			}

		}

		try {
			work.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Task>();
	}


	public static void main(String[] args) throws Exception {
		Refacter.dropTable(Work.class);
		Refacter.createTable(Work.class);

	}
}
