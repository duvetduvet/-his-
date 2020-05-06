package settings;

import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import proto.myMessage;
import tcp.Connect;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Strings.*;
import static tool.Tool.*;

public class Power extends JDialog {
    private JPanel pnPower;
    private JComboBox cbbPosition;
    private JTextField txtNumber;
    private JTextField txtPsw;
    private JCheckBox chkReg;
    private JCheckBox chkPrice;
    private JCheckBox chkCharge;
    private JCheckBox chkDeMed;
    private JCheckBox chkMedicineManage;
    private JCheckBox chkMedInOut;
    private JCheckBox chkPatientManage;
    private JCheckBox chkVip;
    private JCheckBox chkSetVip;
    private JCheckBox chkOther;
    private JCheckBox chkDoctor;
    private JCheckBox chkSelect;
    private JCheckBox chkPre;
    private JCheckBox chkPower;
    private JButton btnOk;
    private JTextField txtAddUser;
    private JCheckBox chkAddUser;
    private JButton btnDelete;
    private JCheckBox chkChooseUser;
    private Map<String, Map<String, Object>> infoMap = new HashMap<>();
    private List<mListener> listeners = new ArrayList();

    public Power() {
        messageManager.removeAllMessageListener();
        initUI();
        textFieldListeners();
        checkBoxListeners();
        comboBoxListeners();
        buttonListeners();
        messageListeners();
        setModal(true);
        setVisible(true);
    }

    private void initUI() {
        setContentPane(pnPower);
        setTitle("用户管理");
        setSize(new Dimension(600, 500));
        setLocationRelativeTo(null);
        setIconImage(getImageIcon().getImage());
        init();
    }

    private void checkBoxListeners() {
        chkChooseUser.addActionListener((ActionEvent e) -> {
            if (chkAddUser.isSelected()) {
                chkAddUser.setSelected(false);
            }
            chkChooseUser.setSelected(true);
            cbbPosition.setEnabled(true);
            txtAddUser.setEnabled(false);
            btnDelete.setEnabled(true);
            cbbPosition.grabFocus();
            init();
        });

        chkAddUser.addActionListener((ActionEvent e) -> {
            if (chkChooseUser.isSelected()) {
                chkChooseUser.setSelected(false);
            }
            cbbPosition.setEnabled(false);
            chkAddUser.setSelected(true);
            txtAddUser.setEnabled(true);
            btnDelete.setEnabled(false);
            txtAddUser.grabFocus();
            init();
        });
    }

