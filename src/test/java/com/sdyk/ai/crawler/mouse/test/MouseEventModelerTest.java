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

		List<Action> actions = loadData("mouse_movements/1521357776022_6a62fed3-55ad-44d6-8ce8-205c6514dc9b.txt");

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

		String output = toMathematicaListStr(modeler.getActions(100));

		FileUtil.writeBytesToFile(output.getBytes(), "new_actions.txt");

	}
}
