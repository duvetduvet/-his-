package logic;

import UI.Frame;
import event.MessageEvent;
import event.mListener;
import org.dom4j.Document;
import org.dom4j.Element;
import proto.myMessage;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;
import tcp.Connect;
import tool.Strings;
import tool.UserInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import static tool.Strings.*;
import static tool.Tool.getImageIcon;
import static tool.Tool.warning;
import static tool.XmlOperator.*;

public class Login {
    static private Frame frame;
    public JPanel pnLogin;
    private JTextField txtUser;
    private JPasswordField txtPsw;
    private JCheckBox chkSave;
    private JCheckBox chkAuto;
    private JButton btnOK;
    private JLabel logo;
    private String number;
    private String password;
    private Document configDoc;
    private Document userDoc;
    private List<Element> elements;
    private List<String> configInfo = new ArrayList<>();
    private List<String> userInfo = new ArrayList<>();
    private List<mListener> listeners = new ArrayList();

    public Login() {
        initUI();
        messageListeners();
        frameListeners();
        textFieldListeners();
        checkBoxListeners();
        buttonListeners();
        config();
    }

    static public JFrame getFrame() {
        return frame;
    }

    // 初始化界面
    private void initUI() {
        int width = Frame.width / 3;
        int height = width * 2 / 3;
        frame = new Frame(pnLogin, sysTitle, width, height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        ImageIcon imageIcon = getImageIcon();
        logo.setIcon(imageIcon);
        frame.setIconImage(imageIcon.getImage());
        txtPsw.setEchoChar('\0');
        btnOK.grabFocus();
        // 读取配置文件
        configDoc = readXml(loginPath);
        configInfo = getText(getElements(getRoot(configDoc)));
        // 读取用户信息
        userDoc = readXml(userPath);
        elements = getElements(getRoot(userDoc));
        userInfo = getText(elements);
    }

    // 消息监听器
    private void messageListeners() {
        // 添加Login监视器
        listeners.add(new mListener("Login") {
            public void messageEvent(MessageEvent event) {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() == 1) {
                    Map map = tcp.buildMessage.AnyToMap(feedback.getDetails(0));
                    int id = (Integer) map.get("id");
                    String name = (String) map.get("name");
                    String power = (String) map.get("power");
                    // 更新本地用户信息文件
                    userInfo.clear();
                    userInfo.add(String.valueOf(id));
                    userInfo.add(name);
                    userInfo.add(number);
                    userInfo.add(password);
                    userInfo.add("true");
                    userInfo.add("true");
                    UserInfo.userid = String.valueOf(id);
                    UserInfo.position = name;
                    // 更新配置
                    for (int i = 0; i < userInfo.size(); i++) {
                        updateDoc(userDoc, "//info", elements.get(i).getName(), userInfo.get(i));
                    }
                    // 保存配置
                    writeXml(configDoc, loginPath);
                    writeXml(userDoc, userPath);
                    frame.dispose();
                    // 传递权限
                    new MainUI(power);
                } else {
                    warning(Strings.infoError);
                    updateDoc(userDoc, "//info", "log", "false");
                    btnOK.doClick();
                }
            }
        });
    }

