package event;

import proto.myMessage;
import tool.Tool;

import java.util.EventObject;

public class MessageEvent extends EventObject {

    private Message feedback = null;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */

    public MessageEvent(Object source, Message feedback) {
        super(new Object());
        this.feedback = feedback;
    }

    public myMessage.feedback getMessage() {
        return (myMessage.feedback)feedback;
    }

    // 为了兼容。。。
    public <T> T getMessage(Class<T> type) {
        return type.cast(feedback);
    }

    public void setMessage(Message feedback) {
        this.feedback = feedback;
    }
}
