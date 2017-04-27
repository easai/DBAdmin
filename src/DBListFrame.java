import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DBListFrame extends JFrame implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DBAdminFrame admin = null;
	JPopupMenu popup = new JPopupMenu();
	JTextArea textArea = new JTextArea();
	JButton selectDatabase = new JButton("Select Database");
	JList<String> dbList;

	DBListFrame(DBAdminFrame admin, String[] list) {
		this.admin = admin;

		if (list != null) {			
			dbList = new JList<>(list);			
		}
		init();
	}

	public void init() {

		JMenuItem miDatabase = new JMenuItem("Select Database");
		miDatabase.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				selectDatabase();
			}
		});
		selectDatabase.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				selectDatabase();
				dispose();
			}
		});

		Container pane = getContentPane();
		popup.add(miDatabase);
		
		if (dbList != null) {
			JScrollPane scroll=new JScrollPane(dbList);
			pane.add(scroll, BorderLayout.CENTER);			
		}		
		pane.add(selectDatabase, BorderLayout.SOUTH);

		textArea.addMouseListener(this);

		setTitle("Select Database");
		setSize(500, 500);
		setVisible(true);
	}

	public void selectDatabase() {
		String database=dbList.getSelectedValue();		
		database = database.trim();
		admin.dbAdmin.dbName = database;
		admin.setTitle(admin.hostname() + ":" + database);
		popup.setVisible(false);
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
