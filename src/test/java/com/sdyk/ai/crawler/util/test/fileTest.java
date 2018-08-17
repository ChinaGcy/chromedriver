package com.sdyk.ai.crawler.util.test;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static one.rewind.util.FileUtil.readFileByLines;

public class fileTest {

	@Test
	public void test(){

		String src = readFileByLines("fileForTest/hospital-doctors.txt");

		Set<String> set = new HashSet<>();

		Pattern pattern = Pattern.compile("INSERT INTO `(?<T>.+?)` ");
		Matcher matcher = pattern.matcher(src);
		if( matcher.find() ){
			set.add(matcher.group("T"));
		}

		for (String s : set) {
			System.out.println(s);
		}

	}


}
