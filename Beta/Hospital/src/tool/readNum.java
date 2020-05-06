package tool;

public class readNum {

    //　字节数组转长整形
    public static long unIntByte2long(byte[] res) {
        int index = 0;
        int firstByte = (0x000000FF & ((int) res[index]));
        int secondByte = (0x000000FF & ((int) res[index + 1]));
        int thirdByte = (0x000000FF & ((int) res[index + 2]));
        int fourthByte = (0x000000FF & ((int) res[index + 3]));
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }
}
