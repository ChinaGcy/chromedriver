import com.sdyk.ai.crawler.zbj.StringUtil;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.openqa.selenium.By;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.util.FileUtil;

import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShootText {


	public static void main(String[] args) throws InterruptedException {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.getDriver().get("http://zbj.com");
		Thread.sleep(1000);

		String description_src = agent.getDriver()
				.findElement(By.cssSelector("#utopia_widget_5 > div > div > div.serviceShop-right.fr > div.serviceShop-body.j-serviceShop-scroll > ul")).getAttribute("innerHTML");

		Set<String> img_urls = new HashSet<>();

		String s = StringUtil.cleanContent(description_src, img_urls);


		int x =0;
		for (String i: img_urls) {
			try {
				agent.getDriver().get(i);

				byte[] a = agent.shoot("body > img");

				FileUtil.writeBytesToFile(a, "baidu"+ x +".png");

				x++;
				System.err.println("/*******/");

			} catch (Exception e) {
				continue;
			}

		}
	}



}
