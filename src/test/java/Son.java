import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.model.Project;

import db.Refacter;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;

import java.lang.reflect.Proxy;

@DBName("crawler")
@DatabaseTable(tableName = "son")
public class Son extends Parent {

	public Son() {}

	public Son(String name, int age) {
		this.setName(name);
		this.setAge(age);
	}



	/*@DatabaseField(dataType = DataType.INTEGER)
	public int id;*/

	/*@DatabaseField(dataType = DataType.STRING)
	public String look;*/



	/*public String getLook() {
		return look;
	}

	public void setLook(String look) {
		this.look = look;
	}*/

	/*String clazzname = new Throwable().getStackTrace()[1].getClassName();
	Class<?> clazz = Class.forName(clazzname);
	Dao dao = OrmLiteDaoManager.getDao(clazz);
	return (Proxy) dao.queryForId(id);*/

/*	public boolean insert() throws Exception{

		Dao<Son, String> dao = OrmLiteDaoManager.getDao(Son.class);

		if (dao.create(this) == 1) {
			return true;
		}

		return false;
	}*/

	/*public void queryForId1(String id ) throws Exception {
		Dao<Son, String> dao = OrmLiteDaoManager.getDao(Son.class);
		Son son = dao.queryForId(id);

		if (son != null) {
			System.out.println(son.getName()+""+son.getAge());
		}else {
			System.out.println("空啊！");
		}
	}*/





		/*
		if (son != null) {
			System.out.println(son.getName()+"-"+son.getAge());
		}else {
			System.out.println("空啊！");
		}*/


	/*public void updateForId(Son son,String id) throws Exception {
		Dao<Son, String> dao = OrmLiteDaoManager.getDao(Son.class);
		int num = dao.update(son);
		System.out.println(num);
		//dao.deleteById(id);

	}*/


}
