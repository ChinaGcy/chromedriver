package com.sdyk.ai.crawler.zbj.mouse;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.math.Fraction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.json.JSON;
import org.tfelab.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import java.lang.reflect.Type;
import java.util.Random;

/**
 * 对鼠标事件进行建模分析
 * @Author scisaga@gmail.com
 * @Date 2018/3/30
 *
 * 1. 加载鼠标事件
 * 2. 对鼠标轨迹进行分析
 * 2.1 将轨迹进行分组
 *   找到较为平滑的阶段
 *   较为平滑的阶段可以伸缩操作，以自适应不同的位移和时间输入
 *   非平滑阶段可进行相应变形，保留人工关键特征
 * 3. 设计方法，随机选择已有轨迹，对轨迹进行缩减或拓展，生成新的轨迹
 * 4. 对人工响应时间进行找到对应模型，生成事件列表（轨迹）
 */
public class MouseEventModeler {

	private static final Logger logger = LogManager.getLogger(MouseEventModeler.class.getName());

	List<MouseEventTracker> trackerList = new ArrayList<>();

	List<Model> models = new ArrayList<>();

	/**
	 * Step 刻画两个Actions之间的过程
	 */
	public static class Step {

		// 时间差
		int dt = 0;

		// x方向速度
		Fraction v_x = Fraction.ONE;
		// y方向速度
		Fraction v_y = Fraction.ONE;

		int dx;
		int dy;

		// 是否为平滑阶段
		boolean flat_phase = false;

		boolean flat_phase_edge = false;

		/**
		 * 构建一个Step
		 * @param from 起始事件
		 * @param to 终止事件
		 * @throws Exception 起始时间和终止时间的时间差为0时 抛出异常
		 */
		public Step (Action from, Action to) throws Exception {

			dt = (int) (to.time - from.time);
			if(dt == 0) {
				throw new Exception("Two actions with same timestamp.");
			}
			dx = to.x - from.x;
			dy = to.y - from.y;
			v_x = Fraction.getFraction(dx, dt);
			v_y = Fraction.getFraction(dy, dt);
		}

		/**
		 * Add 1px
		 */
		public void addOnePixel() {
			// 限平滑阶段 t = 8ms
			if(flat_phase) {
				v_x = v_x.add(Fraction.getFraction(1, 8));

				// TODO 此处可能没有意义，有可能会导致轨迹发生显著变化
				if(dy > 0) {
					v_y = v_y.add(Fraction.getFraction(1, 8));
				}
				dx = (int) Math.round(v_x.doubleValue() * dt);
				dy = (int) Math.round(v_y.doubleValue() * dt);
			}
		}

		/**
		 * Subtract 1px
		 */
		public void subtractOnePixel() {
			// 限平滑阶段 && X方向速度 > 0
			// t = 8ms
			if(flat_phase && v_x.doubleValue() > 0) {
				v_x = v_x.subtract(Fraction.getFraction(1, 8));

				// TODO 此处可能没有意义，有可能会导致轨迹发生显著变化
				if(dy > 0) {
					v_y = v_y.subtract(Fraction.getFraction(1, 8));
				}
				dx = (int) Math.round(v_x.doubleValue() * dt);
				dy = (int) Math.round(v_y.doubleValue() * dt);
			}
		}

		/**
		 * 随机变异
		 *
		 * 非平滑过程 Step 时间间隔，速度联动变化 （特征变化）
		 * 保证Step期间总位移不变，但轨迹特征在可控范围内随机变化
		 * dt' * v' = dt * v
		 */
		public void mutation() {

			// 限非平滑阶段，阶段时间为10ms以上
			if(!flat_phase && dt > 10) {

				Random generator = new Random();
				double f = generator.nextDouble() * 0.4 - 0.2;

				dt = (int) (dt * (1 + f));
				if(dt < 8) dt = 8;
				v_x = Fraction.getFraction(dx, dt);
				v_y = Fraction.getFraction(dy, dt);
			}
		}
	}

	/**
	 * 用于保存一个轨迹模型
	 *
	 *
	 * 轨迹模型的基本变化思路
	 * 1. 平滑拖拽过程的速度变化
	 * 2. 非平滑拖拽过程的采样点间隔变化
	 *
	 * 首先找到平滑阶段的Actions
	 *
	 * 三种变化过程
	 *
	 * A. 平滑过程增减Step （总位移变化，但变化有一定限制）
	 *   由于平滑过程的时间间隔是8ms 增加点速度可以是 1/8 px/ms 的整倍数
	 *   增加一个step，相当于增加了一个Action，整体轨迹被拉长或压缩
	 *   新增的Step 其速度应该与相邻的 Step 差别不大（正负50%）
	 *
	 *   TODO 找到一个平滑过程的像素位移分布
	 *
	 * B. 平滑过程Step 增减速度 （总位移变化）
	 *   增减速度为 1/8 px/ms 的整倍数，速度不能小于0，速度增量不能大于50px/ms，不能大于当前step速度的50%
	 *   这个变化过程相当于对整体轨迹进行局部拉伸或压缩
	 *
	 * C. 非平滑过程 Step 时间间隔，速度联动变化 （总轨迹不变，时间变化，特征变化）
	 *    保证Step期间总位移不变，但轨迹特征在可控范围内随机变化
	 *    dt' * v' = dt * v
	 *
	 * 对于一个轨迹，可适应的总位移变化是有限的，暂定 正负10px
	 * 针对一个Model 和 dx 变化
	 *
	 */
	public static class Model {

