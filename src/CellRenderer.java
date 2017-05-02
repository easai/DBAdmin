import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ArrayList<Integer> keyList = new ArrayList<>();

	public Component getTableCellRendererComponent(JTable t, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if ((row & 1) == 0) {
			setBackground(new Color(0xff, 0xff, 0xff));
		} else {
			setBackground(new Color(0xea, 0xeb, 0xff));
		}

		if (column == 0) {
			setBackground(new Color(0xd3, 0xd6, 0xff));
		}

		int nKey = keyList.size();
		if (0 < nKey) {
			int n = 0;
			while (keyList.get(n) != row && ++n < nKey)
				;
			if (n < nKey) {
				setBackground(new Color(0xff, 0xcc, 0xcc));
			}
		}
		return super.getTableCellRendererComponent(t, value, isSelected,
				hasFocus, row, column);
	}
}
