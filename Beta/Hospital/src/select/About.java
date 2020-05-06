package select;

import javax.swing.*;
import java.awt.*;

import static tool.Tool.getImageIcon;

public class About extends JDialog {
    private JPanel pnAbout;
    private JLabel logo;

    public About() {
        setContentPane(pnAbout);
        setTitle("关于赣州济世堂中医门诊管理系统");
        setSize(new Dimension(600, 400));
        ImageIcon imageIcon = getImageIcon();
        logo.setIcon(imageIcon);
        setIconImage(imageIcon.getImage());
        setLocationRelativeTo(null);
        setModal(true);
        setVisible(true);
    }
}
