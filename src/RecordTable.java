import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class RecordTable extends JTable implements ListSelectionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void init(RecordSet recordSet) {

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
	}

	
	
}
