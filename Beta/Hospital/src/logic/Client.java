package logic;

import insurance.Insurance;

import javax.swing.*;
import java.awt.*;

public class Client {
    public static void main(String[] args) throws Exception {
        // 设置UI为系统默认风格
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        // 设置全局字体
        Font font = new Font("微软雅黑", Font.PLAIN, 20);
        String components[] = {"Label", "CheckBox", "PopupMenu", "MenuItem", "CheckBoxMenuItem",
                "JRadioButtonMenuItem", "ComboBox", "Button", "Tree", "ScrollPane",
                "TabbedPane", "EditorPane", "TitledBorder", "Menu", "TextArea",
                "OptionPane", "MenuBar", "ToolBar", "ToggleButton", "ToolTip",
                "ProgressBar", "TableHeader", "Panel", "List", "ColorChooser",
                "PasswordField", "TextField", "Table", "Label", "Viewport",
                "RadioButtonMenuItem", "RadioButton", "DesktopPane", "InternalFrame"
        };
        for (String component : components) {
            UIManager.put(component + ".font", font);
        }
        new Check();

        Insurance.getInsurance();
    }
}
