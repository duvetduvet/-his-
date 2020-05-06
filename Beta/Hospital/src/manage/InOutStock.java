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
import tool.ExcelExporter;
import tool.ExcelImporter;
import tool.UserInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tool.ChineseToPinyin.getPinYinHeadChar;
import static tool.Headers.inStockCn;
import static tool.Headers.medicineCn;
import static tool.Headers.outStockCn;
import static tool.Regex.floatRegex;
import static tool.Strings.*;
import static tool.Tool.confirm;
import static tool.Tool.trueorfalse;
import static tool.Tool.warning;

public class InOutStock
{
    public JPanel pnInStock;
    private Table table;
    private JTextField txtNum;
    private JLabel lbType;
    private JLabel lbMoney;
    private JButton btnUpload;
    private JLabel lbOperator;
    private JButton btnDelete;
    private JCheckBox chkOut;
    private JCheckBox chkIn;
    private JTextField txtSpell;
    private JButton btnImport;
    private JButton btnExport;
    private DefaultTableModel model;
    private List<mListener> listeners = new ArrayList<>();
    private Map<Object, Map<String, Object>> medicineMap = new HashMap<>();

    private int rowindex = 1;
    private int rowmax = 0;
    private int failrows = 0;
    private Object[] tempRow;
    private DefaultTableModel notOkModel = new DefaultTableModel(new String[]{"药品名称", "出入库数量"},0);
    private Workbook workbook;
    private Sheet sheet;

