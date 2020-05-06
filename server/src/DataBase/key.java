package DataBase;

import user.userStatement;

import java.util.*;

public class key
{
    static String[][] powerFunction;

    static
    {


        // 基础函数权限
        String[] p1 = {
                "getDepartmentIdFromName",
                "getUnitByName",
                "getDrNameByDepartname",
                "getMedicineIdByName",
                "getDoctorIdByName",
                "getTable",
                "getAllByFactoryOrFeeType",
                "selectVipByPhoneOrId",
                "getNameByPatientId",
                "getPreInfoByPreId",
                "getRegiseterDetailById",
                "getDrNameByDepartname",
                "getDoctorByDepart"
        };

        // 挂号权限
        String[] p2 = {
                "getTodayRegister",
                "insertRegister",
                "getDrNameByDepartname",
                "getPatientInfo",
                "getRegiseterDetailById",
                "addPatient",
                "getPatientBySpell",
                "editRegister",              // 编辑挂号
                "selectPatientByPhoneOrId",
                "insuranceRegister",         // 医保挂号
                "getSelectRegister"

        };

        // 划价权限
        String[] p3 = {
                "addPrescriptions",
                "getMedicineBySpell",
                "getRegiseterDetailById",
                "getIdByPreState",
                "getHistory",
                "getPreInfoByPreId"

        };

        // 收费
        String[] p4 = {
                "Account",
                "getIdByChargestate",
                "getRegiseterDetailById",
                "getPreList",
                "getInsuranceHistory",
                "insuranceAccount"      //医保收费后插入到数据库收费详情

        };

        // 发药权限
        String[] p5 = {
                "accountMedicine",
                "getNoMed",
                "getIdByMedstate",
                "getPrescription",
                "getPreList"
        };

        // 药品权限
        String[] p6 = {
                "addMedicine",
                "updateMedicine",
                "deleteMedicine",
                "getDiagnosis",
                "getMedicine",
                "getWarnMedicine",
                "addMedicineByExcel",
                "getPreRecordByCondition",
                "getStockRecordByCondition"
        };

        //药库权限
        String[] p7 = {
                "maxIdFromStockrecord",
                "MedicineEnterAndOut",
                "resetStock",
                "getMedicineBySpell",
                "getWarnMedicine",
                "getMedicineBySpells",
                "getPreRecordByCondition",
                "getStockRecordByCondition"
                // 损益记录查询
        };

        // 病人相关权限
        String[] p8 = {
                "getPatientInfo",
                "addPatient",
                "getAllPatient",
                "getOnesPres",
                "patientVisits"
        };

        // 会员卡权限
        String[] p9 = {
                "getVip",
                "deleteVip",
                "updateVip",
                "payForVip",
                "addVip",
                "lostVip",
                "cancelLostVip",
                "payRecord",
                "consumeRecord",
                "vipRecord",
                "selectVipByPhoneOrId",
                "updateVip",
                "remakeCard"
        };

        // 会员卡等级权限
        String[] p10 = {
                "insertIntegrate",
                "updateIntegrate",
                "deleteIntegrate"
        };

        // 设置权限
        String[] p11 = {
                "addSetting",
                "updateSetting",
                "deleteSetting"
        };

        // 医生类
        String[] p12 = {
                "getDoctor",
                "getDoctorByDepart",
                "insertDoctor",
                "updateDoctor",
                "deleteDoctor"
        };

        // 业务查询
        String[] p13 = {
                "getStockRecordByCondition",
                "getMedSoldByMed",
                "getMedSold",
                "getMedSoldByTime",
                "getCureSold",
                "getCureSoldByCure",
                "getCureSoldByTime",
                "getPreRecordByCondition",
                "getCountByDepart",
                "getCountByDoctor",
                "getProfitDate",
                "getProfitByDate",
                "getDatePres",
                "getRechargeCounts",
                "getOnesPres"
        };

        // 处方作废
        String[] p14 = {
                "resetUsefulByPreId",
                "getPatientByDate",
                "getPreListOrder",
                "getPreInfoByPreId"
        };

        // 账号设置
        String[] p15 = {
                "getPosition",
                "changeMessage",
                "getOnePosition",
                "addUser",
                "deleteUser"
        };

        powerFunction = new String[15][];
        powerFunction[0] = p1;
        powerFunction[1] = p2;
        powerFunction[2] = p3;
        powerFunction[3] = p4;
        powerFunction[4] = p5;
        powerFunction[5] = p6;
        powerFunction[6] = p7;
        powerFunction[7] = p8;
        powerFunction[8] = p9;
        powerFunction[9] = p10;
        powerFunction[10] = p11;
        powerFunction[11] = p12;
        powerFunction[12] = p13;
        powerFunction[13] = p14;
        powerFunction[14] = p15;
    }

    private static List<String> buildList(String[] strings)
    {
        List<String> list = new LinkedList<>();
        for (String temp : strings)
            list.add(temp);
        return list;
    }

    public static boolean getFuctionList(userStatement userStatement)
    {
        String power = userStatement.getPower();
        if (power == null || power.equals(""))
            return false;
        char[] p = power.toCharArray();
        if (power.length() == powerFunction.length)
        {
            List<String> list = new LinkedList<>();
            for (int i = 0; i < p.length; i++)
            {
                if (p[i] == '1')
                    list.addAll(buildList(powerFunction[i]));
            }
            list = removeStringListDupli(list);
            userStatement.setList(list);
            return true;
        }
        else
            return false;
    }

    // 权限去重
    public static List<String> removeStringListDupli(List<String> stringList)
    {
        Set<String> set = new LinkedHashSet<>();
        set.addAll(stringList);

        stringList.clear();

        stringList.addAll(set);
        return stringList;
    }

    public static void main(String[] a)
    {
        userStatement userStatement = new userStatement();
        userStatement.setPower("100000000000000");
        getFuctionList(userStatement);
        System.out.println(userStatement);
    }

}
