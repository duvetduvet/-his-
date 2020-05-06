package tcp;

import java.nio.ByteBuffer;

public class readBuffer {
    static boolean mark = true;
    static ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
    static ByteBuffer buffer = ByteBuffer.allocate(10240);
    static int index = 0;
    static byte[] bytes;
    static int size = 0;
}
