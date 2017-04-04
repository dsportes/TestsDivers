package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.ds.PGPoolingDataSource;

public class TestDB {
	/*
	create table gstate (
	groupid varchar(255),
	ndn int
	);
	ALTER TABLE gstate OWNER TO "docdb";
	insert into gstate (groupid, ndn) values ('g1', 3);
	*/
	PGPoolingDataSource dataSource = new PGPoolingDataSource();

	private Connection conn;

	protected Connection conn() throws SQLException { 
		if (conn == null) {
			dataSource.setUrl("jdbc:postgresql://localhost:5432/testdb");
			dataSource.setUser("docdb");
			dataSource.setPassword("docdb3542");
			dataSource.setMaxConnections(100);
			conn = dataSource.getConnection();
			}
		return conn; 
	}

	public void closeConnection() throws SQLException {
		if (conn != null){
			conn.close();
			conn = null;
		} 	
	}

	protected void err(PreparedStatement preparedStatement, ResultSet rs) throws SQLException {
		if (preparedStatement != null)
			try { preparedStatement.close(); } catch (SQLException e1) {}
		if (rs != null)
			try { rs.close(); } catch (SQLException e1) {}
		closeConnection();
	}

	private static final String UPDDN = "update gstate set ndn = ndn + 1 where groupid = ? returning ndn;";
	
	public int newDn(String groupid) throws SQLException  {
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		int n = -2;
		try {
			preparedStatement = conn().prepareStatement(UPDDN);
			preparedStatement.setString(1, groupid);
			rs = preparedStatement.executeQuery();
			if (rs.next()){
				n = rs.getInt("ndn");
			}
			preparedStatement.close();
			return n;
		} catch(Exception e){
			err(preparedStatement, rs);
			return -1;
		}
	}
	
	public static void main(String[] args) {
		try {
			TestDB t = new TestDB();
			for(int i = 0; i < 3; i++)
				System.out.println(t.newDn("g1"));
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
