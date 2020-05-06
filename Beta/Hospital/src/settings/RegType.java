package settings;

import static tool.Headers.regTypeCn;

public class RegType extends SetModel {

    public RegType() {
        String view = "regtype";
        setMessageListener(view);
        initUI(regTypeCn, "挂号类型设置", view);
        buttonListeners(view);
        windowListeners();
        lbName.setText("挂号类型：");
        setModal(true);
        setVisible(true);
    }
}
