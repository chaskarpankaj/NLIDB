package model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;


public class SchemaGraph {
	
	/**
	 * table name, column name, column type
	 */
	private Map<String, Map<String, String>> tables;
	//table name, column name, column values
	private Map<String, Map<String, Set<String>>> tableRows;
	
	/**
	 * Construct a schemaGraph from database meta data.
	 * @see <a href="http://docs.oracle.com/javase/6/docs/api/java/sql/
	 * DatabaseMetaData.html#getTables%28java.lang.String,%20java.lang.
	 * String,%20java.lang.String,%20java.lang.String%5b%5d%29">document of getTables</a>
	 * @param meta
	 * @throws SQLException
	 */
	public SchemaGraph(Connection c) throws SQLException {
		System.out.println("Retrieving schema graph...");
		DatabaseMetaData meta = c.getMetaData();
		Statement stmt = c.createStatement();
		tables = new HashMap<>();
		tableRows = new HashMap<>();
		
		String[] types = {"TABLE"};
		ResultSet rsTable = meta.getTables(null, null, "%", types);
		
		while (rsTable.next()) {
			String tableName = rsTable.getString("TABLE_NAME");
			tables.put(tableName, new HashMap<>());
			tableRows.put(tableName, new HashMap<>());
			
			Map<String, String> table = tables.get(tableName);
			Map<String, Set<String>> tableRow = tableRows.get(tableName);
			
			ResultSet rsColumn = meta.getColumns(null, null, tableName, null);
			while (rsColumn.next()){
				/*retrieve column info for each table, insert into tables*/
				String columnName = rsColumn.getString("COLUMN_NAME");
				String columnType = rsColumn.getString("TYPE_NAME");
				table.put(columnName, columnType); 
				/*draw random sample of size 10000 from each table, insert into tableRows*/
				String query = "SELECT " + columnName + " FROM " + tableName + " ORDER BY RANDOM() LIMIT 10000;";
				ResultSet rows = stmt.executeQuery(query);
				tableRow.put(columnName, new HashSet<String>());
				Set<String> columnValues = tableRow.get(columnName);
				while (rows.next()){
					String columnValue = rows.getString(1);
					//testing if the last column read has a SQL NULL
					if (rows.wasNull())
						System.out.println(tableName+", "+columnName+", Value:"+columnValue);
					else
						columnValues.add(columnValue);
				}
			}			
		}
		if (stmt != null) { stmt.close(); }
		System.out.println("Schema graph retrieved.");
	}

	public Set<String> getTableNames() {
		return tables.keySet();
	}
	
	public Set<String> getColumns(String tableName) {
		return tables.get(tableName).keySet();
	}
	
	public Set<String> getValues(String tableName, String columnName){
		return tableRows.get(tableName).get(columnName);
	}

	@Override
	public String toString() {
		String s = "";
		for (String tableName : tables.keySet()) {
			s += "table: "+tableName+"\n";
			s += "{";
			Map<String, String> columns = tables.get(tableName);
			for (String colName : columns.keySet()) {
				s += colName+": "+columns.get(colName)+"\t";
			}
			s += "}\n\n";
		}
		return s;
	}
}
