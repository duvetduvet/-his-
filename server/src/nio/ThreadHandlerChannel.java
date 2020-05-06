package nio;

import DataBase.buildMessage;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import proto.myMessage;
import user.userStatement;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;


public class ThreadHandlerChannel extends Thread
{
    private SelectionKey key;

    //private ThreadLocal<SelectionKey> selectionKeyThreadLocal = new ThreadLocal<SelectionKey>();

    ThreadHandlerChannel(SelectionKey key)
    {
        this.key = key;
    }

    public SelectionKey getKey()
    {

        return key;
    }

    public void Run()
    {
        SocketChannel channel = (SocketChannel) key.channel();

        // 创建读取的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10240);
        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        // 读取缓冲区数据长度的
        int index = 0;
        int readCount = 0;
        byte[] data = new byte[1024];
        boolean mark = true;
        int size = 0;
        try
        {
            while (true)
            {
                // 读取状态才能用 compact, 先写再读再compact
                sizeBuffer.clear();
                int read = channel.read(buffer);
                if (buffer.position() == 0)
                    break;
                if (read != -1)
                {
                    buffer.flip();
                    if (mark)
                    {
                        byte[] temp = new byte[4];
                        buffer.get(temp);
                        sizeBuffer.put(temp);
                        sizeBuffer.flip();
                        size = sizeBuffer.getInt();
                        data = new byte[size];
                        System.out.println("accept data:" + read);
                        System.out.println("message length:" + size);
                        mark = false;
                        readCount = 0;
                    }

                    // 读取buffer里面的数据
                    while (buffer.hasRemaining())
                    {
                        data[index++] = buffer.get();
                        if (index == size)
                        {
                            buffer.compact();
                            mark = true;
                            readCount = 0;
                            index = 0;
                            break;
                        }
                    }
                    protocolMessage(key, data);
                }
                else
                {
                    channel.close();
                    key.cancel();
                    key.selector().wakeup();
                    return;
                }

            }
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.selector().wakeup();
        }
        catch (Exception e)
        {
            System.out.println("connect closed");
            try
            {
                channel.close();
            }
            catch (IOException ex)
            {
                System.out.println("colse faild");
            }
        }
    }

