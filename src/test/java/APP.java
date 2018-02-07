import db.Refacter;

public class APP {
	public static void main(String[] args) throws Exception {


		Refacter.createTable(Person.class);

		Person p = new Person();
		p.name = "1111";
		p.age = 11;
		p.sex = "nan";

		p.insert();
	}
}
