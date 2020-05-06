package tool;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class Renderer extends DefaultTableCellRenderer {
    public static DefaultTableCellRenderer defaultTableCellRenderer;

    static {
        defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
    }

    private Color selectionColor = new Color(207, 228, 249);//行选择颜色
    private Color evenRowColor = new Color(233, 242, 241);//奇数行颜色
    private Color oddRowColor = new Color(255, 255, 255);//偶数行颜色

    public Renderer() {
        super();
        setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component renderer = defaultTableCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
            renderer.setForeground(Color.black);
            renderer.setBackground(selectionColor);
        } else {
            if (row % 2 == 0) {
                renderer.setForeground(Color.black);
                renderer.setBackground(oddRowColor);
            } else {
                renderer.setForeground(Color.black);
                renderer.setBackground(evenRowColor);
            }
        }
        return renderer;
    }
}
