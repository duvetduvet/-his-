package manage;

import UI.Table;
import UI.hintWindow;
import UI.mTableModel;
import com.google.protobuf.Any;
import event.MessageEvent;
import event.mListener;
import event.messageManager;
import org.jdatepicker.JDatePicker;
import proto.myMessage;
import tcp.Connect;
import tcp.buildMessage;
import tool.ExcelExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.*;

import static tool.Headers.vipCn;
import static tool.Regex.*;
import static tool.Strings.*;
import static tool.Tool.warning;

public class Vip
{
    public JPanel pnVip;
    private Table table;
    private JButton btnSaveMakeCard;
    private JButton btnSaveReCharge;
    private JTextField txtCard;
    private JTextField txtPhone;
    private JTextField txtCardRecharge;
    private JTextField txtRecharge;
    private JTextField txtPresent;
    private JTextField txtName;
    private JPasswordField pswPassword;
    private JPasswordField pswSure;
    private JTextField txtCardOrPhone;
    private JButton btnLose;
    private JTextField txtNewCard;
    private JButton btnOkUp;
    private JTextField txtCardPhone;
    private JButton btnOkReset;
    private JPasswordField pswOkNewPsw;
    private JPasswordField pswNewPsw;
    private JLabel lbCard;
    private JLabel lbRemain;
    private JLabel lbGift;
    private JTextField txtOldCardOrPhone;
    private JTextField txtNewName;
    private JTextField txtNewPhone;
    private JButton btnExport;
    private JLabel lbGrade;
    private JDatePicker datePicker;
    private JTextField identity;
    private JComboBox cbbSex;
    private JTextField fidentity;
    private JComboBox fcbbSex;
    private JDatePicker fdatePicker;
    private JLabel labVipNumber;
    private DefaultTableModel model;
    private int card;
    private int patientId;
    private List<mListener> listeners = new LinkedList<>();
    private Map<Integer, Map<String, Object>> patientMap = new HashMap<>();

