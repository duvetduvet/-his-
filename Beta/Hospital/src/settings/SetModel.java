package settings;

import UI.Table;
import com.google.protobuf.InvalidProtocolBufferException;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import proto.Data;
import proto.myMessage;
import tcp.Connect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.Strings.*;
import static tool.Tool.*;

public class SetModel extends JDialog {
    public Table table;
    public JLabel lbName;
    public JTextField txtName;
    private JPanel pnModel;
    private JButton btnDelete;
    private JButton btnAdd;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    // 初始化界面
    public void initUI(String[] header, String title, String view) {
        setTitle(title);
        setSize(new Dimension(600, 450));
        setIconImage(getImageIcon().getImage());
        setResizable(false);
        setContentPane(pnModel);
        setLocationRelativeTo(null);
        // 初始化表格
        model = new DefaultTableModel(header, 0);
        table.setModel(model);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("table", view);
            map.put("functionName", view);
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMessageListener(String view) {
        listeners.add(new mListener(view) {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<com.google.protobuf.Any> any = feedback.getDetailsList();
                    for (com.google.protobuf.Any temp : any) {
                        try {
                            Data.common data = temp.unpack(Data.common.class);
                            int id = data.getIntMap().get("id");
                            String name = data.getStrMap().get("name");
                            if (name != null)
                                model.addRow(new Object[]{id, name});
                        } catch (InvalidProtocolBufferException e) {
                            System.out.println("any 类无法转换");
                        }
                    }
                    table.setModel(model);
                } else {
                    warning(selectFail);
                }
            }
        });

        listeners.add(new mListener("addSetting") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    String index = "0";
                    if (table.getRowCount() != 0) {
                        index = model.getValueAt(table.getRowCount() - 1, 0).toString();
                    }
                    Object[] data = {Integer.parseInt(index) + 1, txtName.getText()};
                    model.addRow(data);
                    table.setModel(model);
                    warning(addSuccess);
                    txtName.setText("");
                } else {
                    warning(addFail);
                }
            }
        });

        listeners.add(new mListener("updateSetting") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    int row = table.getSelectedRow();
                    model.setValueAt(txtName.getText(), row, 1);
                    table.setModel(model);
                    warning(saveSuccess);
                } else {
                    warning(saveFail);
                }
            }
        });

        listeners.add(new mListener("deleteSetting") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    // 获取选中行
                    int[] selectedRows = table.getSelectedRows();
                    for (int i = 0; i < selectedRows.length; i++) {
                        model.removeRow(selectedRows[i] - i);
                    }
                    warning(deleteSuccess);
                } else {
                    warning(deleteFail);
                }
            }
        });
    }

    // 窗口监听器
    public void windowListeners() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                messageManager.removeAllMessageListener();
            }
        });
    }

    // 按钮监听器
    public void buttonListeners(String view) {
        // 保存提交信息
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnAdd.doClick();
                }
            }
        });

        // 添加信息
        btnAdd.addActionListener((ActionEvent e) -> {
            // 输入验证
            if (txtName.getText().isEmpty()) {
                warning(inputError);
                return;
            }
            // 获取输入
            String index = "0";
            if (table.getRowCount() != 0) {
                index = model.getValueAt(table.getRowCount() - 1, 0).toString();
            }
            Object[] data = {Integer.parseInt(index) + 1, txtName.getText()};
            Map<String, Object> map = new HashMap<>();
            map.put("view", view);
            map.put("id", data[0]);
            map.put("name", data[1]);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("addSetting", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // 删除信息
        btnDelete.addActionListener((ActionEvent e) -> {
            // 未选中一行
            if (table.getSelectedRow() == -1) {
                warning(selectOne);
                return;
            }
            // 确定删除
            if (confirm(askDelete) == JOptionPane.OK_OPTION) {
                int[] rows = table.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("view", view);
                    map.put("id", model.getValueAt(rows[i], 0));
                    try {
                        Connect.sendMessage(tcp.buildMessage.doFunction("deleteSetting", map).toByteArray());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}
