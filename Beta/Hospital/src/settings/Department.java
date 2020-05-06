package settings;

import static tool.Headers.departCn;

public class Department extends SetModel {
    public Department() {
        String view = "department";
        setMessageListener(view);
        initUI(departCn, "科室设置", view);
        windowListeners();
        buttonListeners(view);
        lbName.setText("科室名称：");
        setModal(true);
        setVisible(true);
    }
}
