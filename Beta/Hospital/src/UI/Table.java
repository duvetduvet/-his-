package UI;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Enumeration;

public class Table extends JTable
{
    public int column = -1;

    private Color gridColor = new Color(236, 233, 216);//网格颜色

    public Table(TableModel dm, TableColumnModel cm, ListSelectionModel sm)
    {
        super(dm, cm, sm);
        init();
    }

    public Table()
    {
        this(null, null, null);
        init();

    }

    @Override
    public void setModel(TableModel dataModel)
    {
        RowSorter rowSort = this.getRowSorter();
        if (rowSort != null)
            rowSort.setSortKeys(null);

        super.setModel(dataModel);
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, a default column model,
     * and a default selection model.
     *
     * @param dm the data model for the table
     * @see #createDefaultColumnModel
     * @see #createDefaultSelectionModel
     */
    public Table(TableModel dm)
    {
        this(dm, null, null);
        init();
    }

    /**
     * Constructs a <code>JTable</code> that is initialized with
     * <code>dm</code> as the data model, <code>cm</code>
     * as the column model, and a default selection model.
     *
     * @param dm the data model for the table
     * @param cm the column model for the table
     * @see #createDefaultSelectionModel
     */
    public Table(TableModel dm, TableColumnModel cm)
    {
        this(dm, cm, null);
        init();
    }

    // 获取列下拉菜单模型
    public static ComboBoxModel getColumnDataComboBoxModel(String str, String[] arg)
    {
        String[] elements = new String[arg.length + 1];
        elements[0] = str;
        for (int i = 1; i < elements.length; i++)
        {
            elements[i] = arg[i - 1];
        }
        return new ComboBoxModel(elements);
    }

    private void init()
    {
        setAutoCreateRowSorter(true);
        this.setGridColor(gridColor);
        setDefaultRenderer(Object.class, new tool.Renderer());
        setDefaultRenderer(Integer.class, new tool.Renderer());
        setDefaultRenderer(Float.class, new tool.Renderer());
        setDefaultRenderer(Double.class, new tool.Renderer());
        setDefaultRenderer(BigDecimal.class, new tool.Renderer());
        setDefaultRenderer(Object.class, new tool.Renderer());
    }

    // 表格高度设置为28
    public void setRowHeight(int rowHeight)
    {
        super.setRowHeight(28);
    }

    // 表头不可重排，内容居中
    public JTableHeader getTableHeader()
    {
        JTableHeader tableHeader = super.getTableHeader();
        // 设置表格列不可重排
        tableHeader.setReorderingAllowed(false);
        // 获得表格头的单元格对象
        DefaultTableCellRenderer cellRenderer = (DefaultTableCellRenderer) tableHeader.getDefaultRenderer();
        // 列名居中
        cellRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        return tableHeader;
    }

    // 单元格内容居中
    public TableCellRenderer getDefaultRenderer(Class<?> columnClass)
    {
        DefaultTableCellRenderer cellRenderer = (DefaultTableCellRenderer) super.getDefaultRenderer(columnClass);
        // 单元格内容居中
        cellRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
        return cellRenderer;
    }


    // 设置单元格不可编辑
    public boolean isCellEditable(int row, int column)
    {
        return this.column == column;
    }

    public int viewRowIndexToModel(int row)
    {
        if (row != -1)
            return convertRowIndexToModel(row);
        else
            return -1;
    }

    public int viewColumIndexToModel(int column)
    {
        if (column != -1)
            return convertColumnIndexToModel(column);
        else
            return -1;
    }

    public void setDefaultRenderer(Class<?> columnClass, TableCellRenderer renderer)
    {
        if (renderer != null)
        {
            defaultRenderersByColumnClass.put(columnClass, renderer);
        }
        else
        {
            defaultRenderersByColumnClass.remove(columnClass);
        }
    }

    public void FitTableColumns(JTable myTable)
    {
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements())
        {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable, column.getIdentifier()
                            , false, false, -1, col).getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++)
            {
                int preferedWidth = (int) myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
                        myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column); // 此行很重要
            column.setWidth(width + myTable.getIntercellSpacing().width);
        }
    }
}