		// 事件间间隔
		List<Step> steps = new ArrayList<>();

		// 平滑阶段的索引，只包含非边缘点
		List<Integer> flat_step_indices = new ArrayList<>();
		// 非平滑阶段索引
		List<Integer> non_flat_step_indices = new ArrayList<>();

		// 总时间，总x方向长度，总y方向长度
		long t_sum = 0;
		int x_sum, y_sum = 0;

		// 可支持的 x 拓展范围
		int[] x_span = new int[2];

		/**
		 *
		 * @param actions
		 */
		public Model(List<Action> actions) {

			// 根据actions 生成 steps
			for(int i=1; i<actions.size(); i++) {

				try {
					Step step = new Step(actions.get(i-1), actions.get(i));
					steps.add(step);
					t_sum += step.dt;
					x_sum += step.dx;
					y_sum += step.dy;
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

			// 初始化平滑step的索引 和 非平滑step的索引
			for(int i=0; i < steps.size(); i++) {

				if(steps.get(i).flat_phase) {

					// 判断该阶段是否是平滑阶段的边缘
					if(i > 0 && !steps.get(i).flat_phase) {
						steps.get(i).flat_phase_edge = true;
					} else if(i < steps.size() - 1 && !steps.get(i+1).flat_phase) {
						steps.get(i).flat_phase_edge = true;
					} else {
						// 只包含非边缘点
						flat_step_indices.add(i);
					}
				} else {
					non_flat_step_indices.add(i);
				}
			}
		}

		/**
		 * 将总轨迹 +1 px
		 *
		 * A. 随机找到一个平滑阶段中step（时间间隔8ms），速度 + 0.125 px/ms
		 *
		 * B. 在已有的平滑阶段中增加一个step，速度为 0.125 px/ms
		 */
		public void expand() {

		}

		/**
		 * 找到一个随机平滑阶段
		 * @param hasVelocity 该阶段是否有速度
		 *                    没有速度的平滑阶段无法减少速度，不能用于轨迹压缩
		 * @return 随机找到的平滑阶段
		 */
		public Step getRandomFlatStep(boolean hasVelocity) {

			Step step = null;
			int search_count = 0;
			while (step == null && search_count < 3) {
				int rnd = new Random().nextInt(flat_step_indices.size());
				step = steps.get(rnd);
				if(step.dx == 0 && hasVelocity) step = null;
			}

			return step;
		}

		public void mutation() {

		}

		/**
		 * 通过steps重新构建actions
		 * @return
		 */
		public List<Action> buildActions() {

			List<Action> actions = new ArrayList<>();

			return actions;
		}

	}

	/**
	 * 找到属于平滑阶段的Actions
	 */
	public void findFlatPhase(List<Action> actions) {

	}

	/**
	 * 对鼠标左键按下之前的事件进行清理
	 * @param actions
	 */
	public static void removePreMoveActions(List<Action> actions) {

		List<Action> actions_ = new ArrayList<>();

		for(Action action : actions) {

			if(! action.type.equals(Action.Type.Move)) {
				actions_.add(action);
			}
		}

		actions = actions_;
}

	/**
	 * 加载数据
	 * @return
	 */
	public static List<List<Action>> loadData() {

		List<List<Action>> actions = new ArrayList<>();

		Type type = new TypeToken<ArrayList<Action>>(){}.getType();

		File folder = new File(MouseEventTracker.serPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {

			if (listOfFiles[i].isFile()) {

				logger.info("File: " + listOfFiles[i].getPath());

				List<Action> as = JSON.fromJson(
						FileUtil.readFileByLines(listOfFiles[i].getPath()),
						type
				);

				actions.add(as);

			} else if (listOfFiles[i].isDirectory()) {
				logger.info("Directory: " + listOfFiles[i].getName());
			}
		}

		return actions;
	}

	/**
	 * 读取单个Action 序列
	 * @param path
	 * @return
	 */
	public static List<Action> loadData(String path) {

		List<Action> actions = new ArrayList<>();

		Type type = new TypeToken<ArrayList<Action>>(){}.getType();

		actions = JSON.fromJson(
				FileUtil.readFileByLines(path),
				type
		);

		return actions;
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {


	}
}
