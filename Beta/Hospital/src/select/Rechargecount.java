package select;

import UI.Table;
import UI.mTableModel;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.Headers.rechargeRecord;
import static tool.Strings.dateError;
import static tool.Strings.fileError;
import static tool.Tool.getToday;
import static tool.Tool.warning;

public class Rechargecount {
    public JPanel pnRechargeCount;
    private Table table;
    private JLabel lbRecord;
    private JButton btnExport;
    private JButton btnSearch;
    private JDatePicker startDate;
    private JDatePicker endDate;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    public Rechargecount() {
        messageManager.removeAllMessageListener();
        initUI();
        buttonListeners();
        messageListeners();
    }

    private void initUI() {
        startDate.getFormattedTextField().setText(getToday());
        endDate.getFormattedTextField().setText(getToday());
        model = new mTableModel(rechargeRecord, 0);
        table.setModel(model);
    }

    private void buttonListeners() {
        btnSearch.addActionListener((ActionEvent e) -> {
            String start = startDate.getFormattedTextField().getText();
            String end = endDate.getFormattedTextField().getText();
            if (start.equals("") || end.equals("")) {
                warning(dateError);
                return;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("start", start);
            map.put("end", end);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("getRechargeCounts", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        btnExport.addActionListener((ActionEvent e) -> {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "会员卡充值统计", rechargeRecord, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    private void messageListeners() {
        listeners.add(new mListener("getRechargeCounts") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                model.setRowCount(0);
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        Object id = map.get("card");
                        Object money = map.get("remain");
                        Object present = map.get("present");
                        Object date = map.get("date");
                        model.addRow(new Object[]{id, money, present, date});
                    }
                    model.addRow(new Object[]{"合计"});
                    for (int i = 1; i < 3; i++) {
                        double sum = 0;
                        int j = 0;
                        for (; j < model.getRowCount() - 1; j++) {
                            sum += Double.parseDouble(model.getValueAt(j, i).toString());
                        }
                        model.setValueAt(sum, j, i);
                    }
                    table.setModel(model);
                    lbRecord.setText("已查询到" + (table.getRowCount() - 1) + "条记录");
                }
            }
        });
    }
}
