package com.sdyk.ai.crawler.specific.zbj;


import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.specific.zbj.task.ZbjLoginTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ServiceScanTask;
import com.sdyk.ai.crawler.util.URLUtil;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.task.ChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static spark.route.HttpMethod.get;

/**
 * 任务生成器
 */
public class Scheduler extends com.sdyk.ai.crawler.Scheduler{


    // 项目频道参数
    public String[] project_channels = {
            /*"t-paperwork",         // 策划
            "t-ppsj",              // 品牌设计
            "t-sign",              // 广告设计
            "t-ad",                // 媒介投放
            "t-xcpzzzbj",          // 宣传片制作
            "t-wzkf",              // 网站建设
            "t-hkaifa",            // H5开发
            "t-wxptkf",           // 微信开发
            "t-xxtg",              // 公关活动/线下地推/会议展览
            "t-yxtg",              // 营销传播
            "t-ppglzxzbj",         // 品牌咨询管理
            "t-dsyxfwzbj"          // 电商营销服务*/
            "o-paperwork",         // 策划
            "o-game",               // 游戏
            "o-dsspfw",               // 电商视频服务
            "o-yxzxzbj",               // 营销咨询
            "o-video",               // 影视制作
            "o-dhsjzbj",               // 动画设计
            "o-rjkf",               // 软件开发
            "o-wdfw",               // 电商设计服务
            "o-wdfw",               // 电商设计服务
            "o-ppsj",              // 品牌设计
            "o-sign",              // 广告设计
            "o-ad",                // 媒介投放
            "o-xcpzzzbj",          // 宣传片制作
            "o-wzkf",              // 网站建设
            "o-hkaifa",            // H5开发
            "o-wxptkf",           // 微信开发
            "o-xxtg",              // 公关活动/线下地推/会议展览
            "o-yxtg",              // 营销传播
            "o-ppglzxzbj",         // 品牌咨询管理
            "o-dsyxfwzbj"          // 电商营销服务
    };

    // 服务商频道参数
    public static String[] service_supplier_channels = {
            /*"paperwork",           // 策划
            "ppsj",                // 品牌设计
            "sign",                // 广告设计
            "ad",                  // 媒介投放
            "xcpzzzbj",            // 宣传片制作
            "wzkf",                // 网站建设
            "hkaifa",              // H5开发
            "wxptkf",              // 微信开发
            "xxtg",                // 公关活动/线下地推/会议展览
            "yxtg",                // 营销传播
            "ppglzxzbj",           // 品牌咨询管理
            "dsyxfwzbj"            // 电商营销服务*/
    };

    public String cron = "*/30 * * * *";

    public Scheduler() {
        super();
    }

    public Scheduler(String domain, int driverCount) {
        super(domain, driverCount);
    }

    /**
     *
     */
    public void initAuthorizedRequester() {

        try {

            Account account = AccountManager.getAccountByDomain(domain, "select");

            //Proxy proxy = new one.rewind.io.requester.proxy.ProxyImpl("10.0.0.51", 49999, null, null);

            // 创建一个新的container
            DockerHostManager.getInstance().createDockerContainers(1);

            ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

            // 不使用代理
            ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container, ChromeDriverAgent.Flag.MITM);

            // agent 添加异常回调
            agent.addAccountFailedCallback((a, t)->{

                logger.info("Account {}:{} failed.", account.getDomain(), account.getUsername());

            }).addTerminatedCallback((t)->{

                logger.info("Container {} {}:{} Terminated.", container.name, container.ip, container.vncPort);

            }).addNewCallback((t)->{

                try {
					getLoginTask();
                } catch (Exception e) {
                    logger.error(e);
                }
            });

			ChromeDriverDistributor.getInstance().addAgent(agent);

            logger.info("ChromeDriverAgents are ready.");

        } catch (Exception e) {
            logger.error("Error init authorized requester. ", e);
        }
    }

    /**
     *
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public void getLoginTask() throws MalformedURLException, URISyntaxException {

		try {
			URLUtil.PostTask(ZbjLoginTask.class,
					null,
					ImmutableMap.of("domain", "zbj"),
					null,
					null,
					null,
					null,
					null);
		} catch (ClassNotFoundException e) {
			logger.error("", e);
		}

	}

    /**
     *
     * @param backtrace
     * @return
     */
    public void getTask(boolean backtrace) {

        for(String channel : project_channels) {

			try {
				URLUtil.PostTask(com.sdyk.ai.crawler.specific.zbj.task.scanTask.ProjectScanTask.class,
						null,
						ImmutableMap.of("channel", channel,"page", "1"),
						null,
						null,
						null,
						null,
						null);

				logger.info("PROJECT:" + channel);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			/* ScanTask t = ProjectScanTask.generateTask(channel, 1);
            *//*ScanTask t = ProjectSuccessSacnTask.generateTask(channel, 1);*//*
            t.backtrace = backtrace;
            tasks.add(t);*/

        }

        if (backtrace == true) {
            for (String channel : service_supplier_channels) {
				try {
					URLUtil.PostTask(ServiceScanTask.class,
							null,
							ImmutableMap.of("channel", channel,"page", "1"),
							null,
							null,
							null,
							null,
							null);

					logger.info("PROJECT:" + channel);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}
        }
    }

    /**
     * 获取历史数据
     */
    public void getHistoricalData() {

        // 需求
		getTask(true);
    }

    /**
     * 监控调度
     */
    public void monitoring() {

		getTask(false);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        int num = 0;

        /**
         *
         */
        if (args.length >= 1 && !args[0].equals("") && Integer.parseInt(args[0]) > 1) {
            num = Integer.parseInt(args[0]);
        }

        Scheduler scheduler = new Scheduler("zbj.com", num);

        scheduler.initAuthorizedRequester();

        /*scheduler.getHistoricalData();

        scheduler.monitoring();*/
    }

}
