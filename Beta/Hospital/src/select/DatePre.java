package select;

import UI.Table;
import UI.hintWindow;
import UI.mTableModel;
import com.google.protobuf.Any;
import com.sun.tools.internal.xjc.reader.xmlschema.BindGreen;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import javafx.util.converter.BigDecimalStringConverter;
import org.jdatepicker.JDatePicker;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.datePre;
import static tool.Strings.dateError;
import static tool.Strings.fileError;
import static tool.Tool.getToday;
import static tool.Tool.warning;

public class DatePre {
    public JPanel pnDatePre;
    private JLabel lbRecord;
    private Table table;
    private JButton btnExport;
    private JDatePicker startDate;
    private JButton btnSearch;
    private JDatePicker endDate;
    private JCheckBox chkUse;
    private JCheckBox chkInvalid;
    private JLabel lbSum;
    private JComboBox cbbDepart;
    private JComboBox cbbDoctor;
    private JTextField pName;
    private JLabel labTrue;
    private JLabel labMed;
    private JLabel labCure;
    private JLabel labVip;
    private JLabel labGiven;
    private JLabel labCash;
    private JLabel labOther;
    private JLabel labPre;
    private JLabel labInsurance;
    private JCheckBox cbCharge;
    private JLabel labFee;
    private DefaultTableModel model;
    private int patientId;
    private List<mListener> listeners = new ArrayList<>();

