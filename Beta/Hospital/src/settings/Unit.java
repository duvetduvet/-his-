package settings;

import static tool.Headers.unitCn;

public class Unit extends SetModel {

    public Unit() {
        String view = "unit";
        setMessageListener(view);
        initUI(unitCn, "计量单位设置", view);
        windowListeners();
        buttonListeners(view);
        lbName.setText("单位名称：");
        setModal(true);
        setVisible(true);
    }
}