    public Vip()
    {
        messageManager.removeAllMessageListener();
        messageListeners();
        initUI();
        buttonListeners();
        textFieldListeners();

        identity.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                    txtPhone.grabFocus();
                else if (e.getKeyCode() == KeyEvent.VK_UP)
                    datePicker.grabFocus();
            }
        });
    }

    private void messageListeners()
    {
        listeners.add(new mListener("getVip")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    model.setRowCount(0);
                    labVipNumber.setText(String.valueOf(feedback.getMark()));
                    for (Any temp : anies)
                    {
                        Map map = buildMessage.AnyToMap(temp);
                        String id = (String) map.get("id");
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < 6 - id.length(); i++)
                        {
                            stringBuilder.append("0");
                        }
                        id = stringBuilder.toString() + id;
                        Object name = map.get("name");
                        Object phone = map.get("phone");
                        BigDecimal consume = new BigDecimal(map.get("consume").toString());
                        String useful = map.get("useful").toString();
                        Object sex = map.get("sex");
                        Object identity = map.get("identity");
                        Object birth = map.get("birth");
                        if (useful.equals("2"))
                        {
                            useful = "可用";
                        }
                        else
                        {
                            useful = "挂失";
                        }
                        // "卡号", "姓名","性别","联系电话","身份证号码","出生日期", "积分", "会员卡状态"
                        model.addRow(new Object[]{id, name, sex, phone, identity, birth, consume, useful});
                    }
                    table.setModel(model);
                }
            }
        });

        listeners.add(new mListener("addVip")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                btnSaveMakeCard.setEnabled(true);
                if (feedback.getMark() >= 0)
                {
                    String card = txtCard.getText();
                    String name = txtName.getText();
                    String sex = (String) cbbSex.getSelectedItem();
                    String birth = datePicker.getFormattedTextField().getText();
                    String iden = identity.getText();
                    String phone = txtPhone.getText();
                    // 向表格添加会员卡信息
                    String[] data = {card, name, sex, phone, iden, birth, "0.0", "可用"};
                    model.addRow(data);
                    table.setModel(model);
                    warning(vipMakeCardSuccess);
                    setComponentText(new String[]{"", "", "", ""});
                }
                else
                {
                    warning(vipMakeCardFail);
                }
            }
        });

        listeners.add(new mListener("deleteVip")
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
                    warning(deleteVipSuccess);
                    try
                    {
                        Connect.sendMessage(buildMessage.doFunction("getVip", null).toByteArray());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    warning(deleteVipFail);
                }
            }
        });

        listeners.add(new mListener("payForVip")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                btnSaveReCharge.setEnabled(true);
                if (feedback.getMark() >= 0)
                {
                    txtCardRecharge.setText("");
                    txtRecharge.setText("0.0");
                    txtPresent.setText("0.0");
                    warning(chargeSuccess);
                }
                else
                {
                    warning(chargeFail);
                }
            }
        });

        listeners.add(new mListener("selectVipByPhoneOrId")
        {
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> anies = feedback.getDetailsList();
                    for (Any temp : anies)
                    {
                        Map<String, Object> map = buildMessage.AnyToMap(temp);
                        txtCard.setText(String.valueOf(map.get("id")));
                        txtCardRecharge.setText(String.valueOf(map.get("id")));
                        lbCard.setText("<html><u>" + map.get("id").toString() + "</u></html>");
                        card = Integer.valueOf((String) map.get("id"));
                        txtNewName.setText(map.get("name").toString());
                        txtNewPhone.setText(map.get("phone").toString());
                        fcbbSex.setSelectedItem(map.get("sex"));
                        fdatePicker.getFormattedTextField().setText(map.get("birth").toString());
                        fidentity.setText((String) map.get("identity"));
                        lbRemain.setText("<html><u>" + map.get("remain").toString() + "</u></html>");
                        lbGift.setText("<html><u>" + map.get("present").toString() + "</u></html>");
                        lbGrade.setText("<html><u>" + map.get("consume").toString() + "</u></html>");
                    }
                }
                else
                {
                    warning("查无此vip!!!");
                    txtCard.setText("");
                    card = 0;
                    txtNewCard.setText("");
                    lbCard.setText("");
                    txtNewName.setText("");
                    txtNewPhone.setText("");
                    fdatePicker.getFormattedTextField().setText("");
                    lbRemain.setText("");
                    lbGift.setText("");
                    lbGrade.setText("");
                    fidentity.setText("");

                }
            }
        });

        listeners.add(new mListener("lostVip")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    warning("会员卡挂失成功！");
                    txtCardOrPhone.setText("");
                    try
                    {
                        Connect.sendMessage(buildMessage.doFunction("getVip", null).toByteArray());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    warning("会员卡挂失失败，请检查您的网络设置！");
                }
            }
        });

        listeners.add(new mListener("remakeCard")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() <= 0)
                {
                    warning("会员卡未挂失，请先将卡挂失！");
                }
                else
                {
                    warning("会员卡补办成功！");
                    txtOldCardOrPhone.setText("");
                    txtNewCard.setText("");
                }
            }
        });

        listeners.add(new mListener("updateVip")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    warning("会员卡信息更新成功！");
                    try
                    {
                        Connect.sendMessage(tcp.buildMessage.doFunction("getVip", null).toByteArray());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    warning("会员卡信息更新失败！");
                }
                txtCard.setText("");
                card = 0;
                txtNewCard.setText("");
                lbCard.setText("");
                txtNewName.setText("");
                txtNewPhone.setText("");
                fdatePicker.getFormattedTextField().setText("");
                lbRemain.setText("");
                lbGift.setText("");
                lbGrade.setText("");
                fidentity.setText("");
            }
        });

        listeners.add(new mListener("getPatientBySpell")
        {
            @Override
            public void messageEvent(MessageEvent event)
            {
                myMessage.feedback feedback = event.getMessage();
                if (feedback.getMark() >= 0)
                {
                    List<Any> list = feedback.getDetailsList();
                    List<String> hint = new ArrayList<>();
                    for (Any any : list)
                    {
                        Map temp = buildMessage.AnyToMap(any);
                        int id = (Integer) temp.get("id");
                        patientMap.put(id, temp);
                        String date = (String) temp.get("date");
                        String birth = (String) temp.get("birth");
                        hint.add(temp.get("name") + "/" + temp.get("sex") + "/" + birth.split(" ")[0] + "/" + date.split(" ")[0] + "/" + id);
                    }
                    if (list.size() != 0)
                    {
                        hintWindow hintWindow = UI.hintWindow.map.get(txtName);
                        hintWindow.updateList(hint.toArray(new String[list.size()]));
                    }
                    else
                    {
                        hintWindow hintWindow = UI.hintWindow.map.get(txtName);
                        hintWindow.setVisible(false);
                    }
                }
            }
        });


    }

    // 初始化界面
    private void initUI()
    {
        model = new mTableModel(vipCn, 0);
        table.setModel(model);
        final TableRowSorter sorter = new TableRowSorter(model);
        table.setRowSorter(sorter); //为JTable设置排序器
        // 获取VIP信息
        try
        {
            Connect.sendMessage(buildMessage.doFunction("getVip", null).toByteArray());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void textFieldListeners()
    {
        txtCard.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    pswSure.grabFocus();
                }
            }
        });

        txtName.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    datePicker.grabFocus();
                }
            }
        });

        txtPhone.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    pswPassword.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtName.grabFocus();
                }
            }
        });

        pswPassword.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    pswSure.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtPhone.grabFocus();
                }
            }
        });

        pswSure.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtCard.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    pswPassword.grabFocus();
                }
            }
        });

        txtCardRecharge.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    txtRecharge.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtPresent.grabFocus();
                }
            }
        });

        txtRecharge.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    txtPresent.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtCardRecharge.grabFocus();
                }
            }
        });

        txtRecharge.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                txtRecharge.setText("");
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                if (txtRecharge.getText().equals(""))
                {
                    txtRecharge.setText("0.0");
                }
            }
        });

        txtPresent.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                txtPresent.setText("");
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                if (txtPresent.getText().equals(""))
                {
                    txtPresent.setText("0.0");
                }
            }
        });

        txtPresent.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    txtCardRecharge.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtRecharge.grabFocus();
                }
            }
        });

        txtOldCardOrPhone.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    txtNewCard.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtNewCard.grabFocus();
                }
            }
        });

        txtNewCard.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    txtOldCardOrPhone.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtOldCardOrPhone.grabFocus();
                }
            }
        });

        txtCardPhone.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    String cardphone = txtCardPhone.getText();
                    if (cardphone.equals(""))
                    {
                        warning("请输入会员卡号或联系电话！");
                        return;
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", cardphone);
                    map.put("phone", cardphone);
                    try
                    {
                        Connect.sendMessage(tcp.buildMessage.doFunction("selectVipByPhoneOrId", map).toByteArray());
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                }
            }
        });

        txtNewName.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtNewPhone.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    pswOkNewPsw.grabFocus();
                }
            }
        });

        txtNewPhone.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    pswNewPsw.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtNewName.grabFocus();
                }
            }
        });

        pswNewPsw.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    pswOkNewPsw.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    txtNewPhone.grabFocus();
                }
            }
        });

        pswOkNewPsw.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN)
                {
                    txtNewName.grabFocus();
                }
                if (e.getKeyCode() == KeyEvent.VK_UP)
                {
                    pswNewPsw.grabFocus();
                }
            }
        });


        new hintWindow(txtName)
        {
            @Override
            public void keyTyped(KeyEvent e)
            {

                StringBuilder stringBuilder = new StringBuilder();
                char temp = e.getKeyChar();
                int charCode = Integer.valueOf(temp);
                // 中文 回车 退格 符进行判断
                if (charCode > 255 || charCode == 8 || charCode == 10)
                {
                    if (charCode == 8)
                    {
                        datePicker.getFormattedTextField().setText(null);
                        patientId = 0;
                        cbbSex.setSelectedIndex(0);
                        txtPhone.setText("");
                        identity.setText("");
                        pswPassword.setText("123456");
                        pswSure.setText("123456");

                    }
                    if (charCode != 10)
                    {
                        if (charCode > 255)
                            stringBuilder.append(txtName.getText() + temp);
                        else if (charCode == 8)
                            stringBuilder.append(txtName.getText());
                        String name = stringBuilder.toString();
                        Map<String, Object> map = new HashMap<>();
                        map.put("spell", name);
                        System.out.println(name);
                        if (!name.equals(""))
                        {
                            Connect.sendMessage(buildMessage.doFunction("getPatientBySpell", map).toByteArray());
                        }
                        else
                            hintWindow.map.get(txtName).setVisible(false);
                    }
                    else
                    {
                        if (txtName.getText() != null && !txtName.getText().equals(""))
                            datePicker.getFormattedTextField().grabFocus();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e)
            {
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
            }

            protected void setText(String selectedValue)
            {
                if (selectedValue != null)
                {
                    String[] temp = selectedValue.split("/");
                    if (temp.length == 5)
                    {
                        int id = Integer.parseInt(temp[4]);
                        patientId = id;
                        Map map = patientMap.get(id);

                        if (map.get("vip") != null && !map.get("vip").equals(""))
                        {
                            warning("此病人已经绑定了会员卡！");
                            patientId = 0;
                            txtName.setText("");
                            return;
                        }

                        if (map != null)
                        {
                            txtName.setText((String) map.get("name"));
                            txtPhone.setText((String) map.get("phone"));
                            String date = (String) map.get("birth");
                            datePicker.getFormattedTextField().setText(date.split(" ")[0]);
                            String sex = (String) map.get("sex");
                            if (sex.equals("男"))
                                cbbSex.setSelectedIndex(1);
                            else
                                cbbSex.setSelectedIndex(2);
                            identity.setText((String) map.get("identity"));
                        }
                    }

                }
                else
                {
                    patientId = 0;
                    datePicker.getFormattedTextField().grabFocus();
                }
            }
        };


    }

    // 按钮监听器
    private void buttonListeners()
    {
        // 确认办理
        btnSaveMakeCard.addActionListener((ActionEvent e) ->
        {
            // 两次密码不一致
            String password = new String(pswPassword.getPassword());
            String pswsure = new String(pswSure.getPassword());
            if (password.length() != 6)
            {
                warning(paswordLengthWrong);
                return;
            }
            if (!password.equals(pswsure))
            {
                warning(passwordWrong);
                return;
            }
            // 输入不合法
            String card = txtCard.getText();
            String phone = txtPhone.getText();
            String name = txtName.getText();
            String sex = (String) cbbSex.getSelectedItem();

            if (name.equals("") || !card.matches(cardRegex) || !phone.matches(phoneRegex))
            {
                warning(inputError);
                return;
            }

            // 向数据库添加会员卡信息
            Map<String, Object> map = new HashMap<>();
            map.put("id", card);
            map.put("password", password);
            if (patientId != 0)
                map.put("patientid", patientId);
            map.put("name", name);
            map.put("phone", phone);
            map.put("sex", sex);
            map.put("birth", datePicker.getFormattedTextField().getText());
            map.put("identity", identity.getText());
            try
            {
                Connect.sendMessage(buildMessage.doFunction("addVip", map).toByteArray());
                btnSaveMakeCard.setEnabled(false);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        // 确认充值
        btnSaveReCharge.addActionListener((ActionEvent e) ->
        {
            String card = txtCardRecharge.getText();
            String recharge = txtRecharge.getText();
            String present = txtPresent.getText();
            if (!card.matches(cardRegex))
            {
                warning(inputError);
                return;
            }
            float remain = Float.parseFloat(recharge);
            float gift = Float.parseFloat(present);

            if(remain == 0 && gift ==0)
            {
                warning("请至少输入一个充值金额!");
                return;
            }

            Map<String, Object> map = new HashMap<>();
            map.put("id", Integer.parseInt(card));
            map.put("remain", remain);
            map.put("present", gift);
            // 更新数据库
            try
            {
                Connect.sendMessage(buildMessage.doFunction("payForVip", map).toByteArray());
                btnSaveReCharge.setEnabled(false);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        btnLose.addActionListener((ActionEvent e) ->
        {
            String cardOrPhone = txtCardOrPhone.getText();
            if (!(cardOrPhone.matches(cardRegex) || cardOrPhone.matches(phoneRegex)))
            {
                warning(inputError);
                return;
            }
            Map<String, Object> map = new HashMap<>();
            if (cardOrPhone.length() == 11)
            {
                map.put("phone", cardOrPhone);
            }
            else
            {
                map.put("id", Integer.parseInt(cardOrPhone));
            }
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("lostVip", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        btnOkUp.addActionListener((ActionEvent e) ->
        {
            String oldCardOrPhone = txtOldCardOrPhone.getText();
            String newCard = txtNewCard.getText();
            Map<String, Object> map = new HashMap<>();
            map.put("old", oldCardOrPhone);
            map.put("phone", oldCardOrPhone);
            map.put("new", newCard);
            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("remakeCard", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        btnOkReset.addActionListener((ActionEvent e) ->
        {
            String newname = txtNewName.getText();
            String newphone = txtNewPhone.getText();
            String newPswPsw = new String(pswNewPsw.getPassword());
            String newOkPsw = new String(pswOkNewPsw.getPassword());
            if (lbCard.getText().equals(""))
            {
                txtCard.setText("");
                card = 0;
                txtNewCard.setText("");
                lbCard.setText("");
                txtNewName.setText("");
                txtNewPhone.setText("");
                fdatePicker.getFormattedTextField().setText("");
                lbRemain.setText("");
                lbGift.setText("");
                lbGrade.setText("");
                warning("请输入会员卡号或联系电话！");
                return;
            }
            if (newname.equals("") || !newphone.matches(phoneRegex) || newPswPsw.equals("") || newPswPsw.length() != 6)
            {
                warning(inputError);
                return;
            }
            if (!newPswPsw.equals(newOkPsw))
            {
                warning("两次密码不一致！");
                return;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", card);
            map.put("name", newname);
            map.put("phone", newphone);
            map.put("password", newPswPsw);
            map.put("sex", fcbbSex.getSelectedItem());
            map.put("birth", fdatePicker.getFormattedTextField().getText());
            map.put("identity", fidentity.getText());

            try
            {
                Connect.sendMessage(tcp.buildMessage.doFunction("updateVip", map).toByteArray());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        });

        btnExport.addActionListener((ActionEvent e) ->
        {
            ExcelExporter excelExporter = new ExcelExporter();
            String path = excelExporter.selectPath();
            if (path != null)
            {
                try
                {
                    excelExporter.exportFile(path, "会员信息表", vipCn, model);
                }
                catch (Exception e1)
                {
                    warning(fileError);
                    e1.printStackTrace();
                }
            }
        });
    }

    // 设置某些组件的显示文字
    private void setComponentText(String[] data)
    {
        txtCard.setText(data[0]);
        txtName.setText(data[1]);
        txtPhone.setText(data[2]);
        identity.setText(data[3]);
        datePicker.getFormattedTextField().setText("");
        pswPassword.setText("123456");
        pswSure.setText("123456");
        patientId = 0;
    }

}
