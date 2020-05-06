package select;

import UI.Table;
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

import static tool.Headers.medSoldCn;
import static tool.Strings.dateError;
import static tool.Strings.fileError;
import static tool.Tool.getToday;
import static tool.Tool.warning;

public class MedSold {
    public JPanel pnMedSold;
    private JButton btnSearch;
    private JDatePicker startDate;
    private JDatePicker endDate;
    private Table table;
    private JButton btnExport;
    private JLabel lbRecord;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    public MedSold() {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        buttonListeners();
    }

    // 初始化界面
    private void initUI() {
        startDate.getFormattedTextField().setText(getToday());
        endDate.getFormattedTextField().setText(getToday());
        // 初始化表格
        model = new DefaultTableModel(medSoldCn, 0);
        table.setModel(model);
    }

    // 消息监听器
    private void messageListeners() {
        listeners.add(new mListener("getMedSold") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        Object name = map.get("name");
                        Object unitname = map.get("unitname");
                        Object inprice = map.get("inprice");
                        Object outprice = map.get("outprice");
                        Object number = map.get("number");
                        Object sumimprice = map.get("suminprice");
                        Object sumoutprice = map.get("sumoutprice");
                        Object income = map.get("income");
                        model.addRow(new Object[]{name, unitname, inprice, outprice, number, sumimprice, sumoutprice, income});
                    }
                    model.addRow(new Object[]{"合计"});
                    for (int i = 5; i < model.getColumnCount(); i++) {
                        float sum = 0;
                        int j = 0;
                        for (; j < model.getRowCount() - 1; j++) {
                            sum += Double.parseDouble(model.getValueAt(j, i).toString());
                        }
                        sum = (float)(Math.round(sum*100)/100.0);
                        model.setValueAt(sum, j, i);
                    }
                    table.setModel(model);
                }
                lbRecord.setText("已查询到" + (model.getRowCount() - 1) + "条记录");
            }
        });

        listeners.add(new mListener("getMedSoldByTime") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        Object name = map.get("name");
                        Object unitname = map.get("unitname");
                        Object inprice = map.get("inprice");
                        Object outprice = map.get("outprice");
                        Object number = map.get("number");
                        Object sumimprice = map.get("suminprice");
                        Object sumoutprice = map.get("sumoutprice");
                        Object income = map.get("income");
                        model.addRow(new Object[]{name, unitname, inprice, outprice, number, sumimprice, sumoutprice, income});
                    }
                    model.addRow(new Object[]{"合计"});
                    for (int i = 5; i < model.getColumnCount(); i++) {
                        float sum = 0;
                        int j = 0;
                        for (; j < model.getRowCount() - 1; j++) {
                            sum += Double.parseDouble(model.getValueAt(j, i).toString());
                        }
                        sum = (float)(Math.round(sum*100)/100.0);
                        model.setValueAt(sum, j, i);
                    }
                    table.setModel(model);
                }
                lbRecord.setText("已查询到" + (model.getRowCount() - 1) + "条记录");
            }
        });
    }

    // 按钮监听器
    private void buttonListeners() {
        btnSearch.addActionListener((ActionEvent e) -> {
            String start = startDate.getFormattedTextField().getText();
            String end = endDate.getFormattedTextField().getText();
            if (start.equals("") || end.equals("")) {
                warning(dateError);
                return;
            }
            if (!start.equals("") && !end.equals("")) {
                start += " 00:00:00";
                end += " 23:59:59";
            } else return;
            Map<String, Object> map = new HashMap<>();
            map.put("start", start);
            map.put("end", end);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("getMedSoldByTime", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        btnExport.addActionListener((ActionEvent e) -> {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "药品销售记录", medSoldCn, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    private double ceil(double x) {
        int temp = (int) (x * 100);
        x = (double) temp / 100.0;
        x = Math.ceil(x);
        return x;
    }
}