    public DatePre() {
        messageManager.removeAllMessageListener();
        startDate.getFormattedTextField().setText(getToday());
        endDate.getFormattedTextField().setText(getToday());
        messageManager.removeAllMessageListener();
        model = new mTableModel(datePre, 0);
        table.setModel(model);
        buttonListeners();
        messageListeners();
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(chkInvalid);
        buttonGroup.add(chkUse);
        new hintWindow(pName) {
            @Override
            public void keyTyped(KeyEvent e) {
                StringBuilder stringBuilder = new StringBuilder();
                char temp = e.getKeyChar();
                int charCode = Integer.valueOf(temp);
                // 中文 回车 退格 符进行判断
                if (charCode > 255 || charCode == 8 || charCode == 10) {
                    if (charCode == 8)
                        patientId = -1;
                    if (charCode != 10) {
                        if (charCode > 255)
                            stringBuilder.append(pName.getText() + temp);
                        else if (charCode == 8)
                            stringBuilder.append(pName.getText());
                        String name = stringBuilder.toString();
                        Map<String, Object> map = new HashMap<>();
                        map.put("spell", name);
                        System.out.println(name);
                        if (!name.equals("")) {
                            Connect.sendMessage(buildMessage.doFunction("getPatientBySpell", map).toByteArray());
                        } else
                            hintWindow.map.get(pName).setVisible(false);
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }

            @Override
            protected void setText(String selectedValue) {
                if (selectedValue != null) {
                    String[] temp = selectedValue.split("/");
                    patientId = Integer.parseInt(temp[4]);
                    pName.setText(temp[0]);
                } else {
                    patientId = 0;
                }
            }

        };
        Map<String, Object> map = new HashMap<>();
        map.put("functionName", "department");
        map.put("table", "department");
        Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
    }

    private void buttonListeners() {
        btnSearch.addActionListener((ActionEvent e) ->
        {
            String date = startDate.getFormattedTextField().getText();
            String end = endDate.getFormattedTextField().getText();
            if (date.equals("") || end.equals("")) {
                warning(dateError);
                return;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("start", date);
            map.put("end", end);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("getDatePres", map).toByteArray());
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
                    excelExporter.exportFile(path, "处方金额统计", datePre, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });

        cbbDepart.addActionListener((ActionEvent e) ->
        {
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("departname", cbbDepart.getSelectedItem());
                Connect.sendMessage(tcp.buildMessage.doFunction("getDoctorByDepart", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    private void messageListeners() {
        listeners.add(new mListener("getDatePres") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    Map<Object, Object> orderMap = new HashMap<>();
                    Set<String> regSet = new HashSet<>();
                    int n = 0;
                    BigDecimal aMed = BigDecimal.ZERO;
                    BigDecimal aCure = BigDecimal.ZERO;
                    BigDecimal aVip = BigDecimal.ZERO;
                    BigDecimal aGiven = BigDecimal.ZERO;
                    BigDecimal aCash = BigDecimal.ZERO;
                    BigDecimal aOther = BigDecimal.ZERO;
                    BigDecimal aInsurance = BigDecimal.ZERO;
                    BigDecimal aFee = BigDecimal.ZERO;

                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);

                        Object patientname = map.get("name");
                        String docname = map.get("doctorname").toString();
                        String deparname = map.get("departname").toString();
                        BigDecimal regfee = new BigDecimal(map.get("fee").toString());
                        Object state = map.get("state");
                        Object date = map.get("date");
                        Object regdate = map.get("regdate");

                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            Date start_d = df.parse(startDate.getFormattedTextField().getText());
                            Date end_d = df.parse(endDate.getFormattedTextField().getText());
                            Date reg_d = df.parse(regdate.toString());
                            Date pre_d = null;
                            if (!date.toString().equals(""))
                                pre_d = df.parse(date.toString());

                            /* 目标是  算清楚当天的实际收入金额，包括挂号费和处方费
                            处方有3中状态，空既 只挂号，1 就是划价，2 就是收费 ， -1 就是作废了
                            查询检索条件为： 挂号日期或者收费日期在指定时间内
                            一般情况下，挂号日期和收费日期相同。特殊情况，前几天挂的号，今天收费，导致挂号费统计异常

                             */

                            // 静态筛选（排除处方）
                            if (check(docname, deparname, patientname.toString(), state.toString())) {
                                Object id = map.get("pid");
                                Object rgid = map.get("rgid");
                                Object orderid = map.get("orderid");
                                n++;
                                // 先算挂号费，只要在范围里面就累加挂号费
                                if (reg_d.getTime() >= start_d.getTime() && reg_d.getTime() <= end_d.getTime()) {
                                    if (!regSet.contains(rgid.toString())) {
                                        aFee = aFee.add(regfee);
                                        regSet.add(rgid.toString());
                                    }
                                }


                                // 再计算处方费，如果订单id为空那就没有处方收费记录
                                if (!orderid.equals("") && pre_d.getTime() >= start_d.getTime() && pre_d.getTime() <= end_d.getTime()) {
                                    // 如果不为空而且在时间范围内，就是有收费记录，可以累加处方金额

                                    BigDecimal medfee = new BigDecimal(map.get("medfee").toString());
                                    BigDecimal curefee = new BigDecimal(map.get("curefee").toString());
                                    BigDecimal medDiscount = new BigDecimal(map.get("medicinediscount").toString());
                                    BigDecimal cureDiscount = new BigDecimal(map.get("medcurediscount").toString());
                                    BigDecimal card = new BigDecimal(map.get("card").toString());
                                    BigDecimal given = new BigDecimal(map.get("given").toString());
                                    BigDecimal cash = new BigDecimal(map.get("cash").toString());
                                    BigDecimal other = new BigDecimal(map.get("other").toString());
                                    BigDecimal insurance = new BigDecimal(map.get("insurance").toString());
                                    String insuranceNumber = map.get("insuranceNumber").toString();
                                    // 金钱累加
                                    medfee = medfee.multiply(medDiscount).setScale(2, 4);
                                    curefee = curefee.multiply(cureDiscount).setScale(2, 4);

                                    aMed = aMed.add(medfee);
                                    aCure = aCure.add(curefee);
                                    // 可能一个订单有多个处方，然后通过一个map来控制 一个订单号只累加一次处方
                                    if (orderMap.get(orderid) == null) {
                                        if (card != null)
                                            aVip = aVip.add(card);
                                        if (given != null)
                                            aGiven = aGiven.add(given);
                                        if (cash != null)
                                            aCash = aCash.add(cash);
                                        if (other != null)
                                            aOther = aOther.add(other);
                                        if (insurance != null)
                                            aInsurance = aInsurance.add(insurance);
                                        orderMap.put(orderid, orderid);
                                    }
                                    model.addRow(new Object[]{Integer.parseInt(id.toString()), Integer.valueOf(rgid.toString()), orderid, map.get("insuranceNumber"), patientname, regfee, medfee, curefee, medDiscount, cureDiscount, card, given, cash, other, insurance, docname, deparname, date, regdate});

                                } else {
                                    model.addRow(new Object[]{id.toString(), rgid.toString(), orderid, "---", patientname, regfee, "---", "---", "---", "---", "---", "---", "---", "---", "---", docname, deparname, date, regdate});

                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                            warning("日期转化错误");
                        }

                    }


                    labCash.setText(aCash.setScale(2, 4).toString());
                    labCure.setText(aCure.setScale(2, 4).toString());
                    labMed.setText(aMed.setScale(2, 4).toString());
                    labVip.setText(aVip.setScale(2, 4).toString());
                    labGiven.setText(aGiven.setScale(2, 4).toString());
                    labOther.setText(aOther.setScale(2, 4).toString());
                    labPre.setText(aMed.add(aCure).setScale(2, 4).toString());
                    labTrue.setText(aCash.add(aOther).add(aInsurance).setScale(2, 4).toString());
                    labInsurance.setText(aInsurance.setScale(2, 4).toString());
                    labFee.setText(aFee.setScale(2, 4).toString());
                    lbRecord.setText("已查询到：" + n + "条记录");
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
                    }
                    cbbDepart.setModel(getColumnDataComboBoxModel("全部", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getDoctorByDepart") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        list.add((String) map.get("drname"));
                    }
                    cbbDoctor.setModel(getColumnDataComboBoxModel("全部", list.toArray(new String[list.size()])));
                } else {
                    cbbDoctor.setModel(new DefaultComboBoxModel());
                }
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
                        String date = (String) temp.get("date");
                        String birth = (String) temp.get("birth");
                        hint.add(temp.get("name") + "/" + temp.get("sex") + "/" + birth.split(" ")[0] + "/" + date.split(" ")[0] + "/" + id);
                    }
                    if (list.size() != 0) {
                        hintWindow hintWindow = UI.hintWindow.map.get(pName);
                        hintWindow.updateList(hint.toArray(new String[list.size()]));
                    }
                }
            }
        });

    }

    private double ceil(double x) {
        x = x * 100;
        x = Math.ceil(x);
        x = x / 100.0;
        return x;
    }

    private boolean check(String docname, String depname, String pname, String state) {
        Object cbbdoc = cbbDoctor.getSelectedItem();
        Object cbbdep = cbbDepart.getSelectedItem();
        Object patientName = pName.getText();
        if ((state.equals("") || state.equals("1")) && !cbCharge.isSelected())
            return false;

        if (chkUse.isSelected()) {

            if (state.equals("-1")) {
                return false;
            }
        } else {
            if (state.equals("-1")) {
                return false;
            }
        }

        if (cbbdep == null || cbbdep.equals("") || cbbdep.equals("全部") || cbbdep.equals(depname))
            if (cbbdoc == null || cbbdoc.equals("") || cbbdoc.equals("全部") || cbbdoc.equals(docname))
                if (patientName == null || patientName.equals("") || patientName.equals(pname))
                    return true;
                else
                    return false;
            else
                return false;
        else
            return false;


    }
}
