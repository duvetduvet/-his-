package tool;

import com.google.protobuf.Any;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class PreManage
{
    Map<Integer, Pre> preMap = new HashMap<>();


    // 设置处方详情
    public int setMedcineList(List<Any> anyList)
    {
        // 带入的anyList 必须为 hospital.prescription_view; 表的格式
        Map<Integer, List<medicine>> medMap = new HashMap<>();
        for (Any any : anyList)
        {
            Map map = tcp.buildMessage.AnyToMap(any);
            int prescriptionid = (int) map.get("prescriptionid");
            if (medMap.get(prescriptionid) == null)
            {
                medMap.put(prescriptionid, new LinkedList<>());
            }
            List<medicine> medicineList = medMap.get(prescriptionid);

            medicine med = new medicine();
            med.name = map.get("name").toString();
            med.number = Float.parseFloat(map.get("number").toString());
            med.outprice = Float.parseFloat(map.get("outprice").toString());
            med.unitname = map.get("unitname").toString();
            med.feetypename = map.get("feetypename").toString();
            medicineList.add(med);
        }

        for (Integer pid : medMap.keySet())
        {
            List<medicine> medicineList = medMap.get(pid);
            if (preMap.get(pid) == null)
                continue;
            Pre pre = preMap.get(pid);
            pre.clearMedcineList();
            pre.setMedcineList(medicineList);
        }
        return medMap.size();
    }

    // 返回一个处方对象
    public Pre getPre(int pid)
    {
        return preMap.get(pid);
    }

    // 添加一个处方对象
    public void addPre(Pre pre)
    {
        preMap.put(pre.getPid(), pre);
    }


    // 添加处方列表
    public int addPreList(List<Any> anyList)
    {
        for (Any temp : anyList)
        {
            Map map = tcp.buildMessage.AnyToMap(temp);
            int prescriptionid = (int) map.get("pid");
            String patientname = map.get("name").toString();
            String departname = map.get("departname").toString();
            String doctorname = map.get("doctorname").toString();
            int age = Integer.parseInt(map.get("age").toString());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = null;
            try
            {
                date = simpleDateFormat.parse(map.get("regdate").toString());
            }
            catch (ParseException e)
            {
                System.out.println("日期转化错误");
            }
            int state = (int) map.get("state");

            BigDecimal medfee = new BigDecimal(map.get("medfee").toString());
            BigDecimal curefee = new BigDecimal(map.get("curefee").toString());

            if(state != 1)
            {
                BigDecimal medis = new BigDecimal(map.get("medicinediscount").toString()).setScale(2,BigDecimal.ROUND_FLOOR);
                BigDecimal mcdis = new BigDecimal(map.get("medcurediscount").toString()).setScale(2,BigDecimal.ROUND_FLOOR);
                medfee = medfee.multiply(medis).setScale(2,BigDecimal.ROUND_FLOOR);
                curefee = curefee.multiply(mcdis).setScale(2,BigDecimal.ROUND_FLOOR);
            }
            BigDecimal mon = BigDecimal.ZERO;
            if(!map.get("sum").equals(""))
                mon = new BigDecimal(map.get("sum").toString());
            int patientid = (int) map.get("patientid");
            String sex = map.get("sex").toString();
            int dose = (int) map.get("dose");
            Integer rgid = Integer.valueOf(map.get("rgid").toString());
            String illness = map.get("illness").toString();
            String discription = map.get("discription").toString();

            Pre p = new Pre(prescriptionid, rgid, patientname, sex, age, doctorname, departname, dose, mon, state, date,illness,discription);
            this.addPre(p);
        }
        return anyList.size();
    }

    public void clear()
    {
        preMap.clear();
    }
}
