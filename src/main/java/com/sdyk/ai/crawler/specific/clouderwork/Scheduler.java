package com.sdyk.ai.crawler.specific.clouderwork;

import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ServiceScanTask;
import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
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

public class Scheduler extends com.sdyk.ai.crawler.Scheduler {

    protected static Scheduler instance;
    public static int num = 1;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(com.sdyk.ai.crawler.Scheduler.class.getName());

    public Scheduler(String domain, int driverCount) {
        super(domain, driverCount);
    }

    public Task getLoginTask(Account account) throws MalformedURLException, URISyntaxException {

        Task task = new Task("https://www.zbj.com");
        task.addAction(new LoginWithGeetestClouderWork(account));
        return task;
    }

    /**
     * @param backtrace
     * @return
     */
    @Override
    public List<com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask> getTask(boolean backtrace) {
        return null;
    }

    /**
     * 获取历史数据
     */
    @Override
    public void getHistoricalData() {

    }

    //程序入口
    public static void main(String[] args){


    }

    public void getData(){
        // 需求
        for (Task task : getScanTask()) {
            ChromeDriverRequester.getInstance().submit(task);
        }
    }

    //获取列表任务
    public List<ScanTask> getScanTask(){

        List<ScanTask> scanTaskList = new ArrayList<>();

        scanTaskList.add(ServiceScanTask.generateTask(1));

        scanTaskList.add(ProjectScanTask.generateTask(1));

        return  scanTaskList;
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
                    for (Task task : getScanTask()) {
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

}
