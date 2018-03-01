package com.sdyk.ai.crawler.zbj;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class OpenCV {

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {

		Mat sMat1 = Imgcodecs.imread("geetest1.png");
		Mat sMat2 = Imgcodecs.imread("geetest2.png");

		System.err.println(sMat2.height());
		System.err.println(sMat2.width());
		for (int x = 0; x < sMat2.height(); x++) {
			for (int y = 0; y < sMat2.width(); y++){

				double[] data = sMat2.get(x, y);
				if (colorError(sMat1.get(x,y), sMat2.get(x,y), data.length)) {

					for (int i = 0 ; i < data.length; i++) {
						data[i] = 255;
					}
					sMat2.put(x,y,data);
				}
				else {
					for (int i = 0 ; i < data.length; i++) {
						data[i] = 0;
					}
					sMat2.put(x,y,data);
				}
			}
			double[] data1 = new double[3];

			if (x > 120) {
				for (int i = 0 ; i < data1.length; i++) {
					data1[i] = 255;
				}
				for (int y = 0; y < sMat1.width(); y++ )
				sMat2.put(x,y,data1);
			}
		}

		Imgcodecs.imwrite("p33.png", sMat2);
	}

	/**
	 *
	 * @param a
	 * @param b
	 * @param index
	 * @return
	 */
	public static boolean colorError(double[] a, double[] b, int index) {

		for (int r = 0; r < index; r++) {
			if (a[r] - b[r] > 15) {
				return false;
			}
			if (b[r] - a[r] > 15) {

				return false;
			}
		}
		return true;
	}

	/**
	 * mat.get(高，宽)
	 * @param mat
	 * @return
	 */
	// TODO 未完成
	public static int moveNum(Mat mat) {

		double[] data = new double[3];
		double[] data1 = new double[3];
		for (int x = 0; x < mat.height(); x++) {
			for (int y = 0; y < mat.width(); y++) {

				data = mat.get(x,y);
				data1 = mat.get(x, y+1);
			}
		}
		return 0;
	}
}
