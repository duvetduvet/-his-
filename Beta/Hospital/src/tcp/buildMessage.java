package tcp;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import proto.Data;
import proto.Func;
import proto.myMessage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class buildMessage {
    DecimalFormat format = new DecimalFormat();
    public static myMessage.request Login(int id, String password) {
        myMessage.request.Builder builder = myMessage.request.newBuilder();
        builder.setFunctionName("Login");

        Func.login.Builder builder1 = Func.login.newBuilder();
        builder1.setId(id);
        builder1.setPassword(password);

        builder.setDetails(Any.pack(builder1.build()));
        return builder.build();
    }

    public static myMessage.request getSeting(String view) {
        myMessage.request.Builder builder = myMessage.request.newBuilder();
        builder.setFunctionName("getSeting");

        Data.common.Builder data = Data.common.newBuilder();
        Map strMap = data.getStrMap();
        data.putStr("view", view);
        builder.setDetails(Any.pack(data.build()));
        return builder.build();
    }

    public static myMessage.request getDoctor() {
        myMessage.request.Builder builder = myMessage.request.newBuilder();
        builder.setFunctionName("getDoctor");
        return builder.build();
    }

    public static myMessage.request getDepartment() {
        myMessage.request.Builder builder = myMessage.request.newBuilder();
        builder.setFunctionName("getDepartment");
        return builder.build();
    }


    public static myMessage.request doFunction(String functionName, Map<String, Object> map) {
        myMessage.request.Builder builder = myMessage.request.newBuilder();
        builder.setFunctionName(functionName);
        if (map != null)
            builder.setDetails(mapToAny(map));
        return builder.build();
    }


    public static Any mapToAny(Map<String, Object> map) {
        Data.common.Builder common = Data.common.newBuilder();
        for (String key : map.keySet()) {
            Object object = map.get(key);
            if (object == null)
                common.putStr(key, "");
            else if (object instanceof String)
                common.putStr(key, (String) object);
            else if (object instanceof Double)
                common.putDou(key, (Double) object);
            else if (object instanceof Float)
                common.putFlo(key, (Float) object);
            else if (object instanceof Integer)
                common.putInt(key, (Integer) object);
            else if (object instanceof Date) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = formatter.format((Date) object);
                common.putStr(key, date);
            } else
                common.putStr(key, object.toString());
        }
        return Any.pack(common.build());
    }


    public static Map<String, Object> AnyToMap(Any any) {
        try {
            Data.common common = any.unpack(Data.common.class);
            Map<String, Object> map = new HashMap<>();
            map.putAll(common.getIntMap());
            map.putAll(common.getStrMap());
            map.putAll(common.getDouMap());
            map.putAll(common.getFloMap());
            return map;
        } catch (InvalidProtocolBufferException e) {
            System.out.println("any transform faild");
            return null;
        }

    }

}
