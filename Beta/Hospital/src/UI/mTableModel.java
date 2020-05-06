package UI;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class mTableModel extends DefaultTableModel implements TableModel {
    public mTableModel(int rowCount, int columnCount) {
        super(rowCount, columnCount);
    }

    public mTableModel() {
        super(0, 0);
    }

    public mTableModel(Object[] columnNames, int rowCount) {
        super(columnNames, rowCount);
    }

    public Class<?> getColumnClass(int column) {
        Class<?> returnValue = Object.class;
        if ((column >= 0) && (column < getColumnCount() && getRowCount() > 0)) {
            for (int i = 0; i < getRowCount(); i++)
                if (getValueAt(i, column) != null) {
                    returnValue = getValueAt(i, column).getClass();
                    break;
                }
        }
        return returnValue;
    }
}
