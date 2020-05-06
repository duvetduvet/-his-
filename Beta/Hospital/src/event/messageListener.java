package event;

import java.util.EventListener;

public interface messageListener extends EventListener {
    public void messageEvent(MessageEvent event);

}


