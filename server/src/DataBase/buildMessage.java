package DataBase;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import proto.Data;
import proto.myMessage;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class buildMessage
{

    public static myMessage.feedback getResult(String functionName, ResultSet resultSet) throws SQLException
    {

        ResultSetMetaData rsm = resultSet.getMetaData(); //获得列集
        int col = rsm.getColumnCount();   //获得列的个数
        String colName[] = new String[col];
        //取结果集中的表头名称, 放在colName数组中
        for (int i = 0; i < col; i++)
            colName[i] = rsm.getColumnName(i + 1);

        resultSet.last();
        int row = resultSet.getRow();

        myMessage.feedback.Builder builder = myMessage.feedback.newBuilder();
        builder.setFunctionName(functionName);
        builder.setMark(row);

        resultSet.beforeFirst();
        while (resultSet.next())
        {

            Data.common.Builder common = Data.common.newBuilder();
            for (int i = 0; i < col; i++)
            {
                Object object = resultSet.getObject(i + 1);
                if (object == null)
                    common.putStr(colName[i], "");
                else if (object instanceof String)
                    common.putStr(colName[i], (String) object);
                else if (object instanceof Integer)
                    common.putInt(colName[i], (Integer) object);
                else if (object instanceof Double)
                    common.putDou(colName[i], (Double) object);
                else if (object instanceof Float)
                    common.putFlo(colName[i], (Float) object);
                else if (object instanceof Date)
                {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String date = formatter.format((Date) object);
                    common.putStr(colName[i], date);
                }
                else
                    common.putStr(colName[i], object.toString());


            }
            builder.addDetails(Any.pack(common.build()));
        }
        return builder.build();
    }


    public static myMessage.feedback getFeedback(String functionName, int mark, String backMessage)
    {
        myMessage.feedback.Builder builder = myMessage.feedback.newBuilder();
        builder.setFunctionName(functionName);
        builder.setMark(mark);
        builder.setBackMessage(backMessage);
        return builder.build();
    }

    public static myMessage.feedback getResult(String functionName, Map<String, String>[] outData, int mark, String backMessage)
    {
        myMessage.feedback.Builder builder = myMessage.feedback.newBuilder();
        builder.setBackMessage(backMessage);
        builder.setMark(mark);
        for (Map map : outData)
        {
            Data.common.Builder common = Data.common.newBuilder();
            common.putAllStr(map);
            builder.addDetails(Any.pack(common.build()));
        }
        return builder.build();
    }

    public static Map<String, Object> AnyToMap(Any any)
    {
        try
        {
            Data.common common = any.unpack(Data.common.class);
            Map<String, Object> map = new HashMap<>();
            map.putAll(common.getIntMap());
            map.putAll(common.getStrMap());
            map.putAll(common.getDouMap());
            map.putAll(common.getFloMap());
            return map;
        }
        catch (InvalidProtocolBufferException e)
        {
            System.out.println("any transform faild");
            return null;
        }

    }

    public static Any mapToAny(Map<String, Object> map)
    {
        Data.common.Builder common = Data.common.newBuilder();
        for (String key : map.keySet())
        {
            Object object = map.get(key);
            if (object instanceof String)
                common.putStr(key, (String) object);
            else if (object instanceof Double)
                common.putDou(key, (Double) object);
            else if (object instanceof Float)
                common.putFlo(key, (Float) object);
            else if (object instanceof Integer)
                common.putInt(key, (Integer) object);
            else if (object instanceof Date)
            {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = formatter.format((Date) object);
                common.putStr(key, date);
            }
            else
                common.putStr(key, object.toString());
        }
        return Any.pack(common.build());
    }

}
