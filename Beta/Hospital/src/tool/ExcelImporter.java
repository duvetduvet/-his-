package tool;

import UI.Frame;
import jxl.Sheet;
import jxl.Workbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;

import static tool.DataBase.update;

public class ExcelImporter {
    private JLabel lbImport;
    private JProgressBar pgb;
    private JPanel pnPgb;

    public String selectFile() {
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        //只能选择目录
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = 0;
        try {
            result = fileChooser.showOpenDialog(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            //获得文件路径
            path = fileChooser.getSelectedFile().getPath();
        }
        return path;
    }

    public void importFile(String path, DefaultTableModel model, String view) throws Exception {
        //创建文件对象
        File file = new File(path);
        //获取文件
        Workbook workbook = Workbook.getWorkbook(file);
        Sheet sheet = workbook.getSheet(0);
        int rows = sheet.getRows();
        int cols = sheet.getColumns();
        if (sheet.getColumns() != model.getColumnCount()) {
            throw new Exception();
        }
        JFrame frame = new Frame(pnPgb, "Excel导入", 400, 100);
        int value = 1, success = 0, fail = 0;
        pgb.setMaximum(rows);
        pgb.setValue(value);
        for (int i = 0; i < rows; i++) {
            Object[] row = new Object[cols];
            for (int j = 0; j < cols; j++) {
                row[j] = sheet.getCell(j, i).getContents().trim();
            }
            try {
                String query = "INSERT INTO " + view + " VALUES('";
                for (int k = 0; k < row.length - 1; k++) {
                    query += row[k] + "','";
                }
                query += row[row.length - 1] + "');";
                update(query);
                //model增加一行
                model.addRow(row);
                success++;
            } catch (Exception e) {
                fail++;
//                e.printStackTrace();
            }
            pgb.setValue(value);
            lbImport.setText("正在导入..." + value * 100 / rows + "％");
            value++;
        }
        lbImport.setText("已完成！");
        frame.dispose();
        JOptionPane.showMessageDialog(null, "\n导入完成！成功：" + success + "；失败：" + fail + "\n ", "系统消息", JOptionPane.WARNING_MESSAGE);
        //关闭流
        workbook.close();
    }
}
