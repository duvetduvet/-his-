package logic;

import UI.Frame;
import manage.*;
import org.dom4j.Document;
import select.*;
import settings.*;
import tool.UserInfo;

import javax.swing.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static tool.Strings.*;
import static tool.Tool.confirm;
import static tool.Tool.getImageIcon;
import static tool.XmlOperator.*;

public class MainUI extends JFrame {
    public static String user;
    private JPanel pnWelcome;
    private JLabel logo;
    private JLabel lbUser;
    private JLabel lbTime;
    private JMenuItem exitSystem = new JMenuItem("退出(E)");
    private JMenuItem vipCardInfoManage = new JMenuItem("会员卡信息管理(V)");
    private JMenuItem doctorInfoManage = new JMenuItem("医生信息管理(S)");
    private JMenuItem medicineInfoManage = new JMenuItem("药品信息管理(M)");
    private JMenuItem cureInfoManage = new JMenuItem("诊疗信息管理(C)");
    private JMenuItem registerManage = new JMenuItem("挂号管理(R)");
    private JMenuItem distributeMedicine = new JMenuItem("药房发药(D)");
    private JMenuItem medicineChecker = new JMenuItem("药品盘点(S)");
    private JMenuItem medicinePrice = new JMenuItem("药房划价(C)");
    private JMenuItem medicineCharger = new JMenuItem("药品收费(C)");
    private JMenuItem prescriptionInvalid = new JMenuItem("处方作废(I)");
    private JMenuItem medicineIntoStock = new JMenuItem("药品出入库(I)");
    private JMenuItem consumeRecord = new JMenuItem("会员卡充值消费查询(R)");
    private JMenuItem rechargeCounter = new JMenuItem("会员卡充值统计(C)");
    private JMenuItem medicineSoldRecord = new JMenuItem("药品诊疗销售统计(S)");
    private JMenuItem prescriptionRecord = new JMenuItem("处方记录查询(P)");
    private JMenuItem patientVisits = new JMenuItem("病人初复诊查询");
    private JMenuItem medicineIntoOutRecord = new JMenuItem("药品出入库查询(R)");
    private JMenuItem profit = new JMenuItem("损益查询(P)");
    private JMenuItem preCount = new JMenuItem("处方金额统计(P)");
    private JMenuItem sumAsCharge = new JMenuItem("按收费项目汇总(C)");
    private JMenuItem feeTypeSet = new JMenuItem("费用类型设置(F)");
    private JMenuItem regTypeSet = new JMenuItem("挂号类型设置(G)");
    private JMenuItem vipTypeSet = new JMenuItem("会员制度设置(V)");
    private JMenuItem unitSet = new JMenuItem("计量单位设置(U)");
    private JMenuItem departmentSet = new JMenuItem("科室设置(D)");
    private JMenuItem factorySet = new JMenuItem("生产厂家设置(F)");
    private JMenuItem powerSet = new JMenuItem("用户设置(U)");
    private JMenuItem welcomePage = new JMenuItem("欢迎页(W)");
    private JMenuItem aboutSystem = new JMenuItem("关于(A)");
    private JMenuItem patientInfo = new JMenuItem("病人信息");
    private JMenu set;
    private JMenu select;
    private Document configDoc;
    private Document userDoc;

    MainUI(String power) {
        init();
        analysePower(power);
        frameListeners();
        menuItemListeners();
    }

    // 解析权限
    private void analysePower(String power) {
        char[] powers = power.toCharArray();
        if (powers[1] == '0') {
            registerManage.setVisible(false);
        }
        if (powers[2] == '0') {
            medicinePrice.setVisible(false);
        }
        if (powers[3] == '0') {
            medicineCharger.setVisible(false);
        }
        if (powers[4] == '0') {
            distributeMedicine.setVisible(false);
        }
        if (powers[5] == '0') {
            medicineInfoManage.setVisible(false);
            cureInfoManage.setVisible(false);
            medicineChecker.setVisible(false);
        }
        if (powers[6] == '0') {
            medicineIntoStock.setVisible(false);
        }
        if (powers[8] == '0') {
            vipCardInfoManage.setVisible(false);
            consumeRecord.setVisible(false);
            rechargeCounter.setVisible(false);
        }
        if (powers[9] == '0') {
            vipTypeSet.setVisible(false);
        }
        if (powers[10] == '0') {
            set.setVisible(false);
        }
        if (powers[11] == '0') {
            doctorInfoManage.setVisible(false);
        }
        if (powers[12] == '0') {
            select.setVisible(false);
        }
        if (powers[13] == '0') {
            prescriptionInvalid.setVisible(false);
        }
        if (powers[14] == '0') {
            powerSet.setVisible(false);
        }
    }

