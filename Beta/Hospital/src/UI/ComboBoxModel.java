package UI;


import javax.swing.*;

public class ComboBoxModel extends DefaultComboBoxModel {
    public ComboBoxModel(String[] elements) {
        for (String element : elements) {
            addElement(element);
        }
    }

}
