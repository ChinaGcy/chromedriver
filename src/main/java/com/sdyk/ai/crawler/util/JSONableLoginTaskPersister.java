package com.sdyk.ai.crawler.util;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.json.JSON;

import java.util.List;

public class JSONableLoginTaskPersister extends StringType {

	private static final JSONableLoginTaskPersister INSTANCE = new JSONableLoginTaskPersister();

	private JSONableLoginTaskPersister() {
		super(SqlType.STRING, new Class<?>[] { LoginTask.class });
	}

	public static JSONableLoginTaskPersister getSingleton() {
		return INSTANCE;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) {

		LoginTask loginTask = (LoginTask) javaObject;

		return loginTask != null ? JSON.toJson(loginTask) : null;
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {

		LoginTask loginTask = JSON.fromJson((String)sqlArg, LoginTask.class);
		return sqlArg != null ? loginTask : null;
	}
}
