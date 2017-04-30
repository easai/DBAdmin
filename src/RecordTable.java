import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class RecordTable extends JTable {

	public void init(RecordSet recordSet) {

		int nRow = recordSet.value.size();		
		
		ArrayList<String> header=new ArrayList<String>();
		header.add("Field");
		for(int i=0;i<recordSet.value.size();i++){
			header.add(""+(i+1));
		}
		System.out.println("header.value: "+recordSet.headerList.size());
		
		
		DefaultTableModel tableModel = new DefaultTableModel(header.toArray(), 0);

		for (int index = 0; index < recordSet.headerList.size(); index++) {
			ArrayList<String> array = new ArrayList<>();
			array.add(recordSet.headerList.get(index));
			for (int i = 0; i < recordSet.value.size(); i++) {
				ArrayList<String> list = recordSet.value.get(i);
				array.add(list.get(index));
			}
			tableModel.addRow(array.toArray());
		}
/*
		DefaultTableModel tableModel = new DefaultTableModel(
				recordSet.headerList.toArray(), 0);

		for (int i = 0; i < nRow; i++) {
			tableModel.addRow(recordSet.value.get(i).toArray());
		}
*/

		setModel(tableModel);
	}

}
