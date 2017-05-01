import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
	JLabel statusBar = new JLabel("");
	String dbTable = "";
	SchemaTree tree = new SchemaTree(this);
	JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	RecordTable table = new RecordTable();
	JTextArea cellArea = new JTextArea();
	JPanel controlPanel = new JPanel();
	JButton update = new JButton("Update");
	CellTable cellTable=new CellTable();
	
	public enum Database {
		TSQL, POSTGRES, MYSQL
	};

	Database dbType = Database.TSQL;

	private boolean painted;

	public void paint(Graphics g) {
		
		super.paint(g);
		if (!painted) {
			painted = true;
			bottomSplit.setDividerLocation(.25);
		}
	}

	public void init() {
		dbAdmin.readIniFile();
		setDBType();

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
		JMenuItem miField = new JMenuItem("Describe Field");
		mDatabase.add(miAllDB);
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
		splitPanel.setTopComponent(new JScrollPane(sql));

		listSchemaTree();
		bottomSplit.setLeftComponent(new JScrollPane(tree));
		JSplitPane bottomRightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
		controlPanel.add(cellTable);

		

		update.addActionListener(new ActionAdaptor() {

			@Override
			public void actionPerformed(ActionEvent e) {
				updateField();

			}

		});

		//controlPanel.add(update);
		bottomRightSplit.setTopComponent(controlPanel);
		bottomRightSplit.setBottomComponent(new JScrollPane(table));
		bottomSplit.setRightComponent(bottomRightSplit);

		table.setCellSelectionEnabled(true);
		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		cellSelectionModel
				.addListSelectionListener(new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						Object selectedData = null;

						int[] selectedRow = table.getSelectedRows();
						int[] selectedColumns = table.getSelectedColumns();

						for (int i = 0; i < selectedRow.length; i++) {
							for (int j = 0; j < selectedColumns.length; j++) {
								selectedData = (String) table.getValueAt(
										selectedRow[i], selectedColumns[j]);
							}
						}
						cellTable.setObject(selectedData);
					}
				});

		splitPanel.setBottomComponent(bottomSplit);

		Container pane = getContentPane();
		pane.add(splitPanel, BorderLayout.CENTER);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		pane.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setPreferredSize(new Dimension(getWidth(), 20));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusBar.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusBar);

		setSize(1000, 700);
		String title = hostname();
		String db = database();
		if (!db.isEmpty()) {
			title += " :" + db;
		}
		setTitle(title);
		setVisible(true);
	}

	public void tableSelected(String tableStr){
		update.setText("Update "+tableStr);
		controlPanel.add(update);
	}
	
	public void updateField() {
		int selectedRow = table.getSelectedRow();
		int selectedColumn = table.getSelectedColumn();
		String tableStr=update.getText();
		int index=tableStr.indexOf(" ");
		tableStr=tableStr.substring(index);
		String sql = "UPDATE " + tableStr+" ";

		int nRow=table.getRowCount();
		Object value[]=new Object[nRow];
		String cond="";
		int i=1;
		for (int j = 0; j < nRow; j++) {
			String field=(String)table.getValueAt(j,0);
			if(j==selectedRow){
				value[0] = cellTable.getObject();
				sql+="SET "+field+" = ?";
			}else{
				value[i] = table.getValueAt(j, selectedColumn);					
				if(!cond.isEmpty()){
					cond+=" AND ";
				}				
				cond+=field+"=?";
				i++;
			}	
		}
		if(!cond.isEmpty())
			sql=sql+" WHERE "+cond;
		dbAdmin.getList(sql,value);
	}

	public void setDBType() {
		if (!dbAdmin.database.isEmpty()) {
			if (dbAdmin.database.toUpperCase().equals(Constants.TSQL_TYPE)) {
				dbType = Database.TSQL;
			} else if (dbAdmin.database.toUpperCase().equals(
					Constants.POSTGRES_TYPE)) {
				dbType = Database.POSTGRES;
			} else {
				dbType = Database.MYSQL;
			}
			log.info("Database set:" + dbType.name());
		}
	}

	public void setStatusBar(String str) {
		String label = "Host: " + hostname() + " Database: " + database() + " "
				+ str;
		statusBar.setText(label);
	}

	public void executeSQL() {
		String sqlStr = sql.getText();
		executeSQL(sqlStr);
	}

	public void executeSQL(String sqlStr) {
		RecordSet recordSet = dbAdmin.getList(sqlStr);
		if (recordSet != null) {
			table.init(recordSet);
		}
	}

	public void executeSQL(String sqlStr, Object paramList[]) {
		RecordSet recordSet = dbAdmin.getList(sqlStr, paramList);
		if (recordSet != null) {
			table.init(recordSet);
		}
	}

	public String hostname() {
		String str = "";

		String[] list = new String[] { Constants.TSQL_HOST,
				Constants.POSTGRES_HOST, Constants.MYSQL_HOST };
		String sqlStr = list[dbType.ordinal()];
		String res = dbAdmin.getRecord(sqlStr, false);
		if (res != null && !res.isEmpty()) {
			str += res;
		}

		list = new String[] { Constants.TSQL_PORT, Constants.POSTGRES_PORT,
				Constants.MYSQL_PORT };
		sqlStr = list[dbType.ordinal()];
		res = dbAdmin.getRecord(sqlStr, false);
		if (res != null && !res.isEmpty()) {
			str += ":" + res;
		}
		return str;
	}

	public String database() {
		String list[] = new String[] { Constants.TSQL_CURRENT_DATABASE,
				Constants.POSTGRES_CURRENT_DATABASE,
				Constants.MYSQL_CURRENT_DATABASE };
		String sqlStr = list[dbType.ordinal()];
		String res = dbAdmin.getRecord(sqlStr, false);
		return res;
	}

	public void listDatabase() {
		String list[] = new String[] { Constants.TSQL_DATABASE,
				Constants.POSTGRES_DATABASE, Constants.MYSQL_DATABASE };
		String sqlStr = list[dbType.ordinal()];
		RecordSet recordSet = dbAdmin.getList(sqlStr);
		new DBListFrame(this, recordSet.getFirst());
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
		listSchemaTree();
		setStatusBar("");
	}

	public void listSchema() {
		String list[] = new String[] { Constants.TSQL_SCHEMA,
				Constants.POSTGRES_SCHEMA, Constants.MYSQL_SCHEMA };
		String sqlStr = list[dbType.ordinal()];
		String res = dbAdmin.getRecord(sqlStr, false);
		result.setText(res);
	}

	public void listSchemaTree() {
		String list[] = new String[] { Constants.TSQL_SCHEMA,
				Constants.POSTGRES_SCHEMA, Constants.MYSQL_SCHEMA };
		String sqlStr = list[dbType.ordinal()];
		RecordSet recordSet = dbAdmin.getList(sqlStr);
		tree.setTree(recordSet.getFirst());
	}

	public Object[] listTable(String schema) {
		String list[] = new String[] { Constants.TSQL_TABLE,
				Constants.POSTGRES_TABLE, Constants.MYSQL_TABLE };
		String sqlStr = list[dbType.ordinal()];
		RecordSet recordSet = dbAdmin.getList(sqlStr, new String[] { schema });
		setTitle(schema);
		setStatusBar(" Schema: " + schema);
		return recordSet.getFirst();
	}

	public void describeTable() {
		String table = sql.getText();
		if (table.isEmpty()) {
			table = result.getSelectedText();
		}
		table = table.trim();
		dbTable = table;
		String res = dbAdmin.describe(table);
		result.setText(res);
		setTitle(table);
		setStatusBar(" Table: " + table);
	}

	public Object[] listColumn(String schema, String table) {
		String list[] = new String[] { Constants.TSQL_LIST_COLUMN,
				Constants.POSTGRES_LIST_COLUMN, Constants.MYSQL_LIST_COLUMN };
		String sqlStr = list[dbType.ordinal()];
		setTitle(schema + "." + table);
		setStatusBar(" Schema: " + schema + " Table: " + table);
		RecordSet recordSet = dbAdmin.getList(sqlStr, new String[] { schema,
				table });
		return recordSet.getFirst();
	}

	public void describeField() {
		String field = sql.getText();
		if (field.isEmpty()) {
			field = result.getSelectedText();
		}
		field = field.trim();
		String list[] = new String[] { Constants.TSQL_COLUMN,
				Constants.POSTGRES_COLUMN, Constants.MYSQL_COLUMN };
		String sqlStr = list[dbType.ordinal()];

		String table = dbTable;
		int index = dbTable.indexOf('.');
		if (0 <= index) {
			table = dbTable.substring(index + 1);
		}
		String res = dbAdmin.getRecord(sqlStr, new String[] { table, field });
		result.setText(res);
		setTitle(field);
		setStatusBar(" Table: " + dbTable + " Field: " + field);
	}

	public void describeField(String t, String field) {
		String list[] = new String[] { Constants.TSQL_COLUMN,
				Constants.POSTGRES_COLUMN, Constants.MYSQL_COLUMN };
		String sqlStr = list[dbType.ordinal()];
		// String res = dbAdmin.getRecord(sqlStr, new String[] { table, field
		// });
		RecordSet recordSet = dbAdmin
				.getList(sqlStr, new String[] { t, field });
		table.init(recordSet);
		// result.setText(res);
		setTitle(field);
		setStatusBar(" Table: " + table + " Field: " + field);
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
			listSchemaTree();
			setStatusBar("");
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
