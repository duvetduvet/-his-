package UI;

import javax.swing.*;
import java.awt.*;

public class Frame extends JFrame {
    public static int width;
    public static int height;

    static {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        width = dimension.width;
        height = dimension.height;
    }

    //无菜单窗口
    public Frame(JPanel panel, String title, int width, int height) {
        setSize(width, height);
        setResizable(false);
        setContentPane(panel);
        setTitle(title);
        //使窗口处于屏幕中央
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
