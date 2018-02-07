import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.DatabaseTable;
import org.tfelab.db.DBName;
import org.tfelab.db.OrmLiteDaoManager;

@DBName("crawler")
@DatabaseTable(tableName = "BigSon")
public class BigSon extends Parent {

	public BigSon() {}

	public BigSon(String name, int age) {
		this.setName(name);
		this.setAge(age);
	}


}
