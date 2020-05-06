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
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.ChineseToPinyin.getPinYinHeadChar;
import static tool.Headers.medicineCn;
import static tool.Regex.floatRegex;
import static tool.Strings.*;
import static tool.Tool.*;

public class Medicine
{
    public JPanel pnMedicine;
    private Table table;
    private JTextField txtName;
    private JComboBox cbbUnit;
    private JTextField txtSellPrice;
    private JTextField txtInPrice;
    private JComboBox cbbType;
    private JComboBox cbbFactory;
    private JTextField txtStock;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnImport;
    private JButton btnExport;
    private JLabel lbRecord;
    private JCheckBox chkAdd;
    private JCheckBox chkModify;
    private JComboBox cbbUse;
    private JButton danButton;
    private JComboBox isInclude;
    private JComboBox itemGread;
    private JTextField itemCode;
    private JTextField textName2;
    private int rowindex = 1;
    private int rowmax = 0;
    private int failrows = 0;
    private DefaultTableModel model;
    private DefaultTableModel danModel;
    private Object[] tempRow;
    private DefaultTableModel notOkModel;
    private Workbook workbook;
    private Sheet sheet;
    private List<mListener> listeners = new ArrayList<>();
    private Map<String, Integer> unitMap = new HashMap<>();
    private Map<String, Integer> typeMap = new HashMap<>();
    private Map<String, Integer> factoryMap = new HashMap<>();
    private Map<String, Integer> medicineMap = new HashMap<>();
    private boolean isdan = false;
    private boolean flag;
    private int chooseId = -1;

