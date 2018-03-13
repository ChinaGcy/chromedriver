import com.sdyk.ai.crawler.zbj.requester.ChromeDriverLoginWrapper;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.LinkedList;
import java.util.Queue;

public class ModuleTest {

	private static final Logger logger = LogManager.getLogger(ModuleTest.class.getName());

	@Test
	public void projectScanTask() throws Exception {
		ChromeDriverAgent agent = (new ChromeDriverLoginWrapper("zbj.com")).login(null, null);
		Queue<Task> taskQueue = new LinkedList<>();
		taskQueue.add(ProjectScanTask.generateTask("t-dhsjzbj",1,null));

		while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			if(t != null) {
				try {
					agent.fetch(t);
					for (Task t_ : t.postProc(agent.getDriver())) {
						taskQueue.add(t_);
						//agent.fetch(t_);
					}

				} catch (Exception e) {
					logger.error("Exception while fetch task. ", e);
					taskQueue.add(t);
				}
			}
		}
	}
}
