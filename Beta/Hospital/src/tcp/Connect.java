package tcp;

import com.google.protobuf.Any;
import event.messageManager;
import insurance.Insurance;
import insurance.insuranceMessage;
import proto.Func;
import proto.myMessage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static tool.Strings.disConnectFromServer;

public class Connect implements Runnable {
    public static SocketChannel socketChannel;
    private static Selector selector;
    private static String hostIp;
    private int port;

    public Connect(String ip, int port) throws IOException {
        if (socketChannel == null) {
            this.hostIp = ip;
            this.port = port;
            SocketChannel channel = SocketChannel.open();
            socketChannel = channel;
            channel.configureBlocking(false);
            selector = Selector.open();
            channel.connect(new InetSocketAddress(hostIp, this.port));
            channel.register(selector, SelectionKey.OP_CONNECT);

            // 一共有3个线程，一个读写服务器线程，一个GUI线程（也负责发送），一个新校线程
            // 新加：医保单独开一个线程负责发送

            // 读取线程启动
            new Thread(new TCPClientReadThread(selector)).start();
            // 当前类，心跳线程启动
            new Thread(this).start();

        } else
            return;

    }

    public static void colseConnect() {
        try {
            selector.close();
            socketChannel.close();
            selector = null;
            socketChannel = null;
            tool.Tool.warning(disConnectFromServer);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static boolean sendMessage(byte[] bytes) {
        if (socketChannel == null)
            return false;
        if (socketChannel.isConnected() == false) {
            tool.Tool.warning("与服务器连接断开，请重新登录！");
            System.exit(0);
        }
        if (bytes.length == 0)
            return false;
        else {
            ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
            int size = bytes.length;
            ByteBuffer buffer = ByteBuffer.allocate(size);
            sizeBuffer.putInt(size);
            sizeBuffer.flip();
            buffer.put(bytes);
            buffer.flip();
            ByteBuffer dest[] = {sizeBuffer, buffer};
            while (sizeBuffer.hasRemaining() || buffer.hasRemaining()) {
                try {
                    System.out.println(socketChannel.write(dest));
                } catch (IOException e) {
                    tool.Tool.warning("与服务器连接断开，请重新登录！");
                    System.exit(0);
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public synchronized static void sendMessage(insuranceMessage message)
    {
        new Thread(()->{
            // 新开一个临时线程执行医保的函数，防止卡住gui界面
            if(Insurance.getInsurance().getStatus() == 1)
            {
                Insurance insurance = Insurance.getInsurance();
                // 调用函数
                insurance.doFunciton(message);
                // 触发事件
                messageManager.triggerEvent(message);
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            // 39.108.133.117
            // <Login><int:1|String:123456>
            Connect connect = new Connect("127.0.0.1", 8000);
            //Thread.sleep(2000);
            //sendMessage(client.socketChannel, "lalala");

            Thread.sleep(3000);
            myMessage.request.Builder builder = myMessage.request.newBuilder();
            builder.setFunctionName("Login");
            Func.login.Builder builder1 = Func.login.newBuilder();
            builder1.setId(1);
            builder1.setPassword("123456");
            builder.setDetails(Any.pack(builder1.build()));

            byte[] temp = new byte[]{0};
            sendMessage(builder.build().toByteArray());
            sendMessage(builder.build().toByteArray());
//            for (int i = 0; i < 10; i++)
//                sendMessage(builder.build().toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        System.out.println("start beat thread");
        while (socketChannel != null) {
            try {
                Thread.sleep(60000);
                if (socketChannel != null)
                    sendMessage(buildMessage.doFunction("beat", null).toByteArray());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        tool.Tool.warning("与服务器连接断开，请重新登录！");
        System.exit(0);
    }
}
