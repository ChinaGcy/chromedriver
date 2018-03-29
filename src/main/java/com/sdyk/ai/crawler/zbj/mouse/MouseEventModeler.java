package com.sdyk.ai.crawler.zbj.mouse;

import org.apache.commons.lang3.math.Fraction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.json.JSON;
import org.tfelab.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 对鼠标事件进行建模分析
 * 1. 加载鼠标事件
 * 2. 对鼠标轨迹进行分析
 * 2.1 将轨迹进行分组
 *   找到较为平滑的阶段
 *   较为平滑的阶段可以伸缩操作，以自适应不同的位移和时间输入
 *   非平滑阶段保留，保留人工关键特征
 * 3. 设计方法，随机选择已有轨迹，对轨迹进行缩减或拓展，生成新的轨迹
 * 4. 对人工响应时间进行建模
 * 5. 对
 *
 */
public class MouseEventModeler {

	private static final Logger logger = LogManager.getLogger(MouseEventModeler.class.getName());

	List<MouseEventTracker> trackerList = new ArrayList<>();

	List<Model> models = new ArrayList<>();

	/**
	 * Step 刻画两个Actions之间的过程
	 */
	public static class Step {

		MouseEventTracker.Action from;
		MouseEventTracker.Action to;

		int dt = 0;

		Fraction v_x = Fraction.ONE;
		Fraction v_y = Fraction.ONE;

		boolean flat_phase = false;

		public Step (MouseEventTracker.Action from, MouseEventTracker.Action to) throws Exception {
			this.from = from;
			this.to = to;
			dt = (int) (to.time - from.time);
			if(dt == 0) {
				throw new Exception("Two actions with same timestamp.");
			}
			v_x = Fraction.getFraction(to.x - from.x, dt);
			v_y = Fraction.getFraction(to.y - from.y, dt);
		}
	}

	/**
	 * 用于保存一个轨迹模型
	 */
	public static class Model {

		// 原始事件
		List<MouseEventTracker.Action> actions;

		// 事件间间隔
		List<Step> steps = new ArrayList<>();

		long t_sum = 0;
		int x_sum = 0;
		int y_sum = 0;

		public Model(List<MouseEventTracker.Action> actions) {

			this.actions = actions;

			// 根据actions 生成 steps
			for(int i=1; i<actions.size(); i++) {

				try {
					Step step = new Step(actions.get(i-1), actions.get(i));
					steps.add(step);
				} catch (Exception e) {
					logger.warn("Two actions with same timestamp.");
				}
			}

			// 遍历 steps 找到 平滑拖拽阶段
			for(int i=0; i < steps.size() - 2; i++) {
				if(steps.get(i).dt == 8
						&& steps.get(i + 1).dt == 8
						&& steps.get(i + 2).dt == 8) {
					steps.get(i).flat_phase = true;
					steps.get(i + 1).flat_phase = true;
					steps.get(i + 2).flat_phase = true;
				}
			}


		}


	}

	/**
	 * 加载数据
	 */
	public void loadData() {

		File folder = new File("mouse_movements");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile()) {

				logger.info("Load file: " + listOfFiles[i].getPath());

				MouseEventTracker m = JSON.fromJson(
						FileUtil.readFileByLines(listOfFiles[i].getPath()),
						MouseEventTracker.class
				);

				trackerList.add(m);

			} else if (listOfFiles[i].isDirectory()) {
				logger.info("Directory: " + listOfFiles[i].getName());
			}
		}
	}

	/**
	 * 找到属于平滑阶段的Actions
	 */
	public void findFlatPhase(List<MouseEventTracker.Action> actions) {

	}



	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		MouseEventModeler modeler = new MouseEventModeler();
		modeler.loadData();
	}

}
