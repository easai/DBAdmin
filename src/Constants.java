
public class Constants {
	public static final String OPTION_INI = "inifile";
	
	public static final String TSQL_DATABASE="SELECT name FROM sys.databases";
	public static final String TSQL_SCHEMA="SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA";
	public static final String TSQL_TABLE="SELECT table_name FROM information_schema.tables WHERE table_type='BASE TABLE' AND table_schema like ?";
	public static final String TSQL_HOST="SELECT HOST_NAME()";
	public static final String TSQL_PORT="SELECT distinct local_tcp_port from sys.dm_exec_connections where local_net_address is not null";
	public static final String TSQL_CURRENT_DATABASE="SELECT DB_NAME()";
	public static final String TSQL_COLUMN="SELECT * FROM INFORMATION_SCHEMA.COLUMNS Where TABLE_NAME = ? AND COLUMN_NAME=?";
	
	public static final String POSTGRES_DATABASE="SELECT datname FROM pg_database WHERE datistemplate = false";
	public static final String POSTGRES_SCHEMA="SELECT schema_name from information_schema.schemata";
	public static final String POSTGRES_TABLE="SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname like ";
	public static final String POSTGRES_HOST="SELECT inet_server_addr()";
	public static final String POSTGRES_PORT="SELECT inet_server_port()";
	public static final String POSTGRES_CURRENT_DATABASE="SELECT current_database()";
	public static final String POSTGRES_COLUMN="";
	
	public static final String MYSQL_DATABASE="SHOW DATABASES";
	public static final String MYSQL_SCHEMA="SHOW TABLES";
	public static final String MYSQL_TABLE="DESCRIBE ";
	public static final String MYSQL_HOST="SHOW variables like 'hostname'";
	public static final String MYSQL_PORT="SHOW variables like ‘port’";
	public static final String MYSQL_CURRENT_DATABASE="SELECT database()";
	public static final String MYSQL_COLUMN="";
}
