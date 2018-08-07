package com.sdyk.ai.crawler.opencv.test;

import one.rewind.opencv.OpenCVUtil;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class NumberExtractTest {

	@Test
	public void test() {

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Mat mat_1 = Imgcodecs.imread("tmp/geetest/geetest-1-1525699558660-48bb0a2f5eb74e44a671986d6f964c47.png");

	}

}
