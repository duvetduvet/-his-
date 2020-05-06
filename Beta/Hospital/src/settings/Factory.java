package settings;

import static tool.Headers.factoryCn;

public class Factory extends SetModel {

    public Factory() {
        String view = "factory";
        setMessageListener(view);
        initUI(factoryCn, "生产厂家设置", view);
        windowListeners();
        buttonListeners(view);
        lbName.setText("厂家名称：");
        setModal(true);
        setVisible(true);
    }
}
