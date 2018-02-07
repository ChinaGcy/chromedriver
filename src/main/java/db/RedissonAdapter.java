package db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.tfelab.common.Configs;

/**
 * Created by Luke on 1/25/16. 
 * mailto:stormluke1130@gmail.com
 */
public class RedissonAdapter {

	public final static Logger logger = LogManager.getLogger(org.tfelab.db.RedissonAdapter.class.getName());

	public static RedissonClient redisson;

	static {
		try {
			Config config = new Config();
			logger.info("Connecting Redis...");

			config.useSingleServer()
				.setAddress(Configs.getConfig(org.tfelab.db.RedissonAdapter.class).getString("url"))
				.setPassword(Configs.getConfig(org.tfelab.db.RedissonAdapter.class).getString("password"))
				.setConnectionPoolSize(100)
				.setSubscriptionConnectionPoolSize(1000)
				.setTimeout(10000)
				.setFailedAttempts(3)
				.setRetryAttempts(3)
				.setRetryInterval(1000);
			
			redisson = Redisson.create(config);
			logger.info("Connected to Redis");
		} catch (Throwable err) {
			logger.error("Redis init error", err);
			throw err;
		}
	}

}
