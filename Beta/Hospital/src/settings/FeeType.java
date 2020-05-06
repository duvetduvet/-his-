package settings;

import static tool.Headers.feeTypeCn;

public class FeeType extends SetModel {

    public FeeType() {
        String view = "feetype";
        setMessageListener(view);
        initUI(feeTypeCn, "费用类型设置", view);
        windowListeners();
        buttonListeners(view);
        lbName.setText("费用名称：");
        setModal(true);
        setVisible(true);
    }
}
