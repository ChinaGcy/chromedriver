package com.sdyk.ai.crawler.util;

import com.sdyk.ai.crawler.model.Binary;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.txt.URLUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.rewind.io.requester.BasicRequester;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BinaryDownloader {

	private static final Logger logger = LogManager.getLogger(BinaryDownloader.class.getName());

	static String bannedUrlReg = ".*?common/img/fuwubao/.+?\\.png.*?";
	static String bannedUrlReg1 = ".*?img/space\\.gif.*?";

	/**
	 * 下载附件
	 * 1. encode 不下载
	 * 2. 没有协议头
	 * 3. 相对路径
	 * 4. 完整路径
	 */
	public static String download(String context_url, Map<String, String> url_filename ) {

		StringBuffer result = new StringBuffer();

		// 处理下载
		for (String url : url_filename.keySet()) {

			String oldurl = url;

			// 补全url
			url = complementUrl(url, context_url);

			String id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(url));
			Binary oldBinary = Binary.getBinaryById(id);

			// 已存在该附件
			if( oldBinary != null ){

				result.append(id);
				result.append(",");
			}
			// 不存在附件
			else {

				// 1.判断是否为无效地址
				if (url.matches(bannedUrlReg) || url.matches(bannedUrlReg1)) { continue; }

				logger.info("Begin to download: {}.", url);

				try {

					ChromeTask t_;

					if( url != null ){

						t_ = new ChromeTask(url);

						Binary binary = new Binary(url);
						BasicRequester.getInstance().submit(t_);

						binary.src = t_.getResponse().getSrc();

						List<String> contentTypeList = t_.getResponse().getHeader().get("Content-Type");
						StringBuffer contentType = new StringBuffer();
						for( String s : contentTypeList ){
							contentType.append(s);
							contentType.append(s);
						}
						if( contentType.length() > 0 ){
							binary.content_type = contentType.substring(0, contentType.length() - 1);
						}
						else {
							binary.content_type = url.split(".")[url.split(".").length -1];
						}

						binary.file_name = url_filename.get(oldurl);

						if( binary.file_name == null ){
							binary.file_name = getFileName(url);
						}

						if (binary.file_name.length() < 128) {

							binary.file_size = binary.src.length / 1024;
							binary.insert();

							result.append(binary.id);
							result.append(",");

							logger.info(" Download done: {}.", url);
						}
					}

				} catch (Exception e) {
					logger.error("download error {}", e);
					continue;
				}
			}
		}

		if( result.length() < 1 ){
			return null;
		}
		return  result.substring(1, result.length()-1);
	}



	/**
	 * 下载图片操作
	 * 1. encode 不下载
	 * 2. 没有协议头
	 * 3. 相对路径
	 * 4. 完整路径
	 */
	public static String download(String des_src, Set<String> urls, String context_url) {

		// 处理下载
		for (String url : urls) {

			String oldUrl = url;

			// 补全url
			url = complementUrl(url, context_url);

			String id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(url));
			Binary oldBinary = Binary.getBinaryById(id);

			// 已存在该附件
			if( oldBinary != null ){

				des_src = des_src.replace(url, id);
			}
			// 不存在附件
			else {

				// 1.判断是否为无效地址
				if (url.matches(bannedUrlReg) || url.matches(bannedUrlReg1)) {

					// 1.1 将无效地址在文本中删除
					des_src.replace(url,"");
					continue;
				}

				logger.info("Begin to download: {}.", url);

				try {

					ChromeTask t_;

					t_ = new ChromeTask(url);

					Binary binary = new Binary(url);

					BasicRequester.getInstance().submit(t_);

					binary.src = t_.getResponse().getSrc();

					List<String> contentTypeList = t_.getResponse().getHeader().get("Content-Type");
					StringBuffer contentType = new StringBuffer();
					for( String s : contentTypeList ){
						contentType.append(s);
						contentType.append(s);
					}
					if( contentType.length() > 0 ){
						binary.content_type = contentType.substring(0, contentType.length() - 1);
					}
					else {
						binary.content_type = url.split(".")[url.split(".").length -1];
					}

					binary.file_name = getFileName(url);

					if (binary.file_name.length() < 128) {

						binary.file_size = binary.src.length / 1024;

						binary.insert();

						String newUrl = "http://10.0.0.61:50100/binarys/" + binary.id;
						des_src = des_src.replace(oldUrl, newUrl);

						logger.info(" Download done: {}.", url);
					}
					// binary 不合理
					else {
						des_src = des_src.replace(oldUrl, "");
					}

				} catch (Exception e) {
					logger.error("download error {}", e);
					continue;
				}
			}


		}

		return  des_src;
	}

	/**
	 * 补全正确的URL
	 * @param url
	 * @param context_url
	 * @return
	 */
	public static String complementUrl(String url, String context_url){

		// 2.判断地址是否为相对路径
		if ( !url.matches("^http.+?") && !url.contains(".com") ) {

			// 根据 context_url 拼接相对路径
			url = context_url.replaceAll("com/.+?$", "com/") + url;
		}
		// 3.缺少协议头
		else if (!url.contains("https") && !url.contains("http") && url.contains("//")) {

			// 根据 context_url 判断默认协议头
			try {
				url = URLUtil.getProtocol(context_url) + ":" + url;
			} catch (Exception e) {
				logger.error("error fro URLUtil.getProtocol(context_url)", e);
				return null;
			}
		}
		// 4. 完整地址
		else if (url.contains("http:") || url.contains("https:")) { }

		return url;
	}



	/**
	 * 获取文件名
	 * @param t_
	 * @param binary
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getFileName(ChromeTask t_, Binary binary, String url) throws UnsupportedEncodingException {

		String fileName = null;

		if (url.contains("https://homesitetask.zbjimg.com/homesite/task/")) {
			fileName = url.split("/")[5];
		}

		if (t_.getResponse().getHeader() != null) {

			for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

				if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-category")) {

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

	/**
	 * 获取文件名
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getFileName(String url) throws UnsupportedEncodingException {

		String[] files = url.split("/");
		return files[files.length - 1];
	}

}
