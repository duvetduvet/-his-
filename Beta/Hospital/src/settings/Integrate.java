package settings;

import UI.Table;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import proto.myMessage;
import tcp.Connect;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.Headers.integrateCn;
import static tool.Regex.floatRegex;
import static tool.Strings.*;
import static tool.Tool.*;

public class Integrate extends JDialog {
    private JPanel pnVip;
    private Table table;
    private JTextField txtMoney;
    private JTextField txtDiscount;
    private JButton btnDelete;
    private JButton btnAdd;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    public Integrate() {
        messageManager.removeAllMessageListener();
        initUI();
        buttonListeners();
        textFieldListeners();
        messageListeners();
        setModal(true);
        setVisible(true);
    }

    private void initUI() {
        setContentPane(pnVip);
        setTitle("会员制度设置");
        setSize(new Dimension(600, 450));
        setLocationRelativeTo(null);
        setIconImage(getImageIcon().getImage());
        // 获取数据
        model = new DefaultTableModel(integrateCn, 0);
        table.setModel(model);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("functionName", "integrate");
            map.put("table", "integrate");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void messageListeners() {
        listeners.add(new mListener("integrate") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        Object level = map.get("level");
                        Object integrate = map.get("integrate");
                        Object discount = map.get("discount");
                        model.addRow(new Object[]{level, integrate, discount});
                    }
                    table.setModel(model);
                }
            }
        });

        listeners.add(new mListener("insertIntegrate") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    String index = "0";
                    if (table.getRowCount() != 0) {
                        index = model.getValueAt(table.getRowCount() - 1, 0).toString();
                    }
                    Object[] data = {Integer.parseInt(index) + 1, txtMoney.getText(), txtDiscount.getText()};
                    model.addRow(data);
                    table.setModel(model);
                    warning(addSuccess);
                    // 保存成功
                    txtDiscount.setText("");
                    txtMoney.setText("");
                    txtMoney.grabFocus();
                } else {
                    warning(addFail);
                }
            }
        });

        listeners.add(new mListener("updateIntegrate") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    int row = model.getRowCount();
                    Object[] data = {txtMoney.getText(), txtDiscount.getText()};
                    for (int i = 0; i < data.length; i++) {
                        model.setValueAt(data[i], row - 1, i);
                    }
                    table.setModel(model);
                    warning(saveSuccess);
                } else {
                    warning(saveFail);
                }
            }
        });

        listeners.add(new mListener("deleteIntegrate") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    int[] rows = table.getSelectedRows();
                    for (int i = 0; i < rows.length; i++) {
                        model.removeRow(rows[i] - i);
                    }
                    table.setModel(model);
                    warning(deleteSuccess);
                } else {
                    warning(deleteFail);
                }
            }
        });
    }

    private void textFieldListeners() {
        txtMoney.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {
                    txtDiscount.grabFocus();
                }
            }
        });

        txtDiscount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnAdd.doClick();
                }
                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    txtMoney.grabFocus();
                }
            }
        });
    }

    private void buttonListeners() {
        // 添加信息
        btnAdd.addActionListener((ActionEvent e) -> {
            String number = txtMoney.getText();
            String discount = txtDiscount.getText();
            if (!number.matches(floatRegex) || !discount.matches(floatRegex) || Float.parseFloat(discount) > 1) {
                warning(inputError);
                return;
            }
            String index = "0";
            if (table.getRowCount() != 0) {
                index = model.getValueAt(table.getRowCount() - 1, 0).toString();
            }
            Map<String, Object> map = new HashMap<>();
            map.put("level", Integer.parseInt(index) + 1);
            map.put("integrate", Float.parseFloat(number));
            map.put("discount", Float.parseFloat(discount));
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("insertIntegrate", map).toByteArray());
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
            if (table.getSelectedRow() != table.getRowCount() - 1) {
                warning("请从最高等级开始删除！");
                return;
            }
            // 确定删除
            if (confirm(askDelete) == JOptionPane.OK_OPTION) {
                int[] rows = table.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("level", model.getValueAt(rows[i], 0));
                    try {
                        Connect.sendMessage(tcp.buildMessage.doFunction("deleteIntegrate", map).toByteArray());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }
}
