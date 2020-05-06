package manage;

import UI.Table;
import UI.hintWindow;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import jxl.Sheet;
import jxl.Workbook;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Table.getColumnDataComboBoxModel;
import static tool.ChineseToPinyin.getPinYinHeadChar;
import static tool.Headers.cureCn;
import static tool.Regex.floatRegex;
import static tool.Strings.*;
import static tool.Tool.*;

public class Cure
{
    public JPanel pnCure;
    private Table table;
    private JTextField txtName;
    private JComboBox cbbUnit;
    private JTextField txtSellPrice;
    private JComboBox cbbType;
    private JButton btnAdd;
    private JButton btnDelete;
    private JButton btnImport;
    private JButton btnExport;
    private JLabel lbRecord;
    private JCheckBox chkAdd;
    private JCheckBox chkModify;
    private JComboBox cbbUse;
    private JComboBox isInclude;
    private JComboBox itemGread;
    private JTextField itemCode;
    private JTextField txtName2;
    private int rowindex = 1;
    private int rowmax = 0;
    private int failrows = 0;
    private DefaultTableModel model;
    private Object[] tempRow;
    private DefaultTableModel notOkModel;
    private Workbook workbook;
    private Sheet sheet;
    private List<mListener> listeners = new ArrayList<>();
    private Map<String, Integer> unitMap = new HashMap<>();
    private Map<String, Integer> typeMap = new HashMap<>();
    private Map<String, Integer> medicineMap = new HashMap<>();

