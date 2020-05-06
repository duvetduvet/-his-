package insurance;

import event.Message;
import tcp.Connect;

import java.text.SimpleDateFormat;
import java.util.*;

public class insuranceMessage implements Message
{
    private String functionName;
    private int mark = 0;
    private String retMsg = null;
    private List<Map<String, String>> outData = new LinkedList<>();
    private List<Map<String, String>> inData = new LinkedList<>();
    private int index = 0;

    @Override
    public String getFunctionName()
    {
        return functionName;
    }

    public int getMark()
    {
        return mark;
    }

    @Override
    public String getBackMessage()
    {
        return retMsg;
    }

    public void setMark(int mark)
    {
        this.mark = mark;
    }

    public String getRetMsg()
    {
        return retMsg;
    }

    public void setRetMsg(String retMsg)
    {
        this.retMsg = retMsg;
    }


    public List<Map<String, Object>> getOutData()
    {
        List<Map<String,Object>> list = new LinkedList<>();
        for(int i = 0;i<outData.size();i++)
        {
            Map<String,String> map = outData.get(i);
            Map<String,Object> map1 = new HashMap();
            for(String string : map.keySet())
                map1.put(string,map.get(string));
            list.add(map1);
        }
        return list;
    }

    public void setOutData(List<Map<String, String>> outData)
    {
        this.outData = outData;
    }


    public void setFunctionName(String functionName)
    {
        this.functionName = functionName;
    }

    public List<Map<String, String>> getInData()
    {
        return inData;
    }

    public void setInData(List<Map<String, String>> inData)
    {
        this.inData = inData;
    }

    // -------------------------------------------------------------------------------
    // outData 操作方法
    public Map<String, String> getOutMaps(int index)
    {
        Map<String, String> map = new HashMap<>();
        for (String key : outData.get(index - 1).keySet())
            map.put(key, outData.get(index - 1).get(key).toString());
        return map;
    }

    public String getOutElement(int index, int index2)
    {
        // Map是从0开始。。。统一下角标，index 从1开始。index2也从1开始
        if(index > outData.size())
            return null;
        return outData.get(index - 1).get(String.valueOf(index2 - 1)).toString();
    }

    // -------------------------------------------------------------------------------
    // inData 操作方法，只为了偷个懒。。 表-》就是一个Map
    // 添加一个inData 表
    public void addIndataMap(Map<String, String> map)
    {
        if (inData == null)
            inData = new LinkedList<>();
        inData.add(map);
    }

    // 删除最后一个表
    public void removeLastMapIndata()
    {
        if (inData == null || inData.isEmpty())
            return;
        else
            inData.remove(inData.size());
    }

    // 向当前表加入一个元素
    public void addElement(String key, String value)
    {
        if (inData == null || inData.size() == 0 || inData.size() - index == 0)
        {
            Map<String, String> temp = new HashMap<>();
            temp.put(key, value);
            inData.add(temp);
        }
        else
            inData.get(index).put(key, value);
    }

    public void addElement(int key, String value)
    {
        // 重载一下，偷懒。。。
        if (value == null)
            value = "NULL";
        addElement(String.valueOf(key - 1), value);
    }

    public void addElement(int key, Object value)
    {
        addElement(key, value.toString());
    }

    // 移除当前表的某个元素
    public void removeElement(String key)
    {
        Map map = inData.get(index);
        map.remove(key);
    }

    // 移动当前表
    public void addIndex()
    {
        // 只允许超过当前表数量一个
        if (index <= inData.size())
            index++;
    }

    public void subIndex()
    {
        // 判断下角标
        if (index != 0)
            index--;
        else
            index = 0;
    }

    public void doFunction()
    {
        Connect.sendMessage(this);
    }


    // 格式化日期
    public static String formatDate(Date date)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        return format.format(date);
    }

    public static String formatTime(Date date)
    {
        SimpleDateFormat format = new SimpleDateFormat("HHmm");
        return format.format(date);
    }
}
