import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBAdmin {

	private static Logger log = LoggerFactory.getLogger(DBAdmin.class);

	String database = "";
	String jdbc_url = "";
	String dbName = "";
	String user = "";
	String password = "";
	String driver = "";
	String dbType;

	String _iniFile = "DBAdmin.ini";

	final String DATABASE = "database";
	final String URL = "url";
	final String USER = "user";
	final String PASSWORD = "password";
	final String DRIVER = "driver";
	final String DBNAME = "dbname";

	Properties prop = new Properties();

	public void readIniFile() {
		try {
			log.info("Reading INI file: " + _iniFile);
			File file = new File(_iniFile);
			if (!file.exists()) {
				throw new Exception("INI File not found");
			} else if (file.isDirectory()) {
				throw new Exception("INI File not found");
			} else {// read the ini file
				BufferedReader reader = new BufferedReader(new FileReader(file));
				prop.load(reader);
				jdbc_url = prop.getProperty(URL);
				user = prop.getProperty(USER);
				password = prop.getProperty(PASSWORD);
				database = prop.getProperty(DATABASE);
				dbName = prop.getProperty(DBNAME);
				driver = prop.getProperty(DRIVER);
			}
		} catch (Exception e) {
			log.error("Ini file read error: " + _iniFile, e);
		}
	}

	public void saveIniFile() {
		FileOutputStream out = null;
		try {
			log.info("Saving INI file: " + _iniFile);
			File file = new File(_iniFile);
			if (file.isDirectory()) {
				throw new Exception("INI File not found");
			} else {// save
				out = new FileOutputStream(_iniFile);
				prop.setProperty(URL, jdbc_url);
				prop.setProperty(USER, user);
				prop.setProperty(PASSWORD, password);
				prop.setProperty(DRIVER, driver);
				prop.setProperty(DATABASE, database);
				prop.setProperty(DBNAME, dbName);
				DateFormat dateFormat = DateFormat.getDateTimeInstance(
						DateFormat.FULL, DateFormat.FULL);
				prop.store(out, dateFormat.format(new java.util.Date()));
			}
		} catch (Exception e) {
			log.error("Ini file write error", e);
		}
	}

	public String getRecord(String sql) {
		return getRecord(sql, true);
	}

	public String getRecord(String sql, boolean newLine) {
		return getRecord(sql, newLine, "");
	}

	public void executeSQL(String sql) {
		try {
			if (database.isEmpty()) {
				throw new Exception("Database configuration error");
			}
			log.info("URL: " + jdbc_url + dbName);
			log.info("User: " + user);
			log.info("Password: " + password);
			Class.forName(driver);
			Connection con = DriverManager.getConnection(jdbc_url + dbName,
					user, password);
			con.setAutoCommit(true);
			Statement statement = con.createStatement();

			log.info("SQL: " + sql);
			statement.executeUpdate(sql);
			statement.close();
			con.close();
		} catch (Exception e) {
			log.error("DB access error", e);
		}
	}

	public String getRecord(String sql, boolean newLine, String prefix) {
		ResultSet resultSet;
		String str = "";
		try {
			if (database.isEmpty()) {
				throw new Exception("Database configuration error");
			}
			log.info("URL: " + jdbc_url + dbName);
			Class.forName(driver);
			Connection con = DriverManager.getConnection(jdbc_url + dbName,
					user, password);
			con.setAutoCommit(true);
			Statement statement = con.createStatement();

			log.info("SQL: " + sql);
			resultSet = statement.executeQuery(sql);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			String field = "";
			while (resultSet.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					field = rsmd.getColumnName(i);
					if (newLine) {
						str += field + ": " + resultSet.getString(field)
								+ "\r\n";
					} else {
						str += prefix + resultSet.getString(field);
					}
				}
				str += "\r\n";
			}

			statement.close();
			con.close();

		} catch (Exception e) {
			log.error("DB access error", e);
		}
		return str;
	}

	public RecordSet getList(String sql) {
		return getList(sql, null);
	}
	
	public ArrayList<String> getPK(String table){
		ArrayList<String> keyList=new ArrayList<>();
		ResultSet resultSet = null;
		
		try {
			if (database.isEmpty()) {
				throw new Exception("Database configuration error");
			}

			Class.forName(driver);
			Connection con = null;
			con = DriverManager.getConnection(jdbc_url + dbName, user,
					password);			
			con.setAutoCommit(true);
			
			DatabaseMetaData meta=con.getMetaData();
			resultSet=meta.getPrimaryKeys(null, null, table);
			resultSet.beforeFirst();
			while(resultSet.next()){				
				String pk=resultSet.getString("COLUMN_NAME");
				keyList.add(pk);
			}
		}catch(Exception e){
			log.info("Error getting primary keys",e);
		}
		return keyList;
	}

	public RecordSet getList(String sql, Object paramList[]) {
		RecordSet recordSet = new RecordSet();
		ResultSet resultSet = null;
		ArrayList<Object> array = new ArrayList<>();
		PreparedStatement statement = null;
		try {
			if (database.isEmpty()) {
				throw new Exception("Database configuration error");
			}
			log.info("SQL: " + sql);
			Class.forName(driver);
			Connection con = null;
			if (sql.equals(Constants.MYSQL_TABLE)
					|| sql.equals(Constants.MYSQL_SCHEMA)) {
				con = DriverManager.getConnection(jdbc_url + dbName
						+ "?useUnicode=true&characterEncoding=utf8", user,
						password);
			} else {
				con = DriverManager.getConnection(jdbc_url + dbName, user,
						password);
			}
			con.setAutoCommit(true);

			statement = con.prepareStatement(sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ParameterMetaData metaData = statement.getParameterMetaData();
			if (paramList != null) {
				for (int i = 0; i < paramList.length; i++) {
					if (paramList[i] == null) {
						int sqlType = metaData.getParameterType(i + 1);						
						statement.setNull(i + 1, sqlType);						
					} else {						
						statement.setObject(i + 1, paramList[i]);
					}					
				}
			}

			if (database.toUpperCase().equals(Constants.MYSQL_TYPE)) {
				DatabaseMetaData meta = con.getMetaData();
				if (sql.equals(Constants.MYSQL_DATABASE)) {
					resultSet = meta.getCatalogs();
				} else if (sql.equals(Constants.MYSQL_TABLE)
						|| sql.equals(Constants.MYSQL_SCHEMA)) {
					resultSet = meta.getTables(null, null, "%", null);
					while (resultSet.next()) {
						array.add(resultSet.getObject("TABLE_NAME"));
					}
					recordSet.value.add(array);
				} else {
					resultSet = statement.executeQuery();
				}
			} else {
				resultSet = statement.executeQuery();				
			}

			if (resultSet != null && array.isEmpty()) {
				ResultSetMetaData rsmd = resultSet.getMetaData();
				String field = "";
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					field = rsmd.getColumnName(i);
					recordSet.headerList.add(field);
					resultSet.beforeFirst();
					array = new ArrayList<>();
					while (resultSet.next()) {
						Object value = resultSet.getObject(field);
						array.add(value);
					}
					recordSet.value.add(array);
				}
			}

			statement.close();
			con.close();

		} catch (Exception e) {
			try {
				// log.info("SQL execution error",e);
				if (statement != null) {
					recordSet = null;
					statement.executeUpdate();
					
				}
			} catch (Exception ex) {
				recordSet = null;
				log.error("DB update error", ex);
			}
		}

		return recordSet;
	}

	public String getRecord(String sql, Object[] paramList) {
		return getRecord(sql, paramList, true, "");
	}

	public String getRecord(String sql, Object[] paramList, boolean newLine) {
		return getRecord(sql, paramList, newLine, "");
	}

	public String getRecord(String sql, Object[] paramList, boolean newLine,
			String prefix) {
		ResultSet resultSet;
		String str = "";
		try {
			if (database.isEmpty()) {
				throw new Exception("Database configuration error");
			}
			log.info("URL: " + jdbc_url + dbName);
			Class.forName(driver);
			Connection con = DriverManager.getConnection(jdbc_url + dbName,
					user, password);
			con.setAutoCommit(true);
			Statement statement = con.createStatement();

			PreparedStatement stmt = con.prepareStatement(sql);
			for (int i = 0; i < paramList.length; i++) {
				stmt.setObject(i + 1, paramList[i]);
			}

			log.info("SQL: " + sql);
			resultSet = stmt.executeQuery();
			ResultSetMetaData rsmd = resultSet.getMetaData();
			String field = "";
			while (resultSet.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					field = rsmd.getColumnName(i);
					if (newLine) {
						str += field + ": " + resultSet.getString(field)
								+ "\r\n";
					} else {
						str += prefix + resultSet.getString(field);
					}
				}
				str += "\r\n";
			}

			statement.close();
			con.close();

		} catch (Exception e) {
			log.error("DB access error", e);
		}
		return str;
	}

	public String describe(String table) {
		ResultSet resultSet;
		String str = "";
		try {
			if (database.isEmpty()) {
				throw new Exception("Database configuration error");
			}
			Class.forName(driver);
			Connection con = DriverManager.getConnection(jdbc_url + dbName,
					user, password);
			con.setAutoCommit(true);
			Statement statement = con.createStatement();
			String sql = "SELECT * FROM " + table;
			resultSet = statement.executeQuery(sql);
			ResultSetMetaData rsmd = resultSet.getMetaData();
			String field = "";
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				str += "Field " + i + ": ";
				field = rsmd.getColumnName(i);
				str += field + "\r\n";
			}

			statement.close();
			con.close();
		} catch (Exception e) {
			log.error("Error starting DBAdmin", e);
		}
		return str;
	}

	public void read() {
		Scanner sc = new Scanner(System.in);
		while (sc.hasNext()) {
			String str = sc.nextLine();
			System.out.println("> " + str);
			if (str.toUpperCase().startsWith("DESCRIBE ")) {
				describe(str.substring(9));
			} else if (str.toUpperCase().startsWith("QUIT")) {
				saveIniFile();
				break;
			} else {
				getRecord(str);
			}
		}
		sc.close();
	}

	public static void main(String[] args) {

		Options opt = new Options();
		opt.addOption("i", Constants.OPTION_INI, true, "INI file");

		try {
			DBAdmin db = new DBAdmin();

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(opt, args);

			if (cmd.hasOption(Constants.OPTION_INI)) {
				db._iniFile = cmd.getOptionValue(Constants.OPTION_INI);
			}

			db.readIniFile();
			db.read();
		} catch (Exception e) {
			log.error("Error starting DBAdmin", e);
		}
	}

}
