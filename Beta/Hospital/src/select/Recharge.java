package select;

import UI.Table;
import UI.mTableModel;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.Headers.consumeRecord;
import static tool.Strings.fileError;
import static tool.Tool.warning;

public class Recharge {
    public JPanel pnRecharge;
    private Table table;
    private JButton btnSearch;
    private JTextField txtCard;
    private JLabel lbRemain;
    private JLabel lbGift;
    private JLabel lbRecord;
    private JButton btnExport;
    private JLabel lbGrade;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    public Recharge() {
        messageManager.removeAllMessageListener();
        initUI();
        textFieldListeners();
        buttonListeners();
        messageListeners();
    }

    private void initUI() {
        model = new mTableModel(consumeRecord, 0);
        table.setModel(model);
    }

    private void textFieldListeners() {
        txtCard.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnSearch.doClick();
                }
            }
        });
    }

    private void buttonListeners() {
        btnSearch.addActionListener((ActionEvent e) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", txtCard.getText());
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("selectVipByPhoneOrId", map).toByteArray());
                Connect.sendMessage(tcp.buildMessage.doFunction("vipRecord", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        btnExport.addActionListener((ActionEvent e) -> {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "会员卡充值消费记录", consumeRecord, model);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    private void messageListeners() {
        listeners.add(new mListener("selectVipByPhoneOrId") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        lbRemain.setText("<html><u>" + String.valueOf(map.get("remain")) + "</u></html>");
                        lbGift.setText("<html><u>" + String.valueOf(map.get("present")) + "</u></html>");
                        lbGrade.setText("<html><u>" + String.valueOf(map.get("consume")) + "</u></html>");
//                        String state = map.get("useful").toString();
//                        if(state.equals("2")) {
//                            state = "可用";
//                        } else  {
//                            state = "挂失";
//                        }
//                        lbState.setText("<html><u>" + state + "</u></html>");
                    }
                } else {
                    lbRemain.setText("");
                    lbGift.setText("");
                    lbGrade.setText("");
                    model.setRowCount(0);
                    table.setModel(model);
                    lbRecord.setText("已查询到0条记录");
                }
            }
        });

        listeners.add(new mListener("vipRecord") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                model.setRowCount(0);
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies) {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        double money = Double.parseDouble(map.get("sum").toString());
                        double charge = 0, consume = 0;
                        if (money > 0) {
                            charge = money;
                        } else if (money < 0) {
                            consume = -money;
                        }
                        String date = (String) map.get("date");
                        model.addRow(new Object[]{charge, consume, date});
                    }
                    table.setModel(model);
                    lbRecord.setText("已查询到" + table.getRowCount() + "条记录");
                }
            }
        });
    }
}
