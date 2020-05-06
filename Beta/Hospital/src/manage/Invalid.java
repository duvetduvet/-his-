package manage;

import UI.Table;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import insurance.insuranceMessage;
import javafx.collections.ObservableMap;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.Tool;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.chargeDetailCn;
import static tool.Headers.preList;
import static tool.Strings.*;
import static tool.Tool.getToday;
import static tool.Tool.warning;

public class Invalid
{
    public JPanel pnInvalid;
    private JComboBox cbbName;
    private JButton btnSearch;
    private JDatePicker datePicker;
    private Table table;
    private JButton btnOk;
    private Table listTable;
    private JButton print;
    private DefaultTableModel model;
    private DefaultTableModel listmodel;
    private List<mListener> listeners = new ArrayList<>();

    private Map<String, Map<String, Object>> pMap = new HashMap<>();

    public Invalid()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        datePickerListeners();
        buttonListeners();


    }

    // 初始化界面
    private void initUI()
    {
        // 初始化表格
        model = new DefaultTableModel(chargeDetailCn, 0);
        listmodel = new DefaultTableModel(preList, 0);
        table.setModel(model);
        listTable.setModel(listmodel);
        table.setRowSorter(new TableRowSorter(model));
        datePicker.getFormattedTextField().setText(getToday());
        Map<String, Object> map = new HashMap<>();
        map.put("date", getToday());
        try
        {
            Connect.sendMessage(tcp.buildMessage.doFunction("getPatientByDate", map).toByteArray());
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    // 消息监听器
    private void messageListeners()
    {

        listeners.add(new mListener("getPreListOrder")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    pMap.clear();
                    List<Any> anies = feedback.getDetailsList();
                    listmodel.setRowCount(0);
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        Vector vector = new Vector();
                        vector.add(map.get("pid"));
                        vector.add(map.get("medfee"));
                        vector.add(map.get("curefee"));
                        vector.add(map.get("dose"));
                        pMap.put(map.get("pid").toString(), map);
                        listmodel.addRow(vector);
                    }
                    listTable.setModel(listmodel);
                }
                else
                {
                    warning("获取病人处方列表失败");
                }
            }
        });


        listeners.add(new mListener("getPatientByDate")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                List<String> list = new ArrayList<>();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies)
                    {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        Integer id = Integer.valueOf(map.get("orderid").toString());
                        String name = (String) map.get("name");
                        list.add(id + "/" + name + "/" + map.get("insuranceNumber"));
                    }
                    cbbName.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getPreInfoByPreId")
        {
            public void messageEvent(MessageEvent event)
            {
                model.setRowCount(0);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies)
                    {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        Object name = map.get("name");
                        Object unitname = map.get("unitname");
                        BigDecimal outprice = new BigDecimal(map.get("outprice").toString());
                        BigDecimal number = new BigDecimal(map.get("number").toString());
                        Object feetypename = map.get("feetypename");
                        Object[] objects = new Object[]{name, unitname, outprice, number, feetypename,map.get("itemCode"),map.get("itemGrade"), outprice.multiply(number).setScale(2,4)};
                        model.addRow(objects);
                        btnOk.setEnabled(true);
                    }
                }
                else
                {
                    btnOk.setEnabled(false);
                }
            }
        });

        listeners.add(new mListener("resetUsefulByPreId")
        {
            public void messageEvent(MessageEvent event)
            {
                btnOk.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    warning(preInvalidSuccess);
                    btnOk.setEnabled(false);
                    model.setRowCount(0);
                    datePicker.getFormattedTextField().setText("");
                    cbbName.setModel(new DefaultComboBoxModel());
                }
                else
                {
                    warning(preInvalidFail);
                }
            }
        });

        listeners.add(new mListener("clinicChargeOffset")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                insuranceMessage message = event.getMessage(insuranceMessage.class);
                if (message.getMark() > 0)
                {
                    String orderid = cbbName.getSelectedItem().toString().split("/")[0];
                    Map<String, Object> map = new HashMap<>();
                    map.put("orderid", orderid);
                    try
                    {
                        btnOk.setEnabled(false);
                        Connect.sendMessage(tcp.buildMessage.doFunction("resetUsefulByPreId", map).toByteArray());
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }

                }
                else
                    Tool.warning("处方作废失败！");
            }
        });
    }

    // 日期选择器监听器
    private void datePickerListeners()
    {
        datePicker.getFormattedTextField().getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                String date = datePicker.getFormattedTextField().getText();
                Map<String, Object> map = new HashMap<>();
                map.put("date", date);
                try
                {
                    Connect.sendMessage(tcp.buildMessage.doFunction("getPatientByDate", map).toByteArray());
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }

            public void removeUpdate(DocumentEvent e)
            {
            }

            public void changedUpdate(DocumentEvent e)
            {
            }
        });
    }

    // 按钮监听器
    private void buttonListeners()
    {
        cbbName.addActionListener(e ->
        {
            int index = cbbName.getSelectedIndex();
            if (index == 0)
                return;
            int orderid = Integer.parseInt(cbbName.getSelectedItem().toString().split("/")[0]);
            Map map = new HashMap();
            map.put("orderid", orderid);
            Connect.sendMessage(buildMessage.doFunction("getPreListOrder", map).toByteArray());
        });

        listTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int pid = (int) listmodel.getValueAt(listTable.getSelectedRow(), 0);
                Map map = new HashMap();
                map.put("prescriptionid", pid);
                Connect.sendMessage(buildMessage.doFunction("getPreInfoByPreId", map).toByteArray());
            }
        });

        btnSearch.addActionListener((ActionEvent e) ->
        {
            String date = datePicker.getFormattedTextField().getText();
            if (date.equals("") || cbbName.getSelectedIndex() == 0)
            {
                warning(selectCondition);
                return;
            }
            String preid = cbbName.getSelectedItem().toString().split("/")[0];
            Map<String, Object> map = new HashMap<>();
            map.put("prescriptionid", preid);
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("getPreInfoByPreId", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        btnOk.addActionListener((ActionEvent e) ->
        {
            String orderid = cbbName.getSelectedItem().toString().split("/")[0];

            // 判断是否医保收费,只选中一行，处方列表有处方详情，处方详情中单据流水号不为空
            if (pMap.size() == 1)
            {
                Map map = (Map<String, Object>) pMap.values().toArray()[0];
                if(map.get("insuranceNumber").equals("") == false)
                {
                    insuranceMessage message = new insuranceMessage();
                    message.setFunctionName("clinicChargeOffset");
                    message.addElement(1, map.get("securityId").toString());
                    message.addElement(2, map.get("name").toString());
                    message.addElement(3, map.get("cardId").toString());
                    message.addElement(4, map.get("areaId").toString());
                    message.addElement(5, map.get("clinicNumber").toString());
                    message.addElement(6, map.get("insuranceNumber").toString());
                    message.doFunction();
                    return;
                }
            }

            Map<String, Object> map = new HashMap<>();
            map.put("orderid", orderid);
            try
            {
                btnOk.setEnabled(false);
                Connect.sendMessage(tcp.buildMessage.doFunction("resetUsefulByPreId", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });
    }
}
