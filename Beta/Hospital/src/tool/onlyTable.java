package tool;

import UI.Table;
import logic.Login;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class onlyTable {
    public int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    public int height = Toolkit.getDefaultToolkit().getScreenSize().height;
    private JPanel panel;
    private Table table;

    public onlyTable(DefaultTableModel model, String title) {
        table.setModel(model);
        JFrame jFrame = Login.getFrame();
        JDialog dialog = new JDialog(jFrame, title, true);
        dialog.setContentPane(panel);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

}
