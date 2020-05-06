package manage;

import UI.Table;
import com.google.protobuf.Any;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import event.MessageEvent;
import event.mListener;
import insurance.insuranceMessage;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.Print;
import tool.ShowMessage;
import tool.Tool;
import tool.UserInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static event.messageManager.removeAllMessageListener;
import static tool.Headers.chargeDetailCn;
import static tool.Headers.preList;
import static tool.Strings.*;
import static tool.Tool.warning;

/*
医保收费流程
1. 向服务器查询当前订单是否有医保收费历史记录
   1.1 如果有医保收费记录那么就直接赋值医保支付
   1.2 如果没有医保收费记录，那么就进行医保预支付
2. 确保支付金额累计等于总金额
3. 向本地服务器进行预支付
   3.1 如果预支付成功，那就进行医保刷卡，进行支付，医保刷卡失败则终止支付
   3.2 预支付失败，提示为什么失败，终止交易
 */
public class Charge {
    public static Password password = null;
    public JPanel pnCharge;
    private JComboBox cbbId;
    private JButton btnFresh;
    private Table table;
    private JLabel lbFeeType;
    private JLabel lbMoney;
    private JTextField txtShould;
    private JTextField txtNot;
    private JCheckBox chkNonVip;
    private JCheckBox chkVip;
    private JTextField txtNonVipCash;
    private JTextField txtVipCard;
    private JTextField txtRecharge;
    private JTextField txtPresent;
    private JTextField txtMedDiscount;
    private JTextField txtCureDiscount;
    private JPanel pnCash;
    private JPanel pnVip;
    private JTextField txtDiscount;
    private JCheckBox chkDiscount;
    private JCheckBox chkNoDiscount;
    private JTextField txtVipCash;
    private JButton btnOkCharge;
    private JTextField txtNonVipOther;
    private JTextField txtVipOther;
    private JTextField txtDiscountCharge;
    private JTextField txtNonDiscountCharge;
    private JTextField txtCashCharge;
    private JTextField txtOtherCharge;
    private JLabel lbDiscountCharge;
    private JLabel lbNonDiscountCharge;
    private JLabel lbDepart;
    private JLabel lbDoctor;
    private JLabel lbRegType;
    private JButton ReChargeButton;
    private JTable listTable;
    private JTextField txtInsuranceCharge;
    private JLabel labStatus;
    private JTextField textField1;
    private JPanel statusLab;
    private DefaultTableModel model;
    private DefaultTableModel listModel;

    // 支付参数，打折情况和药品，诊疗总金额
    private BigDecimal meddiscount = new BigDecimal(100.00);
    private BigDecimal curediscount = new BigDecimal(100.00);
    private BigDecimal medMon = new BigDecimal(0);
    private BigDecimal cureMon = new BigDecimal(0);

    // 支付详情
    private BigDecimal cash = new BigDecimal(0); // 现金
    private BigDecimal other = new BigDecimal(0); // 其他
    private BigDecimal remaincharge = new BigDecimal(0); // 卡内可打折消费
    private BigDecimal presentcharge = new BigDecimal(0); // 卡内不可打折消费
    private BigDecimal insurancecharge = new BigDecimal(0);

    // 会员卡信息
    private int vipId = 0;
    private BigDecimal remain = new BigDecimal(0); // 卡内本金余额
    private BigDecimal present = new BigDecimal(0);// 卡内赠送余额
    private BigDecimal vipDiscount = new BigDecimal(100);

    // 总金额和剩余未支付金额
    private BigDecimal sum = new BigDecimal(0);
    private BigDecimal left = new BigDecimal(0);


    private List<mListener> listeners = new ArrayList<>();
    private Map<String, Object> patientMap = new HashMap<>();
    private String selectId;        // 选择的挂号id


    // 用户信息变量
    private String p_name;
    private String p_securityId;
    private String p_cardId;
    private String p_areaId;
    private String p_clinicNumber;  // 挂号单号
    private String p_doctorName;
    private String p_departName;
    private String p_chargeNumber;  // 流水单号
    private String p_entityCode = "";
    private String p_entityName = "";
    private String p_illness;

    // 储存处方详情, 一个处方id 对应一个药品List ， 一个药品list对应一个药品详情Map
    private Map<Integer, List<Map<String, String>>> preMap = new HashMap<>();
    // 选中的 处方编号
    private int pid = 0;
    // 医保是否支付
    private boolean isInsuranceCharge = false;

