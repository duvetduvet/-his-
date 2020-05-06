package manage;

import UI.Table;
import com.google.protobuf.Any;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIGlobalBinding;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.Pre;
import tool.PreManage;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.deMedCn;
import static tool.Headers.preList;
import static tool.Strings.deMedFail;
import static tool.Strings.deMedSuccess;
import static tool.Tool.warning;

public class DeMed
{
    public JPanel pnDeMed;
    private JComboBox cbbName;
    private JButton btnFresh;
    private JButton btnDeMed;
    private Table table;
    private JLabel lbDepart;
    private JLabel lbDoctor;
    private JLabel lbRegType;
    private JLabel lbResult;
    private Table listTable;
    private JButton print;
    private JComboBox cbbName2;
    private JLabel labillness;
    private JTextArea labdiscription;
    private DefaultTableModel model;
    private DefaultTableModel listmodel;
    private Map<String, Object> nameMap = new HashMap<>();
    private List<mListener> listeners = new ArrayList<>();
    private Map<Integer, Map<String, Object>> rgmap = new HashMap<>();
    private Map<Integer, Integer> st = new HashMap<>();
    private tool.PreManage preManage = new PreManage();
    private int pid = 0;

    public DeMed()
    {
        messageManager.removeAllMessageListener();

        messageListeners();
        initUI();
        comboBoxListeners();
        buttonListeners();

        listTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                pid = (int) listmodel.getValueAt(listTable.getSelectedRow(), 0);
                Map map = new HashMap();
                map.put("prescriptionid", pid);
                Connect.sendMessage(buildMessage.doFunction("getPreInfoByPreId", map).toByteArray());
            }
        });


        print.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("开始打印");
                if (listTable.getSelectedRow() != -1)
                {
                    int p = (int) listTable.getValueAt(listTable.getSelectedRow(), 0);
                    Pre pre = preManage.getPre(p);
                    if (pre != null)
                        pre.print();
                }
            }
        });
        cbbName2.addActionListener((ActionEvent e) ->
        {
            int index = cbbName2.getSelectedIndex();
            listmodel.setRowCount(0);
            if (index == 0)
            {
                setTextFieldText(new String[]{"", "", "", ""});
                model.setRowCount(0);
                table.setModel(model);
                labdiscription.setText("");
                labillness.setText("");
                lbResult.setText("已查询到" + table.getRowCount() + "条结果");
                return;
            }
            String idname = cbbName2.getSelectedItem().toString();
            cbbName.setSelectedIndex(0);
            Map<String, Object> map = new HashMap<>();
            String id = idname.split("/")[0];
            map.put("id", id);
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("getRegiseterDetailById", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            map.clear();
            map.put("rgid", id);
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("getPreList", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });
    }

    // 初始化
    private void initUI()
    {
        // 初始化表格
        String[] preList = {"处方id", "药品费", "诊疗费", "剂量", "状态"};
        model = new DefaultTableModel(deMedCn, 0);
        listmodel = new DefaultTableModel(preList, 0);
        table.setModel(model);
        // 初始化单据编号
        try
        {
            Connect.sendMessage(tcp.buildMessage.doFunction("getIdByMedstate", null).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void messageListeners()
    {
        listeners.add(new mListener("getIdByMedstate")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    List<String> list2 = new ArrayList<>();
                    for (int i = 0; i < anies.size(); i++)
                    {
                        Map map = tcp.buildMessage.AnyToMap(anies.get(i));
                        Integer rgid = (Integer) map.get("rgid");
                        String patientname = (String) map.get("name");
                        int patientid = (Integer) map.get("patientid");
                        String idname = rgid + "/" + patientname;
                        Integer state = (Integer) map.get("state");
                        if (state == 2)
                            list.add(idname);
                        else
                            list2.add(idname);
                    }
                    cbbName.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                    cbbName.setSelectedIndex(0);

                    cbbName2.setModel(getColumnDataComboBoxModel("", list2.toArray(new String[list2.size()])));
                    cbbName2.setSelectedIndex(0);
                }
            }
        });

        listeners.add(new mListener("getRegiseterDetailById")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies)
                    {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        String idname = cbbName.getSelectedItem().toString();
                        String preid = idname.split("/")[0];
                        String departname = (String) map.get("departname");
                        String doctorname = (String) map.get("doctorname");
                        String regtypename = (String) map.get("regtypename");
                        labillness.setText(map.get("illness").toString());
                        setTextFieldText(new Object[]{preid, departname, doctorname, regtypename});
                    }
                }
            }
        });

        listeners.add(new mListener("getPrescription")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies)
                    {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        String name = (String) map.get("name");
                        String unitname = (String) map.get("unitname");
                        float outprice = (float) map.get("outprice");
                        Object number = map.get("number");
                        String feetypename = (String) map.get("feetypename");
                        int payment = (int) map.get("payment");
                        int give = (int) map.get("give");
                        if (payment == 1 && give == 0)
                            model.addRow(new Object[]{name, unitname, outprice, number, feetypename});
                    }
                    table.setModel(model);
                    btnDeMed.setEnabled(true);
                }
                else
                {
                    setTextFieldText(new String[]{"", "", "", ""});
                    model.setRowCount(0);
                    table.setModel(model);
                    btnDeMed.setEnabled(false);
                }
                lbResult.setText("已查询到" + table.getRowCount() + "条结果");
            }
        });

        listeners.add(new mListener("accountMedicine")
        {
            public void messageEvent(MessageEvent event)
            {
                btnDeMed.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    warning(deMedSuccess);
                    print.doClick();
                    btnFresh.doClick();
                }
                else
                {
                    warning(deMedFail);
                }
            }
        });

        listeners.add(new mListener("getPreList")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    preManage.addPreList(anies);
                    listmodel.setRowCount(0);
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        int state = (int) map.get("state");
                        if (state <= 1)
                            continue;
                        String stat = "未知";
                        if (state == 2)
                            stat = "待发药";
                        else if (state == 3)
                            stat = "已发药";


                        BigDecimal medfee =new BigDecimal(map.get("medfee").toString()).setScale(2,BigDecimal.ROUND_FLOOR);
                        BigDecimal curefee = new BigDecimal(map.get("curefee").toString()).setScale(2,BigDecimal.ROUND_FLOOR);
                        BigDecimal medis = new BigDecimal(map.get("medicinediscount").toString()).setScale(2,BigDecimal.ROUND_FLOOR);
                        BigDecimal mcdis = new BigDecimal(map.get("medcurediscount").toString()).setScale(2,BigDecimal.ROUND_FLOOR);

                        medfee = medfee.multiply(medis).setScale(2,BigDecimal.ROUND_FLOOR);
                        curefee = curefee.multiply(mcdis).setScale(2,BigDecimal.ROUND_FLOOR);

                        Vector vector = new Vector();
                        vector.add(map.get("pid"));
                        vector.add(medfee);
                        vector.add(curefee);
                        vector.add(map.get("dose"));
                        vector.add(stat);
                        int id = (int) map.get("pid");
                        st.put(id, state);
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

        // 得到处方详情
        listeners.add(new mListener("getPreInfoByPreId")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    model.setRowCount(0);
                    List<Any> anies = feedback.getDetailsList();
                    preManage.setMedcineList(anies);
                    Pre p = preManage.getPre(pid);
                    labdiscription.setText(p.getDoctor_Advice());
                    for (Any temp : anies)
                    {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        Object name = map.get("name");
                        Object unitname = map.get("unitname");
                        BigDecimal outprice = new BigDecimal(map.get("outprice").toString()) ;
                        BigDecimal number = new BigDecimal(map.get("number").toString());
                        Object type = map.get("type");
                        BigDecimal sum = outprice.multiply(number);
                        model.addRow(new Object[]{name, unitname, outprice, number, type, sum});
                    }
                    table.setModel(model);
                    lbResult.setText("已查询到" + table.getRowCount() + "条结果");
                    if (table.getRowCount() == 0)
                        btnDeMed.setEnabled(false);
                    else
                        btnDeMed.setEnabled(true);
                }
            }
        });

    }

    // 下拉菜单监听器
    private void comboBoxListeners()
    {
        // 单据编号选择
        cbbName.addActionListener((ActionEvent e) ->
        {
            int index = cbbName.getSelectedIndex();
            listmodel.setRowCount(0);
            if (index == 0)
            {
                setTextFieldText(new String[]{"", "", "", ""});
                labdiscription.setText("");
                labillness.setText("");
                model.setRowCount(0);
                table.setModel(model);
                lbResult.setText("已查询到" + table.getRowCount() + "条结果");
                return;
            }
            String idname = cbbName.getSelectedItem().toString();
            cbbName2.setSelectedIndex(0);
            Map<String, Object> map = new HashMap<>();
            String id = idname.split("/")[0];
            map.put("id", id);
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("getRegiseterDetailById", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            map.clear();
            map.put("rgid", id);
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("getPreList", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });
    }

    // 按钮监听器
    private void buttonListeners()
    {
        // 刷新按钮
        btnFresh.addActionListener((ActionEvent e) ->
        {
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("getIdByMedstate", null).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            setTextFieldText(new String[]{"", "", "", ""});
            model.setRowCount(0);
            table.setModel(model);
        });

        // 确认发药
        btnDeMed.addActionListener((ActionEvent e) ->
        {
            Map<String, Object> map = new HashMap<>();
            for (int row : listTable.getSelectedRows())
            {
                int pid = (int) listmodel.getValueAt(row, 0);
                int state = st.get(pid);
                if (state != 2)
                {
                    warning("处方：" + pid + " 不能发药!");
                    return;
                }
                map.put(String.valueOf(pid), null);
            }
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("accountMedicine", map).toByteArray());
                btnDeMed.setEnabled(false);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });
    }

    // 病人信息显示
    private void setTextFieldText(Object[] data)
    {
        lbDepart.setText("<html><u>" + data[1].toString() + "</u></html>");
        lbDoctor.setText("<html><u>" + data[2].toString() + "</u></html>");
        lbRegType.setText("<html><u>" + data[3].toString() + "</u></html>");
    }
}
