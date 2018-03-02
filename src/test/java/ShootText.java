import com.sdyk.ai.crawler.zbj.util.StringUtil;
import org.openqa.selenium.By;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.util.FileUtil;

import java.util.HashSet;
import java.util.Set;

public class ShootText {


	public static void main(String[] args) throws InterruptedException {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.getDriver().get("http://zbj.com");
		Thread.sleep(1000);

		String description_src = agent.getDriver()
				.findElement(By.cssSelector("#utopia_widget_5 > div > div > div.serviceShop-right.fr > div.serviceShop-body.j-serviceShop-scroll > ul")).getAttribute("innerHTML");

		Set<String> img_urls = new HashSet<>();
		Set<String> a_urls = new HashSet<>();
		String s = StringUtil.cleanContent(description_src, img_urls, a_urls);


		int x =0;
		for (String i: img_urls) {
			try {
				agent.getDriver().get(i);

				byte[] a = agent.shoot("body > img", agent.getDriver());

				FileUtil.writeBytesToFile(a, "baidu"+ x +".png");

				x++;
				System.err.println("/*******/");

			} catch (Exception e) {
				continue;
			}

		}
	}



}
