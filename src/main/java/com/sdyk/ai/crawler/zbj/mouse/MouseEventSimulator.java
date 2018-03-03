package com.sdyk.ai.crawler.zbj.mouse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.json.JSON;
import org.tfelab.util.FileUtil;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MouseEventSimulator {

	private static final Logger logger = LogManager.getLogger(MouseEventTracker.class.getName());

	MouseEventTracker tracker;
	Robot bot;

	public MouseEventSimulator(MouseEventTracker tracker) throws AWTException {

		this.tracker = tracker;
		bot = new Robot();
	}

	public void simulate(int offset) {

		offset = offset + ThreadLocalRandom.current().nextInt(-4, 4 + 1);
		long ts = 0;

		int[] p1 = new int[2];
		int[] p2 = new int[2];

		for(MouseEventTracker.Action action : this.tracker.actions) {
			if(action.type.equals(MouseEventTracker.Action.Type.Press)) {
				p1[0] = action.x;
				p1[1] = action.y;
			} else if(action.type.equals(MouseEventTracker.Action.Type.Release)) {
				p2[0] = action.x;
				p2[1] = action.y;
			}
		}

		double width = p2[0] - p1[0];
		double scale = (double) offset / width;
		logger.info("Original width:{}, scale:{}.", width, scale);

		for(MouseEventTracker.Action action : this.tracker.actions) {

			if(action.type.equals(MouseEventTracker.Action.Type.Press)) {
				bot.mousePress(InputEvent.BUTTON1_MASK);
			} else if(action.type.equals(MouseEventTracker.Action.Type.Release)) {
				bot.mouseRelease(InputEvent.BUTTON1_MASK);
			} else if(action.type.equals(MouseEventTracker.Action.Type.Move)) {
				bot.mouseMove(action.x, action.y);
			} else {
				bot.mouseMove(p1[0] + (int) Math.round ((action.x-p1[0]) * scale),
						p1[1] + (int) Math.round ((action.y-p1[1]) * scale));
			}
			bot.delay((int) (action.time - ts));
			ts = action.time;
		}
	}

	public static void main(String[] args) throws AWTException {

		MouseEventTracker m = JSON.fromJson(
				FileUtil.readFileByLines("mouse_movements/1520069499746.txt"),
				MouseEventTracker.class
		);

		new MouseEventSimulator(m).simulate(100);
	}

}
