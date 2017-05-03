import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

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
	RecordTable table = new RecordTable(this);
	JTextArea cellArea = new JTextArea();
	JPanel controlPanel = new JPanel();
	JButton update = new JButton("Update");
	CellTable cellTable = new CellTable();
	JComboBox<String> dbCombo=new JComboBox<>();
	JComboBox<String> pageCombo=new JComboBox<>();
	JButton forward=new JButton(">");
	JButton backward=new JButton("<");
	JTextField page=new JTextField();
	JSplitPane bottomRight=new JSplitPane();
	private boolean bottomRightPainted;
	JButton go=new JButton("Go");
	
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
				listSchemaTree();
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

		controlPanel.add(update);
		controlPanel.add(dbCombo);
		
		dbCombo.addItemListener(new ItemAdaptor(){
			public void itemStateChanged(ItemEvent event) {
			       if (event.getStateChange() == ItemEvent.SELECTED) {
			          Object item = event.getItem();
			          tree.setTable((String)item);
			       }			      
			}
		});
		
//		controlPanel.add(pageCombo);
		bottomRightSplit.setTopComponent(controlPanel);
		
		JPanel pageControl=new JPanel();
		pageControl.setLayout(new FlowLayout());
		page.setPreferredSize(new Dimension(50,30));
		pageControl.add(backward);
		pageControl.add(page);
		pageControl.add(forward);
		page.setText("0");
		pageControl.add(go);
		
		forward.addActionListener(new ActionAdaptor() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				int start=Integer.parseInt(page.getText());
				start+=10;
				page.setText(""+start);
				String tbl=(String)dbCombo.getSelectedItem();
				tree.setTable(tbl, start);				
			}
		});
		backward.addActionListener(new ActionAdaptor() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				int start=Integer.parseInt(page.getText());
				start-=10;
				if(start<0)
					start=0;
				page.setText(""+start);
				String tbl=(String)dbCombo.getSelectedItem();
				tree.setTable(tbl, start);				
			}
		});
		go.addActionListener(new ActionAdaptor() {
			@Override
			public void actionPerformed(ActionEvent e) {				
				int start=Integer.parseInt(page.getText());
				String tbl=(String)dbCombo.getSelectedItem();
				tree.setTable(tbl, start);
			}
		});

		bottomRight=new JSplitPane(JSplitPane.VERTICAL_SPLIT){
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g){
				super.paint(g);
				if(!bottomRightPainted){
					bottomRightPainted=true;
					bottomRight.setDividerLocation(.93);
				}
			}
		};
		bottomRight.add(new JScrollPane(table));
		bottomRight.add(pageControl);
		
		bottomRightSplit.setBottomComponent(bottomRight);
		bottomSplit.setRightComponent(bottomRightSplit);

		table.setCellSelectionEnabled(true);
		ListSelectionModel cellSelectionModel = table.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				Object selectedData = null;

				int selectedRow = table.getSelectedRow();
				int selectedColumn = table.getSelectedColumn();

				if (0 <= selectedRow && 0 <= selectedColumn) {
					selectedData = table.getValueAt(selectedRow, selectedColumn);
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
	
	public void tableSelected(String tableStr) {
		
		controlPanel.add(update);
		controlPanel.add(dbCombo);
	}

	public void tableDeselected() {
		controlPanel.remove(update);
		controlPanel.remove(dbCombo);
		revalidate();
		repaint();
	}

	public void updateField() {
		int selectedRow = table.getSelectedRow();
		int selectedColumn = table.getSelectedColumn();
		String tableStr = (String)dbCombo.getSelectedItem();
		int index = tableStr.indexOf(" ");		
		index = tableStr.indexOf(".");

		String table0 = tableStr.substring(index + 1);

		ArrayList<String> pkName = dbAdmin.getPK(table0);

		String sql = "UPDATE " + tableStr + " ";
		int nRow = table.getRowCount();
		Object value[] = new Object[nRow];
		String cond = "";
		int i = 1;

		for (int j = 0; j < nRow; j++) {
			String field = (String) table.getValueAt(j, 0);
			if (j == selectedRow) {
				TableCellEditor editor = cellTable.getCellEditor();
				if (editor != null) {
					editor.stopCellEditing();
				}
				value[0] = cellTable.getObject();

				sql += "SET " + field + " = ?";
			} else {
				String col = (String) table.getValueAt(j, 0);
				int nKey = pkName.size();
				if (nKey == 0) {
					return;
				} else {
					int n = 0;
					while (!pkName.get(n).equals(col) && ++n < nKey)
						;
					if (n < nKey) {
						value[i] = table.getValueAt(j, selectedColumn);
						if (!cond.isEmpty()) {
							cond += " AND ";
						}
						cond += field + "=?";
						i++;
					}
				}
			}
		}
		if (!cond.isEmpty())
			sql = sql + " WHERE " + cond;
		dbAdmin.getList(sql, value);

		String[] sqlList = new String[] { Constants.TSQL_LIMIT10, Constants.POSTGRES_LIMIT10, Constants.MYSQL_LIMIT10 };
		String sqlStr = sqlList[dbType.ordinal()];
		sqlStr = String.format(sqlStr, tableStr);
		executeSQL(sqlStr);
		tableSelected(tableStr);
	}

	public void setDBType() {
		if (!dbAdmin.database.isEmpty()) {
			if (dbAdmin.database.toUpperCase().equals(Constants.TSQL_TYPE)) {
				dbType = Database.TSQL;
			} else if (dbAdmin.database.toUpperCase().equals(Constants.POSTGRES_TYPE)) {
				dbType = Database.POSTGRES;
			} else {
				dbType = Database.MYSQL;
			}
			log.info("Database set:" + dbType.name());
		}
	}

	public void setStatusBar(String str) {
		String label = "Host: " + hostname() + " Database: " + database() + " " + str;
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

		String[] list = new String[] { Constants.TSQL_HOST, Constants.POSTGRES_HOST, Constants.MYSQL_HOST };
		String sqlStr = list[dbType.ordinal()];
		String res = dbAdmin.getRecord(sqlStr, false);
		if (res != null && !res.isEmpty()) {
			str += res;
		}

		list = new String[] { Constants.TSQL_PORT, Constants.POSTGRES_PORT, Constants.MYSQL_PORT };
		sqlStr = list[dbType.ordinal()];
		res = dbAdmin.getRecord(sqlStr, false);
		if (res != null && !res.isEmpty()) {
			str += ":" + res;
		}
		return str;
	}

	public String database() {
		String list[] = new String[] { Constants.TSQL_CURRENT_DATABASE, Constants.POSTGRES_CURRENT_DATABASE,
				Constants.MYSQL_CURRENT_DATABASE };
		String sqlStr = list[dbType.ordinal()];
		RecordSet recordSet = dbAdmin.getList(sqlStr);
		String db="";
		if(recordSet!=null && recordSet.value!=null && recordSet.value.get(0)!=null){
			db=(String)recordSet.value.get(0).get(0);
		}
		return db.trim();
	}

	public void listDatabase() {
		String list[] = new String[] { Constants.TSQL_DATABASE, Constants.POSTGRES_DATABASE, Constants.MYSQL_DATABASE };
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

	public void listSchemaTree() {
		String list[] = new String[] { Constants.TSQL_SCHEMA, Constants.POSTGRES_SCHEMA, Constants.MYSQL_SCHEMA };
		String sqlStr = list[dbType.ordinal()];
		RecordSet recordSet = dbAdmin.getList(sqlStr);
		Object[] objList = null;
		if (recordSet != null) {
			objList = recordSet.getFirst();
		}
		tree.setTree(objList);
		if (dbType == Database.MYSQL){			
			setDBCombo(objList);
		}						
	}

	public Object[] listTable(String schema) {
		String list[] = new String[] { Constants.TSQL_TABLE, Constants.POSTGRES_TABLE, Constants.MYSQL_TABLE };
		String sqlStr = list[dbType.ordinal()];

		RecordSet recordSet = dbAdmin.getList(sqlStr, new String[] { schema });
		setTitle(schema);

		setStatusBar(" Schema: " + schema);
		Object[] objList = null;
		if (recordSet != null) {
			objList = recordSet.getFirst();
			if (dbType != Database.MYSQL){
				setDBCombo(objList,schema);
			}
		}

		return objList;
	}
	
	public void setDBCombo(Object objList[]){
		setDBCombo(objList,"");
	}
	
	public void setDBCombo(Object objList[],String prefix){
		dbCombo.removeAll();
		dbCombo.addItem("-");
		for(int i=0;i<objList.length;i++){
			dbCombo.addItem(prefix+"."+(String)objList[i]);
		}					
	}

	public void selectDBCombo(String tableStr){
		int nCombo=dbCombo.getItemCount();
		int i=0;
		while(!dbCombo.getItemAt(i).equals(tableStr) && ++i<nCombo);
		if(i<nCombo){
			dbCombo.setSelectedIndex(i);
		}					
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

	public Object[] listColumn(String schema, String tableStr) {
		String list[] = new String[] { Constants.TSQL_LIST_COLUMN, Constants.POSTGRES_LIST_COLUMN,
				Constants.MYSQL_LIST_COLUMN };
		String sqlStr = list[dbType.ordinal()];
		if (dbType == Database.MYSQL) {
			setTitle(tableStr);
		} else {
			setTitle(schema + "." + tableStr);
		}
		setStatusBar(" Schema: " + schema + " Table: " + tableStr);
		RecordSet recordSet = null;
		if (dbType == Database.TSQL) {
			recordSet = dbAdmin.getList(sqlStr, new String[] { schema, tableStr });
		} else if (dbType == Database.POSTGRES) {
			recordSet = dbAdmin.getList(sqlStr, new String[] { tableStr });
		} else {
			recordSet = dbAdmin.getList(sqlStr, new String[] { database(), tableStr });
		}

		Object objList[] = null;
		if (recordSet != null) {
			objList = recordSet.value.get(0).toArray();
		}
		return objList;
	}

	public void describeField() {
		String field = sql.getText();
		if (field.isEmpty()) {
			field = result.getSelectedText();
		}
		field = field.trim();
		String list[] = new String[] { Constants.TSQL_COLUMN, Constants.POSTGRES_COLUMN, Constants.MYSQL_COLUMN };
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
		String list[] = new String[] { Constants.TSQL_COLUMN, Constants.POSTGRES_COLUMN, Constants.MYSQL_COLUMN };
		String sqlStr = list[dbType.ordinal()];
		// String res = dbAdmin.getRecord(sqlStr, new String[] { table, field
		// });
		RecordSet recordSet = dbAdmin.getList(sqlStr, new String[] { t, field });
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
				frame.dbAdmin._iniFile = cmd.getOptionValue(Constants.OPTION_INI);
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

	public ArrayList<Integer> setKeyList(String tbl) {
		ArrayList<Integer> rowList = new ArrayList<>();
		ArrayList<String> keyList = dbAdmin.getPK(tbl);
		int n = 0;
		int nKey = keyList.size();

		if (0 < nKey) {
			Object obj = null;
			for (int i = 0; i < table.getRowCount(); i++) {
				n = 0;
				while ((obj = table.getValueAt(i, 0)) != null && !((String) obj).equals(keyList.get(n)) && ++n < nKey)
					;
				if (n < nKey) {
					rowList.add(i);
				}
			}
		}
		return rowList;

	}
}
