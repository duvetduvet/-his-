package manage;

import UI.Table;
import UI.hintWindow;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.chargeDetailCn;
import static tool.Regex.floatRegex;
import static tool.Strings.*;
import static tool.Tool.confirm;
import static tool.Tool.warning;

public class Prescription {
    public JPanel pnPrescription;
    private Table table;
    private JButton btnFresh;
    private JTextField txtNum;
    private JTextField txtSpell;
    private JButton btnSave;
    private JComboBox cbbName;
    private JLabel lbFeeType;
    private JLabel lbMoney;
    private JButton btnDelete;
    private JComboBox cbbHistoryId;
    private JLabel lbDepart;
    private JLabel lbDoctor;
    private JLabel lbRegType;
    private JTextField number;
    private JComboBox cbbNowId;
    private JTextArea textArea1;
    private JTextArea textArea2;
    private DefaultTableModel model;
    private boolean flag = false;       // 我自己都不知道这个flage到底是干嘛的，仿佛是控制是否有提示框
    private List<mListener> listeners = new ArrayList<>();
    private Map<String, Object> nameMap = new HashMap<>();
    private Map<Object, Map<String, Object>> medicineMap = new HashMap<>();
    private int old = 1;
    private int pid = 0;
    private Map<String, Integer> dose = new HashMap<>();             // 药品剂量map
    private Map<String, String> discriptionMap = new HashMap<>();             // 药品剂量map

    // 病人是否医保挂号！
    boolean isInsurance = false;


