package tcp;

import com.google.protobuf.InvalidProtocolBufferException;
import event.messageManager;
import proto.myMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Hashtable;
import java.util.Iterator;

public class TCPClientReadThread implements Runnable
{
    public static Hashtable hashtable = new Hashtable();

    private Selector selector;

    public TCPClientReadThread(Selector s)
    {
        selector = s;
    }

    static public void read(SelectionKey key)
    {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer sizeBuffer = readBuffer.sizeBuffer;
        // 读取缓冲区数据长度的
        ByteBuffer buffer = readBuffer.buffer;
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
                    if (!readBuffer.buffer.hasRemaining())
                    {
                        // 如果没有接收到那就等待下一次的读取再拼接
                        buffer.compact();
                        key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                        key.selector().wakeup();
                        return;
                        // 好像这里只有一个出口
                    }
                    // 是否首消息
                    if (readBuffer.mark)
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
                        readBuffer.size = sizeBuffer.getInt();
                        System.out.println("accept head length:" + readBuffer.size);
                        readBuffer.bytes = new byte[readBuffer.size];
                        readBuffer.mark = false;
                        readBuffer.index = 0;
                    }

                    // 读取buffer里面的数据
                    while (buffer.hasRemaining())
                    {
                        readBuffer.bytes[readBuffer.index++] = buffer.get();
                        // 如果构成一个完整的信息
                        if (readBuffer.index == readBuffer.size)
                        {
                            readBuffer.mark = true;
                            readBuffer.index = 0;
                            myMessage.feedback feedback = myMessage.feedback.parseFrom(readBuffer.bytes);
                            if (feedback.getMark() == -100)
                                tool.Tool.warning("您的权限不足，请联系管理员申请权限，并尝试重新操作！");
                            else
                            {
                                hashtable.put(feedback.getFunctionName(), feedback);
                                // 触发事件
                                messageManager.triggerEvent(feedback);
                                break;
                            }
                        }
                    }
                    // 重回写状态
                    buffer.compact();
                }
                else
                {
                    System.out.println("与服务器连接断开，请重新登录！");
                    Connect.colseConnect();
                }
            }
        }
        catch (InvalidProtocolBufferException e)
        {
            // 不知道这里是否要重新连接
            e.printStackTrace();
        }
        catch (IOException e)
        {
            System.out.println("与服务器连接断开，请重新登录！");
            Connect.colseConnect();
        }
    }

    public void run()
    {
        try
        {
            while (true)
            {
                // 判断是否有服务器有返回消息
                selector.select();
                Iterator ite = selector.selectedKeys().iterator();
                while (ite.hasNext())
                {
                    SelectionKey key = (SelectionKey) ite.next();
                    ite.remove();
                    if (key.isConnectable())
                    {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (channel.isConnectionPending())
                        {
                            channel.finishConnect();
                            System.out.println("has finish Connect");
                        }
                        channel.configureBlocking(false);
                        channel.register(selector, SelectionKey.OP_READ);
                    }
                    else if (key.isReadable())
                    {
                        key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                        new Thread(() ->
                        {
                            TCPClientReadThread.read(key);
                        }).start();

                    }
                }

                // 判断是否医保有返回信息


            }
        }
        catch (ClosedChannelException e)
        {
            Connect.colseConnect();
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Connect.colseConnect();
            e.printStackTrace();
        }

    }

}
