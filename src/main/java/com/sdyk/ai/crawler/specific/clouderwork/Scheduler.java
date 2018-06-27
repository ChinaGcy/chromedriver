package com.sdyk.ai.crawler.specific.clouderwork;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ServiceScanTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.apache.logging.log4j.LogManager;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Scheduler extends com.sdyk.ai.crawler.Scheduler {

    protected static Scheduler instance;
    public static int num = 1;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(com.sdyk.ai.crawler.Scheduler.class.getName());

    public Scheduler(String domain, int driverCount) {
        super(domain, driverCount);
    }

    public Task getLoginTask(Account account) throws MalformedURLException, URISyntaxException {

        String url = "https://passport.clouderwork.com/signin";
        String usernameCssPath = "#app > div > div > div > section > dl > dd:nth-child(1) > input[category=\"text\"]";
        String passwordCssPath = "#app > div > div > div > section > dl > dd:nth-child(2) > input[category=\"password\"]";
        String loginButtonCssPath = "#app > div > div > div > section > button:nth-child(3)";

        Task task = new Task("https://passport.clouderwork.com/signin");
        task.addAction(
                new LoginWithGeetestClouderWork(account,url,usernameCssPath,passwordCssPath,loginButtonCssPath));
        return task;
    }

    /**
     * 获取详情页列表
     * @param backtrace
     * @return
     */
    @Override
    public void getTask(boolean backtrace) {

        List<com.sdyk.ai.crawler.task.Task> scanTaskList = new ArrayList<>();

        scanTaskList.add(ServiceScanTask.generateTask(1));

        scanTaskList.add(ProjectScanTask.generateTask(1));

        return  scanTaskList;
    }


    /**
     * 获取历史数据
     */
    @Override
    public void getHistoricalData() {

        for (com.sdyk.ai.crawler.task.Task task : getTask(true)) {
            ChromeDriverRequester.getInstance().submit(task);
        }

    }

    /**
     * 程序入口
     * @param args
     */
    public static void main(String[] args){

        //调用sparkjava查看队列任务
        new Thread(()->{
            new ServiceWrapper();
        }).start();

        Scheduler scheduler = new Scheduler("passport.clouderwork.com", 4);
        if(args.length>0){
            scheduler.monitoring();
        }else{
            scheduler.getHistoricalData();

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
                    for (com.sdyk.ai.crawler.task.Task task : getTask(true)) {
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