    public void run()
    {
        SocketChannel channel = (SocketChannel) key.channel();
        userStatement userStatement = (userStatement) key.attachment();
        ByteBuffer sizeBuffer = userStatement.sizeBuffer;
        // 读取缓冲区数据长度的
        int readCount = 0;
        ByteBuffer buffer = userStatement.buffer;
        try
        {
            while (true)
            {
                // 读取状态才能用 compact, 先写再读再compact , copact 方法会成为写状态 position  = limit -position,limit = capacity
                // 假设buffer 状态为写状态
                int read = channel.read(buffer);
                System.out.println(read);
                // 读取状态
                buffer.flip();
                // 判断是否断开连接
                if (read != -1)
                {
                    // 缓冲区没有数据了
                    if (!userStatement.buffer.hasRemaining())
                    {
                        // 如果没有接收到那就等待下一次的读取再拼接
                        buffer.compact();
                        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                        key.selector().wakeup();
                        return;
                        // 好像这里只有一个出口
                    }
                    // 是否首消息
                    if (userStatement.mark)
                    {
                        // 如果新信息少于4个不能构成一个完整头,就返回
                        if (buffer.remaining() < 4)
                        {
                            // 从读取状态到写状态
                            buffer.flip();
                            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                            key.selector().wakeup();
                            return;
                        }
                        // 如果新消息大于4,先读取出一个头
                        byte[] temp = new byte[4];
                        buffer.get(temp);
                        sizeBuffer.clear();
                        sizeBuffer.put(temp);
                        sizeBuffer.flip();
                        userStatement.size = sizeBuffer.getInt();
                        System.out.println("accept head length:" + userStatement.size);
                        userStatement.bytes = new byte[userStatement.size];
                        userStatement.mark = false;
                        userStatement.index = 0;
                    }

                    // 读取buffer里面的数据
                    while (buffer.hasRemaining())
                    {
                        userStatement.bytes[userStatement.index++] = buffer.get();
                        // 如果构成一个完整的信息
                        if (userStatement.index == userStatement.size)
                        {

                            userStatement.mark = true;
                            userStatement.index = 0;
                            protocolMessage(key, userStatement.bytes);
                            break;
                        }
                    }
                    // 重回写状态
                    buffer.compact();
                }
                else
                {
                    System.out.println("连接断开");
                    channel.close();
                    key.channel();
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("connect closed");
            try
            {
                channel.close();
            }
            catch (IOException ex)
            {
                System.out.println("close failed");
            }
        }
    }


    public static void protocolMessage(SelectionKey selectionKey, byte[] bytes)
    {
        try
        {
            myMessage.request request = myMessage.request.parseFrom(bytes);
            System.out.println(request);
            String functionName = request.getFunctionName();
            userStatement userStatement = (user.userStatement) selectionKey.attachment();
            String login = new String("Login");

            if (functionName.equals("beat"))
            {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("beat", 1, "success").toByteArray());
                return;
            }

            if (!userStatement.isLogin())
            {
                if (functionName.equals(login) == false)
                {
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback(functionName, -100, "权限不够").toByteArray());
                    return;
                }
            }
            else
            {
                if (!userStatement.judge(functionName))
                {
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback(functionName, -100, "权限不够").toByteArray());
                    return;
                }
            }
            byte[] parameter;
            Any any = request.getDetails();

            try
            {
                Class<?> c = null;
                c = Class.forName("DataBase.db");
                if (any == null || any.getTypeUrl().equals("") == true)
                {
                    Method method = c.getMethod(functionName, SelectionKey.class);
                    method.invoke(null, selectionKey);
                }
                else
                {
                    Method method = c.getMethod(functionName, SelectionKey.class, Any.class);
                    method.invoke(null, selectionKey, any);
                }
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                System.out.println("没有该类");
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
                System.out.println("没有该方法");
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }

        }
        catch (InvalidProtocolBufferException e)
        {
            System.out.println("所传的信息不是requestMessage");

        }
    }

    public static void sendMessage(SocketChannel socketChannel, byte[] bytes)
    {

        if (bytes.length == 0 || !socketChannel.isConnected())
            return;
        else
        {
            ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
            int size = bytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(size);
            sizeBuffer.putInt(size);
            sizeBuffer.flip();
            buffer.put(bytes);
            buffer.flip();
            System.out.println("sendMessageLenth:" + bytes.length);
            System.out.println();
            ByteBuffer dest[] = {sizeBuffer, buffer};
            try
            {
                while (sizeBuffer.hasRemaining() || buffer.hasRemaining())
                    socketChannel.write(dest);
            }
            catch (IOException e)
            {
                System.out.println("通道异常关闭:" + socketChannel);
                e.printStackTrace();
                try
                {
                    socketChannel.close();
                }
                catch (IOException e1)
                {
                    e1.printStackTrace();
                    System.out.println("关闭通道失败:" + socketChannel);
                }
            }


        }
    }

    public static void sendMessage(SelectionKey selectionKey, byte[] bytes)
    {
        sendMessage((SocketChannel) selectionKey.channel(), bytes);
    }

    public static void sendMessage(SelectionKey selectionKey, myMessage.feedback feedback)
    {
        System.out.println("FunctionName:"+feedback.getFunctionName());
        System.out.println("Mark:" + feedback.getMark());
        System.out.println("Message"+ feedback.getBackMessage());
        sendMessage((SocketChannel) selectionKey.channel(), feedback.toByteArray());
    }

    public static void main(String[] arg) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, SQLException
    {
        /*Object[] objects = new Object[3];
        Class<?> c = Class.forName("DataBase.db");
        Method method = c.getMethod("Login", Connection.class, List.class);
        method.invoke(null, C3P0ConnentionProvider.getConnection(), objects);
*/
        ServerSocketChannel channel;

        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName());
    }
}