    public Prescription() {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        tableListeners();
        comboBoxListeners();
        textFieldListeners();
        buttonListeners();
        number.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    txtSpell.grabFocus();
            }
        });

        number.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                number.setText("");
            }

            @Override
            public void focusLost(FocusEvent e) {
                try {
                    int newvalue = Integer.parseInt(number.getText());
                    if (newvalue == 0)
                        throw new Exception();
                    if (newvalue != old)
                        updateNum(old, newvalue);
                    old = newvalue;
                } catch (Exception e1) {
                    number.setText(String.valueOf(old));
                }
            }
        });


        cbbNowId.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbbNowId.getSelectedIndex() == 0) {
                    // 清空当前处方
                    pid = 0;
                    // 设置老剂量为1
                    old = 1;
                    number.setText("1");
                    textArea2.setText("");
                    model.setRowCount(0);
                    return;
                }
                cbbHistoryId.setSelectedIndex(0);
                flag = true;
                Object id = cbbNowId.getSelectedItem().toString().split("/")[0];
                Map<String, Object> map = new HashMap<>();
                pid = Integer.parseInt(id.toString());
                map.put("prescriptionid", id);
                try {
                    Connect.sendMessage(buildMessage.doFunction("getPreInfoByPreId", map).toByteArray());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void initUI() {
        // 初始化表格
        table.column = 3;
        model = new DefaultTableModel(chargeDetailCn, 0);
        table.setModel(model);
        final TableRowSorter sorter = new TableRowSorter(model);
        table.setRowSorter(sorter); //为JTable设置排序器
        try {
            Connect.sendMessage(buildMessage.doFunction("getIdByPreState", null).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void messageListeners() {
        listeners.add(new mListener("getIdByPreState") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any any : anies) {
                        Map map = buildMessage.AnyToMap(any);
                        int id = (Integer) map.get("id");
                        String patientname = (String) map.get("patientname");
                        int patientid = (Integer) map.get("patientid");
                        String birth = String.valueOf(map.get("birth"));
                        String sex = String.valueOf(map.get("sex"));
                        birth = birth.substring(0, 4);
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int age = Integer.valueOf(birth);
                        age = year - age;
                        String idname = id + "/" + patientname + "/" + sex + "/" + age + "岁";
                        list.add(idname);
                        nameMap.put(idname, patientid);
                    }
                    cbbName.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getMedicineBySpell") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        String name = (String) map.get("name");
                        String outprice = map.get("outprice").toString();
                        Object stock = map.get("stock");
                        // 过滤没有医保项目编码的
                        if (isInsurance)
                            if (map.get("itemCode").toString().equals(""))
                                continue;
                        list.add(name + "---" + outprice + "---" + stock);
                        map.remove("name");
                        medicineMap.put(name, map);
                    }
                    if (!flag) {
                        hintWindow hintWindow = UI.hintWindow.map.get(txtSpell);
                        hintWindow.updateList(list.toArray(new String[list.size()]));
                    }
                    if (list.size() == 0) {
                        hintWindow hintWindow = UI.hintWindow.map.get(txtSpell);
                        hintWindow.setVisible(false);
                    }
                }
            }
        });

        listeners.add(new mListener("getRegiseterDetailById") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        Map<String, Object> maps = new HashMap<>();
                        String idname = cbbName.getSelectedItem().toString();
                        maps.put("patientid", nameMap.get(idname));

                        // 换人清空历史信息
                        model.setRowCount(0);
                        pid = 0;
                        old = 1;
                        isInsurance = false;
                        number.setText("1");

                        try {
                            Connect.sendMessage(buildMessage.doFunction("getHistory", maps).toByteArray());
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        String preid = idname.split("/")[0];
                        String departname = (String) map.get("departname");
                        String doctorname = (String) map.get("doctorname");
                        String regtypename = (String) map.get("regtypename");

                        textArea1.setText(map.get("illness").toString());

                        if (map.get("insurance").toString().equals("true")) {
                            isInsurance = true;
                            regtypename = "医保·" + regtypename;
                        } else {
                            regtypename = "非医保·" + regtypename;
                        }
                        setTextFieldText(new Object[]{preid, departname, doctorname, regtypename});
                        number.grabFocus();
                    }
                }
            }
        });

        listeners.add(new mListener("getHistory") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                dose.clear();
                discriptionMap.clear();
                if (feedback.getMark() > 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    List<String> listn = new ArrayList<>();
                    String regid = cbbName.getSelectedItem().toString().split("/")[0];
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        String rgid = map.get("rgid").toString();
                        if (regid.equals(rgid) &&  (int)map.get("state")==1) {
                            listn.add(map.get("pid") + "/" + map.get("regdate"));
                        } else {
                            list.add(map.get("pid") + "/" + map.get("regdate"));
                        }
                        dose.put(map.get("pid").toString(), (Integer) map.get("dose"));
                        discriptionMap.put(map.get("pid").toString(), map.get("discription").toString());
                    }
                    cbbHistoryId.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                    cbbNowId.setModel(getColumnDataComboBoxModel("", listn.toArray(new String[listn.size()])));
                }
            }
        });

        listeners.add(new mListener("addPrescriptions") {
            public void messageEvent(MessageEvent event) {
                btnSave.setEnabled(true);
                number.setText("1");
                btnSave.setEnabled(true);
                pid = 0;
                btnFresh.doClick();
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    warning(saveSuccess);
                } else {
                    warning(feedback.getBackMessage());
                }
            }
        });

        listeners.add(new mListener("getPreInfoByPreId") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    model.setRowCount(0);
                    List<Any> anies = feedback.getDetailsList();
                    boolean one = true;         //是否第一次循环变量
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        Object medid = map.get("medid");
                        Object name = map.get("name");
                        Map<String, Object> map1 = new HashMap<>();
                        map1.put("id", medid);
                        medicineMap.put(name, map1);
                        Object unitname = map.get("unitname");
                        Object outprice = map.get("outprice");
                        Object numbers = map.get("number");
                        Object type = map.get("type");
                        String itemCode = map.get("itemCode").toString();
                        String itemGrade = map.get("itemGrade").toString();

                        String pid = map.get("prescriptionid").toString();
                        if (one) {
                            one = false;
                            number.setText(String.valueOf(dose.get(pid)));
                            textArea2.setText(discriptionMap.get(pid));
                            old = dose.get(pid);
                        }
                        model.addRow(new Object[]{name, unitname, outprice, numbers, type,itemCode,itemGrade});
                    }
                    table.setModel(model);
                    setTypeAndMoney();
                    btnSave.setEnabled(true);
                    int newvalue = Integer.parseInt(number.getText());

                    updateNum(newvalue, newvalue);
                }
            }
        });
    }

    // 表格监听器
    private void tableListeners() {
        // 监听数据改变
        model.addTableModelListener((TableModelEvent e) ->
        {
            setTypeAndMoney();
        });
    }

    // 下拉菜单监听器
    private void comboBoxListeners() {
        // 单据编号选择
        cbbName.addActionListener((ActionEvent e) ->
        {
            int index = cbbName.getSelectedIndex();
            if (index == 0) {
                setTextFieldText(new String[]{"", "", "", ""});
                cbbHistoryId.setModel(new DefaultComboBoxModel());
                cbbNowId.setModel(new DefaultComboBoxModel());
                model.setRowCount(0);
                textArea1.setText("");
                textArea2.setText("");

                setTypeAndMoney();
                return;
            }
            String idname = cbbName.getSelectedItem().toString();
            Map<String, Object> map = new HashMap<>();
            String id = idname.split("/")[0];
            map.put("id", id);
            try {
                Connect.sendMessage(buildMessage.doFunction("getRegiseterDetailById", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // 获取历史处方Id
        cbbHistoryId.addActionListener((ActionEvent e) ->
        {

            if (cbbHistoryId.getSelectedIndex() == 0) {
                model.setRowCount(0);
                old = 1;
                number.setText("1");
                textArea2.setText("");
                return;
            }
            cbbNowId.setSelectedItem("");

            flag = true;
            Object id = cbbHistoryId.getSelectedItem().toString().split("/")[0];
            Map<String, Object> map = new HashMap<>();
            map.put("prescriptionid", id);
            warning("历史处方记录由于药品信息调整，可能会出现以下几个问题：\n" +
                    "1. 药品名称和现在药品名称对不上\n" +
                    "2. 药品单价和现在药品对不上 \n" +
                    "3. 医保编号未更新\n" +
                    "建议将更新的药品删除然后再重新输入更新后的药品（从拼音码处）!");
            try {
                Connect.sendMessage(buildMessage.doFunction("getPreInfoByPreId", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    // 文本框监听器
    private void textFieldListeners() {
        // 拼音码
        new hintWindow(txtSpell) {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (txtSpell.getText().equals(""))
                    return;
                if (e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    getMedModel();
            }

            @Override
            protected void setText(String selectedValue) {
                String medname = selectedValue.split("---")[0];
                txtSpell.setText(medname);
            }
        };

        txtSpell.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                flag = false;
            }
        });

        txtSpell.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    txtNum.grabFocus();
                }
            }
        });

        txtNum.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)
                    txtSpell.grabFocus();

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (cbbName.getSelectedIndex() == 0) {
                        warning("请选择病人姓名！");
                        return;
                    }
                    if (txtSpell.getText().equals("") || !txtNum.getText().matches(floatRegex)) {
                        warning(inputError);
                        return;
                    }
                    try {
                        // 剂量
                        BigDecimal n = new BigDecimal(number.getText());
                        // 药品名称
                        Object feename = txtSpell.getText();
                        // 药品数量
                        BigDecimal num = new BigDecimal(txtNum.getText());
                        // 判断是否有重复的药标记
                        boolean flag = true;
                        int i = 0;
                        for (; i < table.getRowCount(); i++) {
                            if (feename.equals(model.getValueAt(i, 0))) {
                                flag = false;
                                break;
                            }
                        }

                        // 如果table 中有重复的药
                        if (!flag) {
                            BigDecimal temp = n.multiply(num).add(new BigDecimal(table.getValueAt(i, 3).toString()));
                            BigDecimal outPrice = new BigDecimal(table.getValueAt(i, 2).toString());
                            model.setValueAt(temp, i, 3);
                            model.setValueAt(temp.multiply(outPrice).setScale(2, BigDecimal.ROUND_HALF_UP), i, 7);
                        } else {
                            Map<String, Object> map = medicineMap.get(txtSpell.getText());
                            BigDecimal outprice = new BigDecimal(map.get("outprice").toString()).setScale(2, BigDecimal.ROUND_HALF_UP);
                            BigDecimal money = n.multiply(num).multiply(outprice);
                            Object[] row = {feename, map.get("unitname"), outprice, n.multiply(num), map.get("type"), map.get("itemCode"), map.get("itemGrade"), money.setScale(2, 4)};
                            model.addRow(row);
                        }
                        txtSpell.setText("");
                        txtNum.setText("");
                        setTypeAndMoney();
                        btnSave.setEnabled(true);
                        txtSpell.grabFocus();
                    } catch (Exception f) {
                        warning("剂量输入错误！！！");
                    }
                }
            }
        });

    }

    // 按钮监听器
    private void buttonListeners() {
        // 刷新按钮
        btnFresh.addActionListener((ActionEvent e) ->
        {
            setTextFieldText(new String[]{"", "", "", ""});
            cbbHistoryId.setModel(new DefaultComboBoxModel());
            cbbNowId.setModel(new DefaultComboBoxModel());
            textArea1.setText("");
            textArea2.setText("");
            model.setRowCount(0);
            table.setModel(model);
            dose.clear();
            old = 1;
            discriptionMap.clear();
            pid = 0;
            number.setText("1");
            setTypeAndMoney();

            try {
                Connect.sendMessage(buildMessage.doFunction("getIdByPreState", null).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // 删除选中
        btnDelete.addActionListener((ActionEvent e) ->
        {
            if (table.getSelectedRow() == -1) {
                warning(selectOne);
                return;
            }
            if (confirm(askDelete) == JOptionPane.OK_OPTION) {
                int[] index = table.getSelectedRows();
                for (int i = 0; i < index.length; i++) {
                    model.removeRow(index[i] - i);
                }
            } else return;
            setTypeAndMoney();
        });

        // 保存处方
        btnSave.addActionListener((ActionEvent e) ->
        {
            if (!btnSave.isEnabled())
                return;
            if (model.getRowCount() == 0 && pid == 0) {
                warning("新建处方最少需要一种药品");
                return;
            }
            btnSave.setEnabled(false);
            String idname = cbbName.getSelectedItem().toString();
            Map<String, Object> preMap = new HashMap<>();
            // 设置参数
            preMap.put("regid", idname.split("/")[0]);
            // pid 标记当前处方
            preMap.put("pid", pid);
            preMap.put("dose", Integer.valueOf(number.getText()));

            // 添加药品
            StringBuffer medName = new StringBuffer();
            StringBuffer medNumber = new StringBuffer();
            for (int i = 0; i < table.getRowCount(); i++) {
                String name = model.getValueAt(i, 0).toString();
                medName.append(name);
                medName.append("|");

                BigDecimal number = new BigDecimal(model.getValueAt(i, 3).toString()).setScale(2, BigDecimal.ROUND_FLOOR);
                medNumber.append(number);
                medNumber.append("|");
            }
            medName.deleteCharAt(medName.lastIndexOf("|"));
            medNumber.deleteCharAt(medNumber.lastIndexOf("|"));

            preMap.put("medName", medName.toString());
            preMap.put("medNumber", medNumber);

            preMap.put("illness", textArea1.getText());
            preMap.put("discription", textArea2.getText());

            try {
                btnSave.setEnabled(false);
                Connect.sendMessage(buildMessage.doFunction("addPrescriptions", preMap).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            btnFresh.doClick();
            flag = false;
        });
    }

    // 病人信息显示
    private void setTextFieldText(Object[] data) {
        lbDepart.setText("<html><u>" + data[1].toString() + "</u><html>");
        lbDoctor.setText("<html><u>" + data[2].toString() + "</u><html>");
        lbRegType.setText("<html><u>" + data[3].toString() + "</u><html>");
    }

    // 通过拼音码获取药品列表
    private void getMedModel() {
        Map<String, Object> map = new HashMap<>();
        map.put("spell", txtSpell.getText());
        try {
            if (txtSpell.getText() == null || txtSpell.getText().equals("")) {
                hintWindow hintWindow = UI.hintWindow.map.get(txtSpell);
                hintWindow.setVisible(false);
                return;
            } else
                Connect.sendMessage(buildMessage.doFunction("getMedicineBySpell", map).toByteArray());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    // 设置种类和金额
    private void setTypeAndMoney() {
        int row = table.getRowCount();
        lbFeeType.setText(row + "种");
        BigDecimal money = new BigDecimal(0);
        for (int i = 0; i < row; i++) {
            BigDecimal price = new BigDecimal(model.getValueAt(i, 2).toString());
            BigDecimal number = new BigDecimal(model.getValueAt(i, 3).toString());
            money = money.add(price.multiply(number));
        }
        lbMoney.setText(money.setScale(2, BigDecimal.ROUND_HALF_UP) + "元");
    }

    // 更新数量
    private void updateNum(int old, int newly) {
        int all = model.getRowCount();
        for (int i = 0; i < all; i++) {
            BigDecimal n = new BigDecimal(model.getValueAt(i, 3).toString());
            BigDecimal mon = new BigDecimal(model.getValueAt(i, 2).toString());


            n = n.divide(new BigDecimal(old)).multiply(new BigDecimal(newly));
            mon = n.multiply(mon);

            model.setValueAt(n.setScale(2, 4), i, 3);
            model.setValueAt(mon.setScale(2, 4), i, 7);
            setTypeAndMoney();
        }
    }

    private void clear() {

    }


}