    private void textFieldListeners() {
        txtAddUser.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtPsw.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtNumber.grabFocus();
                }
            }
        });

        txtNumber.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (chkAddUser.isSelected()) {
                        txtAddUser.grabFocus();
                    } else {
                        txtPsw.grabFocus();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtPsw.grabFocus();
                }
            }
        });

        txtPsw.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtNumber.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (chkAddUser.isSelected()) {
                        txtAddUser.grabFocus();
                    } else {
                        txtNumber.grabFocus();
                    }
                }
            }
        });
    }

    private void comboBoxListeners() {
        cbbPosition.addActionListener((ActionEvent e) -> {
            Object object = cbbPosition.getSelectedItem();
            if (!object.equals("")) {
                Map<String, Object> map = infoMap.get(object.toString());
                txtNumber.setText(map.get("number").toString());
                txtPsw.setText(map.get("password").toString());
                powerAnalyser(map.get("power").toString());
            } else {
                txtNumber.setText("");
                txtPsw.setText("");
                powerAnalyser("000000000000000");
            }
        });
    }

    private void buttonListeners() {
        btnOk.addActionListener((ActionEvent e) -> {
            String number = txtNumber.getText();
            String password = txtPsw.getText();
            String power = powerGetter();
            String function;
            Map<String, Object> map = new HashMap<>();
            if (chkAddUser.isSelected()) {
                String username = txtAddUser.getText();
                if (username.equals("") || password.equals("")) {
                    warning(inputError);
                    return;
                }
                map.put("name", username);
                function = "addUser";
            } else {
                if (cbbPosition.getSelectedItem() == "") {
                    return;
                }
                function = "changeMessage";
            }
            map.put("number", number);
            map.put("password", password);
            map.put("power", power);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction(function, map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        btnDelete.addActionListener((ActionEvent e) -> {
            Object user = cbbPosition.getSelectedItem();
            if (user == "") {
                return;
            }
            if (confirm(deleteUser.replace("user", user.toString())) == JOptionPane.OK_OPTION) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", infoMap.get(user).get("id"));
                try {
                    Connect.sendMessage(tcp.buildMessage.doFunction("deleteUser", map).toByteArray());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }


    private void messageListeners() {
        listeners.add(new mListener("getPosition") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anyList = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anyList) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        String name = map.get("name").toString();
                        map.remove("name");
                        infoMap.put(name, map);
                        list.add(name);
                    }
                    cbbPosition.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("changeMessage") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    warning(userModifySuccess);
                    init();
                } else {
                    warning(userModifyFail);
                }
            }
        });

        listeners.add(new mListener("addUser") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    warning(userAddSuccess);
                } else {
                    warning(userAddFail);
                }
            }
        });

        listeners.add(new mListener("deleteUser") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    warning(userDeleteSuccess);
                    init();
                } else {
                    warning(userDeleteFail);
                }
            }
        });
    }

    private void init() {
        try {
            Connect.sendMessage(tcp.buildMessage.doFunction("getPosition", null).toByteArray());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        txtAddUser.setText("");
        txtNumber.setText("");
        txtPsw.setText("");
        powerAnalyser("000000000000000");
    }

    private void powerAnalyser(String power) {
        char[] powers = power.toCharArray();
        if (powers[1] == '1') {
            chkReg.setSelected(true);
        } else {
            chkReg.setSelected(false);
        }
        if (powers[2] == '1') {
            chkPrice.setSelected(true);
        } else {
            chkPrice.setSelected(false);
        }
        if (powers[3] == '1') {
            chkCharge.setSelected(true);
        } else {
            chkCharge.setSelected(false);
        }
        if (powers[4] == '1') {
            chkDeMed.setSelected(true);
        } else {
            chkDeMed.setSelected(false);
        }
        if (powers[5] == '1') {
            chkMedicineManage.setSelected(true);
        } else {
            chkMedicineManage.setSelected(false);
        }
        if (powers[6] == '1') {
            chkMedInOut.setSelected(true);
        } else {
            chkMedInOut.setSelected(false);
        }
        if (powers[7] == '1') {
            chkPatientManage.setSelected(true);
        } else {
            chkPatientManage.setSelected(false);
        }
        if (powers[8] == '1') {
            chkVip.setSelected(true);
        } else {
            chkVip.setSelected(false);
        }
        if (powers[9] == '1') {
            chkSetVip.setSelected(true);
        } else {
            chkSetVip.setSelected(false);
        }
        if (powers[10] == '1') {
            chkOther.setSelected(true);
        } else {
            chkOther.setSelected(false);
        }
        if (powers[11] == '1') {
            chkDoctor.setSelected(true);
        } else {
            chkDoctor.setSelected(false);
        }
        if (powers[12] == '1') {
            chkSelect.setSelected(true);
        } else {
            chkSelect.setSelected(false);
        }
        if (powers[13] == '1') {
            chkPre.setSelected(true);
        } else {
            chkPre.setSelected(false);
        }
        if (powers[14] == '1') {
            chkPower.setSelected(true);
        } else {
            chkPower.setSelected(false);
        }
    }

    private String powerGetter() {
        StringBuilder stringBuilder = new StringBuilder("1");
        if (chkReg.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkPrice.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkCharge.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkDeMed.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkMedicineManage.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkMedInOut.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkPatientManage.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkVip.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkSetVip.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkOther.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkDoctor.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkSelect.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkPre.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        if (chkPower.isSelected()) {
            stringBuilder.append('1');
        } else {
            stringBuilder.append('0');
        }
        return stringBuilder.toString();
    }
}
