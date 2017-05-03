import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class SchemaTree extends JTree implements TreeSelectionListener,
		MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DBAdminFrame admin = null;
	DefaultTreeModel model = (DefaultTreeModel) getModel();
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

	enum Level {
		ROOT, SCHEMA, TABLE, COLUMN
	};

	SchemaTree(DBAdminFrame admin) {
		this.admin = admin;
		addTreeSelectionListener(this);
		addMouseListener(this);
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();

		if (node == null) {
			return;
		}
		String current = (String) node.getUserObject();

		int level=node.getLevel();
		if(admin.dbType==DBAdminFrame.Database.MYSQL){
			level++;
		}
		if (level == Level.SCHEMA.ordinal()) {
			Object list[] = admin.listTable(current);
			setTree(node, list);			
		} else if (level == Level.TABLE.ordinal()) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
					.getParent();
			String schema = (String) parent.getUserObject();
			Object list[] = admin.listColumn(schema, current);
			setTree(node, list);
			
			// show table

			String table="";
			if(admin.dbType==DBAdminFrame.Database.MYSQL){
				table=current;				
			}else{
				table=schema+"."+current;
			}
			setTable(table);
			admin.selectDBCombo(table);
			
			// show button
			admin.tableSelected(table);
		} else if (level == Level.COLUMN.ordinal()) {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
					.getParent();
			String table= (String) parent.getUserObject();
			admin.describeField(table, current);
		}
	}
	
	public void setTable(String table){
		String[] sqlList = new String[] { Constants.TSQL_LIMIT10, Constants.POSTGRES_LIMIT10, Constants.MYSQL_LIMIT10 };
		String sqlStr = sqlList[admin.dbType.ordinal()];
		sqlStr=String.format(sqlStr,table);
		admin.executeSQL(sqlStr);
		String current="";
		if(admin.dbType==DBAdminFrame.Database.MYSQL){
			current=table;
		}else{
			int index=table.indexOf(".");
			if(0<=index){				
				current=table.substring(index);
			}			
		}		
		ArrayList<Integer> keyList=admin.setKeyList(current);
		admin.table.cellRenderer.keyList=keyList;
		
	}

	public void setTree(Object schemaList[]) {
		root.removeAllChildren();
		if(schemaList!=null){
			for (int i = 0; i < schemaList.length; i++) {
				root.add(new DefaultMutableTreeNode(schemaList[i]));
			}
		}
		model.reload(root);
		setRootVisible(false);
		repaint();
	}

	public void setTree(DefaultMutableTreeNode node, Object schemaList[]) {
		TreePath path = getSelectionPath();
		if (node == null || schemaList == null || !node.isLeaf()) {
			return;
		}		

		for (int i = 0; i < schemaList.length; i++) {
			node.add(new DefaultMutableTreeNode(schemaList[i]));
		}
		model.reload(root);
		setRootVisible(false);
		repaint();
		this.expandPath(path);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
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
