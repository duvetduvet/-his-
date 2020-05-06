package UI;


import javax.swing.*;

public class ExcelButton {
    public JButton btnImport;
    public JButton btnExport;
    private JPanel pnButton;

    public void setVisible(boolean status) {
        btnImport.setVisible(status);
        btnExport.setVisible(status);
    }

}
