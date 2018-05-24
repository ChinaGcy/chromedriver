package com.sdyk.ai.crawler.specific.zbj;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ServiceScanTask;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.proxy.Proxy;
import org.apache.logging.log4j.LogManager;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
            "t-wxptkf",            // 微信开发
            "t-xxtg",              // 公关活动/线下地推/会议展览
            "t-yxtg",              // 营销传播
            "t-ppglzxzbj",         // 品牌咨询管理
            "t-dsyxfwzbj"          // 电商营销服务*/
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
            "ppglzxzbj",*/           // 品牌咨询管理
            "dsyxfwzbj"            // 电商营销服务
    };

    public Scheduler(String domain, int driverCount) {
        super(domain, driverCount);
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
    public List<ScanTask> getTask(boolean backtrace) {

        List<ScanTask> tasks = new ArrayList<>();

        for(String channel : project_channels) {
            ScanTask t = ProjectScanTask.generateTask(channel, 1);
            t.backtrace = backtrace;
            tasks.add(t);
            System.out.println("PROJECT:" + channel);
        }

        if (backtrace == true) {
            for (String channel : service_supplier_channels) {
                ScanTask t = ServiceScanTask.generateTask(channel, 1);
                t.backtrace = backtrace;
                tasks.add(t);
                System.out.println("SERVICE:" + channel);
            }
        }

        return tasks;
    }

    /**
     * 获取历史数据
     */
    public void getHistoricalData() {

        // 需求
        for (Task task : getTask(true)) {
            ChromeDriverRequester.getInstance().submit(task);
        }
    }

    /**
     * 监控调度
     */
    public void monitoring() {

        try {

            it.sauronsoftware.cron4j.Scheduler s = new it.sauronsoftware.cron4j.Scheduler();

            // 每隔十分钟，生成实时扫描任务
            s.schedule("*/10 * * * *", new Runnable() {

                public void run() {

                    for (Task task : getTask(false)) {

                        task.setBuildDom();
                        ChromeDriverRequester.getInstance().submit(task);
                    }
                }
            });

            s.start();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        int num = 1;

        /**
         *
         */
        if (!args[1].equals("") && Integer.parseInt(args[1]) > 1) {
            num = Integer.parseInt(args[1]);
        }

        Scheduler scheduler = new Scheduler("zbj.com", num);

        /**
         *
         */
        if (args.length == 2 && args[0].equals("H")){
            // 获取历史数据
            System.out.println("历史数据");
            scheduler.getHistoricalData();

        }
        else {
            // 监控数据
            System.out.println("监控数据");
            scheduler.monitoring();
        }
    }

}
