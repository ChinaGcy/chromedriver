package com.sdyk.ai.crawler;

public class Son extends Father{

	@Override
	public void aa() {
		System.err.println("son ---aaa");
	}

	public static void main(String[] args) {

		Father father = new Son();
		father.bb();
	}
}
