package tfelab.util.test;

import org.junit.Test;
import org.tfelab.txt.DateFormatUtil;
import org.tfelab.txt.StringUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TestFormatUtil {
	
	@Test
	public void binaryId() {

		UUID uid = UUID.randomUUID();

		byte[] id = StringUtil.getIdAsByte(uid);
		String id1 = StringUtil.byteArrayToHex(id);
		byte[] id2 = StringUtil.hexStringToByteArray(id1);
		String id3 = StringUtil.byteArrayToHex(id2);

		assertEquals(id1, id3);
	}


	@Test
	public void test() throws ParseException {
		System.out.println(new Date("Jan 3, 2014 7:30:16"));
		System.out.println(DateFormatUtil.parseTime("2014.10.01"));
	}

}
