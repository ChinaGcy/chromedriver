package com.sdyk.ai.crawler;

import one.rewind.db.PooledDataSource;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JDBCStreamTest {

	public void getData(String sql, int start, int maxCount) throws Exception {

		Connection conn = PooledDataSource.getDataSource("xueqiu_1").getConnection();

		PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);

		ps.setMaxRows(start+maxCount-1);

		ResultSet rs = ps.executeQuery();

		rs.last();
		System.err.println(rs.getRow());

		rs.first();

		rs.relative(start-2);

		while(rs.next()) {
			System.err.println(rs.getString(14));
		}
	}

	@Test
	public void SQLtest() throws Exception {

		String sql = "SELECT * FROM posts";
		int start = 110000;
		int maxCount = 10;

		for (int i=0; i< 10; i++ ) {
			if (i == 0) {
				getData(sql, start, maxCount);
			}else {
				start = start+maxCount;
				getData(sql, start, maxCount);
			}
		}

	}
}
