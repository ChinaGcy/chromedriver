package com.sdyk.ai.crawler.specific.zbj;


import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ServiceScanTask;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static spark.route.HttpMethod.get;

/**
 * 任务生成器
 */
public class Scheduler extends com.sdyk.ai.crawler.Scheduler{


    // 项目频道参数
    public String[] project_channels = {
            "t-paperwork",         // 策划
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
            "t-dsyxfwzbj"          // 电商营销服务
    };

    // 服务商频道参数
    public static String[] service_supplier_channels = {
            "paperwork",           // 策划
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
            "dsyxfwzbj"            // 电商营销服务
    };

    public String cron = "*/30 * * * *";

    public Scheduler(String domain, int driverCount) {
        super(domain, driverCount);
    }

    /**
     *
     */
    public void initAuthorizedRequester() {

        try {

            Account account = AccountManager.getAccountByDomain(domain, "select");

            com.sdyk.ai.crawler.task.Task task = getLoginTask(account);

            // 创建一个新的container
            DockerHostManager.getInstance().createDockerContainers(1);

            ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

            // 不使用代理
            ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container, ChromeDriverAgent.Flag.MITM);

            // agent 添加异常回调
            agent.addAccountFailedCallback(()->{

                logger.info("Account {}:{} failed.", account.domain, account.username);

            }).addTerminatedCallback(()->{

                logger.info("Container {} {}:{} Terminated.", container.name, container.ip, container.vncPort);

            }).addNewCallback(()->{

                try {
                    agent.submit(task, 300000);
                } catch (Exception e) {
                    logger.error(e);
                }
            });

            AuthorizedRequester.getInstance().addAgent(agent);
            agent.start();

            logger.info("ChromeDriverAgents are ready.");

        } catch (Exception e) {
            logger.error("Error init authorized requester. ", e);
        }
    }

    /**
     *
     * @param account
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public Task getLoginTask(Account account) throws MalformedURLException, URISyntaxException {

        Task task = new Task("https://www.zbj.com");
        task.addAction(new LoginWithGeetestAction(account));
        return task;
    }

    /**
     *
     * @param backtrace
     * @return
     */
    public List<com.sdyk.ai.crawler.task.Task> getTask(boolean backtrace) {

        List<com.sdyk.ai.crawler.task.Task> tasks = new ArrayList<>();

        for(String channel : project_channels) {
            ScanTask t = ProjectScanTask.generateTask(channel, 1);
            t.backtrace = backtrace;
            tasks.add(t);
            logger.info("PROJECT:" + channel);
        }

        if (backtrace == true) {
            for (String channel : service_supplier_channels) {
                ScanTask t = ServiceScanTask.generateTask(channel, 1);
                t.backtrace = backtrace;
                tasks.add(t);
                logger.info("SERVICE:" + channel);
            }
        }

        return tasks;
    }

    /**
     * 获取历史数据
     */
    public void getHistoricalData() {

        // 需求
        for (com.sdyk.ai.crawler.task.Task task : getTask(true)) {
            ChromeDriverRequester.getInstance().submit(task);
        }
    }

    /**
     * 监控调度
     */
    public void monitoring() {

        try {

            it.sauronsoftware.cron4j.Scheduler s = new it.sauronsoftware.cron4j.Scheduler();

            // 每隔30分钟，生成实时扫描任务
            s.schedule(cron, () -> {
                for (com.sdyk.ai.crawler.task.Task task : getTask(false)) {
                    ChromeDriverRequester.getInstance().submit(task);
                }
            });

            s.start();

        } catch (Exception e) {

            logger.error(e);
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        new Thread(() -> {
            ServiceWrapper.getInstance();
        });

        int num = 1;

        /**
         *
         */
        if (args.length > 1 && !args[0].equals("") && Integer.parseInt(args[0]) > 1) {
            num = Integer.parseInt(args[0]);
        }

        Scheduler scheduler = new Scheduler("zbj.com", num);

        scheduler.initAuthorizedRequester();

        /*scheduler.getHistoricalData();

        scheduler.monitoring();*/
    }

}
