import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class RecordTable extends JTable implements ListSelectionListener,ActionListener,MouseListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DBAdminFrame admin=null;
	CellRenderer cellRenderer=new CellRenderer();
	Point rightClickPos=null;
	JPopupMenu popup = new JPopupMenu();
	
	RecordTable(DBAdminFrame admin){
		this.admin=admin;

		JMenuItem miSort=new JMenuItem("Sort by");
		popup.add(miSort);
		miSort.addActionListener(this);
		
		addMouseListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		rightClickPos=e.getPoint();
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
	
	public void init(RecordSet recordSet) {
		
		if(recordSet==null || recordSet.value==null || recordSet.value.size()==0){
			return;
		}
		
		int page=Integer.parseInt(admin.page.getText());
		ArrayList<String> header=new ArrayList<String>();
		header.add("Field");
		for(int i=0;i<recordSet.value.get(0).size();i++){
			header.add(""+(page+i+1));
		}
		
		DefaultTableModel tableModel = new DefaultTableModel(header.toArray(), 0);
		
		for (int index = 0; index < recordSet.headerList.size(); index++) {
			ArrayList<Object> array = new ArrayList<>();
			array.add(recordSet.headerList.get(index));
			array.addAll(recordSet.value.get(index));
			tableModel.addRow(array.toArray());
		}
		setModel(tableModel);			
		setDefaultEditor(Object.class, null);
		
		TableColumnModel tcm=getColumnModel();
		for(int i=0;i<tcm.getColumnCount();i++){
			TableColumn tc=tcm.getColumn(i);
			tc.setCellRenderer(cellRenderer);
		}
	}		
	
	public void actionPerformed(ActionEvent e){
		
		int row=rowAtPoint(rightClickPos);
		
		String field=(String)getValueAt(row, 0);	
/*
		if(!admin.sortBy.isEmpty()){
			admin.sortBy+=",";
		}
		admin.sortBy+=field;
		*/
		admin.sortBy=field;
		popup.setVisible(false);		
		admin.goPage();
	}

}