    // 窗口初始化
    private void init() {
        setJMenuBar(menu());
        ImageIcon imageIcon = getImageIcon();
        logo.setIcon(imageIcon);
        userDoc = readXml(userPath);
        lbUser.setText(lbUser.getText().replace("user", UserInfo.position));
        setContentPane(pnWelcome);
        setTitle(sysTitle);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // 获取配置信息
        configDoc = readXml(configPath);
        List<String> configInfo = getText(getElements(getRoot(configDoc)));
        // 将窗口设置为用户自定义大小
        int width = Frame.width * 3 / 5;
        int height = width * 2 / 3;
        setSize(width, height);
        if (configInfo.get(2).equals("max")) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        // 使窗口处于屏幕中央
        setLocationRelativeTo(null);
        setIconImage(imageIcon.getImage());
        setTime();
        setVisible(true);
    }

    // 时间
    private void setTime() {
        new Thread(() -> {
            while (true) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = simpleDateFormat.format(Calendar.getInstance().getTime());
                lbTime.setText(date);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

    // 菜单栏
    private JMenuBar menu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(option());
        menuBar.add(register());
        menuBar.add(charge());
        menuBar.add(medicineManage());
        menuBar.add(vipManage());
        menuBar.add(superFunction());
        menuBar.add(set());
        menuBar.add(help());
        return menuBar;
    }

    // 选项
    private JMenu option() {
        JMenu menu = new JMenu("选项(O)");
        menu.add(exitSystem);
        return menu;
    }

    // 挂号管理
    private JMenu register() {
        JMenu menu = new JMenu("挂号管理(R)");
        menu.add(registerManage); // 添加挂号
        menu.add(prescriptionInvalid); // 处方作废
        return menu;
    }

    // 划价收费
    private JMenu charge() {
        JMenu menu = new JMenu("划价收费(C)");
        menu.add(medicinePrice); // 划价
        menu.add(medicineCharger); // 收费
        menu.add(distributeMedicine); // 发药
        return menu;
    }

    // 药品管理
    private JMenu medicineManage() {
        JMenu menu = new JMenu("药品管理(M)");
        menu.add(medicineInfoManage); // 药品信息管理
        menu.add(cureInfoManage); // 诊疗信息管理
        menu.add(medicineChecker); // 药品盘点
        menu.add(medicineIntoStock); // 药品出入库
        menu.add(prescriptionRecord); // 处方记录查询
        menu.add(medicineIntoOutRecord); // 药品出入库记录查询


        return menu;
    }

    // 查询
    private JMenu vipManage() {
        JMenu menu = new JMenu("会员管理(V)");
        menu.add(vipCardInfoManage); // 会员卡信息管理
        menu.add(consumeRecord); // 会员卡充值消费记录
        menu.add(rechargeCounter);
        menu.add(vipTypeSet); // 会员卡制度设置
        return menu;
    }

    // 高级功能
    private JMenu superFunction() {
        JMenu menu = new JMenu("高级功能(S)");
        menu.add(doctorInfoManage); // 医生信息管理
        select = new JMenu("业务查询");
        select.add(medicineSoldRecord); // 药品销售记录
        select.add(patientInfo);        // 病人详情页
        select.add(profit); // 损益管理
        select.add(preCount); // 处方金额统计
        select.add(sumAsCharge); // 按收费项目汇总
        select.add(patientVisits); // 病人记录查询
        menu.add(select); // 业务查询
        menu.add(powerSet); // 权限设置
        return menu;
    }

    // 设置
    private JMenu set() {
        set = new JMenu("设置(S)");
        set.add(feeTypeSet); // 费用类型设置
        set.add(regTypeSet); // 挂号类型设置
        set.add(unitSet); // 单位设置
        set.add(departmentSet); // 部门设置
        set.add(factorySet); // 生产厂家设置
        return set;
    }

    // 帮助
    private JMenu help() {
        JMenu menu = new JMenu("帮助(H)");
        menu.add(welcomePage); // 欢迎页
        menu.add(aboutSystem); // 关于
        return menu;
    }

    // 系统界面的监听器
    private void frameListeners() {
        // 窗口事件
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closeProcess();
            }
        });

