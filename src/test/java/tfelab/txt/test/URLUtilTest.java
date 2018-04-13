package tfelab.txt.test;

import org.junit.Test;
import org.tfelab.txt.URLUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class URLUtilTest {

	@Test
	public void getRootDomainNameTest() {
		URLUtil.getRootDomainName("abc.sss.taobao.com.cn");
	}

	@Test
	public void getProtocolTest() throws MalformedURLException, URISyntaxException {
		URLUtil.getProtocol("https://pic.36krcnd.com/avatar/201609/08060207/6b5gr77ktodsf70j.jpg!heading");
	}
}
