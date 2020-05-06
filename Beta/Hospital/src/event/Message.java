package event;

import java.util.List;
import java.util.Map;

public interface Message
{
    String getFunctionName();
    int getMark();
    String getBackMessage();

    List<Map<String,Object>> getOutData();
}
