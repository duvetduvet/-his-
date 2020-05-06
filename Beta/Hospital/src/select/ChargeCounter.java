package select;

import UI.Table;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import tool.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.Strings.dateError;
import static tool.Strings.fileError;
import static tool.Tool.getToday;
import static tool.Tool.warning;

public class ChargeCounter {
    public JPanel pnChargeCounter;
    private JButton btnSearch;
    private JDatePicker startDate;
    private JDatePicker endDate;
    private JCheckBox chkDepart;
    private Table table;
    private JButton btnExport;
    private JCheckBox chkDoctor;
    private JLabel lbRecord;
    private DefaultTableModel model;
    private List<String> header = new ArrayList<>();
    private List<mListener> listeners = new ArrayList<>();

    public ChargeCounter() {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        checkBoxListeners();
        buttonListeners();
    }

    private void initUI() {
        startDate.getFormattedTextField().setText(getToday());
        endDate.getFormattedTextField().setText(getToday());
        header.add("科室");
        Map<String, Object> map = new HashMap<>();
        map.put("functionName", "feetype");
        map.put("table", "feetype");
        try {
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(chkDepart);
        buttonGroup.add(chkDoctor);
    }

    private void messageListeners() {
        listeners.add(new mListener("feetype") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        header.add((String) map.get("name"));
                    }
                    header.add("合计");
                    String[] head = header.toArray(new String[header.size()]);
                    model = new DefaultTableModel(head, 0);
                    table.setModel(model);
                }
                lbRecord.setText("已查询到" + model.getRowCount() + "条记录");
            }
        });

        listeners.add(new mListener("getCountByDepart") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                model.setRowCount(0);
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    if (anies.size() == 0) {
                        warning("没有数据！！！");
                        return;
                    }
                    getData(anies);
                    lbRecord.setText("已查询到" + (model.getRowCount() - 1) + "条记录");
                } else
                    warning("查询失败："+ feedback.getBackMessage());

            }

        });

        listeners.add(new mListener("getCountByDoctor") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                model.setRowCount(0);
                if (feedback.getMark() >= 0) {
                    getData(feedback.getDetailsList());
                    lbRecord.setText("已查询到" + (model.getRowCount() - 1) + "条记录");
                }else {
                    warning("查询失败："+feedback.getBackMessage());
                }
            }
        });
    }

    private void checkBoxListeners() {
        chkDepart.addActionListener((ActionEvent e) -> {
            header.set(0, "科室");
            String[] head = header.toArray(new String[header.size()]);
            model = new DefaultTableModel(head, 0);
            table.setModel(model);
            lbRecord.setText("已查询到" + model.getRowCount() + "条记录");
        });

        chkDoctor.addActionListener((ActionEvent e) -> {
            header.set(0, "医生");
            String[] head = header.toArray(new String[header.size()]);
            model = new DefaultTableModel(head, 0);
            table.setModel(model);
            lbRecord.setText("已查询到" + model.getRowCount() + "条记录");
        });
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
            String function;
            if (chkDepart.isSelected()) {
                function = "getCountByDepart";
            } else {
                function = "getCountByDoctor";
            }
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction(function, map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        btnExport.addActionListener((ActionEvent e) -> {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "按收费项目汇总", header.toArray(new String[header.size()]), model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    private void getHead(Map<String, Object> map) {
        header.clear();
        map.remove("名称");
        header.add("名称");
        header.addAll(map.keySet());
        header.add("合计");
    }

    private void getData(List<Any> anies) {
        getHead(tcp.buildMessage.AnyToMap(anies.get(0)));
        // 动态生成列
        model = new DefaultTableModel(header.toArray(), 0);
        for (Any any : anies) {
            Map map = tcp.buildMessage.AnyToMap(any);
            List<Object> row = new ArrayList<>();
            BigDecimal sum = BigDecimal.ZERO;
            for (int i = 0; i < header.size() - 1; i++) {
                if (i != 0) {
                    BigDecimal temp = new BigDecimal(map.get(header.get(i)).toString()).setScale(2, 4);
                    sum = sum.add(temp);
                    row.add(new BigDecimal(map.get(header.get(i)).toString()).setScale(2, 4));
                } else
                    row.add(map.get(header.get(i)).toString());
            }
            row.add(sum);
            model.addRow(row.toArray());
        }
        // 计算纵向和
        List<Object> row = new ArrayList<>();
        row.add("合计");
        for(int i =1;i<header.size();i++){
            BigDecimal temp = BigDecimal.ZERO;
            for(int j = 0;j<model.getRowCount();j++) {
                temp = temp.add(new BigDecimal(model.getValueAt(j,i).toString()));
            }
            row.add(temp);
        }
        model.addRow(row.toArray());
        table.setModel(model);
    }
}