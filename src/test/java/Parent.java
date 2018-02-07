import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;

import java.lang.reflect.Proxy;

public abstract class Parent {



	public void setName(String name) {
		this.name = name;
	}

	public void setAge(int age) {
		this.age = age;
	}

	@DatabaseField(generatedId = true,dataType = DataType.INTEGER )
	private int id;

	@DatabaseField(dataType = DataType.STRING)
	private String name;

	@DatabaseField(dataType = DataType.INTEGER)
	private int age;

	public static Dao dao;

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public void go() {
		System.out.println("00000");

	}

	public static Parent queryForId(String id) throws Exception {
		return (Parent) dao.queryForId(id);
	}

	public static int create(Object object) throws Exception {
		return  dao.create(object);
	}


}
