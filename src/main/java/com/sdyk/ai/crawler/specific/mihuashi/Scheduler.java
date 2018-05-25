package com.sdyk.ai.crawler.specific.mihuashi;

import com.sdyk.ai.crawler.specific.clouderwork.LoginWithGeetestClouderWork;
import com.sdyk.ai.crawler.specific.mihuashi.task.Task;
import com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ServiceScanTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Scheduler extends com.sdyk.ai.crawler.Scheduler {

    public Scheduler(String domain, int driverCount) {
        super(domain, driverCount);
    }

    /**
     * 添加登陆任务
     * @param account
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    @Override
    public Task getLoginTask(Account account) throws MalformedURLException, URISyntaxException {

        // 定义登陆url
        String url = "https://www.mihuashi.com/login";
        // 用户名输入 path
        String usernameCssPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(1) > input";
        // 密码输入 path
        String passwordCssPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(2) > input";
        // 登陆按钮 path
        String loginButtonCssPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(3) > button";

        Task task = new Task(url) {
        };
        task.addAction(
                new LoginWithGeetestClouderWork(account, url, usernameCssPath, passwordCssPath, loginButtonCssPath)
        );

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
        scanTaskList.add(ServiceScanTask.generateTask(1));
        scanTaskList.add(ProjectScanTask.generateTask("1", 1));
        scanTaskList.add(ProjectScanTask.generateTask("2", 1));

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
                    ChromeDriverRequester.getInstance().submit(task);
                }
            }
        });

        s.start();
    }

    /**
     * 程序入口
     */
    public static void main(String[] args){

        Scheduler scheduler = new Scheduler("mihuashi.com", 1);
        if(args.length>0){
            scheduler.monitoring();
        }else{
            System.out.println("历史数据");
            scheduler.getHistoricalData();
        }
    }
}
