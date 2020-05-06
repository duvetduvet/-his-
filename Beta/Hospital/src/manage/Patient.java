package manage;

import UI.Table;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import com.google.protobuf.Any;
import tcp.buildMessage;
import tool.ExcelExporter;
import tool.Headers;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.SimpleDateFormat;
import java.util.*;

import static tool.Headers.medicineCn;
import static tool.Headers.patientInfo;
import static tool.Strings.fileError;
import static tool.Tool.warning;


public class Patient
{
    private Table pTable;
    public JPanel paitnetPanel;
    private JButton exportAll;
    private JButton exportPart;
    private JTextField top;
    private JTextField low;
    private JTextField name;
    private JTextField phone;
    private JButton getPatient;
    private JDatePicker start;
    private JDatePicker last;
    private List<mListener> listeners = new ArrayList<>();
    private DefaultTableModel model;

    public Patient()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        init();
        exportAll.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                ExcelExporter excelExporter = new ExcelExporter();
                String path = excelExporter.selectPath();
                if (path != null)
                {
                    try
                    {
                        excelExporter.exportFile(path, "病人信息表", patientInfo, model);
                    }
                    catch (Exception e1)
                    {
                        warning(fileError);
                        e1.printStackTrace();
                    }
                }
            }
        });

        exportPart.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                ExcelExporter excelExporter = new ExcelExporter();
                String path = excelExporter.selectPath();
                if (path != null)
                {
                    try
                    {
                        DefaultTableModel m = new DefaultTableModel(patientInfo, 0);
                        int[] rows = pTable.getSelectedRows();
                        for (int temp : rows)
                        {
                            Vector v = new Vector();
                            for (int n = 0; n <= 8; n++)
                            {
                                v.add(model.getValueAt(temp, n));
                            }
                            m.addRow(v);
                        }
                        excelExporter.exportFile(path, "病人信息表", patientInfo, m);
                    }
                    catch (Exception e1)
                    {
                        warning(fileError);
                        e1.printStackTrace();
                    }
                }
            }
        });
        getPatient.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                String st = start.getFormattedTextField().getText();
                String la = last.getFormattedTextField().getText();
                Map map = new HashMap();
                map.put("start", st);
                map.put("last", la);
                Connect.sendMessage(tcp.buildMessage.doFunction("getAllPatient", map).toByteArray());
            }
        });
    }

    private void messageListeners()
    {
        messageManager.removeAllMessageListener();
        listeners.add(new mListener("getAllPatient")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() > 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        Vector v = new Vector();
                        v.add(map.get("id"));
                        v.add(map.get("name"));
                        v.add(map.get("sex"));
                        v.add(map.get("birth"));
                        v.add(map.get("identity"));
                        v.add(map.get("phone"));
                        v.add(map.get("date"));
                        v.add(map.get("last"));
                        v.add(map.get("sum"));

                        if (low.getText() != null && !low.getText().equals(""))
                            if (Float.parseFloat(map.get("sum").toString()) < Float.parseFloat(low.getText()))
                                continue;

                        if (top.getText() != null && !top.getText().equals(""))
                            if (Float.parseFloat(map.get("sum").toString()) > Float.parseFloat(top.getText()))
                                continue;

                        if (name.getText() != null && !name.getText().equals(""))
                            if (!map.get("name").toString().equals(name.getText()))
                                continue;

                        if (phone.getText() != null && !phone.getText().equals(""))
                            if (!map.get("phone").toString().equals(phone.getText()))
                                continue;

                        model.addRow(v);
                    }
                    pTable.setModel(model);
                }
            }
        });
    }

    private void init()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        model = new DefaultTableModel(Headers.patientInfo, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        last.getFormattedTextField().setText(simpleDateFormat.format(calendar.getTime()));
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        start.getFormattedTextField().setText(simpleDateFormat.format(calendar.getTime()));
    }

}