    // 窗口监听器
    private void frameListeners() {
        // 窗口关闭事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                writeXml(configDoc, loginPath);
                writeXml(userDoc, userPath);
                System.exit(0);
            }
        });
    }

    // 文本框监听器
    private void textFieldListeners() {
        // 账号框聚焦事件
        txtUser.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (txtUser.getText().equals("账号")) {
                    txtUser.setText("");
                    txtUser.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                if (txtUser.getText().equals("")) {
                    txtUser.setText("账号");
                    txtUser.setForeground(new Color(187, 187, 187));
                }
            }
        });

        // 账号框回车触发点击登录按钮事件
        txtUser.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnOK.doClick();
                }
            }
        });

        // 密码框聚焦事件
        txtPsw.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                String password = new String(txtPsw.getPassword());
                if (password.equals("密码")) {
                    txtPsw.setText("");
                    txtPsw.setEchoChar('●');
                    txtPsw.setForeground(Color.BLACK);
                }
            }

            public void focusLost(FocusEvent e) {
                String password = new String(txtPsw.getPassword());
                if (password.equals("")) {
                    txtPsw.setEchoChar('\0');
                    txtPsw.setText("密码");
                    txtPsw.setForeground(new Color(187, 187, 187));
                }
            }
        });

        // 密码框回车触发点击登录按钮事件
        txtPsw.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnOK.doClick();
                }
            }
        });
    }

    // 复选框监听器
    private void checkBoxListeners() {
        // chkSave被选中，将其配置设置为true
        chkSave.addActionListener((ActionEvent e) -> {
            String flag = "false";
            if (chkSave.isSelected()) {
                flag = "true";
            } else {
                // chkSave没有被选中，chkAuto不能被选中
                chkAuto.setSelected(false);
                // 将chkAuto配置设置为0
                updateDoc(configDoc, "//chkAuto", "selected", "false");
            }
            // 将chkSave的配置设置为flag
            updateDoc(configDoc, "//chkSave", "selected", flag);
        });

        // chkAuto被选中
        chkAuto.addActionListener((ActionEvent e) -> {
            String flag = "false";
            if (chkAuto.isSelected()) {
                // 同时选中chkSave
                chkSave.setSelected(true);
                // 将chkSave配置设置为true
                updateDoc(configDoc, "//chkSave", "selected", "true");
                flag = "true";
            }
            // 将chkAuto配置设置为flag
            updateDoc(configDoc, "//chkAuto", "selected", flag);
        });
    }

    // 按钮监听器
    private void buttonListeners() {
        // 监听登录按钮
        btnOK.addActionListener((ActionEvent e) -> {
            number = txtUser.getText();
            password = new String(txtPsw.getPassword());
            if (number.equals("账号") || password.equals("密码")) {
                warning(nonInput);
                return;
            }
            String log = btnOK.getText();
            if (log.equals("登录")) {
                setComponentsEnable(false);
                btnOK.setText("取消");
                // 判断进程是否存在
                try {
                    // 获取监控主机
                    MonitoredHost local = MonitoredHost.getMonitoredHost("localhost");
                    // 取得所有在活动的虚拟机集合
                    Set<?> vmList = new HashSet<Object>(local.activeVms());
                    // 遍历集合，输出PID和进程名
                    int count = 0; // 当前logic.Client进程数
                    for (Object process : vmList) {
                        MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + process));
                        // 获取类名
                        String processName = MonitoredVmUtil.mainClass(vm, true);
                        if (processName.equals("logic.Client")) {
                            count++;
                        }
                        if (count == 2) {
                            warning(logExist);
                            btnOK.doClick();
                            return;
                        }
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", number);
                    map.put("password", password);
                    Connect.sendMessage(tcp.buildMessage.doFunction("Login", map).toByteArray());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else {
                setComponentsEnable(true);
                btnOK.setText("登录");
                updateDoc(userDoc, "//info", "state", "false");
            }
        });
    }


    // 读取配置信息
    private void config() {
        // 如果从未登录过，不读取配置文件
        if (userInfo.get(0).equals("null")) {
            return;
        }
        // 获取账号
        txtUser.setText(userInfo.get(2));
        txtUser.setForeground(Color.BLACK);
        // 如果记住密码被选中
        if (configInfo.get(0).equals("true")) {
            chkSave.setSelected(true);
            if (userInfo.get(5).equals("true")) {
                txtPsw.setText(userInfo.get(3));
                txtPsw.setForeground(Color.BLACK);
                txtPsw.setEchoChar('•');
            }
        }
        // 如果自动登陆被选中，则下次登录时触发登录按钮
        if (configInfo.get(1).equals("true")) {
            chkAuto.setSelected(true);
            btnOK.doClick();
        }
    }

    // 设置界面部分组件可用状态
    private void setComponentsEnable(boolean state) {
        txtUser.setEnabled(state);
        txtPsw.setEnabled(state);
        chkSave.setEnabled(state);
        chkAuto.setEnabled(state);
    }
}
