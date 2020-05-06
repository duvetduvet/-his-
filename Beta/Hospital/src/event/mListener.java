package event;

import java.util.EventListener;

public abstract class mListener implements EventListener {
    String functionName = null;

    public mListener(String functionName) {
        this.functionName = functionName;
        messageManager.addMessageListener(this);
    }

    public abstract void messageEvent(MessageEvent event);

    public boolean isFunction(String functionName) {
        if (this.functionName.equals(functionName))
            return true;
        else
            return false;
    }

    public void colse() {
        messageManager.removeMessageListener(this);
    }
}
