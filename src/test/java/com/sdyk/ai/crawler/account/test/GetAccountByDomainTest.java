package com.sdyk.ai.crawler.account.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.model.Domain;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;

import java.util.List;

public class GetAccountByDomainTest {

	@Test
	public void test() throws Exception {

		List<Domain> domains = Domain.getAll();

		for( Domain d : domains ){

			for( int i = 0; i< 1; i++ ){

				Account account = AccountManager.getInstance().getAccountByDomain(d.domain);

				if( account != null ){
					System.out.println(account.username);
				}
				else {
					System.out.println("account 为 null");
				}

			}

		}

	}

}
