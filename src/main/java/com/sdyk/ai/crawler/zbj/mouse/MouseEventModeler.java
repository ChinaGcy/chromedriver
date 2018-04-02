package com.sdyk.ai.crawler.zbj.mouse;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.math.Fraction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;
import org.tfelab.util.FileUtil;

import java.io.File;
import java.util.*;

import java.lang.reflect.Type;

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

	List<Model> models = new ArrayList<>();

	/**
	 * Step 刻画两个Actions之间的过程
	 */
	public static class Step implements JSONable<Step>{

		// 时间差
		int dt = 0;

		// x方向速度
		transient Fraction v_x = Fraction.ONE;
		// y方向速度
		transient Fraction v_y = Fraction.ONE;

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
		 *
		 * @param dx
		 * @param dy
		 * @param dt
		 * @param flat_phase
		 */
		public Step (int dx, int dy, int dt, boolean flat_phase) {
			v_x = Fraction.getFraction(dx, dt);
			v_y = Fraction.getFraction(dy, dt);
			this.flat_phase = flat_phase;
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

		@Override
		public String toJSON() {
			return JSON.toJson(this);
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

		int x_init, y_init = 0;
		long t0 = 0;

		boolean init = false;

		// 事件间间隔
		List<Step> steps = new ArrayList<>();

		// 平滑阶段的索引，只包含非边缘点
		List<Step> flat_steps = new ArrayList<>();
		// 非平滑阶段索引
		List<Step> non_flat_steps = new ArrayList<>();

		// 总时间，总x方向长度，总y方向长度
		long t_sum = 0;
		int x_sum, y_sum = 0;

		// 可支持的 x 拓展范围
		int x_sum_ub, x_sum_lb = 0;

		// 基于位移的平滑阶段索引，用于根据位移找到对应的平滑阶段
		TreeMap<Integer, List<Step>> dx_to_flat_steps = new TreeMap<>();

		/**
		 *
		 * @param actions
		 */
		public Model(List<Action> actions) {

			logger.info("Init model...");

			// A. 根据actions 生成 steps
			for(int i=1; i<actions.size(); i++) {

				if(i == 1) {
					x_init = actions.get(i-1).x;
					y_init = actions.get(i-1).y;
					t0 = actions.get(i-1).time;
				}

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

			// 计算 x_sum_ub x_sum_lb
			x_sum_ub = x_sum + 10;
			x_sum_lb = x_sum > 20? x_sum - 10: x_sum;

			// B. 遍历 steps 找到 平滑拖拽阶段
			for(int i=0; i < steps.size() - 2; i++) {
				if(steps.get(i).dt == 8
						&& steps.get(i + 1).dt == 8
						&& steps.get(i + 2).dt == 8) {
					steps.get(i).flat_phase = true;
					steps.get(i + 1).flat_phase = true;
					steps.get(i + 2).flat_phase = true;
				}
			}

			// C. 初始化 平滑step 和 非平滑step 相关索引
			for(int i=0; i < steps.size(); i++) {

				if(steps.get(i).flat_phase) {

					// C1. 判断该阶段是否是平滑阶段的边缘
					if(i > 0 && !steps.get(i).flat_phase) {
						steps.get(i).flat_phase_edge = true;
					} else if(i < steps.size() - 1 && !steps.get(i+1).flat_phase) {
						steps.get(i).flat_phase_edge = true;
					} else {
						// 只包含非边缘点
						flat_steps.add(steps.get(i));

						// C2. 创建平滑阶段的时速度索引
						int dx = steps.get(i).dx;
						addStepIntoDxMap(steps.get(i));
					}

				} else {
					non_flat_steps.add(steps.get(i));
				}
			}

			init = true;
		}

		/**
		 * 将一个新的step 插入到 位移step 索引
		 * @param step 新生成 step
		 */
		private void addStepIntoDxMap(Step step) {
			int dx = step.dx;
			if (!dx_to_flat_steps.containsKey(dx)) {
				dx_to_flat_steps.put(dx, new ArrayList<>());
			}
			dx_to_flat_steps.get(dx).add(step);
		}

		/**
		 * 变形
		 *
		 * 需要根据具体的变形像素数决定策略
		 *
		 * @param px 需要变形的像素数
		 */
		public void morph(int px) throws Exception {

			logger.info("Morph: {}px", px);

			// A 拉伸情况
			if (px > 0 && px <= x_sum_ub - x_sum) {

				logger.info("\tStretching...");

				int px_to_stretch = px;

				while (px_to_stretch > 0) {

					int seed = new Random().nextInt(2);

					// 在已有的平滑阶段中增加一个step
					if(seed == 0) {

						// 随机生成这个step所位移的像素
						int new_seed = new Random().nextInt(px_to_stretch) + 1;

						addOneStepToFlatPhase(new_seed);

						px_to_stretch -= new_seed;

					// 随机找到一个平滑阶段中step（时间间隔8ms），速度 + 0.125 px/ms Aka. 1px
					}
					else {
						Step step = getRandomFlatStep(false);

						// 更改统计信息 Part 1
						y_sum -= step.dy;

						step.addOnePixel();

						logger.info("\tAdd 1px: {}", step.toJSON());

						// 更改统计信息 Part 2
						x_sum += 1;
						y_sum += step.dy;

						x_sum_ub = x_sum + 10;
						x_sum_lb = x_sum > 20? x_sum - 10: x_sum;

						px_to_stretch --;
					}

				}

			}
			// B 压缩情况
			else if (px < 0 && px >= x_sum - x_sum_lb) {

				for(int i=0; i<px; i++) {

					Step step = getRandomFlatStep(true);

					// 更改统计信息 Part 1
					y_sum -= step.dy;

					step.subtractOnePixel();
					logger.info("\tSubtract 1px: {}", step.toJSON());

					// 更改统计信息 Part 2
					x_sum -= 1;
					y_sum += step.dy;

					x_sum_ub = x_sum + 10;
					x_sum_lb = x_sum > 20? x_sum - 10: x_sum;

				}

			}
			// C Do nothing.
			else if (px == 0) {
			}
			else {
				throw new MorphException();
			}

			// 不改变总x位移进行轨迹变换
			mutation(); // 不需要修复 dx_to_flat_steps 不需要修复 统计信息
		}

		/**
		 * 随机找到一个平滑Step
		 * @param hasVelocity 该阶段是否有速度
		 *                    没有速度的平滑阶段无法减少速度，不能用于轨迹压缩
		 * @return 随机找到的平滑阶段
		 */
		public Step getRandomFlatStep(boolean hasVelocity) {

			Step step = null;
			int search_count = 0;
			while (step == null && search_count < 5) {
				int rnd = new Random().nextInt(flat_steps.size());
				step = steps.get(rnd);
				if(step.dx == 0 && hasVelocity) step = null;
			}

			return step;
		}

		/**
		 * 在平滑阶段插入一个 Step
		 * 1. 先随机找到一个平滑Step(i)
		 * 2. 获取Step(i) 和 Step(i+1) 的速度，此时 Step(i+1) 也是一个平滑 Step（由于索引的设定）
		 * 3. 在Step(i) Step(i+1) 中间插入一个Step*，Step*的速度为：
		 *     1/8 px/ms 的整倍数，根据实际增加像素数决定
		 *
		 * 新增加的Step* 其速度应该与相邻Step 存在一定关系
		 */
		public void addOneStepToFlatPhase(int px) throws Exception {

			logger.info("Add one step into flat phase, {}px.", px);

			if(flat_steps.size() == 0) {
				throw new NoFlatPhaseStepException();
			}

			// 插入位置Step Step* 插到 Step 之后位置
			Step step;

			// 根据px 找到合适的插入位置
			if(px >= getFlatPhaseMaxPx()) {

				// 找到最大位移的 Step
				List<Step> max_v_steps = dx_to_flat_steps.lastEntry().getValue();
				int seed = new Random().nextInt(max_v_steps.size());
				step = steps.get(seed);

			}
			else {

				List<Step> steps_tmp = new ArrayList<>();

				// 具有相同px位移的 step
				Step step_0 = getStepByPx(px);
				if(step_0 != null) {
					steps_tmp.add(step_0);
					logger.info("\tSame px, step: {}, {}", steps.indexOf(step_0), step_0.toJSON());
				}

				// 逐渐增加px 搜索具有该px 的 step
				Step step_1 = getCloseByStepByPx(px, true);
				if(step_1 != null) {
					steps_tmp.add(step_1);
					logger.info("\tLarger px, step: {}, {}", steps.indexOf(step_1), step_1.toJSON());
				}

				// 逐渐减少px 搜索具有该px 的 step
				Step step_2 = getCloseByStepByPx(px, false);
				if(step_2 != null) {
					steps_tmp.add(step_2);
					logger.info("\tSmaller px, step: {}, {}", steps.indexOf(step_2), step_2.toJSON());
				}

				// 随机找一个
				int seed = new Random().nextInt(steps_tmp.size());
				step = steps_tmp.get(seed);
			}

			if (step == null) throw new NoSuitableOffsetStepException();

			logger.info("\tChosen step: {}, {}", steps.indexOf(step), step.toJSON());

			// 构建新的step
			int dt = 8;
			int dy = step.dy;

			Step new_step = new Step(px, dy, dt, true);
			logger.info("\tNew step: {}", new_step.toJSON());

			int i = flat_steps.indexOf(step);

			// 插入new_step，维护相关索引
			flat_steps.add(i + 1, new_step);

			int i_ = steps.indexOf(step);
			steps.add(i_ + 1, new_step);

			addStepIntoDxMap(step);

			// 更改统计信息
			x_sum += new_step.dx;
			y_sum += new_step.dy;
			t_sum += new_step.dt;

			x_sum_ub = x_sum + 10;
			x_sum_lb = x_sum > 20? x_sum - 10: x_sum;
		}

		/**
		 * 随机选取 1/3 的 non_flat_phase的step
		 * 进行 mutation
		 */
		public void mutation() {

			logger.info("Random mutation...");

			int[] mutation_indices = new Random()
					.ints(0, non_flat_steps.size())
					.distinct().limit((int)Math.ceil(non_flat_steps.size() * 0.33)).toArray();

			for(int i : mutation_indices) {

				// 更改统计信息
				t_sum -= non_flat_steps.get(i).dt;

				logger.info("\tBefore: {}", non_flat_steps.get(i).toJSON());

				non_flat_steps.get(i).mutation();

				logger.info("\tAfter: {}", non_flat_steps.get(i).toJSON());

				// 更改统计信息
				t_sum += non_flat_steps.get(i).dt;
			}
		}

		/**
		 * 平滑阶段的最大速度 1/8的倍数
		 * @return
		 */
		private int getFlatPhaseMaxPx() {

			return dx_to_flat_steps.lastKey();
		}

		/**
		 * 平滑阶段的最小速度 1/8的倍数
		 * @return
		 */
		private int getFlatPhaseMinPx() {

			return dx_to_flat_steps.firstKey();
		}

		/**
		 * 根据px参数，在现有的flat_steps中找到一个位移差不多的step
		 * @param px 初始位移
		 * @param searchUp 逐步增加px搜索
		 * @return
		 */
		public Step getCloseByStepByPx(int px, boolean searchUp) {

			int px_ = px;

			Step step = null;

			int offset = 1;

			while(px_ <= getFlatPhaseMaxPx() && px_ >=0 && step == null) {

				px_ = searchUp? px + (offset ++) : px - (offset ++);

				List<Step> v_steps = dx_to_flat_steps.get(px_);

				if(v_steps != null && v_steps.size() > 0) {

					int i = new Random().nextInt(v_steps.size());
					step = v_steps.get(i);
				}
			}

			return step;
		}

		/**
		 * 根据指定位移，随机找一个Step
		 * @param px
		 * @return
		 */
		public Step getStepByPx(int px) {

			Step step = null;

			List<Step> v_steps = dx_to_flat_steps.get(px);

			if(v_steps != null && v_steps.size() > 0) {

				int i = new Random().nextInt(v_steps.size());
				step = v_steps.get(i);
			}

			return step;
		}

		/**
		 * 通过steps重新构建actions
		 * @return
		 */
		public List<Action> buildActions() throws ModelNoInitException {

			logger.info("Build actions...");

			if(!init) throw new ModelNoInitException();

			List<Action> actions = new ArrayList<>();

			// 构建第一个Action
			Action a0 = new Action(Action.Type.Press, x_init, y_init, t0);
			actions.add(a0);

			// 遍历Step 生成Action
			for(Step step : steps) {
				Action a = new Action(Action.Type.Drag,
						a0.x + step.dx,
						a0.y + step.dy,
						a0.time + step.dt);
				actions.add(a);
			}


			actions.get(actions.size()-1).type = Action.Type.Release;

			return actions;
		}

		public class NoFlatPhaseStepException extends Exception {}

		public class MorphException extends Exception {}

		public class NoSuitableOffsetStepException extends Exception {}

		public class ModelNoInitException extends Exception {}

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
	 * 生成 Action 列表的 Mathematica List
	 * @param actions
	 * @return
	 */
	public static String toMathematicaListStr(List<Action> actions) {
		String output = "{";
		for(Action action : actions) {
			output += "{" + action.time + ", " + action.x + ", " + action.y + "}, ";
		}
		output = output.substring(0, output.length() - 2);
		output += "}";
		return output;
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		List<Action> actions = loadData("mouse_movements/1521357776022_6a62fed3-55ad-44d6-8ce8-205c6514dc9b.txt");

		Model model = new Model(actions);

		String output = toMathematicaListStr(model.buildActions());

		FileUtil.writeBytesToFile(output.getBytes(), "original_actions.txt");

		model.morph(10);

		output = toMathematicaListStr(model.buildActions());

		FileUtil.writeBytesToFile(output.getBytes(), "new_actions.txt");
	}
}
