package event;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import proto.myMessage;
import tool.Tool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class messageManager {
    private static Collection listeners;

    public synchronized static void removeAllMessageListener() {
        if (listeners != null)
            listeners.clear();
    }


    public synchronized static void addMessageListener(mListener mListener) {
        if (listeners == null)
            listeners = new HashSet();
        listeners.add(mListener);
    }

    public synchronized static void removeMessageListener(mListener mListener) {
        if (listeners == null)
            return;
        else
            listeners.remove(mListener);
    }

    private synchronized static void notifyListeners(MessageEvent messageEvent) {
        String functionName = messageEvent.getMessage(Message.class).getFunctionName();

        Iterator iterator = listeners.iterator();
        while (iterator.hasNext()) {
            mListener mListener = (mListener) iterator.next();
            if (mListener.isFunction(functionName))
                try
                {
                    mListener.messageEvent(messageEvent);
                }
                catch (Exception e)
                {
                    // 如果组件被销毁那么就进入异常
                    e.printStackTrace();
                    // 如果是空指针异常那么就是没有及时清理信息的问题，不提示
                    if(e.getClass() != NullPointerException.class)
                        Tool.warning(mListener.functionName + " 操作失败！！！");
                }
        }
    }

    public synchronized static void triggerEvent(Message feedback) {
        if (listeners == null)
            return;
        else {
            MessageEvent event = new MessageEvent(null, feedback);
            notifyListeners(event);
        }
    }



}
