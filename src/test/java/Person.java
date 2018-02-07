
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;

import java.sql.SQLException;

@DBName("crawler")
@DatabaseTable(tableName = "student")
public class Person {

	@DatabaseField(generatedId = true)
	public int id;
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String name;
	@DatabaseField(dataType = DataType.INTEGER, width = 11)
	public int age;
	@DatabaseField(dataType = DataType.STRING, width = 128)
	public String sex;


	public boolean insert() throws Exception{

		Dao<Person, String> dao = OrmLiteDaoManager.getDao(Person.class);
		try {
			if (dao.create(this) == 1) {
				return true;
			}

		}catch (SQLException e) {
			dao.update(this);
		}

		return false;
	}

}
