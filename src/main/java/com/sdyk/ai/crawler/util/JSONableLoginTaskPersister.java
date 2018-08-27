package com.sdyk.ai.crawler.util;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.json.JSON;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

		return loginTask != null ? LoginTask.toJSON(loginTask) : null;
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {

		LoginTask loginTask = null;
		try {
			loginTask = LoginTask.buildFromJson((String) sqlArg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sqlArg != null ? loginTask : null;
	}
}
