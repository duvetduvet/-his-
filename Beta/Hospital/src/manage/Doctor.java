package manage;

import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.doctorCn;
import static tool.Regex.idRegex;
import static tool.Regex.phoneRegex;
import static tool.Strings.*;
import static tool.Tool.*;

public class Doctor {
    public JPanel pnDoctor;
    private JTable table;
    private JTextField txtName;
    private JComboBox cbbSex;
    private JDatePicker datePicker;
    private JTextField txtIdentity;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JComboBox cbbDepart;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnExport;
    private JCheckBox chkAdd;
    private JCheckBox chkModify;
    private JComboBox cbbUse;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();
    private Map<String, Integer> departmentMap = new HashMap<>();
    private Map<String, Object> doctorMap = new HashMap<>();

    public Doctor() {
        messageManager.removeAllMessageListener();
        addListener();
        initUI();
        buttonListeners();
        textFieldListeners();
        chkAdd.addActionListener((ActionEvent e) -> {
            setComponentText(new String[]{"", "", "", "", "", "", "", ""});
            btnAdd.setText("确认添加");
        });
        chkModify.addActionListener((ActionEvent e) -> {
            setComponentText(new String[]{"", "", "", "", "", "", "", ""});
            btnAdd.setText("确认修改");

        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (chkModify.isSelected()) {
                    int index = table.getSelectedRow();
                    txtName.setText(model.getValueAt(index, 0).toString());
                    cbbSex.setSelectedItem(model.getValueAt(index, 1));
                    datePicker.getFormattedTextField().setText(model.getValueAt(index, 2).toString());
                    txtIdentity.setText(model.getValueAt(index, 3).toString());
                    txtPhone.setText(model.getValueAt(index, 4).toString());
                    txtAddress.setText(model.getValueAt(index, 5).toString());
                    cbbDepart.setSelectedItem(model.getValueAt(index, 6));
                    cbbUse.setSelectedItem(model.getValueAt(index, 7));
                }
            }
        });
    }

    // 初始化
    private void initUI() {
        model = new DefaultTableModel(doctorCn, 0);
        table.setModel(model);
        try {
            Connect.sendMessage(tcp.buildMessage.doFunction("getDoctor", null).toByteArray());
            Map<String, Object> map = new HashMap<>();
            map.put("functionName", "department");
            map.put("table", "department");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(chkAdd);
        buttonGroup.add(chkModify);
    }

    // 添加信息返回监听器
    private void addListener() {
        listeners.add(new mListener("getDoctor") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        String name = (String) map.get("drname");
                        doctorMap.put(name, map.get("id"));
                        String sex = (String) map.get("sex");
                        String birth = map.get("birth").toString().split(" ")[0];
                        String identity = (String) map.get("identity");
                        String phone = (String) map.get("phone");
                        String address = (String) map.get("address");
                        String departname = (String) map.get("departname");
                        String useful = isSelected(trueOrFalse((Integer) map.get("useful")));
                        model.addRow(new Object[]{name, sex, birth, identity, phone, address, departname, useful});
                    }
                    table.setModel(model);
                }
            }
        });

        listeners.add(new mListener("department") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                        departmentMap.put((String) map.get("name"), (Integer) map.get("id"));
                    }
                    cbbDepart.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("insertDoctor") {
            public void messageEvent(MessageEvent event) {
                btnAdd.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    Object[] data = getInputData();
                    model.addRow(data);
                    table.setModel(model);
                    warning(addSuccess);
                    // 保存成功，清空，可以继续添加
                    setComponentText(new String[]{"", "", "", "", "", "", "", ""});
                    txtName.grabFocus();
                } else {
                    warning(addFail);
                }
            }
        });

        listeners.add(new mListener("deleteDoctor") {
            public void messageEvent(MessageEvent event) {
                 btnDelete.setEnabled(true);
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

        listeners.add(new mListener("updateDoctor") {
            @Override
            public void messageEvent(MessageEvent event) {
                btnAdd.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    warning("医生信息修改成功！");
                    Object[] data = getInputData();
                    int index = table.getSelectedRow();
                    for (int i = 0; i < table.getColumnCount(); i++) {
                        model.setValueAt(data[i], index, i);
                    }
                } else {
                    warning("医生信息修改失败，请检查您的网络设置！");
                }
            }
        });
    }

    private void textFieldListeners() {
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtIdentity.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtAddress.grabFocus();
                }
            }
        });

        txtIdentity.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPhone.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtName.grabFocus();
                }
            }
        });

        txtPhone.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtAddress.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtIdentity.grabFocus();
                }
            }
        });

        txtAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtPhone.grabFocus();
                }
            }
        });
    }

    // 按钮监听器
    private void buttonListeners() {
        // 保存提交
        btnAdd.addActionListener((ActionEvent e) -> {
            // 输入验证
            if (inputVerifier()) {
                warning(inputError);
                return;
            }
            // 获取输入
            Object[] data = getInputData();
            // 更新表中信息
            Map<String, Object> map = new HashMap<>();
            map.put("name", data[0]);
            map.put("sex", data[1]);
            if (!data[2].equals("")) {
                map.put("birth", data[2]);
            }
            if (!data[3].equals("")) {
                if (!data[3].toString().matches(idRegex)) {
                    warning(inputError);
                    return;
                }
                map.put("identity", data[3]);
            }
            if (!data[4].equals("")) {
                if (!data[4].toString().matches(phoneRegex)) {
                    warning(inputError);
                    return;
                }
                map.put("phone", data[4]);
            }
            if (!data[5].equals("")) {
                map.put("address", data[5]);
            }
            Object departId = departmentMap.get(data[6]);
            map.put("departmentId", departId);
            map.put("useful", trueorfalse(data[7]));
            String function = "insertDoctor";
            if (chkModify.isSelected()) {
                map.put("id", doctorMap.get(model.getValueAt(table.getSelectedRow(), 0).toString()));
                function = "updateDoctor";
            }
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction(function, map).toByteArray());
                btnAdd.setEnabled(false);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // 删除选中
        btnDelete.addActionListener((ActionEvent e) -> {
            if (table.getSelectedRow() == -1) {
                warning(selectOne);
                return;
            }
            if (confirm(askDelete) == JOptionPane.OK_OPTION) {
                int[] rows = table.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doctorMap.get(model.getValueAt(rows[i], 0).toString()));
                    try {
                        btnDelete.setEnabled(false);
                        Connect.sendMessage(tcp.buildMessage.doFunction("deleteDoctor", map).toByteArray());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        btnExport.addActionListener((ActionEvent e) -> {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "医生信息表", doctorCn, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    // 输入验证
    private boolean inputVerifier() {
        if (txtName.getText().equals("") || cbbSex.getSelectedIndex() == 0 || cbbDepart.getSelectedIndex() == 0 || cbbUse.getSelectedIndex() == 0) {
            return true;
        }
        return false;
    }

    // 设置组件内容
    private void setComponentText(String[] data) {
        txtName.setText(data[0]);
        cbbSex.setSelectedItem(data[1]);
        datePicker.getFormattedTextField().setText(data[2]);
        txtIdentity.setText(data[3]);
        txtPhone.setText(data[4]);
        txtAddress.setText(data[5]);
        cbbDepart.setSelectedItem(data[6]);
        cbbUse.setSelectedItem(data[7]);
    }

    // 获取用户输入信息
    private Object[] getInputData() {
        String name = txtName.getText();
        Object sex = cbbSex.getSelectedItem();
        String selectedTime = datePicker.getFormattedTextField().getText();
        String identity = txtIdentity.getText();
        String phone = txtPhone.getText();
        String address = txtAddress.getText();
        Object depart = cbbDepart.getSelectedItem();
        Object use = cbbUse.getSelectedItem();
        return new Object[]{name, sex, selectedTime, identity, phone, address, depart, use};
    }
}
