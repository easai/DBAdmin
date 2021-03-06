import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class CellTable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DefaultTableModel tableModel = new DefaultTableModel(new String[] { "" }, 0);

	CellTable() {

		tableModel = new DefaultTableModel(new String[] { "" }, 0) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int c) {
				Object obj = getValueAt(0, c);
				if (obj != null) {
					return obj.getClass();
				}else{
					return Object.class;
				}
			}
		};

		tableModel.addRow(new Object[] { "" });
		setModel(tableModel);
		setTableHeader(null);
	}

	public void setObject(Object obj) {
		this.setValueAt(obj, 0, 0);
	}

	public Object getObject() {
		return this.getValueAt(0, 0);
	}

}
