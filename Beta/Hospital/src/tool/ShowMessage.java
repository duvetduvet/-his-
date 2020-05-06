package tool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ShowMessage extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextArea textArea1;
    public int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    public int height = Toolkit.getDefaultToolkit().getScreenSize().height;
    StringBuffer stringBuffer = new StringBuffer();

    public ShowMessage() {
        setContentPane(contentPane);
        setSize(width / 5, height / 5);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    // 当前行添加字符串
    public void appendString(String s) {
        stringBuffer.append(s);
        textArea1.setText(stringBuffer.toString());
    }

    // 新的一行添加字符串
    public void newLineString(String s) {
        if (stringBuffer.length() != 0)
            stringBuffer.append("\n");
        stringBuffer.append(s);
        textArea1.setText(stringBuffer.toString());
    }

    // 清空所有信息
    public void clear() {
        stringBuffer.setLength(0);
        textArea1.setText("");
    }

    public static void main(String[] args) {
        ShowMessage dialog = new ShowMessage();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    dialog.newLineString("正在与医保进行结算...");
                    Thread.sleep(1000);
                    dialog.appendString("\t\t\t结算成功");
                    Thread.sleep(1000);
                    dialog.newLineString("正在与本地数据库进行插入医保数据...");
                    Thread.sleep(1000);
                    dialog.appendString("\t\t\t插入成功");
                    Thread.sleep(1000);
                    dialog.newLineString("正在本地数据库进行收费...");
                    Thread.sleep(1000);
                    dialog.appendString("\t\t\t收费成功");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        //dialog.pack();
        dialog.setVisible(true);


        System.exit(0);
    }
}
