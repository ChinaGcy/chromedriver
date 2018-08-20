//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package one.rewind.io.requester.task;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.task.Task.Priority;
import one.rewind.json.JSON;
import one.rewind.json.JSONable;
import one.rewind.txt.StringUtil;

@DBName("requester")
@DatabaseTable(
		tableName = "tasks"
)
public class TaskHolder implements Comparable<TaskHolder>, JSONable<TaskHolder> {
	@DatabaseField(
			dataType = DataType.STRING,
			width = 32,
			canBeNull = false,
			id = true
	)
	public String id;
	@DatabaseField(
			dataType = DataType.STRING,
			width = 32,
			canBeNull = false,
			index = true
	)
	public String generate_task_id;
	@DatabaseField(
			dataType = DataType.STRING,
			width = 32,
			canBeNull = false,
			index = true
	)
	public String scheduled_task_id;
	@DatabaseField(
			dataType = DataType.STRING,
			width = 128,
			canBeNull = false
	)
	public String class_name;
	@DatabaseField(
			dataType = DataType.STRING,
			width = 256,
			canBeNull = false
	)
	public String domain;
	@DatabaseField(
			dataType = DataType.BOOLEAN,
			canBeNull = false
	)
	public boolean need_login;
	@DatabaseField(
			dataType = DataType.STRING,
			width = 128
	)
	public String username;
	public Map<String, Object> vars;
	@DatabaseField(
			dataType = DataType.STRING,
			width = 1024,
			canBeNull = false
	)
	public String url;
	@DatabaseField(
			dataType = DataType.INTEGER,
			width = 5,
			canBeNull = false
	)
	public int step;
	@DatabaseField(
			dataType = DataType.ENUM_STRING,
			width = 32,
			canBeNull = false
	)
	public Priority priority;
	@DatabaseField(
			dataType = DataType.BOOLEAN,
			canBeNull = false
	)
	public boolean done;
	@DatabaseField(
			dataType = DataType.BOOLEAN,
			canBeNull = false
	)
	public boolean all_done;
	public List<String> trace;
	@DatabaseField(
			dataType = DataType.DATE,
			canBeNull = false
	)
	public Date create_time;
	@DatabaseField(
			dataType = DataType.DATE
	)
	public Date exec_time;
	@DatabaseField(
			dataType = DataType.DATE
	)
	public Date done_time;
	@DatabaseField(
			dataType = DataType.DATE
	)
	public Date all_done_time;

	public TaskHolder() {
		this.need_login = false;
		this.step = 0;
		this.priority = Priority.MEDIUM;
		this.done = false;
		this.all_done = false;
		this.create_time = new Date();
	}

	public TaskHolder(String class_name, String domain, Map<String, Object> vars, String url, boolean login_task, String username, int step, Priority priority) {
		this(class_name, domain, vars, url, login_task, username, step, priority, (String)null, (String)StringUtil.MD5(class_name + "-" + JSON.toJson(vars)), (List)null);
	}

	public TaskHolder(String class_name, String domain, Map<String, Object> vars, String url, boolean login_task, String username, int step, Priority priority, String generate_task_id, String scheduled_task_id, List<String> trace) {
		this.need_login = false;
		this.step = 0;
		this.priority = Priority.MEDIUM;
		this.done = false;
		this.all_done = false;
		this.create_time = new Date();
		this.class_name = class_name;
		this.domain = domain;
		this.vars = vars;
		this.url = url;
		this.need_login = login_task;
		this.username = username;
		if (username != null && username.length() > 0) {
			this.need_login = true;
		}

		this.step = step;
		this.priority = priority;
		this.id = StringUtil.MD5(class_name + "-" + JSON.toJson(vars) + "-" + System.currentTimeMillis());
		this.generate_task_id = generate_task_id;
		this.scheduled_task_id = scheduled_task_id;
		this.trace = trace;
	}

	public boolean insert() throws Exception {
		Dao dao = DaoManager.getDao(this.getClass());
		return dao.create(this) == 1;
	}

	public boolean update() throws Exception {
		Dao dao = DaoManager.getDao(this.getClass());
		return dao.update(this) == 1;
	}

	public ChromeTask build() throws Exception {
		return ChromeTaskFactory.getInstance().buildTask(this);
	}

	public int compareTo(TaskHolder another) {
		Priority me = this.priority;
		Priority it = another.priority;
		return me.ordinal() == it.ordinal() ? this.create_time.compareTo(another.create_time) : it.ordinal() - me.ordinal();
	}

	public String toJSON() {
		return JSON.toJson(this);
	}
}