        // 窗口大小设置
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                updateDoc(configDoc, "//config", "width", getWidth() + "");
                updateDoc(configDoc, "//config", "height", getHeight() + "");
                String size = "default";
                if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                    size = "max";
                }
                updateDoc(configDoc, "//config", "size", size);
            }
        });
    }

    // 关闭程序时的处理
    private void closeProcess() {
        if (confirm(sureToQuit) == JOptionPane.OK_OPTION) {
            updateDoc(userDoc, "//info", "state", "false");
            writeXml(userDoc, userPath);
            writeXml(configDoc, configPath);
            System.exit(0);
        }
    }

    // 菜单项的监听器
    private void menuItemListeners() {
        // 退出
        exitSystem.addActionListener((ActionEvent e) -> closeProcess());

        // 医生管理
        doctorInfoManage.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 医生信息管理", new Doctor().pnDoctor));

        // 病人详情
        patientInfo.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 病人详情页",new Patient().paitnetPanel));

        // 药品管理
        medicineInfoManage.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 药品信息管理", new Medicine().pnMedicine));

        // 诊疗管理
        cureInfoManage.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 诊疗信息管理", new Cure().pnCure));

        // 挂号管理
        registerManage.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 挂号管理", new Register().pnRegister));

        // 药房发药
        distributeMedicine.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + "- 药房发药", new DeMed().pnDeMed));

        // 药品盘点
        medicineChecker.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 药品盘点", new MedChecker().pnSum));

        // 药品出入库
        medicineIntoStock.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 药品出入库", new InOutStock().pnInStock));

        // 药房划价
        medicinePrice.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 药房划价", new Prescription().pnPrescription));

        // 药品收费
        medicineCharger.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 药品收费", new Charge().pnCharge));

        // vip管理系统
        vipCardInfoManage.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 会员卡信息管理", new Vip().pnVip));

        // 药品出入库查询
        medicineIntoOutRecord.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 药品出入库查询", new InoutStock().pnInoutStock));

        // 药品销售统计
        medicineSoldRecord.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 药品诊疗销售统计", new MedSold().pnMedSold));

        // 处方记录查询
        prescriptionRecord.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " -  处方记录查询", new PatientPres().pnPres));


        // 初复诊查询
        patientVisits.addActionListener((ActionEvent e)->changeContentPane(sysTitle + "初复诊查询", new patientVisits().panel));

        // 处方作废
        prescriptionInvalid.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 处方作废", new Invalid().pnInvalid));

        // 按收费项目汇总
        sumAsCharge.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 按收费项目汇总", new ChargeCounter().pnChargeCounter));

        // 会员卡充值消费记录
        consumeRecord.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 会员卡充值消费记录查询", new Recharge().pnRecharge));

        // 损益查询
        profit.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 损益查询", new GetAndLose().pnProfit));

        preCount.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 处方金额统计", new DatePre().pnDatePre));

        rechargeCounter.addActionListener((ActionEvent e) -> changeContentPane(sysTitle + " - 会员卡充值统计", new Rechargecount().pnRechargeCount));

        // 费用类型设置
        feeTypeSet.addActionListener((ActionEvent e) -> new FeeType());

        // 挂号类型设置
        regTypeSet.addActionListener((ActionEvent e) -> new RegType());

        // vip类型设置
        vipTypeSet.addActionListener((ActionEvent e) -> new Integrate());

        // 计量单位设置
        unitSet.addActionListener((ActionEvent e) -> new Unit());

        // 部门设置
        departmentSet.addActionListener((ActionEvent e) -> new Department());

        // 生产厂家设置
        factorySet.addActionListener((ActionEvent e) -> new Factory());

        // 权限设置
        powerSet.addActionListener((ActionEvent e) -> new Power());

        // 欢迎页
        welcomePage.addActionListener((ActionEvent e) -> changeContentPane(sysTitle, pnWelcome));

        // 关于
        aboutSystem.addActionListener((ActionEvent e) -> new About());
    }

    // 切换面板
    private void changeContentPane(String title, JPanel panel) {
        setTitle(title);
        setContentPane(panel);
        setVisible(true);
    }
}
