package com.sdyk.ai.crawler.util.test;

import one.rewind.io.ssh.SshManager;
import org.junit.Test;

import java.io.File;

public class SshHostTest {

	@Test
	public void test() throws Exception {

		File privateKey = new File("docker.pem");

		SshManager.Host ssh_host = new SshManager.Host("10.0.0.56", 22, "root", privateKey);

		ssh_host.connect();

		System.err.println(ssh_host.exec("ps aux"));
	}
}
