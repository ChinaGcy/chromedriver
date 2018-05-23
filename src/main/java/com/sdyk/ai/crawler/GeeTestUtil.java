package com.sdyk.ai.crawler;

import java.awt.*;

public class GeeTestUtil {

	public static void getMotion() {

	}

	public static void mouseGlide(Robot r, int x1, int y1, int x2, int y2, int t, int n) {
		try {
			double dx = (x2 - x1) / ((double) n);
			double dy = (y2 - y1) / ((double) n);
			double dt = t / ((double) n);
			for (int step = 1; step <= n; step++) {
				Thread.sleep((int) dt);
				r.mouseMove((int) (x1 + dx * step), (int) (y1 + dy * step));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


}
