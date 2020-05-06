package nio;

import DataBase.C3P0ConnentionProvider;
import protocol.message;
import user.userStatement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioPoolServer
{
    // 线程池
    ExecutorService pool = Executors.newFixedThreadPool(50);
    private Selector selector;
    private boolean run = true;

    public NioPoolServer(int port) throws IOException
    {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(new InetSocketAddress(port));
        this.selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Service startup success!!!");
    }


    public void listen() throws IOException
    {
        System.out.println("start Listen");
        while (run)
        {
            selector.select();
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext())
            {
                SelectionKey key = (SelectionKey) ite.next();
                ite.remove();
                if (key.isAcceptable())
                {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel channel = server.accept();
                    channel.configureBlocking(false);

                    // 返回一个 SelectionKey
                    SelectionKey newKey = channel.register(selector, SelectionKey.OP_READ);
                    // 附加一个登录状态对象
                    newKey.attach(new userStatement());


                    System.out.println("Accept :" + channel.getRemoteAddress());

                    // 接收成功就返回 服务器连接成功 给客户端
                    //channel.write(ByteBuffer.wrap("Service connection success".getBytes()));
                    // NioPoolServer.sendMessage(channel, "Service connection success");
                }
                else if (key.isReadable())
                {
                    // 取消可读标记
                    key.interestOps(key.interestOps() & (~SelectionKey.OP_READ));
                    userStatement userStatement = (userStatement) key.attachment();
                    if (userStatement.isLogin())
                        System.out.println("User " + userStatement.getId() + "：");
                    else
                        System.out.println("未登录：");
                    pool.execute(new ThreadHandlerChannel(key));

                    //key.interestOps(key.interestOps() | SelectionKey.OP_READ);
                    //key.selector().wakeup();
                }
            }
        }
    }


    public static void main(String[] args)
    {
        try
        {

            new C3P0ConnentionProvider();
            NioPoolServer server = new NioPoolServer(8000);
            server.listen();

        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
