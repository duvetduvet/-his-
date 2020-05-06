package insurance;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static tool.XmlOperator.*;
import static tool.Strings.*;

import org.dom4j.Document;
import org.dom4j.Element;
import tool.Tool;


public class Insurance
{
    private int status = -1;
    private static Insurance insurance = null;
    private Document userYibaoDoc;
    private List<Element> elementsYibao;
    private List<String> userYibaoInfo = new ArrayList<>();


    //加载dll文件
    public interface Dll extends StdCallLibrary
    {
        Dll INSTANCE = (Dll) Native.loadLibrary("\\ZRHosJK.dll", Dll.class);//加载动态库文件
        Dll INSTANCE_1 = (Dll) Native.loadLibrary("\\Mwic_32.dll", Dll.class);
        Dll INSTANCE_2 = (Dll) Native.loadLibrary("\\ZrkjDll.dll", Dll.class);
        Dll INSTANCE_3 = (Dll) Native.loadLibrary("\\StICCard.dll", Dll.class);
//        Dll INSTANCE_4 = (Dll) Native.loadLibrary("\\HNIC32.dll", Dll.class);
//        Dll INSTANCE_13 = (Dll) Native.loadLibrary(".\\lib\\decode.dll", Dll.class);
//        Dll INSTANCE_5 = (Dll) Native.loadLibrary(".\\lib\\easycry.dll", Dll.class);
//        Dll INSTANCE_6 = (Dll) Native.loadLibrary(".\\lib\\Mwic_32.dll", Dll.class);
//        Dll INSTANCE_7 = (Dll) Native.loadLibrary(".\\lib\\ociw32.dll", Dll.class);
//        Dll INSTANCE_8 = (Dll) Native.loadLibrary(".\\lib\\sehr.crypto.dll", Dll.class);
//        Dll INSTANCE_9 = (Dll) Native.loadLibrary(".\\lib\\SiCard.dll", Dll.class);
//        Dll INSTANCE_10 = (Dll) Native.loadLibrary(".\\lib\\SSCardDriver_MT.dll", Dll.class);
//        Dll INSTANCE_11 = (Dll) Native.loadLibrary(".\\lib\\Mwic_32.dll", Dll.class);
//        Dll INSTANCE_12 = (Dll) Native.loadLibrary(".\\lib\\StExt32.dll", Dll.class);
//        Dll INSTANCE_14 = (Dll) Native.loadLibrary(".\\lib\\ydwyAf.dll", Dll.class);
//        Dll INSTANCE_15 = (Dll) Native.loadLibrary(".\\lib\\ydwyJX.dll", Dll.class);


        public int f_UserBargaingApply(String YWLX, String Indata, Pointer Outdata, Pointer mess);//交易申请

        public int f_UserBargaingInit(String userId, String passWord, Pointer mess);//交易初始化

        public int f_UserBargaingClose(Pointer mess);//交易关闭
    }

    //单例模式
    private Insurance()
    {
        System.setProperty("jna.encoding", "GBK");
        insuranceMessage message = new insuranceMessage();
        userYibaoDoc = readXml(userYibaoPath);
        elementsYibao = getElements(getRoot(userYibaoDoc));
        userYibaoInfo = getText(elementsYibao);

        if(userYibaoInfo.get(0).equals(""))
            return;
        message.addElement(1, userYibaoInfo.get(0));
        message.addElement(2, userYibaoInfo.get(1));

        if(UserBargaingInit(message).getMark()< 0)
            Tool.warning("医保连接失败！！请重启客户端重试~~~");
    }

    public static Insurance getInsurance()
    {
        if (insurance == null)
        {
            insurance = new Insurance();
        }
        return insurance;
    }

    public int getStatus()
    {
        return status;
    }

