package com.sdyk.ai.crawler.loginTaskTest;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.Scheduler;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.model.LoginTaskWrapper;
import com.sdyk.ai.crawler.model.TaskInitializer;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.HttpTaskSubmitter;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.GeetestAction;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static one.rewind.util.FileUtil.readFileByLines;

public class LoginTaskTest {

	@Test
	public void testLoginTaskOneTime() throws Exception{

		//ChromeDriverDistributor.instance = new Distributor();

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/zbj.com.json"));

		//ChromeDriverAgent agent = new ChromeDriverAgent();
		//((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

		//Account account = new AccountImpl("clouderwork.com", "18618493383", "123456");
		//((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

		//((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		LoginTaskWrapper loginTaskWrapper = new LoginTaskWrapper("zbj.com", loginTask);
		loginTaskWrapper.insert();

		Thread.sleep(1000000);

	}

	@Test
	public void testLoginTaskTwoTime() throws Exception{

		ChromeDriverDistributor.instance = new Distributor();

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/clouderwork.com.json"));
		LoginTask loginTask1 = LoginTask.buildFromJson(readFileByLines("login_tasks/mihuashi.com.json"));

		ChromeDriverAgent agent = new ChromeDriverAgent();
		((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

		Account account = new AccountImpl("clouderwork.com", "18618493383", "123456");
		((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);
		Account account1 = new AccountImpl("mihuashi.com", "18618493383", "123456");
		((LoginAction)loginTask1.getActions().get(loginTask1.getActions().size()-1)).setAccount(account1);

		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask1);
		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		Thread.sleep(1000000);

	}

	@Test
	public void testScheduler() throws Exception {

		//Scheduler.Flags = new ArrayList<>();

		Scheduler.getInstance();

		TaskInitializer.getAll().stream().filter(t -> {
			return t.enable == true;
		}).forEach( t ->{

			try {

				if( t.cron == null ){

					// 历史任务
					HttpTaskSubmitter.getInstance().submit(t.class_name, t.init_map_json);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		});

		Thread.sleep(1000000);
	}

	@Test
	public void ExceptionTest(){

		List<String> srclist = new ArrayList<>();
		try{

			try {

				srclist.get(1);

			} catch (Exception e) {
				System.out.println("1");
			}

		} catch (Exception e){
			e.printStackTrace();
		}
		finally {
			System.out.println( "finally" );
		}

		System.out.println("end");

		long l = Long.valueOf("1499961600000");

		System.out.println(new Date(l));
	}

	@Test
	public void testZBJLoginTaskToJson() throws  Exception{

		LoginTask loginTask = new LoginTask("https://login.zbj.com/login");

		LoginWithGeetestAction loginWithGeetestAction = new LoginWithGeetestAction();
		GeetestAction action = new GeetestAction ();

		action.geetestWindowCssPath = loginWithGeetestAction.geetestWindowCssPath;
		action.geetestSliderButtonCssPath = loginWithGeetestAction.geetestSliderButtonCssPath;
		action.geetestSuccessMsgCssPath = loginWithGeetestAction.geetestSuccessMsgCssPath;
		action.geetestResetTipCssPath = loginWithGeetestAction.geetestResetTipCssPath;
		action.geetestRefreshButtonCssPath = loginWithGeetestAction.geetestRefreshButtonCssPath;

		loginWithGeetestAction.action = action;

		loginTask.addAction(loginWithGeetestAction);

		System.out.println(loginTask.toJSON());

	}

	@Test
	public void testZBJLoginTask() throws Exception{

		ChromeDriverDistributor.instance = new Distributor();

		GeetestAction geetestAction = new GeetestAction();

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/zbj.com.json"));

		ChromeDriverAgent agent = new ChromeDriverAgent();
		((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

		Account account = new AccountImpl("zbj.com", "15284812217", "123456");
		((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		Thread.sleep(1000000);
	}

	@Test
	public void testInsetToDataBase() throws Exception{

		Domain.getAll().stream()
				.map(d -> d.domain)
				.forEach(d -> {

					try {
						LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/" + d + ".json"));
						LoginTaskWrapper loginTaskWrapper = new LoginTaskWrapper(d, loginTask);
						loginTaskWrapper.insert();

					} catch (Exception e) {
						e.printStackTrace();
					}
				});

		Thread.sleep(1000000);
	}

	@Test
	public void testZBJLoginTest() throws Exception {

		ChromeDriverDistributor.instance = new Distributor();

		ChromeDriverAgent agent = new ChromeDriverAgent();
		((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

		/*LoginTaskWrapper taskloging = new LoginTaskWrapper();
		LoginTask task = taskloging.getLoginTaskByDomain("zbj.com");*/

		Dao dao = DaoManager.getDao(LoginTaskWrapper.class);
		LoginTaskWrapper loginTaskWrapper = (LoginTaskWrapper) dao.queryForId("48");

		Account account = new AccountImpl("zbj.com", "15284812217", "123456");
		((LoginAction)loginTaskWrapper.login_task.getActions().get(loginTaskWrapper.login_task.getActions().size()-1)).setAccount(account);

		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTaskWrapper.login_task);

		Thread.sleep(100000000);

	}
}
