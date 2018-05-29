package com.sdyk.ai.crawler.specific.proLagou;

import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.specific.clouderwork.LoginWithGeetestClouderWork;
import com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.apache.logging.log4j.LogManager;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Scheduler extends com.sdyk.ai.crawler.Scheduler{

    protected static com.sdyk.ai.crawler.specific.clouderwork.Scheduler instance;
    public static int num = 1;
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(com.sdyk.ai.crawler.Scheduler.class.getName());

    public Scheduler(String domain, int driverCount) {
        super(domain, driverCount);
    }

    /**
     * 登陆任务
     * @param account
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public Task getLoginTask(Account account) throws MalformedURLException, URISyntaxException {

        String url = "https://passport.lagou.com/pro/login.html";
        String usernameCssPath = "#user_name";
        String passwordCssPath = "#main > form > div:nth-child(2) > input[type=\"password\"]";
        String loginButtonCssPath = "#main > form > div.clearfix.btn_login > input[type=\"submit\"]";

        Task task = new Task("https://passport.lagou.com/pro/login.html");
        task.addAction(new LoginWithGeetestClouderWork(account,url,usernameCssPath,passwordCssPath,loginButtonCssPath));
        return task;
    }

    /**
     * 获取详情页列表
     * @param backtrace
     * @return
     */
    @Override
    public List<com.sdyk.ai.crawler.task.Task> getTask(boolean backtrace) {

        List<com.sdyk.ai.crawler.task.Task> scanTaskList = new ArrayList<>();
        scanTaskList.add(ProjectScanTask.generateTask(90));
        return scanTaskList;
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
     * 监控调度
     */
    @Override
    public void monitoring() {

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
    }

    //程序入口
    public static void main(String[] args){

        new Thread(()->{
            new ServiceWrapper();
        }).start();

        Scheduler scheduler = new Scheduler("pro.lagou.com", 2);
        if(args.length>0){
            scheduler.monitoring();
        }else{
            scheduler.getHistoricalData();
        }
    }
}