    public Medicine()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        buttonListeners();
        textFieldListeners();
        chkAdd.addActionListener((ActionEvent e) ->
        {
            setComponentText(new Object[]{"", "", "", "", "", "", "", "", "", "", "",""});
            btnAdd.setText("确认添加");
        });
        chkModify.addActionListener((ActionEvent e) ->
        {
            setComponentText(new Object[]{"", "", "", "", "", "", "", "", "", "", "",""});
            btnAdd.setText("确认修改");

        });

        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                choose();
            }
        });


        danButton.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (isdan == false)
                {
                    table.setModel(danModel);
                    table.repaint();
                    isdan = true;
                    lbRecord.setText("共" + danModel.getRowCount() + "条数据");
                    danButton.setText("查看所有药品");
                }
                else
                {
                    table.setModel(model);
                    table.repaint();
                    isdan = false;
                    lbRecord.setText("共" + model.getRowCount() + "条数据");
                    danButton.setText("查看警告药品");
                }

            }
        });

    }

    // 初始化用户界面
    private void initUI()
    {
        // 初始化表格
        model = new DefaultTableModel(medicineCn, 0);
        danModel = new DefaultTableModel(medicineCn, 0);
        notOkModel = new DefaultTableModel(medicineCn, 0);
        table.setModel(model);
        // 获取全部药品信息
        try
        {
            Connect.sendMessage(buildMessage.doFunction("getMedicine", null).toByteArray());
            Map<String, Object> map = new HashMap<>();
            map.put("table", "unit");
            map.put("functionName", "unit");
            Connect.sendMessage(buildMessage.doFunction("getTable", map).toByteArray());
            map.clear();
            map.put("table", "feetype");
            map.put("functionName", "feetype");
            Connect.sendMessage(buildMessage.doFunction("getTable", map).toByteArray());
            map.clear();
            map.put("table", "factory");
            map.put("functionName", "factory");
            Connect.sendMessage(buildMessage.doFunction("getTable", map).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(chkAdd);
        buttonGroup.add(chkModify);

        // 拼音码
        new hintWindow(txtName)
        {
            public void keyTyped(KeyEvent e)
            {
            }

            public void keyPressed(KeyEvent e)
            {
            }


            public void keyReleased(KeyEvent e)
            {
                if (txtName.getText().equals(""))
                {
                    clear();
                    return;
                }
                if (e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    getMedModel();
            }

            @Override
            protected void setText(String selectedValue)
            {
                chooseMedicine(selectedValue);
            }
        };

    }

    // 消息监听器
    private void messageListeners()
    {
        listeners.add(new mListener("getMedicine")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    model.setRowCount(0);
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies)
                    {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        String name = (String) map.get("name");
                        String name2 = map.get("name2").toString();
                        medicineMap.put(name, (int) map.get("id"));
                        String unitname = (String) map.get("unitname");
                        String itemCode = (String) map.get("itemCode");
                        String itemGrade = (String) map.get("itemGrade");
                        BigDecimal inprice = new BigDecimal(map.get("inprice").toString());
                        BigDecimal outprice = new BigDecimal(map.get("outprice").toString());
                        String factoryname = (String) map.get("factoryname");
                        BigDecimal stock = new BigDecimal(map.get("stock").toString());
                        BigDecimal warn = null;
                        if (map.get("warn").equals(""))
                        {
                            warn = null;
                        }
                        else
                        {
                            warn = new BigDecimal(map.get("warn").toString());
                        }
                        String spell = (String) map.get("spell");
                        String feetypename = (String) map.get("feetypename");
                        String useful = isSelected(trueOrFalse((Integer) map.get("useful")));
                        String isInclude = null;
                        if (!map.containsKey("isInclude"))
                        {
                            isInclude = null;
                        }
                        else
                        {
                            isInclude = isSelected(trueOrFalse(Integer.valueOf(map.get("isInclude").toString())));
                        }
                        model.addRow(new Object[]{name,name2, itemCode, unitname, inprice, outprice, factoryname, stock, warn, spell, feetypename, useful, isInclude, itemGrade});
                        if (stock.compareTo(warn) != 1)
                            danModel.addRow(new Object[]{name,name2, itemCode, unitname, inprice, outprice, factoryname, stock, warn, spell, feetypename, useful, isInclude, itemGrade});
                    }
                    table.setModel(model);
                }
                lbRecord.setText("已查询到" + model.getRowCount() + "条信息");
            }
        });

        listeners.add(new mListener("unit")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                        unitMap.put((String) map.get("name"), (Integer) map.get("id"));
                    }
                    cbbUnit.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("feetype")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                        typeMap.put((String) map.get("name"), (Integer) map.get("id"));
                    }
                    cbbType.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("factory")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                        factoryMap.put((String) map.get("name"), (Integer) map.get("id"));
                    }
                    cbbFactory.setModel(getColumnDataComboBoxModel("", list.toArray(new String[list.size()])));
                }
            }
        });

        listeners.add(new mListener("addMedicine")
        {
            public void messageEvent(MessageEvent event)
            {
                btnAdd.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    Object[] data = getUserInput();
                    model.addRow(data);
                    table.setModel(model);
                    lbRecord.setText("已查询到" + model.getRowCount() + "条信息");
                    warning(addSuccess);
                    // 保存成功，清空，可继续添加
                    setComponentText(new String[]{"", "", "", "", "", "", "", "", "", "", "",""});
                }
                else
                {
                    warning(addFail);
                }
                Connect.sendMessage(buildMessage.doFunction("getMedicine", null).toByteArray());
            }
        });

        listeners.add(new mListener("updateMedicine")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                btnAdd.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    warning("药品信息修改成功！");
                    Connect.sendMessage(buildMessage.doFunction("getMedicine", null).toByteArray());
                    setComponentText(new Object[]{"", "", "", "", "", "", "", "", "", "", "",""});
                }
                else
                {
                    warning("药品信息修改失败，请检查您的网络设置！");
                }
            }
        });

        listeners.add(new mListener("addMedicineByExcel")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    model.addRow(tempRow);
                    table.setModel(model);
                }
                else
                {
                    notOkModel.addRow(tempRow);
                    failrows++;
                }
                if (rowindex < rowmax)
                {
                    getSheet();
                }
                else
                {
                    warning(addSuccess);
                    if (failrows > 0)
                    {
                        try
                        {
                            new ExcelExporter().exportFile("C:\\Users\\" + System.getProperty("user.name") +
                                    "\\Desktop", "药品信息错误记录", medicineCn, notOkModel);
                        }
                        catch (Exception e1)
                        {
                            warning(fileError);
                            e1.printStackTrace();
                        }
                        warning("导入失败" + failrows + "条记录，失败记录导出路径为" + "C:\\Users\\" +
                                System.getProperty("user.name") + "\\Desktop\\" + "药品信息错误记录.xls");
                    }
                }
            }
        });

        listeners.add(new mListener("deleteMedicine")
        {
            public void messageEvent(MessageEvent event)
            {
                btnDelete.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    Connect.sendMessage(buildMessage.doFunction("getMedicine", null).toByteArray());
                    setComponentText(new Object[]{"", "", "", "", "", "", "", "", "", "", "",""});
                    tool.Tool.warning(deleteSuccess);
                    lbRecord.setText("已查询到" + model.getRowCount() + "条信息");
                }
                else
                {
                    warning(deleteFail);
                }
            }
        });


        listeners.add(new mListener("getMedicineBySpell")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    List<String> list = new ArrayList<>();
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        String name = (String) map.get("name");
                        String type = (String) map.get("type");
                        if (type.equals("药品费"))
                            list.add(name);
                    }
                    hintWindow hintWindow = UI.hintWindow.map.get(txtName);
                    if (list.size() == 0)
                        hintWindow.setVisible(false);
                    else
                        hintWindow.updateList(list.toArray(new String[list.size()]));
                }
            }
        });

    }

    // 按钮监听器
    private void buttonListeners()
    {
        // 保存提交
        btnAdd.addActionListener((ActionEvent e) ->
        {
            // 输入验证
            if (inputVerifier())
            {
                warning(inputError);
                return;
            }
            // 获取输入
            Object[] data = getUserInput();
            Object unitId = unitMap.get(data[2]);
            Object factoryId = factoryMap.get(data[5]);
            Object feeTypeId = typeMap.get(data[9]);
            Map<String, Object> map = new HashMap<>();
            map.put("itemCode", data[1]);
            map.put("name", data[0]);
            map.put("unitId", unitId);
            map.put("inprice", data[3]);
            map.put("outprice", data[4]);
            map.put("factoryId", factoryId);
            map.put("stock", data[6]);
            map.put("warn", data[7]);
            map.put("spell", data[8]);
            map.put("feetypeId", feeTypeId);
            map.put("type", "药品费");
            map.put("useful", trueorfalse(data[10]));
            map.put("isInclude", trueorfalse(data[11]));
            map.put("itemGrade", data[12]);
            map.put("name2",textName2.getText());

            String function = "addMedicine";
            if (chkModify.isSelected())
            {
                map.remove("stock");
//                if (isdan)
//                    map.put("id", medicineMap.get(danModel.getValueAt(table.getSelectedRow(), 0).toString()));
//                else
//                    map.put("id", medicineMap.get(model.getValueAt(table.getSelectedRow(), 0).toString()));
                map.put("id", chooseId);
                function = "updateMedicine";
            }
            try
            {
                btnAdd.setEnabled(false);
                Connect.sendMessage(buildMessage.doFunction(function, map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        // 删除选中
        btnDelete.addActionListener((ActionEvent e) ->
        {
            if (table.getSelectedRow() == -1)
            {
                warning(selectOne);
                return;
            }
            if (confirm(askDelete) == JOptionPane.OK_OPTION)
            {
                int[] rows = table.getSelectedRows();
                for (int row : rows)
                {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", medicineMap.get(table.getValueAt(row, 0).toString()));
                    try
                    {
                        btnDelete.setEnabled(false);
                        Connect.sendMessage(buildMessage.doFunction("deleteMedicine", map).toByteArray());
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }

        });

        // excel导入
        btnImport.addActionListener((ActionEvent e) ->
        {
            rowindex = 1;
            rowmax = 0;
            failrows = 0;
            notOkModel.setRowCount(0);
            ExcelImporter excelImporter = new ExcelImporter();
            String path = excelImporter.selectFile();
            try
            {
                // 创建文件对象
                File file = new File(path);
                // 获取文件
                workbook = Workbook.getWorkbook(file);
                sheet = workbook.getSheet(0);
                rowmax = sheet.getRows();
                getSheet();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        // excel导出
        btnExport.addActionListener((ActionEvent e) ->
        {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null)
            {
                try
                {
                    excelExporter.exportFile(path, "药品信息表", medicineCn, model);
                }
                catch (Exception e1)
                {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    // 获得excel表格内容
    private void getSheet()
    {
        int i = rowindex;
        String name = sheet.getCell(0, i).getContents().trim();
        String name2 = sheet.getCell(1,i).getContents().trim();
        String itemCode = sheet.getCell(2, i).getContents().trim();
        String unitname = sheet.getCell(3, i).getContents().trim();
        int unitId = unitMap.get(unitname);
        //float inprice = Float.parseFloat(sheet.getCell(3, i).getContents().trim());
        BigDecimal inprice = new BigDecimal(sheet.getCell(4, i).getContents().trim());
        //float outprice = Float.parseFloat(sheet.getCell(4, i).getContents().trim());
        BigDecimal outprice = new BigDecimal(sheet.getCell(5, i).getContents().trim());
        String factory = sheet.getCell(6, i).getContents().trim();
        int factoryId = factoryMap.get(factory);
        float warn = Float.parseFloat(sheet.getCell(8, i).getContents().trim());
        String spell = sheet.getCell(9, i).getContents().trim();
        if (spell.equals(""))
        {
            spell = getPinYinHeadChar(name);
        }
        String feename = sheet.getCell(10, i).getContents().trim();
        int feeId = typeMap.get(feename);
        String useful = sheet.getCell(11, i).getContents().trim();
        String isInclude = sheet.getCell(12, i).getContents().trim();
        String itemGrade = sheet.getCell(13, i).getContents().trim();
        tempRow = new Object[]{name,name2, itemCode, unitname, inprice, outprice, factory, 0, warn, spell, feename, useful, isInclude, itemGrade};
        rowindex++;
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("itemCode", itemCode);
        map.put("unitId", unitId);
        map.put("inprice", inprice);
        map.put("outprice", outprice);
        map.put("feetypeId", feeId);
        map.put("factoryId", factoryId);
        map.put("stock", 0);
        map.put("warn", warn);
        map.put("spell", spell);
        map.put("type", "药品费");
        map.put("useful", trueorfalse(useful));
        map.put("isInclude", trueorfalse(isInclude));
        map.put("itemGrade", itemGrade);
        try
        {
            Connect.sendMessage(buildMessage.doFunction("addMedicineByExcel", map).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void textFieldListeners()
    {
        txtName.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER )
                {
                    itemCode.grabFocus();
                }
            }
        });
        txtInPrice.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtSellPrice.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    itemCode.grabFocus();
                }
            }
        });

        txtSellPrice.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtStock.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtInPrice.grabFocus();
                }
            }
        });

        txtStock.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    btnAdd.doClick();
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtSellPrice.grabFocus();
                }
            }
        });
        itemCode.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtInPrice.grabFocus();
                }
            }
        });
    }

    // 输入验证
    private boolean inputVerifier()
    {
        // 输入为空
        if (txtName.getText().equals("") || cbbUnit.getSelectedIndex() == 0 || txtInPrice.getText().equals("") ||
                txtSellPrice.getText().equals("") || cbbFactory.getSelectedIndex() == 0 ||
                txtStock.getText().equals("") || cbbType.getSelectedIndex() == 0 || cbbUse.getSelectedIndex() == 0)
        {
            return true;
        }
        Object[] data = getUserInput();
        String inprice = data[3].toString();
        String outprice = data[4].toString();
        String stock = data[7].toString();
        // 如果输入不符合正则表达式
        if (!inprice.matches(floatRegex) || !outprice.matches(floatRegex) || !stock.matches(floatRegex))
        {
            return true;
        }
        // 如果进价大于售价
        try
        {
            float inPrice = Float.parseFloat(inprice);
            float outPrice = Float.parseFloat(outprice);
            if (inPrice > outPrice)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            return true;
        }
        return false;
    }

    // 设置组件的文本内容
    private void setComponentText(Object[] data)
    {
        itemCode.setText(data[0].toString());
        txtName.setText(data[1].toString());
        cbbUnit.setSelectedItem(data[2]);
        txtInPrice.setText(data[3].toString());
        txtSellPrice.setText(data[4].toString());
        cbbFactory.setSelectedItem(data[5]);
        txtStock.setText(data[6].toString());
        cbbType.setSelectedItem(data[7]);
        cbbUse.setSelectedItem(data[8]);
        isInclude.setSelectedItem(data[9]);
        itemGread.setSelectedItem(data[10]);
        textName2.setText(data[11].toString());
    }

    // 获取用户输入信息
    private Object[] getUserInput()
    {
        Object item_code = itemCode.getText();
        Object name = txtName.getText();
        Object unit = cbbUnit.getSelectedItem();
        Object inPrice = Float.parseFloat(txtInPrice.getText());
        Object sellPrice = Float.parseFloat(txtSellPrice.getText());
        Object type = cbbType.getSelectedItem();
        Object factory = cbbFactory.getSelectedItem();
        Object stock = Float.parseFloat(txtStock.getText());
        Object spell = getPinYinHeadChar(txtName.getText());
        Object useful = cbbUse.getSelectedItem();
        Object item_gread = itemGread.getSelectedItem();
        Object is_include = isInclude.getSelectedItem();
        return new Object[]{name, item_code, unit, inPrice, sellPrice, factory, "0", stock, spell, type, useful, is_include, item_gread};
    }

    private void getMedModel()
    {
        if (!chkModify.isSelected())
            return;
        Map<String, Object> map = new HashMap<>();
        map.put("spell", txtName.getText());
        try
        {
            if (txtName.getText() == null || txtName.getText().equals(""))
            {
                hintWindow hintWindow = UI.hintWindow.map.get(txtName);
                hintWindow.setVisible(false);
                return;
            }
            else
                Connect.sendMessage(buildMessage.doFunction("getMedicineBySpell", map).toByteArray());
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    private int getRow(String name)
    {
        int i = 0;
        for (i = 0; i < table.getRowCount(); i++)
            if (table.getValueAt(i, 0).equals(name))
                return i;
        return -1;
    }

    private void chooseMedicine(String name)
    {
        int row = getRow(name);
        if (row != -1)
        {
            table.changeSelection(row, 0, false, false);
            choose();
        }
    }

    private void choose()
    {
        if (chkModify.isSelected())
        {
            int index = table.getSelectedRow();
//            DefaultTableModel models;
//            if (isdan == false)
//                models = model;
//            else
//                models = danModel;
//            txtName.setText(models.getValueAt(index, 0).toString());
//            cbbUnit.setSelectedItem(models.getValueAt(index, 1));
//            txtInPrice.setText(models.getValueAt(index, 2).toString());
//            txtSellPrice.setText(models.getValueAt(index, 3).toString());
//            cbbFactory.setSelectedItem(models.getValueAt(index, 4));
//            txtStock.setText(models.getValueAt(index, 6).toString());
//            cbbType.setSelectedItem(models.getValueAt(index, 8));
//            cbbUse.setSelectedItem(models.getValueAt(index, 9));

            chooseId = medicineMap.get(table.getValueAt(index, 0).toString());
            txtName.setText(table.getValueAt(index, 0).toString());
            textName2.setText(table.getValueAt(index,1).toString());
            itemCode.setText(table.getValueAt(index, 2).toString());
            cbbUnit.setSelectedItem(table.getValueAt(index, 3));
            txtInPrice.setText(table.getValueAt(index, 4).toString());
            txtSellPrice.setText(table.getValueAt(index, 5).toString());
            cbbFactory.setSelectedItem(table.getValueAt(index, 6));
            txtStock.setText(table.getValueAt(index, 8).toString());
            cbbType.setSelectedItem(table.getValueAt(index, 10));
            cbbUse.setSelectedItem(table.getValueAt(index, 11));
            isInclude.setSelectedItem(table.getValueAt(index, 12));
            itemGread.setSelectedItem(table.getValueAt(index, 13));
        }
    }

    private void clear()
    {
        itemCode.setText("");
        txtName.setText("");
        cbbUnit.setSelectedItem("");
        txtInPrice.setText("");
        txtSellPrice.setText("");
        cbbFactory.setSelectedItem("");
        txtStock.setText("");
        cbbType.setSelectedItem("");
        cbbUse.setSelectedItem("");
        isInclude.setSelectedItem("");
        itemGread.setSelectedItem("");
    }

}
