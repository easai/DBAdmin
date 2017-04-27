import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class SchemaTree extends JTree implements TreeSelectionListener,
		MouseListener {

	DBAdminFrame admin = null;
	DefaultTreeModel model = (DefaultTreeModel) getModel();
	DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

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
		String schema = (String) node.getUserObject();
		String list[] = admin.listTable(schema);
		
		setTree(node, list);
	}

	public void setTree(String schemaList[]) {
		root.removeAllChildren();
		for (int i = 0; i < schemaList.length; i++) {
			root.add(new DefaultMutableTreeNode(schemaList[i]));
		}
		model.reload(root);
		setRootVisible(false);
	}

	public void setTree(DefaultMutableTreeNode node, String schemaList[]) {
		if (node== null || schemaList == null) {
			return;
		}
		
		for (int i = 0; i < schemaList.length; i++) {			
			node.add(new DefaultMutableTreeNode(schemaList[i]));
		}
			
		model.reload(root);
		setRootVisible(false);
				
		repaint();
		
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