    public Cure()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        buttonListeners();
        textFieldListeners();
    }

    private void initUI()
    {
        // 初始化表格
        model = new DefaultTableModel(cureCn, 0);
        notOkModel = new DefaultTableModel(cureCn, 0);
        table.setModel(model);
        // 获取全部药品信息
        try
        {
            Connect.sendMessage(buildMessage.doFunction("getDiagnosis", null).toByteArray());
            Map<String, Object> map = new HashMap<>();
            map.put("table", "unit");
            map.put("functionName", "unit");
            Connect.sendMessage(buildMessage.doFunction("getTable", map).toByteArray());
            map.clear();
            map.put("table", "feetype");
            map.put("functionName", "feetype");
            Connect.sendMessage(buildMessage.doFunction("getTable", map).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(chkAdd);
        buttonGroup.add(chkModify);
        chkAdd.addActionListener((ActionEvent e) ->
        {
            setComponentText(new Object[]{"", "", "", "", "", "","","",""});
            btnAdd.setText("确认添加");
        });
        chkModify.addActionListener((ActionEvent e) ->
        {
            setComponentText(new Object[]{"", "", "", "", "", "","","",""});
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
        listeners.add(new mListener("getDiagnosis")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                model.setRowCount(0);
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    medicineMap.clear();
                    for (Any temp : anies)
                    {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        String name = (String) map.get("name");
                        String name2 = (String) map.get("name2");
                        medicineMap.put(name, (int) map.get("id"));
                        String itemCode = (String) map.get("itemCode");
                        String itemGread = (String) map.get("itemGrade");
                        String unitname = (String) map.get("unitname");
                        BigDecimal outprice = new BigDecimal(map.get("outprice").toString());
                        String feetypename = (String) map.get("feetypename");
                        String spell = (String) map.get("spell");
                        String useful = isSelected(trueOrFalse((Integer) map.get("useful")));
                        String isInclude = null;
                        if(map.get("isInclude").equals("")){
                            isInclude = null;
                        }else {
                            isInclude = isSelected(trueOrFalse((Integer) map.get("isInclude")));
                        }
                        model.addRow(new Object[]{name,name2,itemCode, unitname, outprice, spell, feetypename, useful,isInclude,itemGread});
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
                    setComponentText(new String[]{"", "", "", "", "", "","","",""});
                    Connect.sendMessage(buildMessage.doFunction("getDiagnosis", null).toByteArray());
                }
                else
                {
                    warning(addFail);
                }
            }
        });

        listeners.add(new mListener("deleteMedicine")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    int[] rows = table.getSelectedRows();
                    for (int i = 0; i < rows.length; i++)
                    {
                        model.removeRow(rows[i] - i);
                    }
                    table.setModel(model);
                    //lbRecord.setText("已查询到" + model.getRowCount() + "条信息");
                    warning(deleteSuccess);
                    clear();
                    Connect.sendMessage(buildMessage.doFunction("getDiagnosis", null).toByteArray());
                }
                else
                {
                    warning(deleteFail);
                }
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
                    warning("诊疗信息修改成功！");
                    clear();
                    Connect.sendMessage(buildMessage.doFunction("getDiagnosis", null).toByteArray());
                }
                else
                {
                    warning("诊疗信息修改失败，请检查您的网络设置！");
                }
            }
        });

        listeners.add(new mListener("addMedicineByExcel")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() > 0)
                {
                    model.addRow(tempRow);
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
                            new ExcelExporter().exportFile("C:\\Users\\" + System.getProperty("user.name") + "\\Desktop", "诊疗信息错误记录", cureCn, notOkModel);
                        }
                        catch (Exception e1)
                        {
                            warning(fileError);
                            e1.printStackTrace();
                        }
                        warning("导入失败" + failrows + "条记录，失败记录导出路径为" + "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\" + "诊疗信息错误记录.xls");
                    }
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
                        if (type.equals("诊疗费"))
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

    private void textFieldListeners()
    {
        txtName.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtName2.grabFocus();
                }
            }
        });

        txtName2.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if(e.getKeyCode() == KeyEvent.VK_UP){
                    txtName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    itemCode.grabFocus();
                }
            }
        });

        itemCode.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtSellPrice.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP){
                    txtName.grabFocus();
                }
            }
        });

        txtSellPrice.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    btnAdd.doClick();
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
            Object unitId = unitMap.get(data[3]);
            Object feeTypeId = typeMap.get(data[6]);
            Map<String, Object> map = new HashMap<>();
            map.put("name", data[0]);
            map.put("itemCode",data[2]);
            map.put("unitId", unitId);
            map.put("inprice", 0);
            map.put("outprice", data[4]);
            map.put("feetypeId", feeTypeId);
            map.put("spell", data[5]);
            map.put("type", "诊疗费");
            map.put("useful", trueorfalse(data[7]));
            map.put("isInclude",trueorfalse(data[8]));
            map.put("itemGrade",data[9]);
            map.put("name2",txtName2.getText());
            String function = "addMedicine";
            if (chkModify.isSelected())
            {
                map.put("id", medicineMap.get(model.getValueAt(table.getSelectedRow(), 0).toString()));
                function = "updateMedicine";
            }
            try
            {
                Connect.sendMessage(buildMessage.doFunction(function, map).toByteArray());
                btnAdd.setEnabled(false);
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
                    map.put("id", medicineMap.get(model.getValueAt(row, 0).toString()));
                    try
                    {
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
                    excelExporter.exportFile(path, "诊疗信息表", cureCn, model);
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
        String name = sheet.getCell(0, rowindex).getContents().trim();
        String name2 = sheet.getCell(1,rowindex).getContents().trim();
        String itemCode = sheet.getCell(2, rowindex).getContents().trim();
        String unitname = sheet.getCell(3, rowindex).getContents().trim();
        int unitId = unitMap.get(unitname);
        float outprice = Float.parseFloat(sheet.getCell(4, rowindex).getContents().trim());
        String spell = sheet.getCell(5, rowindex).getContents().trim();
        if (spell.equals(""))
        {
            spell = getPinYinHeadChar(name);
        }
        String feename = sheet.getCell(6, rowindex).getContents().trim();
        int feeId = typeMap.get(feename);
        String useful = sheet.getCell(7, rowindex).getContents().trim();
        String isInclude = sheet.getCell(8, rowindex).getContents().trim();
        String itemGrade = sheet.getCell(9, rowindex).getContents().trim();
        BigDecimal inprice = new BigDecimal("0") ;
        BigDecimal stock = new BigDecimal("0") ;
        BigDecimal warn = new BigDecimal("0") ;
        tempRow = new Object[]{name,name2,itemCode, unitname,inprice, outprice,null,stock,warn, spell, "诊疗费", useful,isInclude,itemGrade};
        rowindex++;
        Map<String, Object> map = new HashMap<>();
        /*map.put("name", name);
        map.put("itemCode",itemCode);
        map.put("unitId", unitId);
        map.put("outprice", outprice);
        map.put("inprice", 0);
        map.put("feetypeId", feeId);
        map.put("spell", spell);
        map.put("type", "诊疗费");
        map.put("useful", trueorfalse(useful));
        map.put("isInclude",isInclude);
        map.put("itemGrade",itemGrade);*/
        map.put("name", name);
        map.put("name2",name2);
        map.put("itemCode",itemCode);
        map.put("unitId", unitId);
        map.put("inprice", inprice);
        map.put("outprice", outprice);
        map.put("feetypeId", feeId);
        map.put("factoryId", 1);
        map.put("stock", stock);
        map.put("warn", warn);
        map.put("spell", spell);
        map.put("type", "诊疗费");
        map.put("useful", trueorfalse(useful));
        map.put("isInclude",trueorfalse(isInclude));
        map.put("itemGrade",itemGrade);
        try
        {
            Connect.sendMessage(buildMessage.doFunction("addMedicineByExcel", map).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // 输入验证
    private boolean inputVerifier()
    {
        if (txtName.getText().equals("") ||cbbUnit.getSelectedIndex() == 0 || txtSellPrice.getText().equals("") ||
                cbbType.getSelectedIndex() == 0 || cbbUse.getSelectedIndex() == 0 || itemGread.getSelectedIndex() == 0 || itemGread.getSelectedIndex()==0)
        {
            return true;
        }
        // 售价不匹配正则表达式
        if (!txtSellPrice.getText().matches(floatRegex))
        {
            return true;
        }
        return false;
    }

    // 设置组件的文本内容
    private void setComponentText(Object[] data)
    {
        txtName.setText(data[1].toString());
        itemCode.setText(data[2].toString());
        cbbUnit.setSelectedItem(data[2]);
        txtSellPrice.setText(data[3].toString());
        cbbType.setSelectedItem(data[4]);
        cbbUse.setSelectedItem(data[5]);
        isInclude.setSelectedItem(data[6]);
        itemGread.setSelectedItem(data[7]);
        txtName2.setText(data[8].toString());
    }

    // 获取用户输入信息
    private Object[] getUserInput()
    {
        Object item_Code = itemCode.getText();
        Object name = txtName.getText();
        Object name2 = txtName2.getText();
        Object unit = cbbUnit.getSelectedItem();
        Object sellPrice = txtSellPrice.getText();
        Object type = cbbType.getSelectedItem();
        Object spell = getPinYinHeadChar(txtName.getText());
        Object useful = cbbUse.getSelectedItem();
        Object is_Include = isInclude.getSelectedItem();
        Object item_Gread = itemGread.getSelectedItem();
        return new Object[]{name,name2,item_Code, unit, sellPrice, spell, type, useful,is_Include,item_Gread};
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

    private void choose()
    {
        if (chkModify.isSelected())
        {
            int index = table.getSelectedRow();
            txtName.setText(model.getValueAt(index, 0).toString());
            txtName2.setText(table.getValueAt(index,1).toString());
            itemCode.setText(model.getValueAt(index,2).toString());
            cbbUnit.setSelectedItem(model.getValueAt(index, 3));
            txtSellPrice.setText(model.getValueAt(index, 5).toString());
            cbbType.setSelectedItem(model.getValueAt(index, 6));
            cbbUse.setSelectedItem(model.getValueAt(index, 7));
            isInclude.setSelectedItem(model.getValueAt(index,8));
            itemGread.setSelectedItem(model.getValueAt(index,9));
        }
    }

    private void clear()
    {
        txtName.setText("");
        txtName2.setText("");
        itemCode.setText("");
        cbbUnit.setSelectedItem("");
        txtSellPrice.setText("");
        cbbType.setSelectedItem("");
        cbbUse.setSelectedItem("");
        isInclude.setSelectedItem("");
        itemGread.setSelectedItem("");
    }

    private int getRow(String name)
    {
        int i;
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

}
