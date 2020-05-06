package select;

import UI.Table;
import UI.hintWindow;
import com.google.protobuf.Any;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import org.jdatepicker.JDatePicker;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;
import tool.Pre;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import tool.PreManage;
import tool.medicine;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.prescriptionCn;
import static tool.Strings.dateError;
import static tool.Strings.fileError;
import static tool.Tool.warning;

public class PatientPres
{
    public JPanel pnPres;
    private JButton btnSearch;
    private JDatePicker startDate;
    private JDatePicker endDate;
    private JComboBox cbbDepart;
    private Table table;
    private JComboBox cbbDoctor;
    private JButton btnExport;
    private JLabel lbRecord;
    private JTextField pName;
    private JButton print;
    private DefaultTableModel model;
    private String patientName;
    private List<mListener> listeners = new ArrayList<>();
    private PreManage preManage = new PreManage();
    private int patientId = -1;
    private boolean mark = false;

    public PatientPres()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        comboBoxListeners();
        buttonListeners();
        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    try
                    {
                        int id = (int) table.getValueAt(table.getSelectedRow(), 0);
                        Map map = new HashMap();
                        map.put(String.valueOf(id), id);
                        mark = true;
                        Connect.sendMessage(buildMessage.doFunction("getPrescription", map).toByteArray());
                    }
                    catch (Exception e1)
                    {
                        System.out.println(e1);
                    }

                }
            }
        });
        print.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(table.getSelectedRow()!=-1)
                {
                    Pre pre = preManage.getPre(Integer.parseInt(table.getValueAt(table.getSelectedRow(),0).toString()));
                    pre.print();
                }
            }
        });
    }

    private void initUI()
    {

        new hintWindow(pName)
        {
            @Override
            public void keyTyped(KeyEvent e)
            {
                StringBuilder stringBuilder = new StringBuilder();
                char temp = e.getKeyChar();
                int charCode = Integer.valueOf(temp);
                // 中文 回车 退格 符进行判断
                if (charCode > 255 || charCode == 8 || charCode == 10)
                {
                    if (charCode == 8)
                        patientId = -1;
                    if (charCode != 10)
                    {
                        if (charCode > 255)
                            stringBuilder.append(pName.getText() + temp);
                        else if (charCode == 8)
                            stringBuilder.append(pName.getText());
                        String name = stringBuilder.toString();
                        Map<String, Object> map = new HashMap<>();
                        map.put("spell", name);
                        System.out.println(name);
                        if (!name.equals(""))
                        {
                            Connect.sendMessage(buildMessage.doFunction("getPatientBySpell", map).toByteArray());
                        }
                        else
                            hintWindow.map.get(pName).setVisible(false);
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e)
            {

            }

            @Override
            public void keyReleased(KeyEvent e)
            {

            }

            @Override
            protected void setText(String selectedValue)
            {
                if (selectedValue != null)
                {
                    String[] temp = selectedValue.split("/");
                    patientId = Integer.parseInt(temp[4]);
                    pName.setText(temp[0]);
                }
                else
                {
                    patientId = -1;
                }
            }

        };

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        startDate.getFormattedTextField().setText(simpleDateFormat.format(date));
        endDate.getFormattedTextField().setText(simpleDateFormat.format(date));
        // 初始化表格
        model = new DefaultTableModel(prescriptionCn, 0);
        table.setModel(model);
        table.setRowSorter(new TableRowSorter<>(model));
        // 获取全部出入库信息
        try
        {
            Map<String, Object> map = new HashMap<>();


            map.put("functionName", "department");
            map.put("table", "department");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void messageListeners()
    {
        // 返回处方详情 getPrescription
        listeners.add(new mListener("getPrescription")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    int n = preManage.setMedcineList(anies);


                    // 如果是一个处方就弹出窗口 如果标记没有打开就结束
                    if (mark)
                        mark = false;
                    else
                        return;
                    if (n != 1)
                        return;

                    Map map = buildMessage.AnyToMap(anies.get(0));
                    int pid = (int) map.get("prescriptionid");
                    Pre p = preManage.getPre(pid);
                    if (p == null)
                        return;

                    // 弹出小窗口
                    p.getMedcineList();
                    String[] title = {"药品名", "售价","剂量","单剂量","总数量", "单位", "费用类型","小计"};
                    DefaultTableModel mModel = new DefaultTableModel(title, 0);

                    List<medicine> list = p.getMedcineList();
                    for (medicine medicine : list)
                    {

                        mModel.addRow(new Object[]{medicine.getName(), medicine.getOutprice(),p.getDose(),Math.round(medicine.getNumber()/p.getDose()*100.0)/100.0, medicine.getNumber(), medicine.getUnitname(), medicine.getFeetypename(),Math.ceil(medicine.getNumber() * medicine.getOutprice() * 100)/100});
                    }
                    new tool.onlyTable(mModel, p.getPatientname() + " - 处方详情");


                }
            }
        });

        listeners.add(new mListener("department")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies)
                    {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                    }
                    cbbDepart.setModel(getColumnDataComboBoxModel("全部", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getDoctorByDepart")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies)
                    {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        list.add((String) map.get("drname"));
                    }
                    cbbDoctor.setModel(getColumnDataComboBoxModel("全部", list.toArray(new String[list.size()])));
                }
                else
                {
                    cbbDoctor.setModel(new DefaultComboBoxModel());
                }
            }
        });


        listeners.add(new mListener("getPreRecordByCondition")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    preManage.clear();
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    BigDecimal sum = BigDecimal.ZERO;
                    for (Any temp : anies)
                    {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        int prescriptionid = (int) map.get("pid");
                        String patientname = map.get("name").toString();
                        String departname = map.get("departname").toString();
                        String doctorname = map.get("doctorname").toString();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = null;

                        try
                        {
                            date = simpleDateFormat.parse(map.get("regdate").toString());
                        }
                        catch (ParseException e)
                        {
                            System.out.println("日期转化错误");
                        }


                        int state = (int) map.get("state");
                        BigDecimal mon = new BigDecimal(map.get("sum").toString());
                        int patientid = (int) map.get("patientid");
                        String sex = map.get("sex").toString();
                        int dose = (int) map.get("dose");
                        int age = Integer.valueOf(map.get("age").toString());
                        Integer rgid = Integer.valueOf(map.get("rgid").toString());
                        String illness = map.get("illness").toString();
                        String discription = map.get("discription").toString();
                        // 如果处方里面没有记录
                        if (preManage.getPre(prescriptionid) == null)
                        {
                             Pre p = new Pre(prescriptionid, rgid, patientname, sex,age, doctorname, departname, dose, mon, state, date,illness,discription);
                            preManage.addPre(p);
                            String stat = null;

                            if (state == -1)
                                stat = "已作废";
                            else if (state == 1)
                                stat = "未收费";
                            else if (state == 2)
                                stat = "待发药";
                            else if (state == 3)
                                stat = "已发药";
                            if (state != -1)
                                sum = sum.add(mon);
                            model.addRow(new Object[]{prescriptionid, rgid, patientname, departname, doctorname, simpleDateFormat.format(date), mon, stat,discription});
                        }

                    }
                    model.addRow(new Object[]{null, null, null, null, null, "有效处方总金额：", sum, null});
                    table.setModel(model);
                }

                lbRecord.setText("已查询到" + (model.getRowCount()-1) + "条记录");
            }
        });

        listeners.add(new mListener("getPatientBySpell")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> list = feedback.getDetailsList();
                    List<String> hint = new ArrayList<>();
                    for (Any any : list)
                    {
                        Map temp = buildMessage.AnyToMap(any);
                        int id = (Integer) temp.get("id");
                        String date = (String) temp.get("date");
                        String birth = (String) temp.get("birth");
                        hint.add(temp.get("name") + "/" + temp.get("sex") + "/" + birth.split(" ")[0] + "/" + date.split(" ")[0] + "/" + id);
                    }
                    if (list.size() != 0)
                    {
                        hintWindow hintWindow = UI.hintWindow.map.get(pName);
                        hintWindow.updateList(hint.toArray(new String[list.size()]));
                    }
                }
            }
        });

    }

    private void comboBoxListeners()
    {
        // 部门选择
        cbbDepart.addActionListener((ActionEvent e) ->
        {
            try
            {
                Map<String, Object> map = new HashMap<>();
                map.put("departname", cbbDepart.getSelectedItem());
                Connect.sendMessage(tcp.buildMessage.doFunction("getDoctorByDepart", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });
    }

    private void buttonListeners()
    {
        btnSearch.addActionListener((ActionEvent e) ->
        {
            String start = startDate.getFormattedTextField().getText();
            String end = endDate.getFormattedTextField().getText();
            if (start.equals("") || end.equals(""))
            {
                warning(dateError);
                return;
            }
            Object depart = cbbDepart.getSelectedItem();
            if (depart == "全部")
            {
                depart = "%";
            }
            Object doctor = cbbDoctor.getSelectedItem();
            if (doctor == "全部" || doctor == null)
            {
                doctor = "%";
            }
            String date = "date LIKE '%'";
            if (!start.equals("") && !end.equals(""))
            {
                date = "(regdate >= '" + start + " 00:00:00' AND regdate <= '" + end + " 23:59:59')";
            }
            Map<String, Object> map = new HashMap<>();
            map.put("departname", depart);
            map.put("doctorname", doctor);
            if (patientId == -1)
                map.put("patientid", "%");
            else
                map.put("patientid", patientId);
            map.put("date", date);
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("getPreRecordByCondition", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        btnExport.addActionListener((ActionEvent e) ->
        {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null)
            {
                try
                {
                    excelExporter.exportFile(path, "病人处方记录", prescriptionCn, model);
                }
                catch (Exception e1)
                {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }
}

