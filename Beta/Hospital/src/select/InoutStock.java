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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.inoutStockCn;
import static tool.Strings.dateError;
import static tool.Strings.fileError;
import static tool.Tool.getToday;
import static tool.Tool.warning;

public class InoutStock {
    public JPanel pnInoutStock;
    private Table table;
    private JComboBox cbbOperate;
    private JComboBox cbbFactory;
    private JComboBox cbbFeeType;
    private JButton btnSearch;
    private JDatePicker startDate;
    private JDatePicker endDate;
    private JButton btnExport;
    private JLabel lbRecord;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    public InoutStock() {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        tableListeners();
    }

    private void initUI() {
        startDate.getFormattedTextField().setText(getToday());
        endDate.getFormattedTextField().setText(getToday());
        // 初始化表格
        model = new DefaultTableModel(inoutStockCn, 0);
        table.setModel(model);
        // 获取全部出入库信息
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("functionName", "stockrecord_view");
            map.put("table", "stockrecord_view");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
            map.clear();
            map.put("functionName", "factory");
            map.put("table", "factory");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
            map.clear();
            map.put("functionName", "feetype");
            map.put("table", "feetype");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void messageListeners() {
        listeners.add(new mListener("feetype") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                    }
                    cbbFeeType.setModel(getColumnDataComboBoxModel("全部", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("factory") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                    }
                    cbbFactory.setModel(getColumnDataComboBoxModel("全部", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getStockRecordByCondition") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        Object id = map.get("id");
                        Object medicinename = map.get("medicinename");
                        Object unitname = map.get("unitname");
                        Object inprice = map.get("inprice");
                        Object outprice = map.get("outprice");
                        Object feetypename = map.get("feetypename");
                        Object factoryname = map.get("factoryname");
                        Object number = map.get("number");
                        Object positionname = map.get("positionname");
                        Object date = map.get("date").toString().split(" ")[0];
                        model.addRow(new Object[]{id, medicinename, inprice, outprice, feetypename, factoryname, number, positionname, date});
                    }
                    table.setModel(model);
                }
                lbRecord.setText("已查询到" + model.getRowCount() + "条记录");
            }
        });
    }

    private void tableListeners() {
        // 搜索
        btnSearch.addActionListener((ActionEvent e) -> {
            String start = startDate.getFormattedTextField().getText();
            String end = endDate.getFormattedTextField().getText();
            if ((start.equals("") && !end.equals("")) || (!start.equals("") && end.equals(""))) {
                warning(dateError);
                return;
            }
            Object item = cbbOperate.getSelectedItem();
            String operate = "";
            if (item == "入库") {
                operate = "number > 0 AND ";
            } else if (item == "出库") {
                operate = "number < 0 AND ";
            }
            Object factory = cbbFactory.getSelectedItem();
            if (factory == "全部") {
                factory = "%";
            }
            Object feetype = cbbFeeType.getSelectedItem();
            if (feetype == "全部") {
                feetype = "%";
            }
            String date = "date LIKE '%'";
            if (!start.equals("") && !end.equals("")) {
                date = "(date >= '" + start + "' AND date <= '" + end + "')";
            }
            Map<String, Object> map = new HashMap<>();
            map.put("operate", operate);
            map.put("factory", factory);
            map.put("feetype", feetype);
            map.put("date", date);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("getStockRecordByCondition", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // 批量导出
        btnExport.addActionListener((ActionEvent e) -> {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "药品出入库信息表", inoutStockCn, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }
}
