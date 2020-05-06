package protocol;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Protocol
{

    static String regex1 = "<(.*?)>";
    static String regex2 = "\\|";
    static String regex3 = ":";

    static final int number = 50;
    /*
     * 0代表测试通道
     * 1代表登录成功
     * 2代表登录失败
     * 3代表参数无效
     * */

    // <方法名><参数一类型:参数一值|参数二类型:参数二值|...>
    // <结果集><第一行><第二行>...  <第一列类型:第一列值|第二列类型:第二列值>


    static void analysis(int size, String data)
    {
        if (size < number)
            return;
        else
        {
            size = size - number;
            List<Object> res = pro(data);
            System.out.println();
            String name = (String) res.get(0);
            Object[] objects = res.toArray();
            Object object = growArray(objects, objects.length);
            try
            {
                Class<?> c = Class.forName("DataBase.db");
                Method method = c.getMethod(name, Object.class);
                method.invoke(null, object);
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
                System.out.println("No found Class");
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
                System.out.println("No found Method");
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
    }

    // <结果集><第一行><第二行>...  <第一列类型:第一列值|第二列类型:第二列值>
    public static String transform(List<Object> result)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<ResutList>");
        for (int i = 0; i < result.size(); i++)
        {
            stringBuilder.append("<");
            List<Object> temp = (List<Object>) result.get(i);
            for (int j = 0; j < temp.size(); j++)
            {
                Object object = temp.get(j);
                if (object instanceof String)
                {
                    stringBuilder.append("String:");
                    stringBuilder.append((String) object);
                }
                else if (object instanceof Integer)
                {

                    stringBuilder.append("int:");
                    stringBuilder.append((int) object);

                }
                else if (object instanceof Float)
                {
                    stringBuilder.append("folat:");
                    stringBuilder.append((float) object);
                }
                else if (object instanceof Double)
                {
                    stringBuilder.append("double:");
                    stringBuilder.append(object);
                }
                if (j != temp.size() - 1)
                    stringBuilder.append("|");
            }
            stringBuilder.append(">");
        }
        return stringBuilder.toString();
    }

    public static List<Object> pro(String data)
    {
        List<Object> obj = new ArrayList<>();
        List<Object> line = new ArrayList<>();
        Pattern p = Pattern.compile(regex1);
        Matcher m = p.matcher(data);
        int n = 0;
        while (m.find())
        {
            // 一个个遍历
            // System.out.print(m.group(1) + "\t");
            String temp = m.group(1);

            n++;
            if (n == 1)
            {
                if (temp.split(regex2).length == 1)
                {
                    line.add(temp);
                    continue;
                }
            }
            obj.clear();
            String[] two = temp.split(regex2);
            // 对第二个开始进行第二次遍历
            for (String s : two)
            {

                // 第三次遍历
                String[] three = s.split(regex3);
                if (three.length == 2)
                {
                    switch (three[0])
                    {
                        case "int":
                            int i = Integer.parseInt(three[1]);
                            obj.add(i);
                            break;
                        case "float":
                            float f = Float.parseFloat(three[1]);
                            obj.add(f);
                            break;
                        case "double":
                            double d = Double.parseDouble(three[1]);
                            obj.add(d);
                            break;
                        case "String":
                            String str = three[1];
                            obj.add(str);
                            break;
                        case "":
                            break;
                    }
                }
            }
            line.add(obj.toArray());
        }
        return line;
    }

    public static List<Object> convertList(ResultSet rs) throws SQLException
    {
        List list = new ArrayList();
        ResultSetMetaData md = rs.getMetaData();//获取键名
        int columnCount = md.getColumnCount();//获取列的数量
        while (rs.next())
        {
            List<Object> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++)
            {
                row.add(rs.getObject(i));
            }
            list.add(row);
        }
        return list;
    }

    public static Object growArray(Object array, int size)
    {
        Class type = array.getClass().getComponentType();
        Object grown = Array.newInstance(type, size);
        System.arraycopy(array, 0, grown, 0, Math.min(Array.getLength(array), size));
        return grown;
    }

    public static void main(String[] a)
    {
        //pro("<map><String:aoe|int:5|double:3.1415926><float:3.14|String:test>");
        //analysis(60, "<test><int:1|String:123456>");
        List<Object> r = pro("<int:1|String:123456><double:3.1415926|float:3.1415>");
        transform(r);
    }

}
