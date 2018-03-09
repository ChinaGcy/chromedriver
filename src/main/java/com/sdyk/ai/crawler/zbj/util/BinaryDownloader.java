package com.sdyk.ai.crawler.zbj.util;

import com.sdyk.ai.crawler.zbj.model.Binary;
import com.sdyk.ai.crawler.zbj.model.Model;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.io.requester.BasicRequester;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BinaryDownloader {

	private static final Logger logger = LogManager.getLogger(BinaryDownloader.class.getName());

	static String bannedUrlReg = ".*?common/img/fuwubao/.+?\\.png.*?";
	static String bannedUrlReg1 = ".*?img/space\\.gif.*?";

	/**
	 * 下载图片等二进制文件
	 * 1. encode 不下载
	 * 2. 没有协议头
	 * 3. 相对路径
	 * 4. 完整路径
	 */
	public static String download(String des_src, Set<String> urls, String context_url, List<String> fileName_a) {

		// 处理下载
		for (String url : urls) {

			// 1.判断是否为无效地址
			if (url.matches(bannedUrlReg) || url.matches(bannedUrlReg1)) {
				// 1.1 将无效地址在文本中删除
				des_src.replace(url,"");
				continue;
			}

			logger.info("Begin to download: {}.", url);

			try {

				String oldUrl = url;
				org.tfelab.io.requester.Task t_;
				// 2.判断地址是否有协议头
				if (! url.contains("https") && !url.contains("http") && url.contains("//")) {
					url = "https:" + url;
				}
				// 3.判断是否为相对路径
				else if (!url.contains("https") && !url.contains("http") && !url.contains("//")) {
					url = context_url + url;
				}
				// 4. 完整地址
				else if (url.contains("http:")) {
					url = url.replace("http:", "https:");
				}

				t_ = new org.tfelab.io.requester.Task(url);

				Binary binary = new Binary(url);

				BasicRequester.getInstance().fetch(t_);

				binary.src = t_.getResponse().getSrc();

				// 当为图片或者下载的数量与fileName数量不一致则通过header获取 否则直接复制
				if (fileName_a == null || fileName_a.size() ==0 || urls.size() != fileName_a.size()) {
					binary.file_name = getFileName(t_, binary, url);
				}else {
					for (String name : fileName_a) {
						binary.file_name = name;
					}
				}
				des_src = des_src.replace(oldUrl, binary.id);

				if (binary.file_name.length() < 128) {

					System.err.println(binary.toJSON());

					binary.insert();

					logger.info(" Download done: {}.", url);
				}

			} catch (Exception e) {
				logger.error("download error {}", e);
				continue;
			}
		}
		return  des_src;
	}

	/**
	 *
	 * @param t_
	 * @param binary
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getFileName(org.tfelab.io.requester.Task t_, Binary binary, String url) throws UnsupportedEncodingException {

		String fileName = null;

		if (url.contains("https://homesitetask.zbjimg.com/homesite/task/")) {
			fileName = url.split("/")[5];
		}

		if (t_.getResponse().getHeader() != null) {

			for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

				if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {

					binary.content_type = entry.getValue().toString();
				}

				if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

					fileName = entry.getValue().toString()
							.split("filename\\*=utf-8' '")[1]
							/*.replaceAll("^.*?filename\\*=utf-8' '", "")*/
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
			return fileName;
		}
		return fileName;
	}

}
