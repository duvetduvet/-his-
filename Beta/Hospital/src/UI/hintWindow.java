package UI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.WeakHashMap;

abstract public class hintWindow {
    public static WeakHashMap<JTextField, hintWindow> map = new WeakHashMap();
    private JTextField jTextField;
    private DefaultListModel dlm;
    private JList jl1;
    private JPopupMenu popupMenu;
    private int rate;
    private Dimension screenSize;

    public hintWindow(JTextField jTextField, ArrayList<String> items) {
        this.jTextField = jTextField;
        for (String temp : items)
            dlm.addElement(temp);
        initComponent();

    }

    public hintWindow(JTextField jTextField) {
        this.jTextField = jTextField;
        initComponent();
        popupMenu.setVisible(false);
    }

    public static void main(String[] arg) {
        JFrame jFrame = new JFrame();
        jFrame.setSize(600, 800);

        JTextField jTextField = new JTextField();
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    System.out.println("docChange:" + e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    System.out.println("docChange:" + e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    System.out.println("docChange:" + e.getDocument().getText(0, e.getDocument().getLength()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });
        jFrame.setContentPane(jTextField);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jFrame.setVisible(true);

    }

    private void initComponent() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        rate = Toolkit.getDefaultToolkit().getScreenResolution() / 288;
        map.put(jTextField, this);
        dlm = new DefaultListModel();
        jl1 = new JList();
        jl1.setModel(dlm);
        // 除去悬浮选择的焦点
        jl1.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent fe) {
                // 如果有焦点就失去焦点
                jTextField.requestFocus();
            }

            @Override
            public void focusLost(FocusEvent fe) {
            }
        });
        JScrollPane jsp1 = new JScrollPane(jl1);
        popupMenu = new JPopupMenu();
        popupMenu.add(jsp1);
        popupMenu.setPopupSize(400,500);
        // 键盘监视器
        jTextField.addKeyListener(new mKeyListener());

    }

    public boolean updateList(String[] list) {
        dlm.removeAllElements();
        Point jText = jTextField.getLocationOnScreen();
        if (jText.y + jTextField.getHeight() + popupMenu.getComponent().getHeight() < screenSize.getHeight())
            popupMenu.show(jTextField, 0, 0 + jTextField.getHeight());
        else
            popupMenu.show(jTextField, 0, 0 - popupMenu.getComponent().getHeight());
        for (String temp : list)
            dlm.addElement(temp);
        jl1.setSelectedIndex(0);
        jTextField.requestFocusInWindow();
        return dlm.size() != 0;
    }

    public void setVisible(Boolean visible) {
        popupMenu.setVisible(visible);
    }

    protected void setText(String selectedValue) {
        jTextField.setText(selectedValue);
    }

    abstract public void keyTyped(KeyEvent e);

    abstract public void keyPressed(KeyEvent e);

    abstract public void keyReleased(KeyEvent e);

    private class mKeyListener implements KeyListener {
        //用于监听所有在文本框的按键指令
        @Override
        public void keyTyped(KeyEvent e) {
            hintWindow.this.keyTyped(e);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            hintWindow.this.keyPressed(e);
            int keycode = e.getKeyCode();
            if (keycode == KeyEvent.VK_DOWN || keycode == KeyEvent.VK_UP) {
                // 如果输入的上下方向键
                if (popupMenu.isShowing()) {
                    //所以增加该窗口是否show的判断
                    e.setSource(jl1);    //设置新的event源
                    jl1.dispatchEvent(e);//传输给jl1，dispatch: vt. 派遣；分派
                }
            } else if (keycode == KeyEvent.VK_ENTER) {
                e.setSource(popupMenu);
                popupMenu.dispatchEvent(e);
                if (popupMenu.isVisible()) {
                    if(jl1.getSelectedIndex()!=-1){
                        String selectedValue = jl1.getSelectedValue().toString();
                        setText(selectedValue);
                        // 添加完成后隐藏
                        popupMenu.setVisible(false);
                    }
                }

            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            hintWindow.this.keyReleased(e);
        }

    }


}