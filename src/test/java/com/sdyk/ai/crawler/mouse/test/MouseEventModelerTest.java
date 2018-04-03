package com.sdyk.ai.crawler.mouse.test;

import com.sdyk.ai.crawler.zbj.mouse.Action;
import com.sdyk.ai.crawler.zbj.mouse.MouseEventModeler;
import org.junit.Test;
import org.tfelab.util.FileUtil;

import java.util.List;

import static com.sdyk.ai.crawler.zbj.mouse.MouseEventModeler.*;

public class MouseEventModelerTest {

	@Test
	public void singleBuildTest() throws Exception {

		List<Action> actions = loadData(
				"mouse_movements/1521357775962_b556ceac-ce7d-470f-821e-0f142500837f.txt");

		MouseEventModeler.Model model = new MouseEventModeler.Model(actions);

		String output = toMathematicaListStr(model.buildActions());

		FileUtil.writeBytesToFile(output.getBytes(), "original_actions.txt");

		model.morph(-10);

		output = toMathematicaListStr(model.buildActions());

		FileUtil.writeBytesToFile(output.getBytes(), "new_actions.txt");
	}

	@Test
	public void modelerTest() throws Exception {

		MouseEventModeler modeler = new MouseEventModeler();

		for(int i=40; i<=200; i++) {

			try {
				MouseEventModeler.logger.info(i);
				String output = toMathematicaListStr(modeler.getActions(i));
				FileUtil.writeBytesToFile(output.getBytes(), "new_actions/" + i + ".txt");
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
