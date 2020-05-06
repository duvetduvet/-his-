package select;

import UI.Table;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import tool.Headers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class patientVisits
{
    private JDatePicker startDate;
    private JDatePicker endDate;
    private JButton btnsearch;
    public JPanel panel;
    private Table table;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    public patientVisits()
    {

        init();
        btnsearch.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                String start = startDate.getFormattedTextField().getText() + " 00:00:00";
                String end = endDate.getFormattedTextField().getText() + " 23:59:59";
                Map<String, Object> map = new HashMap<>();
                map.put("start", start);
                map.put("end", end);
                try
                {
                    btnsearch.setEnabled(false);
                    Connect.sendMessage(tcp.buildMessage.doFunction("patientVisits", map).toByteArray());
                }
                catch (Exception e1)
                {
                    tool.Tool.warning("信息发送失败");
                }
            }
        });
    }

    private void init()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        model = new DefaultTableModel(Headers.patientVisit, 0);
    }

    private void messageListeners()
    {
        listeners.add(new mListener("patientVisits")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                btnsearch.setEnabled(true);
                // 返回 姓名 初诊次数 复诊次数
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    int i = 1;
                    model.setRowCount(0);
                    int m = 0,n = 0;
                    for (Any any : anies)
                    {
                        Map map = tcp.buildMessage.AnyToMap(any);
                        Integer first = Integer.valueOf((String) map.get("first"));
                        Integer second = Integer.valueOf((String) map.get("second"));
                        String phoneNumber = map.get("phone").toString();
                        m = m + first;
                        n = n + second;
                        model.addRow(new Object[]{i++, map.get("name"),phoneNumber, first, second, first + second});
                    }
                    model.addRow(new Object[]{"合计", "","", m, n, m + n});
                    table.setModel(model);
                }
                else
                    tool.Tool.warning("查询失败!!!");
            }
        });
    }
}
