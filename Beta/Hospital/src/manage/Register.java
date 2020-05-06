package manage;

import UI.ComboBoxModel;
import UI.Table;
import UI.hintWindow;
import UI.mTableModel;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import insurance.Insurance;
import insurance.insuranceMessage;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;
import tool.Tool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.registerCn;
import static tool.Strings.*;
import static tool.Tool.isSelected;
import static tool.Tool.warning;
import static tool.XmlOperator.*;

public class Register {
    public JPanel pnRegister;
    private JButton btnSave;
    private JComboBox cbbSex;
    private JComboBox cbbDepart;
    private JComboBox cbbDoctor;
    private JComboBox cbbRegType;
    private JTextField txtFee;
    private JTextField txtName;
    private JTextField txtId;
    private JDatePicker datePicker;
    private boolean selected = false; // 是否第一次挂号
    private int patientId = 0;
    private Table table;
    private JTextField txtPhone;
    private JButton btnExport;
    private JButton analyze;
    private JButton quit;
    private JButton insuranceBtn;
    private JComboBox insuranceType;
    private JDatePicker startDate;
    private JDatePicker endDate;
    private JComboBox cbbDepart2;
    private JComboBox cbbDoctor2;
    private JTextField patientName;
    private JButton btnSelect;
    private mTableModel model;
    private List<String> userInfo;
    private Object doctorId;
    private List<mListener> listeners = new ArrayList<>();
    private Map<String, Integer> departmentMap = new HashMap<>();
    private Map<String, Integer> typeMap = new HashMap<>();
    private Map<Integer, Map<String, Object>> patientMap = new HashMap<>();
    private Map<String, Map<String, Object>> regtype = new HashMap<>();

    // 刷卡得到的信息存在这里
    private String securityId = null;
    private String cardId = null;
    private String areaId = null;
    private String entityCode = null;
    private String entityName = null;
    private int lastId = 0;             // 返回的挂号id

    // 是否修改状态 0 就说不需要进行修改，否则就是进行修改
    private int rgid = 0;

