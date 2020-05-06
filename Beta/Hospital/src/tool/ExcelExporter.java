package tool;

import UI.Frame;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;

public class ExcelExporter {
    private JProgressBar pgb;
    private JLabel lbExport;
    private JPanel pnPgb;

    public String selectPath() {
        String path = null;
        JFileChooser fileChooser = new JFileChooser();
        // 只能选择目录
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = 0;
        try {
            result = fileChooser.showOpenDialog(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result == JFileChooser.APPROVE_OPTION) {
            // 获得文件夹路径
            path = fileChooser.getSelectedFile().getPath();
        }
        return path;
    }

    public void exportFile(String path, String fileName, String[] header, DefaultTableModel model) throws Exception {
        JFrame frame = new Frame(pnPgb, "导出到Excel", 400, 100);
        File file = new File(path + "\\" + fileName + ".xls");
        // 创建Workbook对象
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(file);
        WritableSheet writableSheet = writableWorkbook.createSheet(fileName, 0);
        new Thread(() -> {
            try {
                int cols = model.getColumnCount();
                int rows = model.getRowCount() + 1;
                pgb.setMaximum(cols * rows);
                int value = 1;
                String cellValue;
                pgb.setValue(value);
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 设置单元格的值
                        if (i == 0) {
                            cellValue = header[j];
                        } else {
                            if (model.getValueAt(i - 1, j) == null) {
                                cellValue = "";
                            } else {
                                cellValue = model.getValueAt(i - 1, j).toString();
                            }
                        }
                        writableSheet.addCell(new Label(j, i, cellValue));
                        pgb.setValue(value);
                        lbExport.setText("正在导出..." + value * 100 / (cols * rows) + "％");
                        value++;
                    }
                }
                lbExport.setText("已完成！");
                // 输出Excel文件，关闭流
                writableWorkbook.write();
                writableWorkbook.close();
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
