package com.sdyk.ai.crawler.util;

import one.rewind.json.JSON;
import one.rewind.json.JSONable;

public class Range implements JSONable<Range> {

	double gte;
	double lte;
	String relation;

	public Range() {}

	public Range(double gte, double lte, boolean within) {
		this.gte = gte;
		this.lte = lte;
		if(within) {
			this.relation = "within";
		}
	}

	@Override
	public String toJSON() {
		return JSON.toJson(this);
	}
}
