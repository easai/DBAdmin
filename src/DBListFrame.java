import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

public class DBListFrame extends JFrame implements MouseListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DBAdminFrame admin=null;
	JPopupMenu popup = new JPopupMenu();
	JTextArea textArea=new JTextArea();
	
	DBListFrame(DBAdminFrame admin, String db){
		this.admin=admin;
		textArea.setText(db);
		init();
	}
	
	public void init(){
				
		JMenuItem miDatabase = new JMenuItem("Select Database");
		miDatabase.addActionListener(new ActionAdaptor() {
			public void actionPerformed(ActionEvent e) {
				selectDatabase();
			}
		});
		popup.add(miDatabase);
		getContentPane().add(textArea);
		
		textArea.addMouseListener(this);
		
		setTitle("");
		setSize(500,500);
		setVisible(true);
	}

	public void selectDatabase(){
		String database=textArea.getSelectedText();
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
