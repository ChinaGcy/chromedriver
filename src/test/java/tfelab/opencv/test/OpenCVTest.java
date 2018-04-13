package tfelab.opencv.test;

import org.junit.Test;
import org.tfelab.opencv.OpenCVUtil;

public class OpenCVTest {

	@Test
	public void testGetOffest() {

		String ts = "1523537181650-44e204ce76d84476b46fa3be7bf640c4";

		String img_1_path = "tmp/geetest/geetest-1-" + ts + ".png";
		String img_2_path = "tmp/geetest/geetest-2-" + ts + ".png";

		int offset = OpenCVUtil.getOffset(img_1_path, img_2_path);

		System.err.println(offset);

	}
}
