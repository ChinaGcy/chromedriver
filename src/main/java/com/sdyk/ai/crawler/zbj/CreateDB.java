package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.model.*;
import org.tfelab.db.Refacter;

import java.util.HashSet;
import java.util.Set;

public class CreateDB {
	public static void main(String[] args) throws Exception {

		Set<Class> table = new HashSet<>();

		table.add(Binary.class);
		table.add(Case.class);
		table.add(Project.class);
		table.add(ServiceSupplier.class);
		table.add(SupplierRating.class);
		table.add(Tenderer.class);
		table.add(TendererRating.class);
		table.add(Work.class);

		for (Class clzss : table) {

			Refacter.dropTable(clzss);
			Refacter.createTable(clzss);
		}
	}
}
