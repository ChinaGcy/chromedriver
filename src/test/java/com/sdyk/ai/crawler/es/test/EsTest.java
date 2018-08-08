package com.sdyk.ai.crawler.es.test;

import com.sdyk.ai.crawler.es.ESTransportClientAdapter;
import org.junit.Test;

public class EsTest {

	/**
	 * 将数据库中的数据导入到ES中
	 */
	@Test
	public void SQLToES(){
		try {

			ESTransportClientAdapter.deleteIndexAndMapping();
			ESTransportClientAdapter.createIndexAndMapping();
			ESTransportClientAdapter.dumpDBtoES();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 测试读取json文件
	 */
	@Test
	public void readFile() {
		String json = one.rewind.util.FileUtil.readFileByLines("index_mappings/Project.json");
		System.err.println(json);
	}

	/**
	 * 测试创建用户索引
	 * @throws Exception
	 */
	@Test
	public void createUserIndex() throws Exception {
		ESTransportClientAdapter.createIndexAndMapping();


	}

	@Test
	public void TestClient() {
		ESTransportClientAdapter.getClient();
	}
}