    public InOutStock()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        textFieldListeners();
        checkBoxListeners();
        buttonListeners();
    }

    private void initUI()
    {
        // 初始化表格
        model = new DefaultTableModel(outStockCn, 0);
        table.setModel(model);
        table.column = 3;
        // 初始化操作员
        lbOperator.setText(UserInfo.position);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(chkIn);
        buttonGroup.add(chkOut);
    }

    private void messageListeners()
    {

        listeners.add(new mListener("getMedicineBySpells")
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
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        list.add((String) map.get("name"));
                        Map<String, Object> medDetail = new HashMap<>();
                        medDetail.put("id", map.get("id"));
                        medDetail.put("unitname", map.get("unitname"));
                        medDetail.put("inprice", map.get("inprice"));
                        medicineMap.put(map.get("name"), medDetail);

                    }
                    hintWindow hintWindow = UI.hintWindow.map.get(txtSpell);
                    hintWindow.updateList(list.toArray(new String[list.size()]));
                }
            }
        });

        listeners.add(new mListener("MedicineEnterAndOut")
        {
            public void messageEvent(MessageEvent event)
            {
                btnUpload.setEnabled(true);
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    warning(saveSuccess + "出入库编号：" + feedback.getMark());
                    model.setRowCount(0);
                    table.setModel(model);
                    setTypeAndMoney();
                    btnUpload.setEnabled(false);
                }
                else
                {
                    warning(saveFail);
                }
            }
        });

        listeners.add(new mListener("getMedicineIdByName")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                List<Any> anies = feedback.getDetailsList();
                if (anies.size() != 0)
                {
                    for (Any temp : anies)
                    {
                        Map map = tcp.buildMessage.AnyToMap(temp);
                        Object[] row = new Object[]{tempRow[0], map.get("unitname"), map.get("inprice"), tempRow[1]};
                        if(Integer.valueOf(tempRow[1].toString()) > 0)
                        {
                            model.addRow(row);
                            medicineMap.put(map.get("name").toString(),map);
                        }
                        else
                        {
                            notOkModel.addRow(tempRow);
                            failrows++;
                        }
                    }
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
                    setTypeAndMoney();
                    if(failrows < rowmax)
                        btnUpload.setEnabled(true);
                }
            }
        });
    }

    // 复选框监听器
    private void checkBoxListeners()
    {
        chkOut.addActionListener((ActionEvent e) ->
        {
            if (chkOut.isSelected())
            {
                btnUpload.setText("确认出库");
                model = new DefaultTableModel(outStockCn, 0);
                table.setModel(model);
                setTypeAndMoney();
            }
        });

        chkIn.addActionListener((ActionEvent e) ->
        {
            if (chkIn.isSelected())
            {
                btnUpload.setText("确认入库");
                model = new DefaultTableModel(inStockCn, 0);
                table.setModel(model);
                setTypeAndMoney();
            }
        });
    }

    // 文本框监听器
    private void textFieldListeners()
    {
        // 拼音码
        new hintWindow(txtSpell)
        {
            public void keyTyped(KeyEvent e)
            {
            }

            public void keyPressed(KeyEvent e)
            {
            }

            public void keyReleased(KeyEvent e)
            {
                if ((e.getKeyCode() >= KeyEvent.VK_A && e.getKeyCode() <= KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) && !txtSpell.getText().equals(""))
                    getMedModel();
            }
        };

        txtSpell.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    txtNum.grabFocus();
                }
            }
        });

        txtNum.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    if (txtSpell.getText().equals("") || !txtNum.getText().matches(floatRegex))
                    {
                        warning(inputError);
                        return;
                    }
                    Map<String, Object> map = medicineMap.get(txtSpell.getText());
                    Object feename = txtSpell.getText();
                    String number = txtNum.getText();
                    Object[] row = {feename, map.get("unitname"), map.get("inprice"), number, map.get("type")};
                    model.addRow(row);
                    txtSpell.setText("");
                    txtNum.setText("");
                    setTypeAndMoney();
                    btnUpload.setEnabled(true);
                    txtSpell.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtSpell.grabFocus();
                }
            }
        });
    }

    // 通过拼音码获取药品列表
    private void getMedModel()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("spell", txtSpell.getText());
        try
        {
            Connect.sendMessage(tcp.buildMessage.doFunction("getMedicineBySpells", map).toByteArray());
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
    }

    // 按钮监听器
    private void buttonListeners()
    {
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
                int[] index = table.getSelectedRows();
                for (int i = 0; i < index.length; i++)
                {
                    model.removeRow(table.convertRowIndexToModel(index[i] - i));
                }
                warning(deleteSuccess);
            }
            else return;
            table.setModel(model);
            setTypeAndMoney();
            // 表格中无数据，不可入库
            if (table.getRowCount() == 0)
            {
                btnUpload.setEnabled(false);
            }
        });

        // 确定出/入库
        btnUpload.addActionListener((ActionEvent e) ->
        {
            StringBuilder stringBuilder = new StringBuilder("您确定要将该" + lbType.getText());
            if (btnUpload.getText().equals("确认出库"))
            {
                stringBuilder.append("药品出库？");
            }
            else if(btnUpload.getText().equals("确认入库"))
            {
                stringBuilder.append("药品入库？");
            }
            else
            {
                warning("请选择出/入库!!!");
                return;
            }
            int confirm = confirm(stringBuilder.toString());
            if (confirm == JOptionPane.OK_OPTION)
            {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < table.getRowCount(); i++)
                {
                    Object id = model.getValueAt(i, 0);
                    Object num = model.getValueAt(i, 3);
                    if (btnUpload.getText().equals("确认出库"))
                    {
                        num = "-" + num;
                    }
                    map.put(id.toString(), num);
                }
                try
                {
                    btnUpload.setEnabled(false);
                    Connect.sendMessage(tcp.buildMessage.doFunction("MedicineEnterAndOut", map).toByteArray());
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
            }
        });


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
            setTypeAndMoney();
        });

        btnExport.addActionListener((ActionEvent e) ->
        {
            String name = "药品出库信息表";
            if (chkIn.isSelected())
            {
                name = "药品入库信息表";
            }
            String[] header = {"药品名称", "出库数量"};
            if (chkIn.isSelected())
            {
                header = new String[]{"药品名称", "入库数量"};
            }
            DefaultTableModel defaultTableModel = new DefaultTableModel(header, 0);
            for (int i = 0; i < model.getRowCount(); i++)
            {
                Object[] objects = {model.getValueAt(i, 0), model.getValueAt(i, 3)};
                defaultTableModel.addRow(objects);
            }
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null)
            {
                try
                {
                    excelExporter.exportFile(path, name, header, defaultTableModel);
                }
                catch (Exception e1)
                {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    // 计算种类以及总金额
    private void setTypeAndMoney()
    {
        int row = model.getRowCount();
        lbType.setText(row + "种");
        float count = 0;
        for (int i = 0; i < row; i++)
        {
            float inprice = Float.parseFloat(model.getValueAt(i, 2).toString());
            float number = Float.parseFloat(model.getValueAt(i, 3).toString());
            count += inprice * number;
        }
        lbMoney.setText(count + "元");
    }


    private void getSheet()
    {
        int i = rowindex;
        String name = sheet.getCell(0, i).getContents().trim();
        String number = sheet.getCell(1, i).getContents().trim();
        tempRow = new Object[]{name, number};
        rowindex++;
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        try
        {
            Connect.sendMessage(tcp.buildMessage.doFunction("getMedicineIdByName", map).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
