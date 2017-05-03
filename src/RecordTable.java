import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class RecordTable extends JTable implements ListSelectionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DBAdminFrame admin=null;
	CellRenderer cellRenderer=new CellRenderer();
	
	RecordTable(DBAdminFrame admin){
		this.admin=admin;
	}

	public void init(RecordSet recordSet) {
		
		if(recordSet==null || recordSet.value==null || recordSet.value.size()==0){
			return;
		}
		
		ArrayList<String> header=new ArrayList<String>();
		header.add("Field");
		for(int i=0;i<recordSet.value.get(0).size();i++){
			header.add(""+(i+1));
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
}
