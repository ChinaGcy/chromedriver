package tfelab.util.test;

import org.junit.Test;
import org.tfelab.util.NetworkUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtilTest {

	@Test
	public void testGetIP() throws UnknownHostException {
		System.err.println(NetworkUtil.getLocalIp());
		System.err.println(InetAddress.getLocalHost());
	}
}
