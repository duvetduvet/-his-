package manage;

import UI.Frame;
import tcp.Connect;

import javax.swing.*;
import java.awt.event.*;
import java.util.Map;

public class Password {
    public JPasswordField pswPassword;
    public JPanel pnPassword;
    public JButton btnOk;

    public Password(Map<String, Object> map) {
        JFrame frame = new Frame(pnPassword, "会员卡支付", 400, 180);
        btnOk.addActionListener((ActionEvent e) -> {
            map.put("password", new String(pswPassword.getPassword()));
            try {
                Connect.sendMessage(tcp.buildMessage.doFunction("Account", map).toByteArray());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            Charge.password = null;
            frame.dispose();
        });

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Charge.password = null;
            }
        });
        pswPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    btnOk.grabFocus();
            }
        });
    }
}
