package UI;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class Button {
    public JButton btnAdd;
    public JButton btnSaveAdd;
    public JButton btnCancelAdd;
    public JButton btnEdit;
    public JButton btnSaveEdit;
    public JButton btnCancelEdit;
    public JButton btnDelete;
    private JPanel pnButton;

    public Button() {
        //添加按钮
        btnAdd.addActionListener((ActionEvent e) -> {
            showButtonVisible(false);
            addHideButtonVisible(true);
            editHideButtonVisible(false);
        });

        //取消添加按钮
        btnCancelAdd.addActionListener((ActionEvent e) -> {
            showButtonVisible(true);
            addHideButtonVisible(false);
            editHideButtonVisible(false);
        });

        //编辑模式按钮
        btnEdit.addActionListener((ActionEvent e) -> {
            showButtonVisible(false);
            addHideButtonVisible(false);
            editHideButtonVisible(true);
        });

        //取消编辑按钮
        btnCancelEdit.addActionListener((ActionEvent e) -> {
            showButtonVisible(true);
            addHideButtonVisible(false);
            editHideButtonVisible(false);
        });
    }

    //可见的按钮事件
    private void showButtonVisible(boolean status) {
        btnAdd.setVisible(status);
        btnEdit.setVisible(status);
        btnCancelEdit.setVisible(status);
        btnDelete.setVisible(status);
    }

    //添加隐藏按钮事件
    private void addHideButtonVisible(boolean status) {
        btnSaveAdd.setVisible(status);
        btnCancelAdd.setVisible(status);
    }

    //编辑隐藏按钮事件
    private void editHideButtonVisible(boolean status) {
        btnSaveEdit.setVisible(status);
        btnCancelEdit.setVisible(status);
    }
}