    public Register() {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        textFieldListeners();
        comboBoxListeners();
        buttonListeners();
        txtFee.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (txtFee.getText().equals("0.0")) {
                    txtFee.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (txtFee.getText().equals("")) {
                    txtFee.setText("0.0");
                }
            }
        });
        analyze.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String[] title = {"挂号类型", "挂号总数", "总金额"};
                DefaultTableModel mModel = new DefaultTableModel(title, 0);
                int num = 0;
                BigDecimal sum = BigDecimal.ZERO;
                for (String key : regtype.keySet()) {
                    Vector one = new Vector();
                    Map map = regtype.get(key);
                    num = num + (int) map.get("num");
                    sum = sum.add(new BigDecimal(map.get("sum").toString()));
                    one.add(key);
                    one.add(map.get("num"));
                    one.add(map.get("sum"));
                    mModel.addRow(one);
                }
                mModel.addRow(new Object[]{"总计", num, sum});
                new tool.onlyTable(mModel, "今日挂号数据");
            }
        });


        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 如果双击进入编辑状态
                if (e.getClickCount() == 2) {
                    btnSave.setText("保存修改");
                    quit.setEnabled(true);
                    int row = table.getSelectedRow();
                    String name = model.getValueAt(row, 1).toString();
                    String sex = model.getValueAt(row, 2).toString();
                    String birth = model.getValueAt(row, 3).toString();
                    String phone = model.getValueAt(row, 4).toString();
                    String identity = model.getValueAt(row, 5).toString();
                    txtName.setText(name);
                    datePicker.getFormattedTextField().setText(birth);
                    cbbSex.setSelectedItem(sex);
                    txtPhone.setText(phone);
                    txtId.setText(identity);
                    txtFee.setText(model.getValueAt(row, 10).toString());
                    insuranceType.setEnabled(false);
                    insuranceBtn.setEnabled(false);
                    // 得到点击的挂号id
                    rgid = Integer.parseInt(model.getValueAt(row, 0).toString());
                }
            }
        });
        quit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!quit.isEnabled())
                    return;
                quit.setEnabled(false);
                clear();
            }
        });
        insuranceBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Insurance insurance = Insurance.getInsurance();
                if (insurance.getStatus() < 0)
                    Tool.warning("医保连接失败");
                else {
                    insuranceMessage insuranceMessage = new insuranceMessage();
                    insuranceMessage.setFunctionName("readCard");
                    insuranceBtn.setEnabled(false);
                    Connect.sendMessage(insuranceMessage);
                }
            }
        });

        cbbDepart2.addActionListener(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("departname", cbbDepart2.getSelectedItem());
            cbbDoctor2.setModel(new ComboBoxModel(new String[]{""}));
            try {
                Connect.sendMessage(buildMessage.doFunction("getDrNameByDepartname", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        btnSelect.addActionListener(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("startDate", startDate.getFormattedTextField().getText());
            map.put("endDate",endDate.getFormattedTextField().getText());
            try {
                Connect.sendMessage(buildMessage.doFunction("getSelectRegister", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    // 初始化GUI
    private void initUI() {
        // 初始化表格
        model = new mTableModel(registerCn, 0) {
            public Class<?> getColumnClass(int column) {
                Class<?> returnValue = Object.class;
                if ((column >= 0) && (column < getColumnCount() && getRowCount() > 0)) {
                    for (int i = 0; i < getRowCount(); i++)
                        if (getValueAt(i, column) != null) {
                            returnValue = getValueAt(i, column).getClass();
                            break;
                        }
                }
                return returnValue;
            }
        };
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startDate.getFormattedTextField().setText(simpleDateFormat.format(date));
        endDate.getFormattedTextField().setText(simpleDateFormat.format(date));
        table.setModel(model);
        txtName.grabFocus();
        // 获取操作员信息
        userInfo = getText(getElements(getRoot(readXml(userPath))));
        // 获取全部药品信息
        try {
            Connect.sendMessage(buildMessage.doFunction("getTodayRegister", null).toByteArray());
            Map<String, Object> map = new HashMap<>();
            map.put("table", "department");
            map.put("functionName", "department");
            Connect.sendMessage(buildMessage.doFunction("getTable", map).toByteArray());
            map.clear();
            map.put("table", "regtype");
            map.put("functionName", "regtype");
            Connect.sendMessage(buildMessage.doFunction("getTable", map).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void messageListeners() {
        listeners.add(new mListener("department") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                        departmentMap.put((String) map.get("name"), (Integer) map.get("id"));
                    }
                    cbbDepart.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                    cbbDepart2.setModel(getColumnDataComboBoxModel("所有", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getSelectRegister") {
            @Override
            public void messageEvent(MessageEvent event) {
                model.setRowCount(0);
                table.setModel(model);
                securityId = null;
                areaId = null;
                cardId = null;
                lastId = 0;
                rgid = 0;
                myMessage.feedback feedback = event.getMessage();
                regtype.clear();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        Integer id = Integer.valueOf(map.get("id").toString());
                        Object name = map.get("patientname");
                        Object sex = map.get("sex");
                        Object phone = map.get("phone");
                        Object birth = map.get("birth").toString().split(" ")[0];
                        Object identity = map.get("identity");
                        Object departname = map.get("departname");
                        Object doctorname = map.get("doctorname");
                        Object regtypename = map.get("regtypename");
                        Object insraunceType = map.get("insuranceType");
                        BigDecimal fee = new BigDecimal(map.get("fee").toString());
                        Object first = map.get("first");
                        Object positionname = map.get("positionname");
                        Object date = map.get("date");
                        String insurance_type = null;
                        if (insraunceType.equals("")) {
                            insurance_type = "";
                        } else if (insraunceType.equals("11")) {
                            insurance_type = "普通门诊";
                        } else
                            insraunceType = "学生统筹门诊";

                        if(check(doctorname.toString(),departname.toString(),name.toString())){
                            if (regtype.get(regtypename.toString()) == null) {
                                Map<String, Object> map1 = new HashMap();
                                BigDecimal sum = new BigDecimal(map.get("fee").toString());
                                map1.put("sum", sum);
                                map1.put("num", 1);
                                regtype.put(regtypename.toString(), map1);
                            } else {
                                Map re = regtype.get(regtypename.toString());
                                BigDecimal sum = new BigDecimal(re.get("sum").toString());
                                sum = sum.add(fee);
                                int num = (int) re.get("num");
                                num = num + 1;
                                re.put("sum", sum);
                                re.put("num", num);
                            }
                            model.addRow(new Object[]{id, name, sex, birth, phone, identity, departname, doctorname, regtypename, insurance_type, fee, first, positionname, date});
                        }
                    }
                    table.setModel(model);
                }
            }
        });

        listeners.add(new mListener("regtype") {
            public void messageEvent(MessageEvent event) {

                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                        typeMap.put((String) map.get("name"), (Integer) map.get("id"));
                    }
                    cbbRegType.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getTodayRegister") {
            public void messageEvent(MessageEvent event) {
                model.setRowCount(0);
                table.setModel(model);
                securityId = null;
                areaId = null;
                cardId = null;
                lastId = 0;
                rgid = 0;
                myMessage.feedback feedback = event.getMessage();
                regtype.clear();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        Integer id = Integer.valueOf(map.get("id").toString());
                        Object name = map.get("patientname");
                        Object sex = map.get("sex");
                        Object phone = map.get("phone");
                        Object birth = map.get("birth").toString().split(" ")[0];
                        Object identity = map.get("identity");
                        Object departname = map.get("departname");
                        Object doctorname = map.get("doctorname");
                        Object regtypename = map.get("regtypename");
                        Object insraunceType = map.get("insuranceType");
                        BigDecimal fee = new BigDecimal(map.get("fee").toString());
                        Object first = map.get("first");
                        Object positionname = map.get("positionname");
                        Object date = map.get("date");
                        String insurance_type = null;
                        if (insraunceType.equals("")) {
                            insurance_type = "";
                        } else if (insraunceType.equals("11")) {
                            insurance_type = "普通门诊";
                        } else
                            insraunceType = "学生统筹门诊";

                        if (regtype.get(regtypename.toString()) == null) {
                            Map<String, Object> map1 = new HashMap();
                            BigDecimal sum = new BigDecimal(map.get("fee").toString());
                            map1.put("sum", sum);
                            map1.put("num", 1);
                            regtype.put(regtypename.toString(), map1);
                        } else {
                            Map re = regtype.get(regtypename.toString());
                            BigDecimal sum = new BigDecimal(re.get("sum").toString());
                            sum = sum.add(fee);
                            int num = (int) re.get("num");
                            num = num + 1;
                            re.put("sum", sum);
                            re.put("num", num);
                        }
                        model.addRow(new Object[]{id, name, sex, birth, phone, identity, departname, doctorname, regtypename, insurance_type, fee, first, positionname, date});
                    }
                    table.setModel(model);
                }
            }
        });

        listeners.add(new mListener("getPatientInfo") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        cbbSex.setSelectedItem(map.get("sex"));
                        txtPhone.setText((String) map.get("phone"));
                        datePicker.getFormattedTextField().setText(map.get("birth").toString().split(" ")[0]);
                        txtId.setText((String) map.get("identity"));
                    }
                }
            }
        });

        listeners.add(new mListener("getDrNameByDepartname") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    String departName = "";
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        list.add((String) map.get("drname"));
                        departName = map.get("departname").toString();
                    }
                    if(cbbDepart.getSelectedItem().toString().equals(departName))
                        cbbDoctor.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                    if(cbbDepart2.getSelectedItem().toString().equals(departName))
                        cbbDoctor2.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("addPatient") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    warning(addFail);
                }
            }
        });

        listeners.add(new mListener("getDoctorIdByName") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                List<Any> anies = feedback.getDetailsList();
                for (Any temp : anies) {
                    Map map = buildMessage.AnyToMap(temp);
                    doctorId = map.get("id");
                }
            }
        });

        listeners.add(new mListener("insertRegister") {
            public void messageEvent(MessageEvent event) {

                btnSave.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    // 判断是否医保挂号，通过变量值来
                    if (securityId != null && areaId != null && cardId != null) {
                        //  医保挂号，向医保服务器发送请求
                        insuranceMessage message = new insuranceMessage();
                        message.setFunctionName("clinicRegister");
                        message.addElement(1, securityId);
                        message.addElement(2, txtName.getText());
                        message.addElement(3, cardId);
                        message.addElement(4, areaId);
                        if (insuranceType.getSelectedIndex() == 0) {
                            message.addElement(5, "11");
                        } else {
                            message.addElement(5, "71");
                        }
                        message.addElement(6, null);
                        message.addElement(7, 0);
                        Date date = new Date();
                        message.addElement(8, insuranceMessage.formatDate(date));
                        message.addElement(9, insuranceMessage.formatTime(date));
                        message.addElement(10, feedback.getMark());
                        message.doFunction();
                        lastId = feedback.getMark();
                        return;
                    }
                    warning("挂号成功！！！");
                    // 刷新挂号的详情页
                    Connect.sendMessage(buildMessage.doFunction("getTodayRegister", null).toByteArray());
                } else {
                    warning(saveFail);
                }
                rgid = 0;
                setComponentText();
                cbbSex.setEnabled(true);
                datePicker.getButton().setEnabled(true);
                txtId.setEditable(true);
                selected = false;
                patientId = 0;
            }
        });

        listeners.add(new mListener("getPatientBySpell") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> list = feedback.getDetailsList();
                    List<String> hint = new ArrayList<>();
                    for (Any any : list) {
                        Map temp = buildMessage.AnyToMap(any);
                        int id = (Integer) temp.get("id");
                        patientMap.put(id, temp);
                        String date = (String) temp.get("date");
                        String birth = (String) temp.get("birth");
                        birth = birth.substring(0, 4);
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int age = Integer.valueOf(birth);
                        age = year - age;
                        hint.add(temp.get("name") + "/" + temp.get("sex") + "/" + age + "岁/" + date.split(" ")[0] + "/" + id);
                    }
                    if (list.size() != 0) {
                        hintWindow hintWindow = UI.hintWindow.map.get(txtName);
                        hintWindow.updateList(hint.toArray(new String[list.size()]));
                    } else {
                        selected = true;
                        hintWindow hintWindow = UI.hintWindow.map.get(txtName);
                        hintWindow.setVisible(false);
                    }
                }
            }
        });

        listeners.add(new mListener("selectPatientByPhoneOrId") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                List<Any> anies = feedback.getDetailsList();
                if (anies.size() == 1) {
                    Any any = anies.get(0);
                    Map map = buildMessage.AnyToMap(any);
                    if (map.get("id") == null) {
                        warning("没有病人信息！");
                    } else {
                        patientId = (int) map.get("id");
                        txtName.grabFocus();
                        txtName.setText(map.get("name").toString());
                        datePicker.getFormattedTextField().setText((String) map.get("birth"));
                        txtPhone.setText((String) map.get("phone"));
                        txtId.setText((String) map.get("identity"));
                        String sex = (String) map.get("sex");
                        if (sex != null)
                            if (sex.equals("男"))
                                cbbSex.setSelectedItem("男");
                            else
                                cbbSex.setSelectedItem("女");
                    }
                } else {
                    txtName.setText("");
                    warning("没有磁卡信息!!!");
                    txtName.grabFocus();
                }

            }
        });

        listeners.add(new mListener("editRegister") {
            public void messageEvent(MessageEvent event) {

                btnSave.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    Connect.sendMessage(buildMessage.doFunction("getTodayRegister", null).toByteArray());
                    warning(saveSuccess);
                } else {
                    warning(saveFail);
                }
                clear();
            }
        });

        // 刷卡事件回调接口
        listeners.add(new mListener("readCard") {
            @Override
            public void messageEvent(MessageEvent event) {
                insuranceMessage message = event.getMessage(insuranceMessage.class);
                if (message.getMark() > 0) {
                    Tool.warning("读卡成功");
                    Tool.warning("用户余额：" + message.getOutElement(1, 14) + "\n卡状态：" + message.getOutElement(1, 13));
                    securityId = message.getOutElement(1, 1);
                    txtName.setText(message.getOutElement(1, 2));
                    cardId = message.getOutElement(1, 3);
                    areaId = message.getOutElement(1, 4);
                    datePicker.getFormattedTextField().setText(message.getOutElement(1, 6));
                    txtId.setText(message.getOutElement(1, 18));
                    if (message.getOutElement(1, 11).equals("男"))
                        cbbSex.setSelectedIndex(1);
                    else
                        cbbSex.setSelectedIndex(2);
                } else
                    Tool.warning("刷卡失败：" + message.getBackMessage());

                insuranceBtn.setEnabled(true);
            }
        });

        // 医保挂号回调
        listeners.add(new mListener("clinicRegister") {
            @Override
            public void messageEvent(MessageEvent event) {
                // 整体顺序，。。先挂号，返回挂号id，判断是否医保挂号，是医保挂号就向医保发送挂号命令，然后。。。现在返回到这里，再向本地服务器发送，挂号成功，修改挂号信息
                insuranceMessage message = event.getMessage(insuranceMessage.class);
                if (message.getMark() > 0) {
                    // 更新数据库挂号信息
                    Map<String, Object> map = new HashMap();
                    map.put("id", String.valueOf(lastId));
                    map.put("securityId", securityId);
                    map.put("areaId", areaId);
                    map.put("cardId", cardId);
                    if (insuranceType.getSelectedIndex() == 0) {
                        map.put("insuranceType", "11");
                        entityCode = "NULL";
                        entityName = "NULL";
                    } else {
                        map.put("insuranceType", "71");
                        entityName = "学生统筹门诊";
                        entityCode = "000001000021";
                    }
                    map.put("entityCode", entityCode);
                    map.put("entityName", entityName);
                    map.put("clinicNumber", event.getMessage(insuranceMessage.class).getOutElement(1, 15));
                    Connect.sendMessage(buildMessage.doFunction("insuranceRegister", map).toByteArray());
                } else {
                    warning("医保挂号失败");
                    clear();
                }

            }
        });


        // 医保挂号后，修改挂号信息回调
        listeners.add(new mListener("insuranceRegister") {
            @Override
            public void messageEvent(MessageEvent event) {
                if (event.getMessage().getMark() > 0) {
                    warning("医保挂号成功！！！");

                } else
                    warning("没有成功用医保挂号，但是本地挂号成功");

                Connect.sendMessage(buildMessage.doFunction("getTodayRegister", null).toByteArray());
                clear();
            }
        });
    }


    // 文本框监听器
    private void textFieldListeners() {
        // 输入姓名
        new hintWindow(txtName) {
            @Override
            public void keyTyped(KeyEvent e) {

                StringBuilder stringBuilder = new StringBuilder();
                char temp = e.getKeyChar();
                int charCode = Integer.valueOf(temp);
                // 中文 回车 退格 符进行判断
                if (charCode > 255 || charCode == 8 || charCode == 10) {
                    if (charCode == 8 && txtName.getText().length() == 0) {

                        datePicker.getFormattedTextField().setText(null);
                        cbbSex.setSelectedIndex(0);
                        txtPhone.setText("");
                        txtId.setText("");
                        patientId = 0;
                    }
                    if (charCode != 10) {
                        if (patientId != 0 || rgid != 0)
                            return;
                        if (charCode > 255)
                            stringBuilder.append(txtName.getText() + temp);
                        else if (charCode == 8)
                            stringBuilder.append(txtName.getText());
                        String name = stringBuilder.toString();
                        Map<String, Object> map = new HashMap<>();
                        map.put("spell", name);
                        System.out.println(name);
                        if (!name.equals("")) {
                            Connect.sendMessage(buildMessage.doFunction("getPatientBySpell", map).toByteArray());
                            selected = false;
                        } else
                            hintWindow.map.get(txtName).setVisible(false);
                    } else {
                        if (txtName.getText() != null && !txtName.getText().equals(""))
                            datePicker.getFormattedTextField().grabFocus();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            protected void setText(String selectedValue) {
                if (selectedValue != null) {
                    String[] temp = selectedValue.split("/");
                    if (temp.length == 5) {
                        int id = Integer.parseInt(temp[4]);
                        patientId = id;
                        Map map = patientMap.get(id);
                        if (map != null) {
                            txtName.setText((String) map.get("name"));
                            txtPhone.setText((String) map.get("phone"));
                            String date = (String) map.get("birth");
                            datePicker.getFormattedTextField().setText(date.split(" ")[0]);
                            String sex = (String) map.get("sex");
                            if (sex.equals("男"))
                                cbbSex.setSelectedIndex(1);
                            else
                                cbbSex.setSelectedIndex(2);
                            txtId.setText((String) map.get("identity"));
                        }
                    }

                } else {
                    patientId = 0;
                    datePicker.getFormattedTextField().grabFocus();
                }
            }
        };

        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String telRegex = "[0-9]{11}";
                    String vipRegex = "[0-9]{6}";
                    String number = txtName.getText();
                    if (number == null || number.equals(""))
                        return;
                    if (number.matches(telRegex) || number.matches(vipRegex)) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", number);
                        m.put("phone", number);
                        Connect.sendMessage(buildMessage.doFunction("selectPatientByPhoneOrId", m).toByteArray());
                    }
                    datePicker.getFormattedTextField().grabFocus();
                }
            }
        });

        datePicker.getFormattedTextField().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    String text = datePicker.getFormattedTextField().getText();
                    try {
                        int age = Integer.parseInt(text);
                        if (age > 150) {
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmdd");
                            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-mm-dd");
                            Date date = simpleDateFormat.parse(text);
                            datePicker.getFormattedTextField().setText(simpleDateFormat1.format(date));

                        } else {
                            Calendar date = Calendar.getInstance();
                            String year = String.valueOf(date.get(Calendar.YEAR));
                            Integer birth = Integer.parseInt(year);
                            birth = birth - age;
                            datePicker.getFormattedTextField().setText(birth + "-1-1");
                        }


                    } catch (Exception e1) {

                    }

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
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtId.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    datePicker.getFormattedTextField().grabFocus();
                }
            }
        });

        txtId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtFee.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtPhone.grabFocus();
                }
            }
        });

        txtFee.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    txtName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    txtId.grabFocus();
                }
            }
        });
    }

    // 下拉菜单列表监听器
    private void comboBoxListeners() {
        // 选择部门
        cbbDepart.addActionListener((ActionEvent e) ->
        {
            Map<String, Object> map = new HashMap<>();
            map.put("departname", cbbDepart.getSelectedItem());
            cbbDoctor.setModel(new ComboBoxModel(new String[]{""}));
            try {
                Connect.sendMessage(buildMessage.doFunction("getDrNameByDepartname", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // 选择医生
        cbbDoctor.addActionListener((ActionEvent e) ->
        {
            Map<String, Object> map = new HashMap<>();
            map.put("name", cbbDoctor.getSelectedItem());
            try {
                Connect.sendMessage(buildMessage.doFunction("getDoctorIdByName", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    // 按钮监听器
    private void buttonListeners() {
        // 保存添加
        btnSave.addActionListener((ActionEvent e) ->
        {
            btnSave.setEnabled(false);
            // 验证输入
            if (!judge()) {
                warning(inputError);
                btnSave.setEnabled(true);
                return;
            }
            // 添加挂号记录
            Object departId = departmentMap.get(cbbDepart.getSelectedItem());
            Object regTypeId = typeMap.get(cbbRegType.getSelectedItem());
            Map<String, Object> maps = new HashMap<>();


            maps.put("patientname", txtName.getText());
            maps.put("sex", cbbSex.getSelectedItem());
            maps.put("birth", datePicker.getFormattedTextField().getText());
            maps.put("patientId", patientId);
            maps.put("phone", txtPhone.getText());
            maps.put("identity", txtId.getText());
            maps.put("departmentId", departId);
            maps.put("doctorId", doctorId);
            maps.put("regtypeid", regTypeId);
            maps.put("fee", txtFee.getText());
            maps.put("first", isSelected(this.selected));

            try {
                btnSave.setEnabled(false);
                // 为0 就是一个新的挂号，否则就是一个修改挂号
                if (rgid == 0) {
                    Connect.sendMessage(buildMessage.doFunction("insertRegister", maps).toByteArray());
                } else {
                    maps.put("rgid", rgid);
                    Connect.sendMessage(buildMessage.doFunction("editRegister", maps).toByteArray());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        btnExport.addActionListener((ActionEvent e) ->
        {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "今日挂号记录", registerCn, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    // 设置部分组件的文字
    private void setComponentText() {
        txtName.setText("");
        cbbSex.setSelectedItem("");
        datePicker.getFormattedTextField().setText("");
        txtPhone.setText("");
        txtId.setText("");
        cbbDepart.setSelectedItem("");
        cbbDoctor.setSelectedItem("");
        cbbRegType.setSelectedItem("");
        txtFee.setText("0.00");
        rgid = 0;
        patientId = 0;
        quit.setEnabled(false);
        insuranceBtn.setEnabled(true);
        insuranceType.setEnabled(true);
    }

    // 获取用户输入
    private boolean judge() {
        try {
            String name = txtName.getText();
            Object sex = cbbSex.getSelectedItem();
            String date = datePicker.getFormattedTextField().getText();
            String phone = txtPhone.getText();
            Object department = cbbDepart.getSelectedItem();
            Object doctor = cbbDoctor.getSelectedItem();
            Object regtype = cbbRegType.getSelectedItem();
            BigDecimal fee = new BigDecimal(txtFee.getText());
            String id = txtId.getText();
            String selected = isSelected(this.selected);
            String user = userInfo.get(0);

            boolean mark = true;
            if (phone.length() != 11 && phone.length() != 0)
                mark = false;
            if (id.length() != 18 && id.length() != 0)
                mark = false;
            if (name == null || name.equals("") || sex == null || sex.equals("") || date == null || date.equals("") || department == null || department.equals(""))
                mark = false;
            if (doctor.equals("") || regtype.equals("") || fee.equals("") || selected.equals("") || user.equals(""))
                mark = false;
            return mark;
        } catch (Exception e) {
            return false;
        }
    }

    // 清空信息
    public void clear() {
        rgid = 0;
        cbbSex.setEnabled(true);
        datePicker.getButton().setEnabled(true);
        txtId.setEditable(true);
        quit.setEnabled(false);
        selected = false;
        patientId = 0;
        insuranceType.setSelectedIndex(0);
        areaId = null;
        cardId = null;
        securityId = null;
        btnSave.setText("确认挂号");
        entityCode = null;
        entityName = null;
        lastId = 0;

        setComponentText();

    }

    // 验证函数
    private boolean check(String docname, String depname, String pname) {
        Object cbbdoc = cbbDoctor2.getSelectedItem();
        Object cbbdep = cbbDepart2.getSelectedItem();
        String patient = patientName.getText();


        if (cbbdep == null || cbbdep.equals("") || cbbdep.equals("所有") || cbbdep.equals(depname))
            if (cbbdoc == null || cbbdoc.equals("") || cbbdoc.equals("全部") || cbbdoc.equals(docname))
                if (patient == null || patient.equals("") || patient.equals(pname))
                    return true;
                else
                    return false;
            else
                return false;
        else
            return false;


    }
}
