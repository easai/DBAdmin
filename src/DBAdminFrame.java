import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBAdminFrame extends JFrame implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LoggerFactory.getLogger(DBAdminFrame.class);

	DBAdmin dbAdmin = new DBAdmin();

	JMenuBar mb = new JMenuBar();
	JTextArea sql = new JTextArea();
	JTextArea result = new JTextArea();
	JPopupMenu popup = new JPopupMenu();	
	JLabel statusBar=new JLabel("");
	String dbTable="";

	enum Database{TSQL, POSTGRES, MYSQL};
	Database dbType=Database.TSQL;
	
	public void init() {
		dbAdmin.readIniFile();

		log.info("Initializing Frame");
		JMenu mFile = new JMenu("File");
		JMenuItem miConnect = new JMenuItem("Connect");
		JMenuItem miQuit = new JMenuItem("Quit");
		mFile.add(miConnect);
		mFile.add(miQuit);
		mb.add(mFile);

		JMenu mSQL = new JMenu("SQL");
		JMenuItem miRun = new JMenuItem("Run");
		mSQL.add(miRun);
		mb.add(mSQL);

		JMenu mDatabase = new JMenu("Database");
		JMenuItem miAllDB = new JMenuItem("Database");
		
		JMenuItem miSchema = new JMenuItem("Schema");
		JMenuItem miTable = new JMenuItem("Table");
		JMenuItem miDescribe = new JMenuItem("Describe Schema.Table");
		JMenuItem miField= new JMenuItem("Describe Field");
		mDatabase.add(miAllDB);
		mDatabase.add(miSchema);
		mDatabase.add(miTable);
		mDatabase.add(miDescribe);
		mb.add(mDatabase);

		miConnect.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				openIniFile();
			}
		});
		miQuit.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		miRun.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				executeSQL();
			}
		});
		miDescribe.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				describeTable();
				popup.setVisible(false);
			}
		});
		miAllDB.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				listDatabase();
			}
		});
		
		miSchema.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				listSchema();
			}
		});
		miTable.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				listTable();
				popup.setVisible(false);
			}
		});
		miField.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				describeField();
				popup.setVisible(false);				
			}
		});

		setJMenuBar(mb);

		result.addMouseListener(this);
		
		popup.add(miTable);
		popup.add(miDescribe);
		popup.add(miField);

		JSplitPane splitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JScrollPane sqlScroll = new JScrollPane(sql);
		splitPanel.setTopComponent(sqlScroll);

		JScrollPane scroll = new JScrollPane(result);
		splitPanel.setBottomComponent(scroll);

		Container pane=getContentPane();
		pane.add(splitPanel,BorderLayout.CENTER);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel statusPanel=new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		pane.add(statusPanel,BorderLayout.SOUTH);
		statusPanel.setPreferredSize(new Dimension(getWidth(),20));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusBar.setHorizontalAlignment(SwingConstants.LEFT);		
		statusPanel.add(statusBar);
		
		setDBType();
		
		setSize(500, 500);
		String title=hostname();
		String db=database();
		if(!db.isEmpty()){
			title+=" :"+db;
		}
		setTitle(title);
		setVisible(true);
	}

	public void setDBType(){
		if(!dbAdmin.database.isEmpty()){
			if(dbAdmin.database.toUpperCase().equals("TSQL")){
				dbType=Database.TSQL;
			}else if(dbAdmin.database.toUpperCase().equals("POSTGRES")){
				dbType=Database.POSTGRES;
			}else{
				dbType=Database.MYSQL;
			}
			log.info("Database set:"+dbType.name());
		}
	}
	
	public void setStatusBar(String str){		
		String label="Database: "+database()+" "+str;
		statusBar.setText(label);
	}
	
	public void describeTable() {
		String table = sql.getText();
		if (table.isEmpty()) {
			table = result.getSelectedText();
		}
		table=table.trim();
		dbTable=table;
		String res = dbAdmin.describe(table);
		result.setText(res);
		setTitle(table);
		setStatusBar(" Table: "+table);
	}
	
	public void describeField() {
		String field = sql.getText();
		if (field.isEmpty()) {
			field = result.getSelectedText();
		}		
		field=field.trim();
		String list[]=new String[]{Constants.TSQL_COLUMN,Constants.POSTGRES_COLUMN,Constants.MYSQL_COLUMN};
		String sqlStr = list[dbType.ordinal()];
				
		String table=dbTable;
		int index=dbTable.indexOf('.');		
		if(0<=index){
			table=dbTable.substring(index+1);
		}
		//sqlStr=replaceParam(sqlStr,new String[]{table, field});		
		//String res = dbAdmin.getRecord(sqlStr);
		String res = dbAdmin.getRecord(sqlStr,new String[]{table, field});
		result.setText(res);
		setTitle(field);
		setStatusBar(" Table: "+dbTable+" Field: "+field);
	}

	public String replaceParam(String str, String[] list) {
		String res = "";
		int index = 0;
		int count = 0;
		int prev = 0;
		while ((index = str.indexOf('?', index)) != -1) {
			if (count < list.length) {
				if (prev < index) {
					res += str.substring(prev, index);
				}
				res += list[count];
				count++;
			}
			index++;
			prev = index;
		}
		if (prev < str.length()) {
			res += str.substring(prev);
		}
		return res;
	}
	
	public void executeSQL() {
		String sqlStr = sql.getText();
		String res = dbAdmin.getRecord(sqlStr);
		result.setText(res);
	}

	public void listDatabase() {
		String list[]=new String[]{Constants.TSQL_DATABASE,Constants.POSTGRES_DATABASE,Constants.MYSQL_DATABASE};
		String sqlStr = list[dbType.ordinal()];
		String dbList[] = dbAdmin.getList(sqlStr);
		new DBListFrame(this,dbList);		
	}

	public void listSchema() {
		String list[]=new String[]{Constants.TSQL_SCHEMA,Constants.POSTGRES_SCHEMA,Constants.MYSQL_SCHEMA};
		String sqlStr = list[dbType.ordinal()];
		String res = dbAdmin.getRecord(sqlStr, false);
		result.setText(res);
	}

	public String hostname() {	
		String list[]=new String[]{Constants.TSQL_DATABASE,Constants.POSTGRES_DATABASE,Constants.MYSQL_DATABASE};
		String sqlStr = list[dbType.ordinal()];
		list=new String[]{Constants.TSQL_HOST,Constants.POSTGRES_HOST,Constants.MYSQL_HOST};
		sqlStr = list[dbType.ordinal()];
		String res = dbAdmin.getRecord(sqlStr);

		sqlStr = Constants.TSQL_PORT;
		if (dbAdmin.database != null && dbAdmin.database.equals("postgres")) {
			sqlStr = Constants.POSTGRES_PORT;
		}
		if (res != null && !res.isEmpty()) {
			res += ":" + dbAdmin.getRecord(sqlStr);
		}
		return res;
	}

	public String database() {
		String list[]=new String[]{Constants.TSQL_CURRENT_DATABASE,Constants.POSTGRES_CURRENT_DATABASE,Constants.MYSQL_CURRENT_DATABASE};
		String sqlStr = list[dbType.ordinal()];
		String res = dbAdmin.getRecord(sqlStr, false);
		return res;
	}

	public void selectDatabase() {
		String dbname = sql.getText();
		if (dbname.isEmpty()) {
			dbname = result.getSelectedText();
		}
		dbname = dbname.trim();
		dbAdmin.dbName = dbname;
		setTitle(hostname() + ":" + database());
		popup.setVisible(false);
	}

	public void listTable() {
		String schema = sql.getText().trim();
		if (schema.isEmpty()) {
			schema = result.getSelectedText();
		}
		String sqlStr = Constants.TSQL_TABLE + " '" + schema + "'";
		if (dbAdmin.database.equals("postgres")) {
			sqlStr = Constants.POSTGRES_TABLE + " '" + schema + "'";
		}
		String res = dbAdmin.getRecord(sqlStr, false, schema + ".");
		result.setText(res);
		setStatusBar("Schema: "+schema);
		setTitle(schema);
	}

	public void openIniFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File("."));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int res = fileChooser.showOpenDialog(this);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			String fn = file.getPath();
			dbAdmin._iniFile = fn;
			dbAdmin.readIniFile();
			setTitle(hostname());
			setDBType();	
		}
	}
	
	public static void main(String[] args) {
		Options opt = new Options();
		opt.addOption("i", Constants.OPTION_INI, true, "INI file");

		try {
			DBAdminFrame frame = new DBAdminFrame();

			CommandLineParser parser = new DefaultParser();
			CommandLine cmd = parser.parse(opt, args);

			if (cmd.hasOption(Constants.OPTION_INI)) {
				frame.dbAdmin._iniFile = cmd
						.getOptionValue(Constants.OPTION_INI);
				log.info("Setting INI file: " + frame.dbAdmin._iniFile);
			}

			frame.init();
		} catch (Exception e) {
			log.error("Error starting DBAdmin", e);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			popup.setLocation(e.getLocationOnScreen());
			popup.setVisible(true);
		} else {
			popup.setVisible(false);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