    public Charge() {
        removeAllMessageListener();
        messageListeners();
        initUI();
        buttonListeners();
        textFieldListeners();
        comboBoxListeners();
        checkBoxListeners();

        txtNonVipCash.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        BigDecimal temp = new BigDecimal(txtNonVipCash.getText());

                        // 判断，总金额大于等于
                        if (sum.subtract(cash).subtract(other).subtract(remaincharge).subtract(presentcharge).subtract(insurancecharge).subtract(temp).compareTo(BigDecimal.ZERO) >= 0 && cash.add(temp).compareTo(BigDecimal.ZERO) >= 0)
                            cash = cash.add(temp);
                        else
                            throw new NumberFormatException();
                        setMoney();
                        txtNonVipOther.grabFocus();
                    } catch (NumberFormatException w) {
                        Tool.warning("输入错误！");
                    }

                }
            }
        });

        txtNonVipOther.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {

                        BigDecimal temp = new BigDecimal(txtNonVipOther.getText());
                        if (sum.subtract(cash).subtract(other).subtract(remaincharge).subtract(presentcharge).subtract(insurancecharge).subtract(temp).compareTo(BigDecimal.ZERO) >= 0 && other.add(temp).compareTo(BigDecimal.ZERO) >= 0)
                            other = other.add(temp);
                        else
                            throw new NumberFormatException();
                        setMoney();
                        btnOkCharge.grabFocus();
                    } catch (NumberFormatException w) {
                        Tool.warning("输入错误！");
                    }

                }
            }
        });
        txtVipCard.addMouseListener(new MouseAdapter() {
        });
        txtVipCard.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        int length = txtVipCard.getText().length();
                        if (length <= 6 && length > 0 || length == 11) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("id", txtVipCard.getText());
                            map.put("phone", txtVipCard.getText());
                            Connect.sendMessage(buildMessage.doFunction("selectVipByPhoneOrId", map).toByteArray());
                        } else
                            Tool.warning("输入错误！");

                    } catch (NumberFormatException w) {
                        Tool.warning("输入错误！");
                    }

                }
                setMoney();
            }
        });

        txtVipCash.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {

                        BigDecimal temp = new BigDecimal(txtVipCash.getText());
                        if (sum.subtract(cash).subtract(other).subtract(remaincharge).subtract(presentcharge).subtract(insurancecharge).subtract(temp).compareTo(BigDecimal.ZERO) >= 0 && cash.add(temp).compareTo(BigDecimal.ZERO) >= 0)
                            cash = cash.add(temp);
                        else
                            throw new NumberFormatException();
                        setMoney();
                        txtVipOther.grabFocus();
                    } catch (NumberFormatException w) {
                        Tool.warning("输入错误！");
                    }
                }
            }
        });
        txtVipOther.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        BigDecimal temp = new BigDecimal(txtVipOther.getText());
                        if (sum.subtract(cash).subtract(other).subtract(remaincharge).subtract(presentcharge).subtract(insurancecharge).subtract(temp).compareTo(BigDecimal.ZERO) >= 0 && other.add(temp).compareTo(BigDecimal.ZERO) >= 0)
                            other = other.add(temp);
                        else
                            throw new NumberFormatException();
                        setMoney();
                        btnOkCharge.grabFocus();
                    } catch (NumberFormatException w) {
                        Tool.warning("输入错误！");
                    }
                }
            }
        });

        ReChargeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                btnOkCharge.setEnabled(true);
                // 金额初始化
                cash = new BigDecimal(0);
                other = new BigDecimal(0);
                remaincharge = new BigDecimal(0);
                presentcharge = new BigDecimal(0);
                insurancecharge = new BigDecimal(0);

                //初始化
                meddiscount = new BigDecimal(100.00);
                curediscount = new BigDecimal(100.00);
                chkDiscount.setSelected(false);
                chkNoDiscount.setSelected(false);
                txtCashCharge.setText("0.00");
                txtOtherCharge.setText("0.00");
                txtVipCash.setText("0.00");
                txtVipOther.setText("0.00");
                txtNonVipCash.setText("0.00");
                txtNonVipOther.setText("0.00");
                txtInsuranceCharge.setText("0.00");
                txtRecharge.setText(String.valueOf(remain));
                txtPresent.setText(String.valueOf(present));
                textField1.setText("");
                setMoney();
            }
        });

        txtMedDiscount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    chkVip.grabFocus();
                }
            }
        });

        txtCureDiscount.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    chkNonVip.grabFocus();
            }
        });

        listTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pid = (int) listModel.getValueAt(listTable.getSelectedRow(), 0);

                if (listTable.getSelectedRows().length > 1 && p_clinicNumber != null) {
                    Tool.warning("医保挂号不允许多张处方同收费！");
                    return;
                }
                // 获取点击的处方详情
                Map map = new HashMap();
                map.put("prescriptionid", pid);
                Connect.sendMessage(buildMessage.doFunction("getPreInfoByPreId", map).toByteArray());
            }
        });

        textField1.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        BigDecimal deMon;
                        if (textField1.getText().equals(""))
                            deMon = BigDecimal.ZERO;
                        else
                            deMon = new BigDecimal(textField1.getText());

                        if (deMon.compareTo(sum) > 0)
                            throw new Exception();
                        meddiscount = new BigDecimal(100.00);
                        curediscount = new BigDecimal(100.00);
                        setTypeMoney();
                        if (deMon.compareTo(BigDecimal.ZERO) == 0) {
                            meddiscount = new BigDecimal(100.00);
                            curediscount = new BigDecimal(100.00);
                            txtAlreadySetText(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                            setTypeMoney();
                        } else {
                            BigDecimal dis = deMon.divide(sum, 4, BigDecimal.ROUND_UP);
                            dis = BigDecimal.ONE.subtract(dis).multiply(new BigDecimal(100));
                            meddiscount = dis.setScale(2,BigDecimal.ROUND_FLOOR);
                            curediscount = dis.setScale(2,BigDecimal.ROUND_FLOOR);
                            txtAlreadySetText(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                            setTypeMoney();
                        }
                        preCharge();
                    } catch (Exception e1) {
                        warning("请输入合法的立减金额！");
                        meddiscount = new BigDecimal(100.00);
                        curediscount = new BigDecimal(100.00);
                        setTypeMoney();
                    }
                }
            }
        });
    }

    private void initUI() {
        // 初始化表格
        model = new DefaultTableModel(chargeDetailCn, 0);
        table.setModel(model);

        listModel = new DefaultTableModel(preList, 0);
        listTable.setModel(listModel);

        // 初始化可以收费单据编号
        try {
            Connect.sendMessage(buildMessage.doFunction("getIdByChargestate", null).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(chkVip);
        buttonGroup.add(chkNonVip);
    }

    private void messageListeners() {

        listeners.add(new mListener("getIdByChargestate") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        list.add(map.get("id") + "/" + map.get("patientname"));
                        patientMap.put(String.valueOf(map.get("id")), map);
                    }
                    cbbId.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getRegiseterDetailById") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 1) {
                    List<Any> anies = feedback.getDetailsList();
                    Any temp = anies.get(0);

                    Map<String, Object> map = buildMessage.AnyToMap(temp);
                    // 储存得到的用户信息
                    p_name = (String) map.get("patientname");
                    p_departName = (String) map.get("departname");
                    p_doctorName = (String) map.get("doctorname");
                    p_securityId = map.get("securityId").toString();
                    p_areaId = map.get("areaId").toString();
                    p_cardId = map.get("cardId").toString();
                    p_clinicNumber = map.get("clinicNumber").toString();
                    String regtypename = (String) map.get("regtypename");
                    BigDecimal fee = new BigDecimal(map.get("fee").toString());
                    p_illness = map.get("illness").toString();
                    p_entityCode = map.get("entityCode").toString();
                    p_entityName = map.get("entityName").toString();
                    p_illness = map.get("illness").toString();

                    setTextFieldText(new Object[]{p_name, p_departName, p_doctorName, regtypename, fee});

                    // 判断是否医保挂号
                    if ((p_securityId == null && p_cardId == null && p_areaId == null) || !p_securityId.equals("") || !p_cardId.equals("") || !p_areaId.equals(""))
                        isInsuranceCharge = true;

                } else {
                    setTextFieldText(new String[]{"", "", "", ""});
                    model.setRowCount(0);
                }
            }
        });


        listeners.add(new mListener("getPreInfoByPreId") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() > 0) {
                    model.setRowCount(0);
                    List<Any> anies = feedback.getDetailsList();

                    Map<Integer, Map<String, String>> aPre = new HashMap<>();

                    int pid = 0;
                    List<Map<String, String>> mapList = new LinkedList<>();
                    // 遍历加入右侧列表
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        // 设置处方id
                        pid = (int) map.get("prescriptionid");

                        // 加入到Table中
                        Object name = map.get("name");
                        Object unitname = map.get("unitname");
                        BigDecimal outprice = new BigDecimal(map.get("outprice").toString());
                        BigDecimal number = new BigDecimal(map.get("number").toString());
                        Object type = map.get("type");
                        model.addRow(new Object[]{name, unitname, outprice, number, type, map.get("itemCode"), map.get("itemGrade"), outprice.multiply(number).setScale(2, 4)});

                        // 药品详情map
                        Map<String, String> med = new HashMap<>();
                        med.put("medId", map.get("medid").toString());
                        med.put("name", map.get("name").toString());
                        med.put("name2", map.get("name2").toString());
                        med.put("unitname", map.get("unitname").toString());
                        med.put("outprice", outprice.setScale(2, BigDecimal.ROUND_FLOOR).toString());
                        med.put("number", number.setScale(2, BigDecimal.ROUND_FLOOR).toString());
                        med.put("type", type.toString());
                        med.put("itemCode", map.get("itemCode").toString());
                        med.put("itemGrade", map.get("itemGrade").toString());
                        mapList.add(med);   // 加入List
                    }
                    // 加入到mapList中，如果已经有则会覆盖
                    preMap.put(pid, mapList);
                    table.setModel(model);
                    initChargePanel();
                    setTypeMoney();

                    // 如果是医保收费才进行查询，否则没事了
                    if (isInsuranceCharge) {
                        Map map = new HashMap();
                        map.put("pid", pid);
                        txtInsuranceCharge.setText("正在查询是否有本处方收费记录...");
                        labStatus.setText("正在查询是否有本处方收费记录...");
                        // 查询本地是否有医保收费记录
                        Connect.sendMessage(buildMessage.doFunction("getInsuranceHistory", map).toByteArray());
                    }
                }
            }
        });


        listeners.add(new mListener("getInsuranceHistory") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                boolean mark = false;
                if (feedback.getMark() > 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any any : anies) {
                        Map map = buildMessage.AnyToMap(any);
                        if (map.get("useful").toString().equals("false"))
                            continue;
                        mark = true;
                        BigDecimal sum = new BigDecimal(map.get("zd26").toString());
                        BigDecimal userCash = new BigDecimal(map.get("zd28").toString());
                        insurancecharge = sum.subtract(userCash);
                        setMoney();
                    }
                }
                // 如果没有历史收费记录医保预收费
                if (!mark)
                    preCharge();
                else {
                    labStatus.setText("查询到本地医保收费记录！！");
                    warning("查询到本地有条该处方收费记录，将不会进行重复刷卡扣费！！");
                }

            }
        });

        listeners.add(new mListener("Account") {
            public void messageEvent(MessageEvent event) {
                btnOkCharge.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                // 预收费返回
                if (insurancecharge.compareTo(BigDecimal.ZERO) > 0 && p_chargeNumber == null) {
                    if (feedback.getMark() > 0) {
                        labStatus.setText("本地系统预收费成功！...");
                        // 进行正式收费
                        iCharge();
                    } else {
                        labStatus.setText("本地系统预收费失败...");
                        warning(feedback.getBackMessage());
                        btnFresh.doClick();
                        initChargePanel();
                        chkNonVip.doClick();
                    }

                } else {
                    // 收费返回
                    if (feedback.getMark() > 0) {
                        labStatus.setText("支付成功！！！交易完成");
                        warning("支付成功！！");
                        setPrint();
                        btnFresh.doClick();
                        initChargePanel();
                        chkNonVip.doClick();
                    } else {
                        warning(feedback.getBackMessage());
                        btnFresh.doClick();
                        initChargePanel();
                        chkNonVip.doClick();
                    }
                }
            }
        });

        listeners.add(new mListener("selectVipByPhoneOrId") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        int useful = (int) map.get("useful");
                        if (useful == 1) {
                            Tool.warning("磁卡已挂失！！！");
                            initChargePanel();
                            return;
                        }
                        BigDecimal discount = new BigDecimal(map.get("discount").toString()).setScale(4, BigDecimal.ROUND_FLOOR);
                        vipDiscount = discount.multiply(new BigDecimal(100));

                        // 比较优惠力度
                        txtDiscount.setText(String.valueOf(vipDiscount));
                        if (vipDiscount.compareTo(meddiscount) < 0)
                            txtMedDiscount.setText(String.valueOf(vipDiscount));
                        if (vipDiscount.compareTo(curediscount) < 0)
                            txtCureDiscount.setText(String.valueOf(vipDiscount));

                        txtVipCard.setText(String.valueOf(map.get("id")));
                        vipId = Integer.parseInt(map.get("id").toString());

                        getLess(discount.multiply(new BigDecimal(100)));
                        remain = new BigDecimal(map.get("remain").toString());
                        txtRecharge.setText(String.valueOf(remain));
                        present = new BigDecimal(map.get("present").toString());
                        txtPresent.setText(String.valueOf(present));
                        setTypeMoney();
                    }
                } else {
                    Tool.warning("无此vip");
                    initChargePanel();
                }
            }
        });

        listeners.add(new mListener("getPreList") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    listModel.setRowCount(0);
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        int state = (int) map.get("state");
                        if (state != 1)
                            continue;
                        Vector vector = new Vector();
                        vector.add(map.get("pid"));
                        vector.add(map.get("medfee"));
                        vector.add(map.get("curefee"));
                        vector.add(map.get("dose"));
                        listModel.addRow(vector);
                    }
                    listTable.setModel(listModel);
                } else {
                    warning("获取病人处方列表失败");
                }
            }
        });

        listeners.add(new mListener("clinicPreCharge") {
            @Override
            public void messageEvent(MessageEvent event) {

                if (event.getMessage(insuranceMessage.class).getMark() > 0) {
                    // 预收费返回结果
                    labStatus.setText("预结算结果返回！");
                    insuranceMessage message = event.getMessage(insuranceMessage.class);
                    BigDecimal all = new BigDecimal(message.getOutElement(1, 26));
                    BigDecimal i_cash = new BigDecimal(message.getOutElement(1, 28));
                    BigDecimal i_left = all.subtract(i_cash);
                    insurancecharge = i_left;
                    setMoney();
                    btnOkCharge.setEnabled(true);
                } else
                    Tool.warning("预收费失败！~ 请检查服务器是否连接，然后重启客户端重试！");
            }
        });

        // 医保收费回调;
        listeners.add(new mListener("clinicCharge") {
            @Override
            public void messageEvent(MessageEvent event) {
                if (event.getMessage(insuranceMessage.class).getMark() > 0) {
                    insuranceMessage message = event.getMessage(insuranceMessage.class);
                    if (message.getMark() > 0) {
                        // 设置收费流水号
                        p_chargeNumber = message.getOutElement(1, 16);
                        labStatus.setText("医保扣费成功，流水编号为:" + p_chargeNumber);
                        // 进入收费
                        charge(message.getOutMaps(1));
                    } else {
                        Tool.warning("医保扣费失败，终止支付流程！！！");
                    }

                }
            }
        });

        listeners.add(new mListener("clinicChargeOffset") {
            @Override
            public void messageEvent(MessageEvent event) {
                // 支付失败冲销收费订单！
                if (event.getMessage(insuranceMessage.class).getMark() > 0)
                    Tool.warning("支付失败，已经自动冲销医保收费");
                else
                    Tool.warning("订单支付失败，并且医保收费冲销失败，请注意客户余额！！！");
            }
        });
    }

    private void comboBoxListeners() {
        cbbId.addActionListener((ActionEvent e) ->
        {
            Object id = cbbId.getSelectedItem();
            selectId = id.toString().split("/")[0];
            Map<String, Object> map = new HashMap<>();
            map.put("id", selectId);
            try {
                Connect.sendMessage(buildMessage.doFunction("getRegiseterDetailById", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            map.clear();

            map.put("rgid", selectId);
            try {
                Connect.sendMessage(buildMessage.doFunction("getPreList", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            initChargePanel();
        });
    }

    private void textFieldListeners() {
        txtMedDiscount.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (txtMedDiscount.getText().equals("")) {
                    txtMedDiscount.setText(String.valueOf(meddiscount));
                    return;
                }
                try {
                    BigDecimal medDiscount = new BigDecimal(txtMedDiscount.getText()).setScale(2,BigDecimal.ROUND_FLOOR);
                    if (medDiscount.compareTo(new BigDecimal(100))>0)
                        throw new NumberFormatException();
                    meddiscount = medDiscount;
                    txtAlreadySetText(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                    setTypeMoney();
                    preCharge();
                } catch (NumberFormatException w) {
                    warning(dateTypeWrong);
                    txtMedDiscount.setText(String.valueOf(meddiscount));
                    txtMedDiscount.grabFocus();
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                txtMedDiscount.setText("");
            }
        });

        txtCureDiscount.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (txtCureDiscount.getText().equals("")) {
                    txtCureDiscount.setText(String.valueOf(curediscount));
                    return;
                }
                try {
                    BigDecimal cureDiscount = new BigDecimal(txtCureDiscount.getText()).setScale(2,BigDecimal.ROUND_FLOOR);
                    if (cureDiscount.compareTo(new BigDecimal(100))>0)
                        throw new NumberFormatException();
                    curediscount = cureDiscount;
                    txtAlreadySetText(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
                    setTypeMoney();
                    preCharge();
                } catch (NumberFormatException w) {
                    warning(dateTypeWrong);
                    txtMedDiscount.grabFocus();
                    txtMedDiscount.setText(String.valueOf(curediscount));
                    return;
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                txtCureDiscount.setText("");
            }
        });

        txtNonVipCash.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (txtNonVipCash.getText().equals("")) {
                    txtNonVipCash.setText("");
                }
            }

            public void focusGained(FocusEvent e) {
                txtNonVipCash.setText("");
            }

        });

        txtNonVipOther.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (txtNonVipOther.getText().equals("")) {
                    txtNonVipOther.setText("请在此输入金额");
                }
            }

            public void focusGained(FocusEvent e) {
                txtNonVipOther.setText(String.valueOf(left));
            }

        });

        txtVipCash.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (txtVipCash.getText().equals("")) {
                    txtVipCash.setText("请在此输入金额");
                }
            }

            public void focusGained(FocusEvent e) {
                txtVipCash.setText("");
            }

        });

        txtVipOther.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if (txtVipOther.getText().equals("")) {
                    txtVipOther.setText("请在此输入金额");
                }
            }

            public void focusGained(FocusEvent e) {
                txtVipOther.setText(String.valueOf(left));
            }
        });
    }

    // 获取更优惠的方式
    private void getLess(BigDecimal vipDiscount) {
        // 修改优惠额度
        if (meddiscount.compareTo(vipDiscount) > 0)
            meddiscount = vipDiscount;
        if (curediscount.compareTo(vipDiscount) > 0)
            curediscount = vipDiscount;

        meddiscount = meddiscount.setScale(2,BigDecimal.ROUND_FLOOR);
        curediscount = curediscount.setScale(2,BigDecimal.ROUND_FLOOR);
    }

    private void checkBoxListeners() {
        // 点击非vip
        chkNonVip.addActionListener((ActionEvent e) ->
        {
            initChargePanel();
            vipId = 0;
            vipDiscount=new BigDecimal(100);
            txtAlreadySetText(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            pnCash.setVisible(true);
            pnVip.setVisible(false);
            chkVip.setSelected(false);
            chkNonVip.setSelected(true);
            setTypeMoney();
        });

        // 点击vip
        chkVip.addActionListener((ActionEvent e) ->
        {
            initChargePanel();
            txtAlreadySetText(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            // 设置面板的可见
            pnCash.setVisible(false);
            pnVip.setVisible(true);

            chkVip.setSelected(true);
            chkNonVip.setSelected(false);
            setTypeMoney();
        });

        // 单击卡内余额按钮（可以打折）
        chkDiscount.addActionListener((ActionEvent e) ->
        {
            // 判断是否有卡
            if (txtVipCard.getText().equals("")) {
                chkDiscount.setSelected(false);
                warning(pleaseGetCard);
                return;
            }
            // 扣钱
            if (chkDiscount.isSelected()) {

                if (remain.compareTo(left) >= 0)
                    remaincharge = left;
                else
                    remaincharge = remain;
                txtRecharge.setText(String.valueOf(remain.subtract(remaincharge)));
                setMoney();
            } else {
                remaincharge = new BigDecimal(0);
                txtRecharge.setText(String.valueOf(remain.subtract(remaincharge)));
                setMoney();
            }

        });

        // 单击卡内赠送（不打折）
        chkNoDiscount.addActionListener((ActionEvent e) ->
        {
            if (txtVipCard.getText().equals("0")) {
                chkNoDiscount.setSelected(false);
                warning(pleaseGetCard);
                return;
            }

            if (chkNoDiscount.isSelected()) {
                if (present.compareTo(left) >= 0)
                    presentcharge = left;
                else
                    presentcharge = present;
                txtPresent.setText(present.subtract(presentcharge).toString());
                setMoney();
            } else {
                presentcharge = new BigDecimal(0);
                txtPresent.setText(present.subtract(presentcharge).toString());
                setMoney();
            }
        });
    }

    private void buttonListeners() {
        // 刷新按钮
        btnFresh.addActionListener((ActionEvent e) ->
        {
            try {
                Connect.sendMessage(buildMessage.doFunction("getIdByChargestate", null).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            // 清空所有用户状态信息
            initPatientPanel();
        });


        // 支付按钮
        btnOkCharge.addActionListener((ActionEvent e) ->
        {
            // 按钮不为灰色才进行下一步
            if (!btnOkCharge.isEnabled())
                return;
            // 如果没有支付完成
            if (left.compareTo(BigDecimal.ZERO) != 0) {
                warning(okChargeAfterComplete);
                return;
            }

            // 如果没有选择挂号编号
            if (selectId == null || selectId.equals("")) {
                warning("请选择挂号编号！！！");
                return;
            }

            charge(null);

            // 如果是医保收费,就进入医保收费流程
//            if (insurancecharge.compareTo(BigDecimal.ZERO) > 0)
//                iCharge();
//            else
//                charge(null);

        });
    }

    // 和本地服务器之间的支付
    public void charge(Map indata) {
        Map<String, Object> map = new HashMap<>();
        // 这个是将处方id 放入map中
        StringBuilder stringBuilder = new StringBuilder();

        // 构造pid 字段
        for (int row : listTable.getSelectedRows()) {
            stringBuilder.append(listTable.getValueAt(row, 0));
            stringBuilder.append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf("|"));

        map.put("pid", stringBuilder.toString());
        // 将医保收费详情加入其中
        if (indata != null)
            map.putAll(indata);

        map.put("rgid", selectId);
        map.put("medicinediscount", meddiscount.divide(new BigDecimal(100)).setScale(4, BigDecimal.ROUND_FLOOR));
        map.put("medcurediscount", curediscount.divide(new BigDecimal(100)).setScale(4, BigDecimal.ROUND_FLOOR));
        map.put("cash", cash.setScale(2, 4));
        map.put("other", other.setScale(2, 4));
        map.put("insurance", insurancecharge.setScale(2, 4));

        // 判断是否预结算
        if (insurancecharge.compareTo(BigDecimal.ZERO) > 0 && p_chargeNumber == null) {
            map.put("preAccount", "true");
            labStatus.setText("准备进行本地收费预结算！！！...");
        } else {
            map.put("preAccount", "false");
            labStatus.setText("准备进行本地收费结算！！！...");
        }

        if (chkNonVip.isSelected()) {
            try {
                btnOkCharge.setEnabled(false);
                Connect.sendMessage(buildMessage.doFunction("Account", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else if (chkVip.isSelected()) {
            if (password == null) {
                map.put("vipId", vipId);
                map.put("card", remaincharge);
                map.put("given", presentcharge);
                // new password() 里面写了发送请求命令，追溯一下（不是我写的）
                password = new Password(map);
                btnOkCharge.setEnabled(false);
            }
        }
    }

    // 分类金额
    private void setTypeMoney() {
        BigDecimal med = new BigDecimal(0);         // 药品费
        BigDecimal cure = new BigDecimal(0);        // 诊疗费
        BigDecimal sum = new BigDecimal(0);         // 总金额
        BigDecimal ceil = new BigDecimal(0);        // ...未知

        // 遍历选中的处方，累加金额
        for (int row : listTable.getSelectedRows()) {
            // 截断保留两位小数
            BigDecimal m = new BigDecimal(listModel.getValueAt(row, 1).toString()).setScale(2, BigDecimal.ROUND_FLOOR);
            BigDecimal c = new BigDecimal(listModel.getValueAt(row, 2).toString()).setScale(2, BigDecimal.ROUND_FLOOR);
            BigDecimal sumTemp;
            med = med.add(m);
            cure = cure.add(c);

            // 每一个处方都向上取整
            sumTemp = m.multiply(meddiscount.divide(new BigDecimal(100))).setScale(2, BigDecimal.ROUND_FLOOR);
            sumTemp = sumTemp.add(c.multiply(curediscount.divide(new BigDecimal(100)))).setScale(2, BigDecimal.ROUND_FLOOR);
            // 单个处方累加金额后进行截断式保留两位小数，然后向上取整
            sumTemp = sumTemp.setScale(2, BigDecimal.ROUND_FLOOR);
            sumTemp = sumTemp.setScale(0, BigDecimal.ROUND_UP);
            // 取整完添加到总金额里
            sum = sum.add(sumTemp);
        }
        medMon = med;
        cureMon = cure;

        this.sum = sum.setScale(0, BigDecimal.ROUND_UP);
        setMoney();
        lbMoney.setText(sum.setScale(2, 4) + "元");
        txtCureDiscount.setText(String.valueOf(curediscount));
        txtMedDiscount.setText(String.valueOf(meddiscount));
    }

    // 病人信息显示
    private void setTextFieldText(Object[] data) {
        lbDepart.setText("<html><u>" + data[1].toString() + "</u></html>");
        lbDoctor.setText("<html><u>" + data[2].toString() + "</u></html>");
        lbRegType.setText("<html><u>" + data[3].toString() + "</u></html>");
    }

    private void txtAlreadySetText(BigDecimal tremaincharge, BigDecimal tpresentcharge, BigDecimal tcash, BigDecimal tother, BigDecimal insurance) {
        remaincharge = tremaincharge;
        presentcharge = tpresentcharge;
        cash = tcash;
        other = tother;
        insurancecharge = insurance;
        setMoney();
    }

    private void setMoney() {
        // 总金额
        txtShould.setText(sum.toString());
        // 设置会员卡支付
        txtDiscountCharge.setText(remaincharge.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        txtNonDiscountCharge.setText(presentcharge.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        // 现金其他支付
        txtCashCharge.setText(cash.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        txtOtherCharge.setText(other.setScale(2, BigDecimal.ROUND_HALF_UP).toString());

        // 设置剩余金额
        left = sum.subtract(cash).subtract(other).subtract(remaincharge).subtract(presentcharge).subtract(insurancecharge);
        txtNot.setText(left.setScale(2, BigDecimal.ROUND_FLOOR).toString());
        // 设置医保已经支付金额
        txtInsuranceCharge.setText(insurancecharge.toString());
    }

    // 初始化收费面板
    private void initChargePanel() {
        meddiscount = new BigDecimal(100);
        curediscount = new BigDecimal(100);
        medMon = new BigDecimal(0);
        cureMon = new BigDecimal(0);
        remaincharge = new BigDecimal(0);
        presentcharge = new BigDecimal(0);
        insurancecharge = new BigDecimal(0);
        cash = new BigDecimal(0);
        other = new BigDecimal(0);
        txtShould.setText("0.00");
        vipId = 0;
        sum = new BigDecimal(0);
        left = new BigDecimal(0);
        btnOkCharge.setEnabled(true);
        txtAlreadySetText(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        txtNot.setText("0.00");
        chkNonVip.setSelected(true);
        chkVip.setSelected(false);
        txtNonVipCash.setText("请在此输入金额");
        txtNonVipOther.setText("请在此输入金额");
        txtVipCash.setText("请在此输入金额");
        txtVipOther.setText("请在此输入金额");
        txtMedDiscount.setText("100");
        txtCureDiscount.setText("100");
        chkDiscount.setSelected(false);
        chkNoDiscount.setSelected(false);
        txtVipCard.setText("");
        txtDiscount.setText("");
        txtRecharge.setText("0.00");
        txtPresent.setText("0.00");
        txtInsuranceCharge.setText("0.00");
        textField1.setText("");
        vipId = 0;
        // 设置面板的可见
        pnCash.setVisible(true);
        pnVip.setVisible(false);
        labStatus.setText("");
    }

    private void initPatientPanel() {
        setTextFieldText(new String[]{"", "", "", "", "", ""});
        p_name = null;
        p_doctorName = null;
        p_securityId = null;
        p_clinicNumber = null;
        p_cardId = null;
        p_chargeNumber = null;
        p_departName = null;
        isInsuranceCharge = false;
        selectId = null;
        model.setRowCount(0);
        listModel.setRowCount(0);
        labStatus.setText("");
    }

    // 打印
    private void setPrint() {
//           Print(String patientName,String time,String registerId,float medMon,float cureMon,
//                  float other,String doctorName,String positionName)
        Map map = (Map) patientMap.get(selectId);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        BigDecimal m = sum.subtract(cureMon.multiply(curediscount.divide(new BigDecimal(100))).setScale(2,BigDecimal.ROUND_FLOOR));
        BigDecimal c = cureMon.multiply(curediscount.divide(new BigDecimal(100).setScale(2,BigDecimal.ROUND_FLOOR)));
        Print print = new Print(
                (String) map.get("patientname")
                , formatter.format(new Date()),
                selectId,
                m,
                c,
                BigDecimal.ZERO,
                (String) map.get("doctorname"),
                UserInfo.position
        );
    }

    // 构建医保收费信息
    private boolean buildChargeDate(insuranceMessage message) {
        if (listTable.getSelectedRows().length > 1) {
            Tool.warning("医保收费只允许一个处方进行收费！");
            return false;
        }

        if (p_securityId != null && p_cardId != null && p_areaId != null) {
            // 主表信息
            message.addElement(1, p_securityId);
            message.addElement(2, p_name);
            message.addElement(3, p_cardId);
            message.addElement(4, p_areaId);
            message.addElement(5, p_clinicNumber);
            if (p_entityCode.equals(""))
                message.addElement(6, null);
            else
                message.addElement(6, p_entityCode);
            if (p_entityName.equals(""))
                message.addElement(7, null);
            else
                message.addElement(7, p_entityName);

            message.addElement(8, p_doctorName);
            message.addElement(9, "FALSE");
            BigDecimal sum_t = BigDecimal.ZERO;         // 消费明细总金额！
            for (int row : listTable.getSelectedRows()) {
                int pre = Integer.parseInt(listTable.getValueAt(row, 0).toString());
                List<Map<String, String>> list = preMap.get(pre);
                if (list == null) {
                    Tool.warning("请先获取处方详情，id：" + pre);
                    return false;
                }

                for (Map<String, String> map : list) {
                    // 添加药品详情表
                    message.addIndex();
                    BigDecimal number = new BigDecimal(map.get("number"));
                    BigDecimal price = new BigDecimal(map.get("outprice")).setScale(2,BigDecimal.ROUND_FLOOR);
                    if (map.get("type").equals("药品费"))
                        number = number.multiply(meddiscount.divide(new BigDecimal(100))).setScale(2,BigDecimal.ROUND_FLOOR);
                    else
                        number = number.multiply(curediscount.divide(new BigDecimal(100))).setScale(2,BigDecimal.ROUND_FLOOR);
                    message.addElement(1, map.get("itemCode"));
                    if (map.get("name2") != null && !map.get("name2").equals(""))
                        message.addElement(2, map.get("name2"));
                    else
                        message.addElement(2, map.get("name"));

                    message.addElement(3, price.toString());
                    message.addElement(4, number.setScale(2, BigDecimal.ROUND_FLOOR).toString());
                    message.addElement(5, number.multiply(price).setScale(2, BigDecimal.ROUND_FLOOR).toString());
                    message.addElement(6, insuranceMessage.formatDate(new Date()));
                    message.addElement(7, map.get("itemCode") + "&" + map.get("itemGrade"));
                    sum_t = sum_t.add(number.multiply(price).setScale(2, BigDecimal.ROUND_FLOOR));
                }
            }

            // 凑整添加药
            // 计算数量
            //BigDecimal sumMed = medMon.multiply(new BigDecimal(meddiscount).divide(new BigDecimal(100))).setScale(2, 4);
            //BigDecimal sumCure = cureMon.multiply(new BigDecimal(curediscount).divide(new BigDecimal(100))).setScale(2, 4);
            //BigDecimal num = sum.subtract(sumCure).subtract(sumMed).multiply(new BigDecimal(100)).setScale(2, 4);

            BigDecimal num = this.sum.subtract(sum_t).setScale(2, BigDecimal.ROUND_FLOOR);
            message.addIndex();
            message.addElement(1, "Y-0229");
            message.addElement(2, "甘草");
            message.addElement(3, "1.00");
            message.addElement(4, num);
            message.addElement(5, num);
            message.addElement(6, insuranceMessage.formatDate(new Date()));
            message.addElement(7, "Y-0229&甘草");
            return true;
        } else
            return false;
    }

    // 预收费
    private synchronized void preCharge() {
        if (isInsuranceCharge && insurancecharge.compareTo(BigDecimal.ZERO) == 0) {
            insuranceMessage message = new insuranceMessage();
            message.setFunctionName("clinicPreCharge");
            labStatus.setText("构造医保收费数据包...");
            // 构建收费项目信息
            if (buildChargeDate(message)) {
                btnOkCharge.setEnabled(false);
                txtInsuranceCharge.setText("正在等待医保预结算结果...");
                labStatus.setText("正在等待医保预结算结果...");
                message.doFunction();
            } else
                warning("构建医保收费数据包失败");
        } else
            return;
    }

    // 医保收费
    private void iCharge() {
        if (p_securityId != null && p_cardId != null && p_areaId != null) {
            insuranceMessage message = new insuranceMessage();
            message.setFunctionName("clinicCharge");
            // 构建收费项目信息
            if (buildChargeDate(message)) {
                labStatus.setText("准备进行医保扣费操作！！！...");
                btnOkCharge.setEnabled(false);
                message.doFunction();
            } else {
                labStatus.setText("构造医保数据包失败...终止收费流程!!!");
                warning("构建收费数据包失败");
            }
        } else
            return;
    }

    // 医保收费冲销
    private void offsetCharge(Map<String, String> map) {
        insuranceMessage mess = new insuranceMessage();
        mess.setFunctionName("clinicChargeOffset");
        mess.addElement("1", map.get("1"));
        mess.addElement("2", map.get("2"));
        mess.addElement("3", map.get("3"));
        mess.addElement("4", map.get("4"));
        mess.addElement("5", map.get("5"));
        mess.addElement("6", map.get("6"));
        mess.doFunction();
    }

    // 备用，插入医保收费结果
    private void insertInsuranceCharge(insuranceMessage message) {
        // 长度适合
        if (message.getOutData().size() >= 1 && message.getOutData().get(0).size() == 55) {
            Map map = message.getOutMaps(1);
            map.put("56", pid);
            Connect.sendMessage(buildMessage.doFunction("insuranceAccount", map).toByteArray());
        }
    }


    private void moneySetVisible(boolean state) {
        lbDiscountCharge.setVisible(state);
        lbNonDiscountCharge.setVisible(state);
        txtDiscountCharge.setVisible(state);
        txtNonDiscountCharge.setVisible(state);
    }
}
