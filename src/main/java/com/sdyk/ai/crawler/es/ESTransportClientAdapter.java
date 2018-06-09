package com.sdyk.ai.crawler.es;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.model.*;
import one.rewind.db.DaoManager;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.List;

/**
 * Created by apple on 2018/2/6.
 */
public class ESTransportClientAdapter {

    public static final Logger logger = LogManager.getLogger(ESTransportClientAdapter.class.getName());

    public final static String cluster_name;
    public final static String host;
    public final static int port;

	static Class clazzes[] = {
			Project.class, Tenderer.class, ServiceProvider.class, Case.class, Resume.class, Work.class
	};

    public static TransportClient client;

    public static boolean Enable_ES = false;

    static {

        System.setProperty("es.set.netty.runtime.available.processors", "false");

        cluster_name = Configs.getConfig(ESTransportClientAdapter.class).getString("cluster_name");
        host = Configs.getConfig(ESTransportClientAdapter.class).getString("host");
        port = Configs.getConfig(ESTransportClientAdapter.class).getInt("port");

        buildClient();
    }

    private static void buildClient() {

        logger.info("{} {}:{}", cluster_name, host, port);

        Settings settings = Settings.builder().put("cluster.name", cluster_name).build();

        try {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
        } catch (Exception e) {
            logger.error("Build client error. ", e);
        }
    }

    /**
     * 获得 ES客户端
     * @return
     * @throws Exception
     */
    public static TransportClient getClient() {

        //TODO 应该判断client的健康程度
        if(client == null) buildClient();

        return client;
    }

    /**
     * 创建用户索引 和 项目索引
     */
    public static void createIndexAndMapping() throws Exception {

        IndicesAdminClient client = getClient().admin().indices();

        for(Class clazz : clazzes) {

            String index = clazz.getSimpleName().toLowerCase();
            String mappingFilePath = "index_mappings/" + clazz.getSimpleName() + ".json";

			client.prepareCreate(index).setSettings(
					one.rewind.util.FileUtil.readFileByLines("index_mappings/settings.json"),
					XContentType.JSON
			).get();

			client.preparePutMapping(index)
					.setType(index)
					.setSource(
							one.rewind.util.FileUtil.readFileByLines(mappingFilePath),
							XContentType.JSON).get();

        }
    }

    /**
     * 删除用户索引和项目索引
     */
    public static void deleteIndexAndMapping() throws Exception {

        TransportClient client = getClient();

		DeleteIndexResponse dResponse;

		try {

			for(Class clazz : clazzes) {

				String index = clazz.getSimpleName().toLowerCase();

				dResponse = client.admin().indices().prepareDelete(index)
						.execute().actionGet();
			}

        } catch (Exception e) {
            logger.error("Error delete Index, ", e);
        }

    }

    /**
     * 索引一条记录
     * @param model
     * @throws Exception
     */
    public static void  insertOne(ESIndex model) {

        TransportClient client = ESTransportClientAdapter.getClient();

		IndexResponse response = client.prepareIndex(
        		model.getClass().getSimpleName().toLowerCase(),
				model.getClass().getSimpleName().toLowerCase(),
				model.getId()
			)
			.setSource(model.toJSON(), XContentType.JSON)
			.get();

		logger.info("{} {} {}", model.getClass().getSimpleName(), model.getId(), response.status());
    }

    /**
     *
     * @param model
     * @throws Exception
     */
    public static void updateOne(String id, ESIndex model) {

        TransportClient client = ESTransportClientAdapter.getClient();

        client.prepareUpdate(
        		model.getClass().getSimpleName().toLowerCase(),
				model.getClass().getSimpleName().toLowerCase(),
				id
			)
			.setDoc(model.toJSON(), XContentType.JSON)
			.get();
    }

    /**
     *
     * @param index
     * @param type
     * @param id
     * @throws Exception
     */
    public static void deleteOne(String index, String type, String id) throws Exception {

        TransportClient client = ESTransportClientAdapter.getClient();

        client.prepareDelete(index, type, id).get();
    }


	/**
	 * 从数据库中获取数据，并插入到ES中
	 * @throws Exception
	 */
	public static void dumpDBtoES() throws Exception {

		for(Class clazz : clazzes) {

			logger.info(clazz.getSimpleName());

			Dao dao = DaoManager.getDao(clazz);

			List<Model> models = dao.queryForAll();

			for(Model m : models) {
				logger.info(m.id);
				insertOne(m);
			}
		}
	}
}
