package com.sdyk.ai.crawler.zbj.mouse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;
import org.tfelab.util.FileUtil;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 */
public class MouseEventTracker implements JSONable {

	private static final String serPath = "mouse_movements/";

	private static final Logger logger = LogManager.getLogger(MouseEventTracker.class.getName());

	transient long lastTs = System.currentTimeMillis();

	transient MouseListener listener;

	public ArrayList<Action> actions = new ArrayList();

	@Override
	public String toJSON() {
		return JSON.toPrettyJson(this);
	}

	/**
	 *
	 */
	public static class Action implements JSONable, Serializable {

		public static enum Type {
			Press, Release, Move, Drag
		}

		public Type type;
		public int x;
		public int y;
		public long time;

		public Action(Type type, int x, int y, long time) {
			this.type = type;
			this.x = x;
			this.y = y;
			this.time = time;
		}

		@Override
		public String toJSON() {
			return JSON.toJson(this);
		}
	}

	public class MouseListener implements NativeMouseInputListener {
		public void nativeMouseClicked(NativeMouseEvent e) {
			//logger.info("Mouse Clicked: " + e.getClickCount());
		}

		public void nativeMousePressed(NativeMouseEvent e) {
			//logger.info("Mouse Pressed: " + e.getButton());
			MouseEventTracker.this.actions.add(
					new Action(Action.Type.Press, e.getX(), e.getY(),
							System.currentTimeMillis() - MouseEventTracker.this.lastTs));
		}

		public void nativeMouseReleased(NativeMouseEvent e) {
			//logger.info("Mouse Released: " + e.getButton());
			MouseEventTracker.this.actions.add(
					new Action(Action.Type.Release, e.getX(), e.getY(),
							System.currentTimeMillis() - MouseEventTracker.this.lastTs));
			MouseEventTracker.this.stop();
		}

		public void nativeMouseMoved(NativeMouseEvent e) {
			//logger.info("Mouse Moved: " + e.getX() + ", " + e.getY());
			MouseEventTracker.this.actions.add(
					new Action(Action.Type.Move, e.getX(), e.getY(),
							System.currentTimeMillis() - MouseEventTracker.this.lastTs));
		}

		public void nativeMouseDragged(NativeMouseEvent e) {
			//logger.info("Mouse Dragged: " + e.getX() + ", " + e.getY());
			MouseEventTracker.this.actions.add(
					new Action(Action.Type.Drag, e.getX(), e.getY(),
							System.currentTimeMillis() - MouseEventTracker.this.lastTs));
		}
	}

	/**
	 *
	 * @throws AWTException
	 */
	public MouseEventTracker() {}

	public void start() {

		try {
			java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
			logger.setLevel(Level.OFF);
			logger.setUseParentHandlers(false);
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException e) {
			logger.error("There was a problem registering the native hook.", e);
			return;
		}

		listener = new MouseListener();
		// Add the appropriate listeners.
		GlobalScreen.addNativeMouseListener(listener);
		GlobalScreen.addNativeMouseMotionListener(listener);

	}

	/**
	 *
	 */
	public void stop() {

		try {
			GlobalScreen.removeNativeMouseListener(listener);
			GlobalScreen.removeNativeMouseMotionListener(listener);
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	public void serializeMovements() {

		FileOutputStream fileOut = null;
		try {

			String path = serPath + System.currentTimeMillis() + ".txt";

			FileUtil.writeBytesToFile(this.toJSON().getBytes(), path);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {

		try {

			MouseEventTracker tracker = new MouseEventTracker();
			Thread.sleep(10000);
			tracker.serializeMovements();


			/*MouseMoveOnScreen m = JSON.fromJson(
					FileUtil.readFileByLines("mouse_movements/1520065134001.txt"),
					MouseMoveOnScreen.class
			);*/

			/*System.err.println(m.toJSON());*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}