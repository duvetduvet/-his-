package logic;

import UI.Frame;
import tcp.Connect;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static tool.Strings.*;
import static tool.Tool.warning;
import static tool.XmlOperator.*;

public class Check {
    private JLabel lbCheck;
    private JPanel pnCheck;
    private JProgressBar pgbCheck;
    private JLabel logo;
    private JFrame frame;
    private List<String> files;

    public Check() {
//        pastDate();
//        init();
//        update();
//        check();
        close();
    }

    private void init() {
        frame = new JFrame();
        // 设置为无边框
        frame.setUndecorated(true);
        frame.setContentPane(pnCheck);
        int width = Frame.width / 4;
        int height = width * 2 / 3;
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        ImageIcon imageIcon = new ImageIcon(logoPath);
        imageIcon.setImage(imageIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH));
        logo.setIcon(imageIcon);
        frame.setVisible(true);
        files = getProperty(getElements(getRoot(readXml(filePath))));
        pgbCheck.setMaximum(100);
    }

    // 检查过期
    private void pastDate() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(simpleDateFormat.format(Calendar.getInstance().getTime()));
            Date ending = simpleDateFormat.parse("2018-07-01");
            if (!today.before(ending)) {
                warning(useless);
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 检查软件更新
    private void update() {
        lbCheck.setText(checkFileUpdate + "100％");
        pgbCheck.setValue(pgbCheck.getMaximum());
    }

    // 检查文件完整性
    private void check() {
        pgbCheck.setValue(0);
        lbCheck.setText(checkFileExist);
        new Thread(() -> {
            for (int i = 0; i < files.size() - 1; i++) {
                File file = new File(files.get(i));
                if (!file.exists()) {
                    warning("路径" + files.get(i) + "不存在，请重新安装软件！");
                    System.exit(0);
                    return;
                }
                int percent = (i + 1) * 100 / files.size();
                lbCheck.setText(checkFileExist + percent + "％");
                pgbCheck.setValue(percent);
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            close();
        }).start();
    }

    private void close() {
//        frame.dispose();
        try {
            files = getProperty(getElements(getRoot(readXml(filePath))));
            new Connect(files.get(files.size() - 1), 8000);
        } catch (Exception e) {
            warning(connectFail);
            return;
        }
        new Login();
    }
}
