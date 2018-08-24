package com.sdyk.ai.crawler.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.task.LoginTask;
import com.sdyk.ai.crawler.util.JSONableListPersister;
import com.sdyk.ai.crawler.util.JSONableLoginTaskPersister;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.json.JSON;

import java.util.Date;
import java.util.List;

@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "login_tasks")
public class LoginTaskWrapper {

	// id
	@DatabaseField(generatedId = true)
	public int id;

	// domain
	@DatabaseField(dataType = DataType.STRING, width = 1024)
	public String domain;

	// login_task_json
	@DatabaseField(persisterClass = JSONableLoginTaskPersister.class, width = 10240)
	public LoginTask login_task;

	// 使用次数记录
	@DatabaseField(dataType = DataType.INTEGER)
	public int use_count = 0;

	public enum Status {
		Invalid,
		Valid
	}

	// status
	@DatabaseField(dataType = DataType.ENUM_INTEGER, width = 2)
	public Status status = Status.Valid;

	// enable
	@DatabaseField(dataType = DataType.BOOLEAN, canBeNull = false)
	public boolean enable = true;

	// 插入时间
	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	// 更新时间
	@DatabaseField(dataType = DataType.DATE)
	public Date update_time = new Date();

	/**
	 *
	 */
	public static class JSONableChromeActionPersister extends StringType {

		private static final JSONableChromeActionPersister INSTANCE = new JSONableChromeActionPersister();

		private JSONableChromeActionPersister() {
			super(SqlType.STRING, new Class<?>[] { List.class });
		}

		public static JSONableChromeActionPersister getSingleton() {
			return INSTANCE;
		}

		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) {

			List list = (List) javaObject;

			return list != null ? JSON.toJson(list) : null;
		}

		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {


			LoginAction a1 = JSON.fromJson((String)sqlArg, LoginAction.class);

			if(a1.className != null && a1.className.equals(LoginWithGeetestAction.class.getName())) {
				a1 = JSON.fromJson((String)sqlArg, LoginWithGeetestAction.class);
			}

			List list = JSON.fromJson((String)sqlArg, List.class);
			return sqlArg != null ? a1 : null;
		}
	}

	/**
	 *
	 * @param domain
	 * @return
	 * @throws Exception
	 */
	public static LoginTask getLoginTaskByDomain(String domain) throws Exception {

		Dao<LoginTaskWrapper, String> dao = DaoManager.getDao(LoginTaskWrapper.class);
		LoginTaskWrapper ltw = dao.queryBuilder().where().eq("domain", domain).queryForFirst();
		if(ltw != null) {
			return ltw.login_task;
		}
		return null;
	}

	/**
	 *
	 * @param domain
	 * @param loginTask
	 */
	public LoginTaskWrapper(String domain, LoginTask loginTask) {
		this.domain = domain;
		this.login_task = loginTask;
	}

	/**
	 * 生成表时需要无餐构造器
	 */
	public LoginTaskWrapper() { }

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception {

		Dao dao = DaoManager.getDao(this.getClass());

		if (dao.create(this) == 1) {
			return true;
		}

		return false;
	}

	/**
	 *
	 * @throws Exception
	 */
	public void succeed() throws Exception {
		this.use_count = this.use_count + 1;
		this.status = Status.Valid;
		this.update();
	}

	/**
	 *
	 * @throws Exception
	 */
	public void failed() throws Exception {
		this.use_count = this.use_count + 1;
		this.status = Status.Invalid;
		this.update();
	}

	/**
	 * 更新数据库
	 */
	public boolean update() throws Exception {

		this.update_time = new Date();

		Dao dao = DaoManager.getDao(this.getClass());

		if (dao.update(this) == 1) {
			return true;
		}

		return false;
	}

}
