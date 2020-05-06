package select;

import UI.Table;
import UI.mTableModel;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
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

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.getAndLose;
import static tool.Strings.fileError;
import static tool.Tool.warning;

public class GetAndLose {
    public JPanel pnProfit;
    private Table table;
    private JButton btnExport;
    private JLabel lbRecord;
    private JComboBox cbbDate;
    private JLabel lbProfit;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    public GetAndLose() {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        comboBoxListeners();
        buttonListeners();
    }

    private void initUI() {
        model = new mTableModel(getAndLose, 0);
        table.setModel(model);
        try {
            Connect.sendMessage(tcp.buildMessage.doFunction("getProfitDate", null).toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void comboBoxListeners() {
        cbbDate.addActionListener((ActionEvent e) -> {
            if (cbbDate.getSelectedIndex() == 0) {
                model.setRowCount(0);
                table.setModel(model);
                lbRecord.setText("已查询到0条记录");
                lbProfit.setText("总计损益金额：" + 0 + "元");
                return;
            }
            String id = cbbDate.getSelectedItem().toString();
            id = id.split("/")[0];
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("getProfitByDate", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
    }

    private void buttonListeners() {
        btnExport.addActionListener((ActionEvent e) -> {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "损益记录", getAndLose, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    private void messageListeners() {
        listeners.add(new mListener("getProfitDate") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        String id = map.get("id").toString();
                        String date = map.get("date").toString().split(" ")[0];
                        list.add(id + "/" + date);
                    }
                    cbbDate.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("getProfitByDate") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                BigDecimal sum = BigDecimal.ZERO;
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        String medname = (String) map.get("medicinename");
                        String outprice = (String) map.get("outprice");
                        BigDecimal inprice = new BigDecimal(map.get("inprice").toString()) ;
                        BigDecimal number = new BigDecimal( map.get("number").toString());
                        BigDecimal profit = inprice.multiply(number);
                        sum = sum.add(profit);
                        String posname = (String) map.get("positionname");
                        model.addRow(new Object[]{medname, outprice, inprice, number, profit, posname});
                    }
                    table.setModel(model);
                    lbRecord.setText("已查询到" + model.getRowCount() + "条记录");
                    lbProfit.setText("总计损益金额：" + sum + "元");
                }
            }
        });
    }


}
