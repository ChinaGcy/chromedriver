package com.sdyk.ai.crawler.es.test;

import com.sdyk.ai.crawler.es.ESTransportClientAdapter;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.witkey.Resume;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import org.junit.Test;

public class EsTest {

	/**
	 * 将数据库中的数据导入到ES中
	 */
	@Test
	public void createMappings(){
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
	public void testClient() {
		ESTransportClientAdapter.getClient();
	}

	/**
	 * 测试service 更新
	 * @throws Exception
	 */
	@Test
	public void testServiceUpdate() throws Exception {

		ServiceProvider serviceProvider = (ServiceProvider) Model.getById(ServiceProvider.class, "bb18e9ffce9314b3f21e47271428b067");

		serviceProvider.updateES();
	}

	/**
	 * 测试 Resume
	 * @throws Exception
	 */
	@Test
    public void testResume() throws Exception {

		Resume resume = (Resume) Model.getById(Resume.class, "f308d8784c5c9365c74360f98c392f34");

		System.err.println(resume.user_id);

		String key = ServiceProvider.class.getSimpleName();

		//ESTransportClientAdapter.deleteOne(key, key, resume.user_id);

		resume.insert();
	}

}