    public insuranceMessage UserBargaingInit(insuranceMessage inMess)
    {
        List<Map<String, String>> list = inMess.getInData();
        Map<String, String> cardInformation = list.get(0);
        String userId = cardInformation.get("0");
        String passWord = cardInformation.get("1");
        Pointer p = new Memory(20480);
        p.setMemory(0,20480,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingInit(userId, passWord, p);
        status = mark;
        inMess.setMark(mark);
        byte[] bytes = p.getByteArray(0, 20480);

        try
        {
            inMess.setRetMsg(new String(bytes, "GBK"));
            System.out.println(new String(bytes, "GBK"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return inMess;
    }

    public insuranceMessage UserBargaingClose()
    {
        insuranceMessage inMess = new insuranceMessage();
        Pointer p = new Memory(20480);
        p.setMemory(0,20480,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingClose(p);
        status = mark;
        inMess.setMark(mark);
        byte[] bytes = p.getByteArray(0, 20480);
        try
        {
            inMess.setRetMsg(new String(bytes, "GBK"));
            new String(new String(bytes, "GBK"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return inMess;
    }

    //挂号
    public insuranceMessage clinicRegister(insuranceMessage inMess)
    {
        List<Map<String, String>> list = new LinkedList<>();
        String[] inData_message = explain_from_inData(inMess.getInData());
        String inData = "";
        for (int i = 0; i < inData_message.length; i++)
        {
            if (i != (inData_message.length - 1))
                inData = inData + inData_message[i] + ";";
            else
                inData = inData + inData_message[i];
        }
        /*List<Map<String,String>> list = new LinkedList<>();
        String[] card_messages = new String[10];
        card_messages[0] = "0100011552";
        card_messages[1] = "朱俊奇";
        card_messages[2] = "36070125963042";
        card_messages[3] = "360701";
        card_messages[4] = "11";
        card_messages[5] = "NULL";
        card_messages[6] = "6";
        card_messages[7] = "20181126";
        card_messages[8] = "2223";
        card_messages[9] = "3";
        String inData = "";
        for(int i = 0; i < card_messages.length; i++){
            if(i != 9)
                inData = inData+card_messages[i]+"|";
            else
                inData = inData+card_messages[9];
        };*/
        System.out.println(inData);
        inData = exchangeCode(inData);
        Pointer p1 = new Memory(10000);
        p1.setMemory(0,10000,(byte)0);
        Pointer p2 = new Memory(10000);
        p2.setMemory(0,10000,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingApply("MZGH", inData, p1, p2);
        inMess.setMark(mark);
        byte[] bytes_1 = p1.getByteArray(0, 10000);
        byte[] bytes_2 = p2.getByteArray(0, 10000);
        list = explainOutData(removeEmptyBytes(bytes_1));
        inMess.setOutData(list);
        try
        {
            inMess.setRetMsg(new String(bytes_2, "GBK"));
            String string = new String(bytes_1, "GBK");
            System.out.println(string);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        //输出返回的数据包数据
        return inMess;
    }

    public insuranceMessage clinicRegisterOffset(insuranceMessage inMess)
    {
        List<Map<String, String>> list = new LinkedList<>();
        String[] card_messages = new String[5];
        String[] strings = information_from_cardInformation(list);
        for (int i = 0; i < strings.length; i++)
        {
            card_messages[i] = strings[i];
        }
        card_messages[4] = "3";
        String inData = null;
        for (int i = 0; i < card_messages.length; i++)
        {
            if (i != 3)
                inData = inData + card_messages[i] + "|";
            else
                inData = inData + card_messages[3];
        }
        Pointer p1 = new Memory(10000);
        p1.setMemory(0,10000,(byte)0);
        Pointer p2 = new Memory(10000);
        p2.setMemory(0,10000,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingApply("MZGHCX", inData, p1, p2);
        inMess.setMark(mark);
        byte[] bytes = p2.getByteArray(0, 10000);
        try
        {
            inMess.setRetMsg(new String(bytes, "GBK"));
            System.out.println(inMess.getRetMsg());
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return inMess;
    }

    //门诊挂号预收费
    public insuranceMessage clinicPreCharge(insuranceMessage inMess)
    {
        List<Map<String, String>> list = new LinkedList<>();
        String[] inData_message = explain_from_inData(inMess.getInData());
        String inData = "";
        for (int i = 0; i < inData_message.length; i++)
        {
            if (i != (inData_message.length - 1))
                inData = inData + inData_message[i] + ";";
            else
                inData = inData + inData_message[i];
        }
        inData = exchangeCode(inData);
        Pointer p1 = new Memory(10000);
        p1.setMemory(0,10000,(byte)0);
        Pointer p2 = new Memory(10000);
        p2.setMemory(0,10000,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingApply("MZSFYJS", inData, p1, p2);
        inMess.setMark(mark);
        byte[] bytes_1 = p1.getByteArray(0, 10000);
        byte[] bytes_2 = p2.getByteArray(0, 10000);
        list = explainOutData(removeEmptyBytes(bytes_1));
        inMess.setOutData(list);
        try
        {
            inMess.setRetMsg(new String(bytes_1, "GBK"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return inMess;
    }

    public insuranceMessage clinicCharge(insuranceMessage inMess)
    {
        List<Map<String, String>> list = new LinkedList<>();
        String[] inData_message = explain_from_inData(inMess.getInData());
        String inData = "";
        for (int i = 0; i < inData_message.length; i++)
        {
            if (i != (inData_message.length - 1))
                inData = inData + inData_message[i] + ";";
            else
                inData = inData + inData_message[i];
        }
        inData = exchangeCode(inData);
       /* String[] package_A = new String[9];
        String[] package_B = new String[7];
        package_A[0] = "0100011552";
        package_A[1] = "朱俊奇";
        package_A[2] = "36070125963042";
        package_A[3] = "360701";
        package_A[4] = "94118717";//挂号返回的门诊号
        package_A[5] = "NULL";
        package_A[6] = "NULL";
        package_A[7] = "杨佳平";
        package_A[8] = "FALSE";
        String inData = "";
        String inData_A = "";
        String inData_B = "";
        for(int i = 0; i < package_A.length; i++){
            if(i != 8)
                inData_A = inData_A+package_A[i]+"|";
            else
                inData_A = inData_A+package_A[8]+";";
        };
        package_B[0] = "Z-A12HA-Y0554";
        package_B[1] = "银杏叶口服液";
        package_B[2] = "6";
        package_B[3] = "10";
        package_B[4] = "60";
        package_B[5] = "20181127";
        package_B[6] = "Z-A12HA-Y0554&银杏叶口服液";
        for(int i = 0; i < package_B.length; i++){
            if(i != 6)
                inData_B = inData_B+package_B[i]+"|";
            else
                inData_B = inData_B+package_B[6];
        };
        inData = inData+inData_A+inData_B;*/
        inData = exchangeCode(inData);
        System.out.println(inData);
        Pointer p1 = new Memory(10000);
        p1.setMemory(0,10000,(byte)0);
        Pointer p2 = new Memory(10000);
        p2.setMemory(0,10000,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingApply("MZSF", inData, p1, p2);
        inMess.setMark(mark);
        byte[] bytes_1 = p1.getByteArray(0, 10000);
        byte[] bytes_2 = p2.getByteArray(0, 10000);
        list = explainOutData(removeEmptyBytes(bytes_1));
        inMess.setOutData(list);
        try
        {
            inMess.setRetMsg(new String(bytes_1, "GBK"));
            System.out.println(inMess.getRetMsg());
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return inMess;
    }

    public insuranceMessage clinicChargeOffset(insuranceMessage inMess)
    {
        List<Map<String, String>> list = new LinkedList<>();
        String[] inData_message = explain_from_inData(inMess.getInData());
        String inData = "";
        for (int i = 0; i < inData_message.length; i++)
        {
            if (i != (inData_message.length - 1))
                inData = inData + inData_message[i] + ";";
            else
                inData = inData + inData_message[i];
        }
        inData = exchangeCode(inData);
        System.out.println(inData);
        Pointer p1 = new Memory(10000);
        p1.setMemory(0,10000,(byte)0);
        Pointer p2 = new Memory(10000);
        p2.setMemory(0,10000,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingApply("MZSFCX", inData, p1, p2);
        inMess.setMark(mark);
        byte[] bytes = p2.getByteArray(0, 10000);
        try
        {
            inMess.setRetMsg(new String(bytes, "GBK"));
            System.out.println(inMess.getRetMsg());
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return inMess;
    }

    public insuranceMessage readCard(insuranceMessage inMess)
    {
        List<Map<String, String>> list = new LinkedList<>();
        Pointer p1 = new Memory(10240);
        p1.setMemory(0,10240,(byte)0);
        Pointer p2 = new Memory(10240);
        p2.setMemory(0,10240,(byte)0);
        int mark = Dll.INSTANCE.f_UserBargaingApply("MZGHSK", null, p1, p2);
        inMess.setMark(mark);
        System.out.println(inMess.getMark());
        byte[] bytes_1 = p1.getByteArray(0, 10240);
        byte[] bytes_2 = p2.getByteArray(0, 10240);
        try
        {
            inMess.setRetMsg(new String(bytes_2, "GBK"));
            System.out.println(inMess.getRetMsg());
            list = explainOutData(removeEmptyBytes(bytes_1));
            inMess.setOutData(list);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return inMess;
    }

    public synchronized insuranceMessage doFunciton(insuranceMessage inMess)
    {
        String functionName = inMess.getFunctionName();
        if (functionName.equals("UserBargaingInit"))
            return UserBargaingInit(inMess);
        else if (functionName.equals("clinicRegister"))
            return clinicRegister(inMess);
        else if (functionName.equals("readCard"))
            return readCard(inMess);
        else if (functionName.equals("clinicCharge"))
            return clinicCharge(inMess);
        else if (functionName.equals("clinicChargeOffset"))
            return clinicChargeOffset(inMess);
        else if (functionName.equals("clinicRegisterOffset"))
            return clinicRegisterOffset(inMess);
        else if (functionName.equals("clinicPreCharge"))
            return clinicPreCharge(inMess);
        else if (functionName.equals("UserBargaingClose"))
            return UserBargaingClose();
        else return null;
    }

    //解析返回的数据包
    public List<Map<String, String>> explainOutData(byte[] bytes)
    {
        List<Map<String, String>> list_1 = new LinkedList<>();
        try
        {
            String userMessage = new String(bytes, "GBK");
            String[] messages = userMessage.split("\\;");
            for (int i = 0; i < messages.length; i++)
            {
                String[] messages_1 = messages[i].split("\\|");
                Map<String, String> map = new HashMap<>();
                for (int j = 0; j < messages_1.length; j++)
                {
                    map.put(String.valueOf(j), messages_1[j]);
                }
                list_1.add(map);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return list_1;
    }

    //从个人参保信息数据包取出保险号、姓名、卡号、地区编号
    public String[] information_from_cardInformation(List<Map<String, String>> list)
    {
        String[] card_messages = new String[4];
        for (int i = 0; i < list.size(); i++)
        {
            Map<String, String> map = new HashMap<>();
            map = list.get(i);
            for (int j = 0; j < 4; j++)
            {
                card_messages[j] = map.get(String.valueOf(j));
            }
        }
        return card_messages;
    }

    //解析inData数据包
    public String[] explain_from_inData(List<Map<String, String>> list)
    {
        String[] input_message = new String[list.size()];
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < list.size(); i++)
        {
            map = list.get(i);
            String string = "";
            for (int j = 0; j < map.size(); j++)
            {
                if (j != (map.size() - 1))
                {
                    string = string + map.get(String.valueOf(j)) + "|";
                }
                else
                {
                    string = string + map.get(String.valueOf(j));
                }
            }
            input_message[i] = string;
        }
        return input_message;
    }

    //转换编码
    public String exchangeCode(String inData)
    {
//        try
//        {
//            byte[] bytes = inData.getBytes();
//            inData = new String(bytes, "GBK");
//
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            e.printStackTrace();
//        }
        return inData;
    }

    //去掉空字节
    public byte[] removeEmptyBytes(byte[] bytes)
    {
        int n = 0;
        for (int i = 0; i < bytes.length; i++)
        {
            if (bytes[i] == 0 && bytes[i + 1] == 0)
                break;
            else
                n++;
        }
        byte[] bytes_1 = new byte[n];
        for (int i = 0; i < bytes.length; i++)
        {
            int j = i + 1;
            int k = j + 1;
            if (bytes[i] == 0)
            {
                if (bytes[j] == 0)
                {
                    break;
                }
                else
                {
                    bytes_1[i] = bytes[i];
                }
            }
            else
            {
                bytes_1[i] = bytes[i];
            }
        }
        return bytes_1;
    }

   /* public static void main(String[] args){
        Insurance insurance = new Insurance();
        insuranceMessage inMess = new insuranceMessage();
        Map<String,String> map = new HashMap<>();
        map.put("1","gzyb-jstlxh");
        map.put("2","000000h");
        List<Map<String,String>> list = new LinkedList<>();
        list.add(map);
        inMess.setInData(list);
        insurance.UserBargaingInit(inMess);
        //insurance.readCard();
        //insurance.clinicRegister(inMess);
        insurance.clinicCharge(inMess);
        //insurance.clinicChargeOffset(inMess);
    }*/
}
