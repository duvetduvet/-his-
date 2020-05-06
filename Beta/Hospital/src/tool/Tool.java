package tool;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static tool.Strings.logoPath;
import static tool.Strings.sysInfo;

public interface Tool {
    // chk是否被选中
    static String isSelected(boolean selected) {
        String str = "否";
        if (selected) {
            return "是";
        }
        return str;
    }

    // chk是否应该被选中
    static boolean isSelected(Object object) {
        return object.toString().equals("是");
    }

    static int trueorfalse(Object object) {
        if (object.toString().equals("是")) {
            return 1;
        } else {
            return 0;
        }
    }

    static boolean trueOrFalse(int value) {
        return value == 1;
    }

    static void warning(String info) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, info, sysInfo, JOptionPane.WARNING_MESSAGE);
    }

    static int confirm(String info) {
        java.awt.Toolkit.getDefaultToolkit().beep();
        return JOptionPane.showConfirmDialog(null, info, sysInfo, JOptionPane.OK_CANCEL_OPTION);
    }

    static String getToday() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }

    static ImageIcon getImageIcon() {
        ImageIcon imageIcon = new ImageIcon(logoPath);
        imageIcon.setImage(imageIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        return imageIcon;
    }

    static float ceil(float num)
    {
        num = num *100;
        int temp = (int)num;
        num = temp /100;
        num = (float) Math.ceil(temp);
        return 0;

    }

}
