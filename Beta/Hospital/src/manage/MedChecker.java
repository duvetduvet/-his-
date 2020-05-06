package manage;

import UI.Table;
import UI.hintWindow;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import jxl.Sheet;
import jxl.Workbook;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;
import tool.ExcelImporter;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.Headers.medCheckerCn;
import static tool.Strings.*;
import static tool.Tool.warning;

public class MedChecker {
    public JPanel pnSum;
    private Table table;
    private JComboBox cbbFactory;
    private JButton btnImportTable;
    private JComboBox cbbFeeType;
    private JButton btnUpload;
    private JButton btnImport;
    private JButton btnExport;
    private JLabel lbType;
    private JLabel lbMoney;
    private JTextField txtSpell;
    private JTextField txtNum;
    private JButton btnAdd;
    private JButton btnDel;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();

    // 药品名称 为key ， 药品详情为 value
    private Map<String, Object> medicineMap = new HashMap<>();

    public MedChecker() {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        tableListeners();
        buttonListeners();

    }

    // 初始化界面
    private void initUI() {
        model = new DefaultTableModel(medCheckerCn, 0);
        table.setModel(model);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("table", "factory");
            map.put("functionName", "factory");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
            map.clear();
            map.put("table", "feetype");
            map.put("functionName", "feetype");
            Connect.sendMessage(tcp.buildMessage.doFunction("getTable", map).toByteArray());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    // 消息监听器
    private void messageListeners() {
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

        listeners.add(new mListener("profit_view") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        Object id = map.get("id");
                        Object name = map.get("name");
                        Object unitname = map.get("unitname");
                        Object inprice = map.get("inprice");
                        Object feetypename = map.get("feetypename");
                        Object factoryname = map.get("factoryname");
                        Object stock = map.get("stock");
                        Object truestock = map.get("truestock");
                        Object number = map.get("number");
                        Object profit = map.get("profit");
                        model.addRow(new Object[]{id, name, unitname, inprice, feetypename, factoryname, stock, truestock, number, profit});
                    }
                    table.setModel(model);
                }
            }
        });

        listeners.add(new mListener("getAllByFactoryOrFeeType") {
            @Override
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    medicineMap.clear();
                    for (Any temp : anies) {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        Object name = map.get("name");
                        medicineMap.put(name.toString(), map.get("id"));
                        Object unitname = map.get("unitname");
                        Object inprice = map.get("inprice");
                        Object feetypename = map.get("feetypename");
                        Object factoryname = map.get("factoryname");
                        Object stock = map.get("stock");
                        Object truestock = map.get("truestock");
                        Object number = map.get("number");
                        Object profit = map.get("profit");
                        model.addRow(new Object[]{name, unitname, inprice, factoryname, stock, "尚未提交数据", "待计算", "待统计", feetypename});
                    }
                    table.setModel(model);
                }
            }
        });

        listeners.add(new mListener("resetStock") {
            @Override
            public void messageEvent(MessageEvent event) {
                btnUpload.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    warning(checkerSuccess);
                    lbType.setText("0种");
                    lbMoney.setText("0.00元");
                    model.setRowCount(0);
                } else
                    warning("盘点失败！");
            }
        });

        listeners.add(new mListener("getMedicineBySpell") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0) {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies) {
                        Map map = buildMessage.AnyToMap(temp);
                        String name = (String) map.get("name");
                        String outprice = map.get("outprice").toString();
                        Object stock = map.get("stock");
                        String type = map.get("type").toString();
                        if (type.equals("诊疗费"))
                            continue;
                        map.remove("name");
                        medicineMap.put(name, map);
                        list.add(name);
                    }
                    hintWindow hintWindow = UI.hintWindow.map.get(txtSpell);
                    if (list.size() == 0)
                        hintWindow.setVisible(false);
                    else
                        hintWindow.updateList(list.toArray(new String[list.size()]));

                }
            }
        });

    }

    // 表格监听器
    private void tableListeners() {
        model.addTableModelListener((TableModelEvent e) -> {
            if (model.getRowCount() != 0) {
                setButtonEnable(true);
            } else {
                setButtonEnable(false);
            }
        });
    }

    private void setButtonEnable(boolean state) {
        btnImport.setEnabled(state);
        btnExport.setEnabled(state);
        btnUpload.setEnabled(state);
    }

    // 按钮监听器
    private void buttonListeners() {
        // 药品导入
        btnImportTable.addActionListener((ActionEvent e) -> {
            Object factory = cbbFactory.getSelectedItem();
            if (factory == "全部") {
                factory = "%";
            }
            Object feetype = cbbFeeType.getSelectedItem();
            if (feetype == "全部") {
                feetype = "%";
            }
            Map<String, Object> map = new HashMap<>();
            map.put("factoryname", factory);
            map.put("feetypename", feetype);
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("getAllByFactoryOrFeeType", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        // 提交
        btnUpload.addActionListener((ActionEvent e) -> {
            int row = table.getRowCount();
            for (int i = 0; i < row; i++) {
                String t = table.getValueAt(i, 5).toString();
                if (t.equals("尚未提交数据")) {
                    warning(checkerNoSuccess);
                    return;
                }
            }
            try {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < row; i++) {
                    BigDecimal fact = new BigDecimal(model.getValueAt(i, 5).toString());
                    BigDecimal should = new BigDecimal(model.getValueAt(i,4).toString());

                    map.put(model.getValueAt(i, 0).toString(), fact.subtract(should));
                }
                btnUpload.setEnabled(false);
                Connect.sendMessage(tcp.buildMessage.doFunction("resetStock", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
                warning("盘点失败！！");
            }
        });

        // 导入表格
        btnImport.addActionListener((ActionEvent e) -> {
            ExcelImporter excelImporter = new ExcelImporter();
            String path = excelImporter.selectFile();
            try {
                // 创建文件对象
                File file = new File(path);
                // 获取文件
                Workbook workbook = Workbook.getWorkbook(file);
                Sheet sheet = workbook.getSheet(0);

                // 得到药品名称和数量
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("以下药品添加失败:");

                int i = 1, j = 0;
                for (; i < sheet.getRows(); i++) {
                    if (sheet.getCell(0, i).getContents().trim().equals("") == false) {
                        String name = sheet.getCell(0, i).getContents().trim();
                        try {
                            BigDecimal num = new BigDecimal(sheet.getCell(1, i).getContents());
                            if (!addMed(name, num))
                                throw new Exception();
                        } catch (Exception e1) {
                            j++;
                            stringBuffer.append(name + ",");
                        }
                    }
                }
                warning("导入药品完成：共计" + (i - 2) + "，失败：" + j + "\n" + stringBuffer.toString());


                // 关闭流
                workbook.close();
            } catch (Exception e1) {
                e1.printStackTrace();
                return;
            }
            setTypeAndMoney();
        });

        // 导出表格
        btnExport.addActionListener((ActionEvent e) -> {
            DefaultTableModel defaultTableModel = new DefaultTableModel(new String[]{"", ""}, 0);
            for (int i = 0; i < model.getRowCount(); i++) {
                Object[] objects = {model.getValueAt(i, 0), model.getValueAt(i, 5)};
                defaultTableModel.addRow(objects);
            }
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null) {
                try {
                    excelExporter.exportFile(path, "药品盘点", new String[]{"药品名称", "实余"}, defaultTableModel);
                } catch (Exception e1) {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });

        // 删除药品
        btnDel.addActionListener(e -> {
            if (table.getSelectedRow() != -1) {
                model.removeRow(table.convertRowIndexToModel(table.getSelectedRow()));
            } else {
                warning("请至少选择一种药品！");
            }


        });

        // 添加盘点药品
        btnAdd.addActionListener(e -> {

            try {
                if (!addMed(txtSpell.getText(), new BigDecimal(txtNum.getText())))
                    warning("添加失败");
                setTypeAndMoney();
            } catch (Exception e1) {
                warning("添加失败");
            }
            table.setModel(model);
            txtSpell.setText("");
            txtNum.setText("");
            txtSpell.grabFocus();
        });

        txtSpell.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    txtNum.grabFocus();
            }
        });

        txtNum.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnAdd.doClick();
                }
            }

        });

        new hintWindow(txtSpell) {
            public void keyTyped(KeyEvent e) {
            }

            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (txtSpell.getText().equals("")) {
                    hintWindow hintWindow = UI.hintWindow.map.get(txtSpell);
                    hintWindow.setVisible(false);
                    return;
                }
                if (e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    getMedModel();
            }

            @Override
            protected void setText(String selectedValue) {
                String medname = selectedValue.split("---")[0];
                txtSpell.setText(medname);
                txtNum.grabFocus();
            }
        };

    }

    // 通过拼音码获取药品列表
    private void getMedModel() {
        Map<String, Object> map = new HashMap<>();
        map.put("spell", txtSpell.getText());
        try {
            if (txtSpell.getText() == null || txtSpell.getText().equals("")) {
                hintWindow hintWindow = UI.hintWindow.map.get(txtSpell);
                hintWindow.setVisible(false);
                return;
            } else
                Connect.sendMessage(buildMessage.doFunction("getMedicineBySpell", map).toByteArray());
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    // 添加一个药品
    private boolean addMed(String name, BigDecimal num) {

        if (name.equals("") || num.equals(""))
            return false;
        // 判断是否有相同的药
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(name)) {
                model.setValueAt(num, i, 5);
                BigDecimal stock = new BigDecimal(model.getValueAt(i, 4).toString());
                BigDecimal number = num.subtract(stock);
                BigDecimal inprice = new BigDecimal(model.getValueAt(i, 2).toString());
                model.setValueAt(number, i, 6);
                model.setValueAt(number.multiply(inprice), i, 7);
                return true;
            }
        }
        // 添加药品
        Map map = (Map) medicineMap.get(name);
        BigDecimal inprice = new BigDecimal(map.get("inprice").toString());
        BigDecimal stock = new BigDecimal(map.get("stock").toString());
        if (map == null)
            return false;
        Vector vector = new Vector();
        vector.add(name);
        vector.add(map.get("unitname"));
        vector.add(inprice);

        vector.add(map.get("factoryname"));
        vector.add(stock);
        vector.add(num);
        vector.add(num.subtract(stock));
        vector.add(num.subtract(stock).multiply(inprice));
        vector.add(map.get("type"));
        model.addRow(vector);
        return true;
    }


    // 设置药品种类和损益金额
    private void setTypeAndMoney() {
        lbType.setText(table.getRowCount() + "种");
        BigDecimal money = BigDecimal.ZERO;
        for (int i = 0; i < table.getRowCount(); i++) {
            try {
                money = money.add(new BigDecimal(table.getValueAt(i, 7).toString()));
            } catch (Exception e) {
                // do nothing
            }
        }
        lbMoney.setText(money + "元");
    }
}
