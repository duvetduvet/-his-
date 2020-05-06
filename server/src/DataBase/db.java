package DataBase;

import com.google.protobuf.Any;
import nio.ThreadHandlerChannel;
import proto.Data;

import proto.myMessage;

import user.userStatement;

import java.math.BigDecimal;
import java.nio.channels.SelectionKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.SimpleDateFormat;
import java.time.Period;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("ALL")
public class db {

    // ------------- 危险函数 ----------------------------------------------------

    // 得到设置类表格
    public static void getTable(SelectionKey selectionKey, Any any) {
        Map map = buildMessage.AnyToMap(any);
        String view = (String) map.get("table");
        try {
            if (!judgeView(view)) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback((String) map.get("functionName"), -2, "表格权限不够"));
                return;
            }
            Connection connection = C3P0ConnentionProvider.getConnection();
            String sql = "select * from " + view + ";";
            PreparedStatement preapred = connection.prepareStatement(sql);

            try {
                preapred.executeQuery();
                ResultSet resultSet = preapred.getResultSet();
                myMessage.feedback feedback = buildMessage.getResult((String) map.get("functionName"), resultSet);
                ThreadHandlerChannel.sendMessage(selectionKey, feedback);
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback((String) map.get("functionName"), -1, "fail"));
                System.out.println("获取数据库链接失败");
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback((String) map.get("functionName"), -1, "fail"));
            System.out.println("获取数据库链接失败");
        }
    }

    // 得到部分表格
    public static void getPartTable(SelectionKey selectionKey, Any any) {
        Map<String, Object> map = buildMessage.AnyToMap(any);
        String table = (String) map.get("table");
        map.remove("table");
        String functionName = (String) map.get("functionName");
        map.remove("functionName");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("select ");
        int i = 0;
        for (String key : map.keySet()) {
            if (i == map.size() - 1)
                stringBuffer.append(map.get(key));
            else
                stringBuffer.append(map.get(key) + ",");
        }
        stringBuffer.append(" from" + table);

        String sql = stringBuffer.toString();
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult(functionName, resultSet));
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback(functionName, 0, "查询失败"));
            e.printStackTrace();
        }

    }

    // ------------- 基础函数 && 查询函数 ----------------------------------------------------

    // 返回getProfit
    public static void getProfit(SelectionKey selectionKey) {
        try {

            Connection connection = C3P0ConnentionProvider.getConnection();
            String sql = "SELECT * FROM profit_view;";
            PreparedStatement preapred = connection.prepareStatement(sql);

            try {
                preapred.executeQuery();
                ResultSet resultSet = preapred.getResultSet();
                myMessage.feedback feedback = buildMessage.getResult("getProfit", resultSet);
                ThreadHandlerChannel.sendMessage(selectionKey, feedback);
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getProfit", -1, "fail"));
                System.out.println("获取数据库链接失败");
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getProfit", -1, "fail"));
            System.out.println("获取数据库链接失败");
        }
    }


    // 返回医生信息 *
    public static void getDoctor(SelectionKey selectionKey) {
        String sql = "SELECT * FROM doctor_view;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                myMessage.feedback feedback = buildMessage.getResult("getDoctor", resultSet);
                ThreadHandlerChannel.sendMessage(selectionKey, feedback);
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDoctor", -1, "查询失败"));
                e.printStackTrace();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDoctor", -1, "查询失败"));
            e.printStackTrace();
        }
    }

    // 通过科室名字得到id
    public static void getDepartmentIdFromName(SelectionKey selectionKey, Any any) {
        String sql = "SELECT id FROM department WHERE name = ?";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("name"));
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getDepartmentIdFromName", resultSet));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDepartmentIdFromName", -1, "查询失败"));
                e.printStackTrace();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDepartmentIdFromName", -1, "查询失败"));
            e.printStackTrace();
        }

    }

    // 返回所有部门信息
    public static void getDepartment(SelectionKey selectionKey) {
        String sql = "SELECT * FROM department;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.executeQuery();
            try {
                ResultSet resultSet = preparedStatement.getResultSet();
                myMessage.feedback feedback = buildMessage.getResult("getDepartment", resultSet);
                ThreadHandlerChannel.sendMessage(selectionKey, feedback);
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDepartment", -1, "查询出错"));
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDepartment", -1, "获取数据库连接失败"));
            System.out.println("获取数据库连接失败");
        }
    }

    // 得到今天的挂号人 *
    public static void getTodayRegister(SelectionKey selectionKey) {
        String sql = "SELECT * FROM all_register_view  where date > ?  order by id desc;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date zero = calendar.getTime();
                preparedStatement.setObject(1, zero);
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getTodayRegister", resultSet));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getTodayRegister", -1, "查询失败"));
                e.printStackTrace();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getTodayRegister", -1, "查询失败"));
            e.printStackTrace();
        }
    }


    // 得到今天的挂号人 *
    public static void getSelectRegister(SelectionKey selectionKey , Any any) {
        String sql = "SELECT * FROM all_register_view  where date > ? and date < ?  order by id desc;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Map map = buildMessage.AnyToMap(any);

                preparedStatement.setObject(1, map.get("startDate") + " 00:00:00");
                preparedStatement.setObject(2,map.get("endDate") +  " 23:59:59");
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getSelectRegister", resultSet));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getSelectRegister", -1, "查询失败"));
                e.printStackTrace();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getSelectRegister", -1, "查询失败"));
            e.printStackTrace();
        }
    }


    // 返回所有挂号类型 *
    public static void getRegType(SelectionKey selectionKey) {
        String sql = "SELECT * FROM regType;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getRegType", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getRegType", -1, "获取失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getRegType", -1, "获取失败"));
            e.printStackTrace();
        }
    }

    // 得到该科室的所有医生名
    public static void getDoctorByDepart(SelectionKey selectionKey, Any any) {
        String sql = "SELECT drname FROM doctor_view WHERE departname =? and useful =1 ;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("departname"));
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getDoctorByDepart", resultSet));
            } catch (SQLException e) {

                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDoctorByDepart", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDoctorByDepart", 0, "获取数据库连接失败"));
            e.printStackTrace();
        }
    }

    // 通过收费名称查询id
    public static void getFeeIdByName(SelectionKey selectionKey, Any any) {
        String sql = "SELECT id FROM unit WHERE name =?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("name"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getFeeIdByName", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getFeeIdFromName", 0, "查询失败！！"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getFeeIdFromName", 0, "查询失败！！"));
            e.printStackTrace();
        }

    }

    // 通过名称查询单位id
    public static void getUnitByName(SelectionKey selectionKey, Any any) {
        String sql = "SELECT id FROM unit WHERE name = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();

            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("name"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getUnitByName", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getUnitByName", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getUnitByName", -1, "fail"));
            e.printStackTrace();
        }

    }

    // 通过id 返回详细挂号信息 *
    public static void getRegiseterDetailById(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM all_register_view WHERE id =?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                Map map = buildMessage.AnyToMap(any);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getRegiseterDetailById", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getRegiseterDetailById", 0, "查询失败！"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getRegiseterDetailById", 0, "查询失败！"));
            e.printStackTrace();
        }

    }


    // 返回诊疗费所有信息 *
    public static void getDiagnosis(SelectionKey selectionKey) {
        String sql = "SELECT itemCode,id, name, unitname, outprice, feetypename, spell,useful,isInclude,itemGrade FROM medcure_view WHERE type = '诊疗费';";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getDiagnosis", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDiagnosis", 0, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDiagnosis", 0, "查询失败"));
            e.printStackTrace();
        }
    }

    // 得到所有药品 *
    public static void getMedicine(SelectionKey selectionKey) {
        String sql = "SELECT * FROM medicine_view;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getMedicine", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicine", 0, "查询所有信息失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicine", 0, "查询所有信息失败"));
            e.printStackTrace();
        }
    }

    // 获取挂号病人信息 *
    public static void getPatientInfo(SelectionKey selectionKey, Any any) {
        String sql = "SELECT id,name,sex,birth, identity,phone FROM patient WHERE id = ? or name like ?";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.setObject(2, map.get("name"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPatientInfo", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPatientInfo", -1, "获取失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPatientInfo", -1, "获取失败"));
            e.printStackTrace();
        }
    }

    // 获取某部门下所有医生名 *
    public static void getDrNameByDepartname(SelectionKey selectionKey, Any any) {
        String sql = "SELECT drname,departname FROM doctor_view WHERE departname = ? and useful = 1 ";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                Map map = buildMessage.AnyToMap(any);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("departname"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getDrNameByDepartname", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDrNameByDepartname", -1, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDrNameByDepartname", -1, "查询失败"));
            e.printStackTrace();
        }
    }

    // 获取处方详情 *
    public static void getPrescription(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM prescription_view where ";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                Map map = buildMessage.AnyToMap(any);
                boolean mark = true;
                for (Object pid : map.keySet()) {
                    if (mark) {
                        sql = sql + " prescriptionid = " + pid.toString();
                        mark = false;
                    } else
                        sql = sql + " or prescriptionid = " + pid.toString();

                }

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPrescription", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPrescription", -1, "查询错误"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPrescription", -1, "查询错误"));
            e.printStackTrace();
        }

    }

    // 获取已缴费没有发药id
    public static void getNoMed(SelectionKey selectionKey) {
        String sql = "SELECT id FROM register_view WHERE prestate = 1 AND chargestate = 1 AND medstate = 0;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getNoMed", resultSet));
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getNoMed", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getNoMed", 0, "查询失败"));
            e.printStackTrace();
        }

    }

    // 获得 药品id 通过名字
    public static void getMedicineIdByName(SelectionKey selectionKey, Any any) {
        String sql = "SELECT id,unitname,inprice,name FROM medcure_view WHERE name = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("name"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getMedicineIdByName", resultSet));

            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicineIdByName", 0, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicineIdByName", 0, "查询失败"));
            e.printStackTrace();
        }

    }

    // 得到可以发药的id
    public static void getIdByMedstate(SelectionKey selectionKey) {
        String sql = "SELECT rgid,name,patientid,state FROM pre_order_view where  date >= ? and state >= 2 group by rgid,state order by rgid DESC ;";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, -3);
        Date zero = calendar.getTime();
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, zero);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getIdByMedstate", resultSet));

            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getIdByMedstate", -1, "select fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getIdByMedstate", -1, "select fail"));
            e.printStackTrace();
        }

    }

    // 得到可以划价的id
    public static void getIdByPreState(SelectionKey selectionKey) {

        String sql = "SELECT id,patientid,patientname,sex,birth FROM all_register_view where  date > ? and useful =1 order by id desc;";

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date zero = calendar.getTime();

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, zero);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getIdByPreState", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getIdByPreState", 0, "select fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getIdByPreState", 0, "select fail"));
            e.printStackTrace();
        }

    }

    // 得到可以收费的id
    public static void getIdByChargestate(SelectionKey selectionKey) {
        String sql = "SELECT rgid id,name patientname,patientid,doctorname,fee FROM pre_order_view WHERE state = 1 and  regdate > ?  group by rgid order by rgid desc;";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, -3);
        Date zero = calendar.getTime();
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.setObject(1, zero);
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getIdByChargestate", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getIdByChargestate", -1, "select fail"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getIdByChargestate", -1, "select fail"));
            e.printStackTrace();
        }
    }

    // 通过拼音码获取药品集合
    public static void getMedicineBySpell(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM medicine_view where spell like ? and useful =1 and name not like '凑整费';";
        Map map = buildMessage.AnyToMap(any);
        String spell = (String) map.get("spell");
        StringBuffer stringBuffer = new StringBuffer();
        char[] c = spell.toCharArray();
//        for (char temp : c)
//        {
//            stringBuffer.append("%");
//            stringBuffer.append(temp);
//        }
//        stringBuffer.append("%");
//        spell = stringBuffer.toString();
        spell = spell + "%";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, spell);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getMedicineBySpell", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicineBySpell", 0, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicineBySpell", 0, "查询失败"));
            e.printStackTrace();
        }
    }

    public static void getMedicineBySpells(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM medicine_view where spell like ? and useful =1 and name not like '凑整费';";
        Map map = buildMessage.AnyToMap(any);
        String spell = (String) map.get("spell");
        StringBuffer stringBuffer = new StringBuffer();
        char[] c = spell.toCharArray();
        for (char temp : c) {
            stringBuffer.append("%");
            stringBuffer.append(temp);
        }
        stringBuffer.append("%");
        spell = stringBuffer.toString();
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, spell);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getMedicineBySpells", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicineBySpells", -1, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedicineBySpell", -1, "查询失败"));
            e.printStackTrace();
        }

    }

    // 通过名字获取病人信息
    public static void getPatientBySpell(SelectionKey selectionKey, Any any) {

        String sql = "SELECT * FROM patient where name like ?;";
        Map map = buildMessage.AnyToMap(any);
        String spell = (String) map.get("spell");
        StringBuffer stringBuffer = new StringBuffer();
        char[] c = spell.toCharArray();
        for (char temp : c) {
            stringBuffer.append("%");
            stringBuffer.append(temp);
        }
        stringBuffer.append("%");
        spell = stringBuffer.toString();
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, spell);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPatientBySpell", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPatientBySpell", -1, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPatientBySpell", -1, "查询失败"));
            e.printStackTrace();
        }

    }


    // 通过姓名获取医生id
    public static void getDoctorIdByName(SelectionKey selectionKey, Any any) {
        String sql = "SELECT id FROM doctor where name = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("name"));
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getDoctorIdByName", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDoctorIdByName", 0, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDoctorIdByName", 0, "查询失败"));
            e.printStackTrace();
        }
    }


    // 返回处方详情 *
    public static void getPreInfoByPreId(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM prescription_view where prescriptionid = ? and medid != 0;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("prescriptionid"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPreInfoByPreId", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreInfoByPreId", -1, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreInfoByPreId", -1, "查询失败"));
            e.printStackTrace();
        }
    }


    // 返回处方列表
    public static void getPreList(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM pre_order_view where rgid = ?;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("rgid"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPreList", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreList", -1, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreList", -1, "查询失败"));
            e.printStackTrace();
        }
    }


    // 返回处方列表 -- 通过订单编号来查询
    public static void getPreListOrder(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM pre_order_view where orderid = ? and state >= 2;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("orderid"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPreListOrder", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreListOrder", -1, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreListOrder", -1, "查询失败"));
            e.printStackTrace();
        }
    }


    // getPatientByDate 得到某天可以作废挂号的信息
    public static void getPatientByDate(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM pre_order_view WHERE (`date` between ? and ?) and `state` >= 2 group by `orderid`;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                Map map = buildMessage.AnyToMap(any);

                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append((String) map.get("date"));
                stringBuffer.append(" 00:00:00");

                StringBuffer stringBuffer2 = new StringBuffer();
                stringBuffer2.append((String) map.get("date"));
                stringBuffer2.append(" 23:59:59");

                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, stringBuffer.toString());
                preparedStatement.setString(2, stringBuffer2.toString());
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPatientByDate", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPatientByDate", -1, "sql执行错误"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPatientByDate", -1, "数据库连接错误"));
            e.printStackTrace();
        }

    }

    // 返回低于警告库存的药品信息
    public static void getWarnMedicine(SelectionKey selectionKey) {
        String sql = "SELECT * FROM medicine where stock<warn;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getWarnMedicine", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getWarnMedicine", 0, "执行失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getWarnMedicine", 0, "执行失败"));
            e.printStackTrace();
        }

    }

    // 通过工厂名和收费类型筛选药品
    public static void getAllByFactoryOrFeeType(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM profit_view WHERE factoryname LIKE ? AND feetypename LIKE ? and name not like '凑整费' ORDER BY id;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("factoryname"));
            preparedStatement.setObject(2, map.get("feetypename"));
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getAllByFactoryOrFeeType", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getAllByFactoryOrFeeType", 0, "select fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getAllByFactoryOrFeeType", 0, "select fail"));
            e.printStackTrace();
        }

    }

    // 得到所有药品销售详情
    public static void getMedSold(SelectionKey selectionKey) {
        String sql = "SELECT name, unitname, inprice, outprice, number, suminprice, sumoutprice, income FROM medsoldrecord_view;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getMedSold", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedSold", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedSold", 0, "查询失败"));
            e.printStackTrace();
        }
    }

    // 通过名字得到药品详情
    public static void getMedSoldByMed(SelectionKey selectionKey) {
        String sql = "SELECT name, unitname, inprice, outprice, SUM(number), SUM(suminprice), SUM(sumoutprice), SUM(income) FROM medsoldrecord_view GROUP BY name;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getMedSoldByMed", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedSoldByMed", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedSoldByMed", 0, "查询失败"));
            e.printStackTrace();
        }
    }

    // 通过日期得到药品销售详情
    public static void getMedSoldByTime(SelectionKey selectionKey, Any any) {
        String sql = "SELECT name,unitname,inprice,outprice,sum(number) number, sum(suminprice) suminprice,sum(sumoutprice) sumoutprice,sum(income) income FROM medsoldrecord_view where date >= ? and date<=? group by name,inprice,outprice;";

        //SELECT name,unitname,inprice,outprice,sum(number) number, sum(suminprice) suminprice,sum(sumoutprice) sumoutprice,sum(income) income FROM hospital.medsoldrecord_view where date >= ? and date<=? group by name;
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("start"));
                preparedStatement.setObject(2, map.get("end"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getMedSoldByTime", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedSoldByTime", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getMedSoldByTime", 0, "查询失败"));
            e.printStackTrace();
        }

    }

    // 得到诊疗详情
    public static void getCureSold(SelectionKey selectionKey) {
        String sql = "SELECT name, unitname, outprice, number, sumoutprice FROM curesoldrecord_view;";
        //SELECT name,unitname,outprice,sum(number) number,sum(sumoutprice) sumoutprice,sum(income) income FROM hospital.medsoldrecord_view where date >= ? and date<=? group by name
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getCureSold", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCureSold", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCureSold", 0, "查询失败"));
            e.printStackTrace();
        }
    }

    // 通过诊疗名称返回所有信息
    public static void getCureSoldByCure(SelectionKey selectionKey) {
        String sql = "SELECT name, unitname, outprice, SUM(number), SUM(sumoutprice) FROM curesoldrecord_view GROUP BY name;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getCureSoldByCure", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCureSoldByCure", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCureSoldByCure", 0, "查询失败"));
            e.printStackTrace();
        }
    }

    // 通过诊疗时间回所有诊疗信息
    public static void getCureSoldByTime(SelectionKey selectionKey, Any any) {
        String sql = "SELECT name,unitname,outprice,sum(number) number,sum(sumoutprice) sumoutprice FROM curesoldrecord_view where date >= ? and date<=? group by name;";
        //SELECT name,unitname,outprice,sum(number) number,sum(sumoutprice) sumoutprice,sum(income) income FROM hospital.medsoldrecord_view where date >= ? and date<=? group by name;
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("start"));
                preparedStatement.setObject(2, map.get("end"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getCureSoldByTime", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCureSoldByTime", 0, "查询失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCureSoldByTime", 0, "查询失败"));
            e.printStackTrace();
        }
    }

    // getStockRecordByCondition得到库存信息
    public static void getStockRecordByCondition(SelectionKey selectionKey, Any any) {
        Map map = buildMessage.AnyToMap(any);
        String operate = (String) map.get("operate");
        String factory = (String) map.get("factory");
        String feetype = (String) map.get("feetype");
        String date = (String) map.get("date");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT * FROM stockrecord_view WHERE iscount = 0 and  ");
        int i = 0;
        if (operate != null && !operate.equals("")) {
            stringBuffer.append(operate);
        }
        if (factory != null && !factory.equals("")) {
            if (i++ != 0)
                stringBuffer.append(" and ");
            stringBuffer.append(" factoryname like '" + factory + "' ");
        }
        if (feetype != null && !feetype.equals("")) {
            if (i++ != 0)
                stringBuffer.append(" and ");
            stringBuffer.append(" feetypename like '" + feetype + "' ");
        }
        if (date != null && !date.equals("")) {
            if (i++ != 0)
                stringBuffer.append(" and ");
            stringBuffer.append(date);
        }

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(stringBuffer.toString());
            preparedStatement.executeQuery();
            ResultSet resultSet = preparedStatement.getResultSet();
            try {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getStockRecordByCondition", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getStockRecordByCondition", 0, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getStockRecordByCondition", 0, "查询失败"));
            e.printStackTrace();
        }

    }

    // 通过条件筛选处方记录表
    public static void getPreRecordByCondition(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM pre_order_view WHERE orderid is not null and departname LIKE ? AND doctorname LIKE ? and patientid like ? AND ";
        Map map = buildMessage.AnyToMap(any);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(sql);
        stringBuffer.append(map.get("date"));
        sql = stringBuffer.toString();
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("departname"));
            preparedStatement.setObject(2, map.get("doctorname"));
            preparedStatement.setObject(3, map.get("patientid"));

            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPreRecordByCondition", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreRecordByCondition", -1, "select fail"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPreRecordByCondition", -1, "get connection fail"));
            e.printStackTrace();
        }


    }

    // ------------ 功能函数 ----------------------------------------------------

    // 登录
    public static void Login(SelectionKey selectionKey, Any any) {
        String sql = "select name,number,power,id from position where number = ? and password = ?";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("id"));
            preparedStatement.setObject(2, map.get("password"));
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                resultSet.last();
                if (resultSet.getRow() == 1) {
                    userStatement userStatement = (userStatement) selectionKey.attachment();
                    userStatement.setId(resultSet.getInt("id"));
                    userStatement.setPower(resultSet.getString("power"));
                    key.getFuctionList(userStatement);
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("Login", resultSet));
                } else
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Login", -1, "登录失败"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Login", -1, "数据库连接失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Login", -1, "数据库连接失败"));
            e.printStackTrace();
        }


    }

    // 插入一个医生
    public static boolean insertDoctor(SelectionKey selectionKey, Any any) {
        try {
            // INSERT INTO `hospital`.`doctor` (`id`, `name`, `sex`, `birth`, `identity`, `phone`, `address`, `departmentId`) VALUES ('7', '123', 'gh', 'ghj', 'ghj', 'fgh', 'fgh', 'jh');
            Map map = buildMessage.AnyToMap(any);
            Connection connection = C3P0ConnentionProvider.getConnection();
            String sql = "INSERT INTO doctor (`id`, `name`, `sex`, `birth`, `identity`, `phone`, `address`, `departmentId`) VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement preapred = connection.prepareStatement(sql);
            preapred.setObject(1, map.get("id"));
            preapred.setObject(2, map.get("name"));
            preapred.setObject(3, map.get("sex"));
            preapred.setObject(4, map.get("birth"));
            preapred.setObject(5, map.get("identity"));
            preapred.setObject(6, map.get("phone"));
            preapred.setObject(7, map.get("address"));
            preapred.setObject(8, map.get("departmentId"));
            try {
                preapred.executeUpdate();
                myMessage.feedback feedback = buildMessage.getFeedback("insertDoctor", 1, "insert success!");
                ThreadHandlerChannel.sendMessage(selectionKey, feedback);
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insertDoctor", -1, "fail"));
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            myMessage.feedback feedback = buildMessage.getFeedback("insertDoctor", 0, "insert faild!");
            ThreadHandlerChannel.sendMessage(selectionKey, feedback);
        }
        return true;
    }

    // 更新医生信息
    public static void updateDoctor(SelectionKey selectionKey, Any any) {
        try {
            // INSERT INTO `hospital`.`doctor` (`id`, `name`, `sex`, `birth`, `identity`, `phone`, `address`, `departmentId`) VALUES ('7', '123', 'gh', 'ghj', 'ghj', 'fgh', 'fgh', 'jh');
            Map map = buildMessage.AnyToMap(any);
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                String sql = "UPDATE doctor SET name=?, sex=?, birth=?, identity=?, phone=?, address=?,departmentId=?,useful=? WHERE id=?;";
                PreparedStatement preapred = connection.prepareStatement(sql);
                preapred.setObject(1, map.get("name"));
                preapred.setObject(2, map.get("sex"));
                preapred.setObject(3, map.get("birth"));
                preapred.setObject(4, map.get("identity"));
                preapred.setObject(5, map.get("phone"));
                preapred.setObject(6, map.get("address"));
                preapred.setObject(7, map.get("departmentId"));
                preapred.setObject(9, map.get("id"));
                preapred.setObject(8, map.get("useful"));
                preapred.executeUpdate();
                myMessage.feedback feedback = buildMessage.getFeedback("updateDoctor", 1, "update success!");
                ThreadHandlerChannel.sendMessage(selectionKey, feedback);
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateDoctor", -1, "update faild!"));
                System.out.println("getConnection faild");
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            myMessage.feedback feedback = buildMessage.getFeedback("updateDoctor", -1, "update faild!");
            ThreadHandlerChannel.sendMessage(selectionKey, feedback);
        }
    }

    // 删除一个医生
    public static void deleteDoctor(SelectionKey selectionKey, Any any) {
        // DELETE FROM doctor WHERE id =
        String sql = "DELETE FROM doctor WHERE id =?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("id"));
                int num = 0;
                num = preparedStatement.executeUpdate();
                myMessage.feedback.Builder feedback = myMessage.feedback.newBuilder();
                feedback.setFunctionName("deleteDoctor");
                feedback.setMark(num);
                feedback.setBackMessage("delete success!");
                ThreadHandlerChannel.sendMessage(selectionKey, feedback.build());
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteDoctor", -1, "fail"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteDoctor", -1, "fail"));
        }
    }

    // 返回所有vip信息
    public static void getVip(SelectionKey selectionKey) {
        String sql = "select patientId, name, sex, birth, identity, phone, id, consume, remain, present, useful FROM vip_view;";
        try (Connection connection = C3P0ConnentionProvider.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getVip", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getVip", -1, "get fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getVip", -1, "get fail"));
            e.printStackTrace();
        }

    }

    // 会员卡充值记录
    public static void payRecord(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM chargerecord where card = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                Map map = buildMessage.AnyToMap(any);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("payRecord", preparedStatement.getResultSet()));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("payRecord", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("payRecord", -1, "fail"));
            e.printStackTrace();
        }

    }

    // 会员卡消费记录
    public static void consumeRecord(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM order_view where vipId = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                Map map = buildMessage.AnyToMap(any);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("consumeRecord", preparedStatement.getResultSet()));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("consumeRecord", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("consumeRecord", -1, "fail"));
            e.printStackTrace();
        }
    }

    // 返回vip充值消费记录
    public static void vipRecord(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM chargeandrecharge_view where card = ? or phone like ?;";
        Map map = buildMessage.AnyToMap(any);
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("id"));
            preparedStatement.setObject(2, map.get("phone"));
            try {
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("vipRecord", preparedStatement.getResultSet()));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("vipRecord", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("vipRecord", -1, "fail"));
            e.printStackTrace();
        }
    }


    // 返回初诊复诊问题
    public static void patientVisits(SelectionKey selectionKey, Any any) {
        String sql = "call firsts_secone_visit(?,?);";
        Map map = buildMessage.AnyToMap(any);
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("start"));
            preparedStatement.setObject(2, map.get("end"));
            try {
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("patientVisits", preparedStatement.getResultSet()));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("patientVisits", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("patientVisits", -1, "fail"));
            e.printStackTrace();
        }
    }


    // 添加一个vip
    public static void addVip(SelectionKey selectionKey, Any any) {
        String sql = "INSERT INTO `vip` (`id`, `password`, `consume`, `remain`, `present`, `useful`) VALUES (?, ?,0, 0,0, 2);";
        String sql2 = "INSERT INTO patient VALUES(?,?,?,?,?,?,?,?,?);";
        //String sql3 = "select LAST_INSERT_ID() id";
        String sql4 = "UPDATE `patient` SET `vip`=? WHERE `id`=?;";
        String sql5 = "UPDATE patient SET name = ? ,sex = ?, birth = ?,identity = ?, phone = ?,vip=? WHERE id =? and vip is null;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            //PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
            PreparedStatement preparedStatement4 = connection.prepareStatement(sql4);
            PreparedStatement preparedStatement5 = connection.prepareStatement(sql5);
            try {
                Map map = buildMessage.AnyToMap(any);
                // 添加vip
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.setObject(2, map.get("password"));
                preparedStatement.executeUpdate();

                // 如果直接选中病人，直接绑定
                if (map.get("patientid") != null) {
                    preparedStatement4.setObject(1, map.get("id"));
                    preparedStatement4.setObject(2, map.get("patientid"));
                    // name = ? ,sex = ?, birth = ?,identity = ?, phone = ?
                    preparedStatement5.setObject(1, map.get("name"));
                    preparedStatement5.setObject(2, map.get("sex"));
                    preparedStatement5.setObject(3, map.get("birth"));
                    if (map.get("identity").equals(""))
                        preparedStatement5.setObject(4, null);
                    else
                        preparedStatement5.setObject(4, map.get("identity"));
                    preparedStatement5.setObject(5, map.get("phone"));
                    preparedStatement5.setObject(6, map.get("id"));
                    preparedStatement5.setObject(7, map.get("patientid"));
                    int n = preparedStatement5.executeUpdate();
                    if (n == 0)
                        throw new SQLException();
                } else {
                    // 添加病人
                    Date date = new Date();
                    preparedStatement2.setObject(1, null);
                    preparedStatement2.setObject(2, map.get("name"));
                    preparedStatement2.setObject(3, map.get("sex"));
                    preparedStatement2.setObject(4, map.get("birth"));
                    preparedStatement2.setObject(5, map.get("id"));
                    preparedStatement2.setObject(6, map.get("identity"));
                    preparedStatement2.setObject(7, map.get("phone"));
                    preparedStatement2.setObject(8, date);
                    preparedStatement2.setObject(9, date);
                    preparedStatement2.executeUpdate();
                }

                // 得到病人id
                // ResultSet r = preparedStatement3.executeQuery();
                // r.last();
                // int id = r.getInt("id");


                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addVip", 1, "add success"));
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addVip", -1, "add fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addVip", -1, "add fail"));
            e.printStackTrace();
        }

    }

    // 删除一个vip
    public static void deleteVip(SelectionKey selectionKey, Any any) {
        String sql = "UPDATE `vip` SET useful=0 WHERE `id`=?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("id"));
            try {
                preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteVip", 1, "deleteVip success"));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteVip", -1, "deleteVip fail"));
                e.printStackTrace();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteVip", -1, "deleteVip fail"));
            e.printStackTrace();
        }

    }

    // 设置vip挂失
    public static void lostVip(SelectionKey selectionKey, Any any) {
        String sql = "UPDATE `.`vip_view` SET useful=1 WHERE `id`=? or phone=  ? and useful = 2;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.setObject(2, map.get("phone"));
                preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("lostVip", 1, "设置成功"));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("lostVip", -1, "设置失败"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("lostVip", -1, "设置失败"));
            e.printStackTrace();
        }

    }

    // 解除挂失卡
    public static void cancelLostVip(SelectionKey selectionKey, Any any) {
        String sql = "UPDATE `vip_view` SET useful =2  WHERE id = ? or phone = ? and useful = 1;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.setObject(2, map.get("phone"));
                preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("cancelLostVip", 1, "设置成功"));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("cancelLostVip", -1, "设置失败"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("cancelLostVip", -1, "设置失败"));
            e.printStackTrace();
        }
    }

    // 补办会员卡
    public static void remakeCard(SelectionKey selectionKey, Any any) {
        String sql = "update vip_view set id = ?, useful = 2 where (id = ? or phone = ?) and useful = 1;";
        String sql2 = "update patient set vip = ? where vip =?;";
        Map map = buildMessage.AnyToMap(any);
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement.setObject(1, map.get("new"));
            preparedStatement.setObject(2, map.get("old"));
            preparedStatement.setObject(3, map.get("phone"));
            preparedStatement2.setObject(1, map.get("new"));
            preparedStatement2.setObject(2, map.get("old"));
            try {
                int n = preparedStatement.executeUpdate();
                if (n == 1) {
                    preparedStatement2.executeUpdate();
                    connection.commit();
                } else
                    throw new SQLException();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("remakeCard", n, "success"));
            } catch (SQLException e) {
                connection.rollback();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("remakeCard", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("remakeCard", -1, "fail"));
            e.printStackTrace();
        }
    }

    // 更新vip信息
    public static void updateVip(SelectionKey selectionKey, Any any) {
        String sql = "UPDATE vip_view SET name = ? ,sex = ?, birth = ?,identity = ?, phone = ? WHERE id = ? and useful =2 and patientId is not null;";
        String sql2 = "UPDATE vip_view SET password = ? WHERE id = ? and useful =2 and patientId is not null;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("name"));
            preparedStatement.setObject(2, map.get("sex"));
            preparedStatement.setObject(3, map.get("birth"));
            preparedStatement.setObject(4, map.get("identity"));
            preparedStatement.setObject(5, map.get("phone"));
            preparedStatement.setObject(6, map.get("id"));

            preparedStatement2.setObject(1, map.get("password"));
            preparedStatement2.setObject(2, map.get("id"));
            try {
                int n = preparedStatement.executeUpdate();
                preparedStatement2.executeUpdate();
                if (n == 0)
                    throw new SQLException("");
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateVip", n, "修改成功"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateVip", -1, "修改失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateVip", -1, "修改失败"));
            e.printStackTrace();
        }

    }

    // vip 充值记录
    public static void getRechargeCounts(SelectionKey selectionKey, Any any) {
        String sql = "SELECT card,remain,present,date FROM chargerecord where date > ? and date <?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("start") + " 00:00:00");
            preparedStatement.setObject(2, map.get("end") + " 23:59:59");
            try {
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getRechargeCounts", preparedStatement.getResultSet()));

            } catch (SQLException r) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getRechargeCounts", -1, "fail"));
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getRechargeCounts", -1, "fail"));
        }

    }

    // 通过一个vip的电话或者id得到所有信息
    public static void selectVipByPhoneOrId(SelectionKey selectionKey, Any any) {
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            String phone = (String) map.get("phone");
            Object id = map.get("id");

            String sql = "SELECT patientId, name, sex, birth, identity, phone, id, consume, remain, present, useful FROM vip_view WHERE id = ? OR phone = ?;";
            String sql3 = "SELECT patientId, name, sex, birth, identity, phone, id, consume, remain, present, useful FROM vip_view WHERE name like ?;";

            PreparedStatement preparedStatement;
            if (map.get("name") != null && !map.get("name").equals("")) {
                preparedStatement = connection.prepareStatement(sql3);
                preparedStatement.setObject(1, map.get("name"));
            } else {
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, id);
                preparedStatement.setObject(2, phone);
            }
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                resultSet.last();
                if (resultSet.getRow() == 1) {
                    Object Id = resultSet.getObject("id");
                    float remain = resultSet.getFloat("remain");
                    String name = resultSet.getString("name");
                    float consume = resultSet.getFloat("consume");
                    String Phone = resultSet.getString("phone");
                    float present = resultSet.getFloat("present");
                    Object birth = resultSet.getObject("birth");
                    String identity = (String) resultSet.getObject("identity");
                    Integer patientid = (Integer) resultSet.getObject("patientid");
                    String sex = resultSet.getString("sex");
                    Integer useful = resultSet.getInt("useful");

                    float discount = 1;
                    String sql2 = "SELECT * FROM integrate where integrate<=? order by integrate;";
                    PreparedStatement preparedStatement1 = connection.prepareStatement(sql2);
                    preparedStatement1.setObject(1, consume);
                    preparedStatement1.executeQuery();
                    ResultSet resultSet1 = preparedStatement1.getResultSet();
                    resultSet1.last();
                    if (resultSet1.getRow() != 0)
                        discount = resultSet1.getFloat("discount");
                    myMessage.feedback.Builder builder = myMessage.feedback.newBuilder();
                    builder.setFunctionName("selectVipByPhoneOrId");
                    builder.setMark(1);
                    Data.common.Builder common = Data.common.newBuilder();
                    if (name != null)
                        common.putStr("name", name);
                    if (Phone != null)
                        common.putStr("phone", Phone);
                    if (Id != null)
                        common.putStr("id", Id.toString());
                    common.putFlo("remain", remain);
                    common.putFlo("present", present);
                    common.putFlo("discount", discount);
                    common.putFlo("consume", consume);
                    common.putInt("useful", useful);
                    if (patientid != null)
                        common.putInt("patientid", patientid);
                    if (identity != null)
                        common.putStr("identity", identity);
                    if (birth != null)
                        common.putStr("birth", birth.toString());
                    if (sex != null)
                        common.putStr("sex", sex);


                    builder.addDetails(Any.pack(common.build()));
                    ThreadHandlerChannel.sendMessage(selectionKey, builder.build());
                } else {
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("selectVipByPhoneOrId", -1, "fail"));
                }
            } catch (Exception e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("selectVipByPhoneOrId", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("selectVipByPhoneOrId", -1, "fail"));
            e.printStackTrace();
        }

    }


    // 通过一个病人的的电话或者id得到所有信息
    public static void selectPatientByPhoneOrId(SelectionKey selectionKey, Any any) {
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            String phone = (String) map.get("phone");
            Object id = map.get("id");

            String sql = "SELECT id, name, sex, birth, identity, phone FROM patient_view WHERE vip = ? OR phone = ?;";
            PreparedStatement p1 = connection.prepareStatement(sql);
            p1.setObject(1, id);
            p1.setObject(2, phone);
            try {
                p1.executeQuery();
                ResultSet resultSet = p1.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("selectPatientByPhoneOrId", resultSet));
            } catch (SQLException e2) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("selectPatientByPhoneOrId", -1, "fail"));
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("selectPatientByPhoneOrId", -1, "fail"));
            e.printStackTrace();
        }

    }


    // 充值vip
    public static void payForVip(SelectionKey selectionKey, Any any) {
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);
            try {

                String sql = "select * from vip_view where id = ? and useful = 2;";
                String sql1 = "INSERT INTO `chargerecord` (`card`, `money`, `remain`, `present`, `date`) VALUES (?, ?, ?, ?, ?);";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
                Map map = buildMessage.AnyToMap(any);
                Object id = map.get("id");
                preparedStatement.setObject(1, id);
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                resultSet.last();
                int n = resultSet.getRow();
                if (n == 1) {
                    BigDecimal consume = resultSet.getBigDecimal("consume");

                    BigDecimal remain = resultSet.getBigDecimal("remain");
                    BigDecimal present = resultSet.getBigDecimal("present");

                    BigDecimal tremain = new BigDecimal(map.get("remain").toString());
                    BigDecimal tpresent = new BigDecimal(map.get("present").toString());

                    consume = consume.add(tremain);
                    remain = remain.add(tremain);
                    present = present.add(tpresent);

                    if (remain.compareTo(BigDecimal.ZERO) < 0 || present.compareTo(BigDecimal.ZERO) < 0)
                        throw new SQLException("金额成负数");


                    String sql2 = "UPDATE vip SET remain = ? , present = ? , consume = ?  where id = ?;";
                    PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                    preparedStatement2.setObject(1, remain);
                    preparedStatement2.setObject(2, present);
                    preparedStatement2.setObject(3, consume);
                    preparedStatement2.setObject(4, id);
                    preparedStatement2.executeUpdate();
                    preparedStatement1.setObject(1, id);
                    preparedStatement1.setObject(2, remain);
                    preparedStatement1.setObject(3, tremain);
                    preparedStatement1.setObject(4, tpresent);
                    preparedStatement1.setObject(5, new Date());
                    preparedStatement1.executeUpdate();


                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("payForVip", 1, "充值成功"));
                } else
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("payForVip", -1, "充值失败!"));
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("payForVip", -1, "充值失败!"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("payForVip", -1, "充值失败!"));
            e.printStackTrace();
        }
    }

    // 添加病人信息
    public static void addPatient(SelectionKey selectionKey, Any any) {
        String sql = "INSERT INTO patient VALUES(?,?,?,?,?,?,?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();

            try {
                Map map = buildMessage.AnyToMap(any);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                java.text.SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd ");
                preparedStatement.setObject(1, null);
                preparedStatement.setObject(2, map.get("name"));
                preparedStatement.setObject(3, map.get("sex"));
                preparedStatement.setObject(4, map.get("birth"));
                preparedStatement.setObject(5, map.get("identity"));
                preparedStatement.setObject(6, map.get("phone"));
                preparedStatement.setObject(7, new Date());
                boolean mark = preparedStatement.execute();
                if (mark)
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPatient", 1, "add success"));
                else
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPatient", -1, "add faild"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPatient", -1, "add faild"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPatient", -1, "add faild"));
            e.printStackTrace();
        }

    }

    // 得到所有病人信息
    public static void getAllPatient(SelectionKey selectionKey, Any any) {
        String sql = "call patient_procedure(?, ?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("start").toString() + " 00:00:00");
            preparedStatement.setObject(2, map.get("last").toString() + " 23:59:59");
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getAllPatient", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getAllPatient", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getAllPatient", -1, "fail"));
            e.printStackTrace();
        }

    }

    // 添加一个药品
    public static void addMedicine(SelectionKey selectionKey, Any any) {
        String sql = "INSERT INTO medicine (`id`, `name`,`itemCode`, `unitId`, `inprice`, `outprice`, `feetypeId`, `factoryId`, `stock`, `warn`, `spell`, `type`,`isInclude`,`itemGrade`,name2) VALUES (?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("id"));
            preparedStatement.setObject(2, map.get("name"));
            preparedStatement.setObject(3, map.get("itemCode"));
            preparedStatement.setObject(4, map.get("unitId"));
            preparedStatement.setObject(5, map.get("inprice"));
            preparedStatement.setObject(6, map.get("outprice"));
            preparedStatement.setObject(7, map.get("feetypeId"));
            preparedStatement.setObject(8, map.get("factoryId"));
            preparedStatement.setObject(9, map.get("stock"));
            preparedStatement.setObject(10, map.get("warn"));
            preparedStatement.setObject(11, map.get("spell"));
            preparedStatement.setObject(12, map.get("type"));
            preparedStatement.setObject(13, map.get("isInclude"));
            preparedStatement.setObject(14, map.get("itemGrade"));
            preparedStatement.setObject(15, map.get("name2"));

            try {
                boolean mark = preparedStatement.execute();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addMedicine", 1, "添加成功"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addMedicine", -1, "添加失败！！！超级失败！~"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addMedicine", -1, "添加失败！！！超级失败！~"));
            e.printStackTrace();
        }
    }

    // 添加一个药品
    public static void addMedicineByExcel(SelectionKey selectionKey, Any any) {
        String sql = "INSERT INTO medicine (`id`, `name`,`itemCode`, `unitId`, `inprice`, `outprice`, `feetypeId`, `factoryId`, `stock`, `warn`, `spell`, `type`,`isInclude`,`itemGrade`) VALUES (?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?,?,?,?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("id"));
            preparedStatement.setObject(2, map.get("name"));
            preparedStatement.setObject(3, map.get("itemCode"));
            preparedStatement.setObject(4, map.get("unitId"));
            preparedStatement.setObject(5, map.get("inprice"));
            preparedStatement.setObject(6, map.get("outprice"));
            preparedStatement.setObject(7, map.get("feetypeId"));
            preparedStatement.setObject(8, map.get("factoryId"));
            preparedStatement.setObject(9, map.get("stock"));
            preparedStatement.setObject(10, map.get("warn"));
            preparedStatement.setObject(11, map.get("spell"));
            preparedStatement.setObject(12, map.get("type"));
            preparedStatement.setObject(13, map.get("isInclude"));
            preparedStatement.setObject(14, map.get("itemGrade"));
            try {
                boolean mark = preparedStatement.execute();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addMedicineByExcel", 1, "添加成功"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addMedicineByExcel", -1, "添加失败！！！超级失败！~"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addMedicineByExcel", -1, "添加失败！！！超级失败！~"));
            e.printStackTrace();
        }
    }

    // 更新一个药品信息
    public static void updateMedicine(SelectionKey selectionKey, Any any) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("UPDATE medicine set");
        Map map = buildMessage.AnyToMap(any);
        Object id = map.get("id");
        map.remove("id");
        int i = 0;
        for (Object o : map.keySet()) {
            if (i++ != map.size() - 1)
                stringBuffer.append(" " + o + " = '" + map.get(o) + "',");
            else
                stringBuffer.append(" " + o + " = '" + map.get(o) + "' ");
        }
        stringBuffer.append(" WHERE id =?");
        String sql = stringBuffer.toString();

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, id);
                int n = preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateMedicine", n, "updata success!"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateMedicine", -1, "updata faild!"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateMedicine", -1, "updata faild!"));
            e.printStackTrace();
        }

    }

    // 删除一个药品
    public static void deleteMedicine(SelectionKey selectionKey, Any any) {
        String sql = "DELETE FROM medicine WHERE id =?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("id"));
                int n = preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteMedicine", n, "delete success!"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteMedicine", 0, "delete fail!"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteMedicine", 0, "delete fail!"));
            e.printStackTrace();
        }
    }

    // 添加一个vip 等级
    public static void insertIntegrate(SelectionKey selectionKey, Any any) {
        String sql = "INSERT INTO integrate VALUES(?,?,?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("level"));
            preparedStatement.setObject(2, map.get("integrate"));
            preparedStatement.setObject(3, map.get("discount"));
            try {
                int n = preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insertIntegrate", n, "添加成功"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insertIntegrate", 0, "添加失败"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insertIntegrate", 0, "获取数据库连接失败"));
            e.printStackTrace();
        }

    }

    // 更新一个vip 等级
    public static void updateIntegrate(SelectionKey selectionKey, Any any) {
        String sql = "UPDATE integrate SET integrate=?, discount=? WHERE level=?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("integrate"));
            preparedStatement.setObject(2, map.get("discount"));
            preparedStatement.setObject(3, map.get("level"));
            try {
                int n = preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateIntegrate", n, "update success"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateIntegrate", 0, "update fail"));
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateIntegrate", 0, "update fail"));
            e.printStackTrace();
        }
    }

    // 删除一个vip 等级
    public static void deleteIntegrate(SelectionKey selectionKey, Any any) {
        String sql = "DELETE FROM `integrate` WHERE `level`=?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("level"));
            try {
                int n = preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteIntegrate", n, "delete success"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteIntegrate", 0, "delete fail"));
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteIntegrate", 0, "delete fail"));
            e.printStackTrace();
        }
    }

    // 结算
    public static void Account(SelectionKey selectionKey, Any any) {
        //
        synchronized (db.class) {
            Map map = buildMessage.AnyToMap(any);

            String rgid = map.get("rgid").toString();

            // 会员卡信息
            int vipId = 0;
            String password = "123456";

            // 支付详情
            BigDecimal card = BigDecimal.ZERO;
            BigDecimal given = BigDecimal.ZERO;
            BigDecimal cash = BigDecimal.ZERO;
            BigDecimal other = BigDecimal.ZERO;
            BigDecimal insurance = BigDecimal.ZERO;

            String chargeNumber = null;
            String[] pid = null;
            boolean preAccount = false;

            // 收费者
            userStatement userStatement = (userStatement) selectionKey.attachment();
            int positionId = userStatement.getId();


            // 如果vipid 不为0 - 有卡
            if (map.containsKey("vipId")) {
                vipId = (Integer) map.get("vipId");
                card = new BigDecimal(map.get("card").toString());
                given = new BigDecimal(map.get("given").toString());
                password = map.get("password").toString();
            }
            if (map.containsKey("cash"))
                cash = new BigDecimal(map.get("cash").toString());
            if (map.containsKey("other"))
                other = new BigDecimal(map.get("other").toString());
            if (map.containsKey("insurance"))
                insurance = new BigDecimal(map.get("insurance").toString());
            if (map.containsKey("15"))
                chargeNumber = map.get("15").toString();

            // 判断是否预结算
            if (map.get("preAccount").toString().equals("true"))
                preAccount = true;
            else
                preAccount = false;

            // 4位小数的折扣率
            BigDecimal medicinediscount = new BigDecimal(map.get("medicinediscount").toString()).setScale(4, BigDecimal.ROUND_FLOOR);
            BigDecimal medcurediscount = new BigDecimal(map.get("medcurediscount").toString()).setScale(4, BigDecimal.ROUND_FLOOR);

            // 拆分处方列表
            pid = map.get("pid").toString().split("\\|");
            String sql_1 = "select medfee,curefee from prescriptionlist where id = ?";
            String sql_2 = "SELECT remain,present FROM vip where id = ? and password = ?;";
            String sql_3 = "INSERT INTO `order` (`id`,`positionId`, `vipId`, `card`, `given`, `cash`, `other`, `insurance`, `medicinediscount`, `medcurediscount`, `date`, `sum`, `insuranceNumber`) VALUES (null ,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);";
            String sql_4 = "UPDATE vip SET remain= remain - ?, present=present - ? ,consume = consume + ? WHERE id=?;";
            String sql_5 = "UPDATE `prescriptionlist` SET `orderid`=?,`state`='2'  WHERE id = ? ";
            String sql_7 = "select  LAST_INSERT_ID() id;";
            String sql_8 = "insert  into insurancechargehistory values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            try {
                Connection connection = C3P0ConnentionProvider.getConnection();
                connection.setAutoCommit(false);
                PreparedStatement preparedStatement_1 = connection.prepareStatement(sql_1);
                PreparedStatement preparedStatement_2 = connection.prepareStatement(sql_2);
                PreparedStatement preparedStatement_3 = connection.prepareStatement(sql_3);
                PreparedStatement preparedStatement_4 = connection.prepareStatement(sql_4);
                PreparedStatement preparedStatement_5 = connection.prepareStatement(sql_5);
                PreparedStatement preparedStatement_7 = connection.prepareStatement(sql_7);
                PreparedStatement preparedStatement_8 = connection.prepareStatement(sql_8);

                try {
                    // 不是预结算则，插入医保收费详情
                    if (insurance.compareTo(BigDecimal.ZERO) > 0 && preAccount == false) {
                        for (int j = 1; j <= 55; j++) {
                            preparedStatement_8.setObject(j, map.get(String.valueOf(j - 1)));
                        }
                        // 56 处方编号，一个医保收费记录对应一个处方！虽然想写多个同时收费
                        preparedStatement_8.setObject(56, pid[0]);
                        preparedStatement_8.setObject(57, 1);
                        preparedStatement_8.executeUpdate();
                        connection.commit();
                    }


                    String sql_c_1 = "UPDATE `prescription` SET `number`=? WHERE listid = ? and medicineid = 0";
                    String sql_c_2 = "UPDATE `prescriptionlist` SET `medfee`=`medfee` + ?  WHERE `id`=?";
                    PreparedStatement ps_1 = connection.prepareStatement(sql_c_1);
                    PreparedStatement ps_2 = connection.prepareStatement(sql_c_2);

                    // 所有处方总金额
                    BigDecimal sum = BigDecimal.ZERO;

                    // 修改药品费用 和 凑整费
                    for (String p : pid) {
                        preparedStatement_1.setObject(1, p);
                        preparedStatement_1.executeQuery();
                        ResultSet resultSet = preparedStatement_1.getResultSet();
                        resultSet.last();
                        BigDecimal medicineMon;
                        BigDecimal medcureMon;
                        BigDecimal pidSum;      // 订单总金额
                        BigDecimal pidSum2;     // 订单向上取整后总金额
                        medicineMon = resultSet.getBigDecimal("medfee").setScale(2, BigDecimal.ROUND_FLOOR);
                        medcureMon = resultSet.getBigDecimal("curefee").setScale(2, BigDecimal.ROUND_FLOOR);

                        // 折扣后总金额
                        pidSum = medicineMon.multiply(medicinediscount).setScale(2, BigDecimal.ROUND_FLOOR);
                        pidSum = pidSum.add(medcureMon.multiply(medcurediscount)).setScale(2, BigDecimal.ROUND_FLOOR);

                        // 向上取整
                        pidSum2 = pidSum.setScale(0, BigDecimal.ROUND_UP);
                        sum = sum.add(pidSum2);

                        // 计算凑整费
                        BigDecimal t = pidSum2.subtract(medcureMon.multiply(medcurediscount).setScale(2, BigDecimal.ROUND_FLOOR));        // 减去  诊疗费* 诊疗折扣 截断后
                        // 会出现无法整除异常,和0异常
                        if (t.compareTo(BigDecimal.ZERO) != 0) {
                            t = t.divide(medicinediscount, 2, BigDecimal.ROUND_UP);     // 理当原价，（会有超出两位小数精确度的值）
                            t = t.subtract(medicineMon);        // 理当凑整值，如果截断，那么就会比 凑整后向上取整的值小！如果不截断那么相乘后会比 凑整后的多
                            t = t.setScale(2, BigDecimal.ROUND_UP);      // 只能比凑整后的整数大，然后进行截断 + 0.01 * medcindiscount
                        }
                        ps_2.setObject(1, t);
                        ps_2.setObject(2, p);
                        ps_2.executeUpdate();

                        // 更新凑整费数量
                        t = t.multiply(new BigDecimal(100));
                        ps_1.setObject(1, t.toString());
                        ps_1.setObject(2, p);
                        ps_1.executeUpdate();

                    }


                    // 会员卡验证
                    BigDecimal remain = BigDecimal.ZERO, present = BigDecimal.ZERO;
                    if (vipId != 0 || card.compareTo(BigDecimal.ZERO) > 0 || given.compareTo(BigDecimal.ZERO) > 0) {
                        preparedStatement_2.setObject(1, vipId);
                        preparedStatement_2.setObject(2, password);
                        preparedStatement_2.executeQuery();
                        ResultSet resultSet2 = preparedStatement_2.getResultSet();
                        resultSet2.last();
                        int n = resultSet2.getRow();
                        if (n == 1) {
                            remain = resultSet2.getBigDecimal("remain");
                            present = resultSet2.getBigDecimal("present");
                        }
                    }

                    // 开始计算验证
                    if (remain.compareTo(card) >= 0 && present.compareTo(given) >= 0) {
                        BigDecimal sumT = sum;
                        sum = sum.subtract(cash).subtract(card).subtract(other).subtract(given).subtract(insurance);

                        // 五分钱以内的误差可以接受
                        if (sum.subtract(new BigDecimal(0.05)).abs().compareTo(new BigDecimal(0.05)) <= 0) {
                            // id,positionId,vipId ,card, given, cash, other,insurance, medicinediscount, medcurediscount, `date`,`sum`,insuranceNumber
                            // 插入订单记录
                            preparedStatement_3.setObject(1, positionId);
                            if (vipId != 0) {
                                preparedStatement_3.setObject(2, vipId);
                                preparedStatement_3.setObject(3, card);
                                preparedStatement_3.setObject(4, given);
                            } else {
                                preparedStatement_3.setObject(2, null);
                                preparedStatement_3.setObject(3, null);
                                preparedStatement_3.setObject(4, null);
                            }
                            preparedStatement_3.setObject(5, cash);
                            preparedStatement_3.setObject(6, other);
                            preparedStatement_3.setObject(7, insurance);
                            preparedStatement_3.setObject(8, medicinediscount);
                            preparedStatement_3.setObject(9, medcurediscount);
                            preparedStatement_3.setObject(10, new Date());
                            preparedStatement_3.setObject(11, sumT);
                            if (insurance.compareTo(BigDecimal.ZERO) > 0 && preAccount == false)
                                preparedStatement_3.setObject(12, chargeNumber);
                            else
                                preparedStatement_3.setObject(12, null);
                            preparedStatement_3.executeUpdate();

                            // 得到插入的id
                            int orderId = 0;
                            preparedStatement_7.executeQuery();
                            ResultSet re = preparedStatement_7.getResultSet();
                            re.last();
                            orderId = re.getInt("id");

                            // 从卡内扣除金钱
                            if (card.compareTo(BigDecimal.ZERO) >= 0 || present.compareTo(BigDecimal.ZERO) >= 0) {
                                preparedStatement_4.setObject(1, card);
                                preparedStatement_4.setObject(2, present);
                                preparedStatement_4.setObject(3, cash.add(other).add(insurance));
                                preparedStatement_4.setObject(4, vipId);
                                preparedStatement_4.executeUpdate();
                            }

                            // 设置已经支付
                            for (String p : pid) {
                                preparedStatement_5.setObject(1, orderId);
                                preparedStatement_5.setObject(2, p);
                                preparedStatement_5.executeUpdate();
                            }

                            // 如果是预结算那就进行回滚，否则就提交
                            if (!preAccount)
                                connection.commit();
                            else
                                connection.rollback();

                            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Account", 1, "结算成功"));
                        } else {
                            connection.rollback();
                            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Account", -3, "金额核对失败"));
                        }
                    } else {
                        connection.rollback();
                        ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Account", -2, "会员卡余额不足"));
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Account", -1, "结算失败"));
                    e.printStackTrace();
                } finally {
                    connection.close();
                }
            } catch (Exception e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("Account", -1, "数据库连接失败"));
                e.printStackTrace();
            }
        }
    }


    // 医保结算（插入结算信息到，insuranceChargeHistory 表中，留存记录）
    public static void insuranceAccount(SelectionKey selectionKey, Any any) {
        // 这个函数只是储存医保以及对应处方已经支付金额，在收费医保收费后会存在一个收费记录，以防有订单有个人支付部分和医保支付，然后退出了支付页面，医保支付部分丢失
        try {
            String sql = "insert  into insurancechargehistory values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);

            // 1-55 个都是返回值对应的字段，插入医保收费记录
            for (int i = 1; i <= 55; i++) {
                preparedStatement.setObject(i, map.get(String.valueOf(i - 1)));
            }
            // 56 处方编号，一个医保收费记录对应一个处方！虽然想写多个同时收费
            preparedStatement.setObject(56, map.get("56"));
            preparedStatement.setObject(57, 1);
            try {
                preparedStatement.execute();
                connection.commit();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insuranceAccount", 1, "insert success"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insuranceAccount", -1, "insert fail"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insuranceAccount", -1, "insert fail"));
            e.printStackTrace();
        }
    }

    // 冲销医保结算信息，本地冲销
    public static void offsetInsuranceAccount(SelectionKey selectionKey, Any any) {
        try {
            String sql = "select * from insurancechargehistory where preid = ?";
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement pre = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            pre.setObject(1, map.get("pid"));
            pre.executeQuery();
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getInsuranceHistory", pre.getResultSet()));
            connection.close();
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getInsuranceHistory", -1, "查询失败"));
            e.printStackTrace();
        }
    }

    // 查询医保收费记录
    public static void getInsuranceHistory(SelectionKey selectionKey, Any any) {
        try {
            String sql = "select * from insurancechargehistory where preid = ?";
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement pre = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            pre.setObject(1, map.get("pid"));
            pre.executeQuery();
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getInsuranceHistory", pre.getResultSet()));
            connection.close();
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getInsuranceHistory", -1, "查询失败"));
            e.printStackTrace();
        }
    }

    // 发药
    public static void accountMedicine(SelectionKey selectionKey, Any any) {
        // 通过处方id 去减少药品的库存 同时设置药品已经拿药

        // 通过挂号id 获取挂号详情
        // 通过处方id 去减少处方的所有药
        // 修改发药状态
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                connection.setAutoCommit(false);
                Map map = buildMessage.AnyToMap(any);
                String sql = "SELECT * FROM pre_order_view where pid = ?;";
                String sql2 = "SELECT medicineid,number FROM prescription where listid = ?;";
                String sql3 = "UPDATE medicine SET stock = stock - ? WHERE id = ?;";
                String sql4 = "UPDATE prescriptionlist SET `state`='3' WHERE `id`=?;";

                for (Object pid : map.keySet()) {
                    // 所有的集都是一个处方，对所有的处方进行遍历
                    // 1. 验证处方是否发药
                    // 2. 没有发药就进行发药
                    // 3. 修改处方的状态信息
                    int id = Integer.valueOf(pid.toString());
                    PreparedStatement p1 = connection.prepareStatement(sql);
                    p1.setObject(1, id);
                    p1.executeQuery();
                    ResultSet re = p1.getResultSet();
                    re.last();
                    if (re.getInt("state") != 2) {
                        throw new SQLException();
                    }
                    PreparedStatement p2 = connection.prepareStatement(sql2);
                    p2.setObject(1, id);
                    p2.executeQuery();
                    Map<Integer, Integer> med = new HashMap<>();
                    re = p2.getResultSet();

                    // 遍历药品
                    while (re.next())
                        med.put(re.getInt("medicineid"), re.getInt("number"));

                    // 减去库存
                    PreparedStatement p3 = connection.prepareStatement(sql3);
                    for (int medicineid : med.keySet()) {
                        if (medicineid == 0)
                            continue;
                        p3.setObject(1, med.get(medicineid));
                        p3.setObject(2, medicineid);
                        p3.executeUpdate();
                    }

                    // 设置已经发药
                    PreparedStatement p4 = connection.prepareStatement(sql4);
                    p4.setObject(1, id);
                    p4.executeUpdate();
                }


                connection.commit();
                connection.setAutoCommit(true);
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("accountMedicine", 1, "发药成功"));
            } catch (SQLException e) {
                connection.rollback();
                connection.setAutoCommit(true);
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("accountMedicine", -1, "发药失败"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("accountMedicine", -1, "发药失败"));
            e.printStackTrace();
        }


    }

    // 通过id得到病人名字
    public static void getNameByPatientId(SelectionKey selectionKey, Any any) {
        String sql = "SELECT name FROM patient where id = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("id"));
            try {
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getNameByPatientId", preparedStatement.getResultSet()));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getNameByPatientId", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getNameByPatientId", -1, "fail"));
            e.printStackTrace();
        }
    }

    // 挂号
    public static void insertRegister(SelectionKey selectionKey, Any any) {
        // 通过事务实现挂号功能
        // 通过 姓名判断是否有这个人 如果有这个人就直接挂号，否则就添加病人信息 再挂号
        String sql6 = "SELECT * FROM patient where identity like ?;";
        String sql5 = "select last_insert_id() id;";
        String sql4 = "UPDATE patient set name = ? , sex = ? ,birth = ? ,identity = ?,phone =? ,last = ? where id = ?";
        String sql2 = "INSERT INTO patient VALUES(?,?,?,?,?,?,?,?,?);";
        String sql = "INSERT INTO register (`id`, `patientid`, `departmentId`, `doctorId`, `regtypeid`, `fee`, `first`, `positionId`, `date`,`useful`,`insurance`,`securityId`, `cardId` , `areaId`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?);";
        int patientId = 0;

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);
            Map map = buildMessage.AnyToMap(any);
            patientId = (Integer) map.get("patientId");
            Date date = new Date();
            try {
                // 通过身份证号码查询 病人id , 如果是医保挂号，病人id为0, 在这里设置病人id，正确更新病人信息
                if (!map.get("identity").equals("")) {
                    PreparedStatement p = connection.prepareStatement(sql6);
                    p.setObject(1, map.get("identity"));
                    p.executeQuery();
                    ResultSet resultSet = p.getResultSet();
                    resultSet.last();
                    if (resultSet.getRow() == 1)
                        patientId = resultSet.getInt("id");
                }

                // 通过patientId 判断是否为新病人
                if (patientId == 0) {
                    // 如果是新病人就进行插入，否则进行更新
                    PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                    preparedStatement2.setObject(1, null);
                    preparedStatement2.setObject(2, map.get("patientname"));
                    preparedStatement2.setObject(3, map.get("sex"));
                    preparedStatement2.setObject(4, map.get("birth"));
                    preparedStatement2.setObject(5, null);
                    if (map.get("identity").toString().equals(""))
                        preparedStatement2.setObject(6, null);
                    else
                        preparedStatement2.setObject(6, map.get("identity"));

                    if (map.get("phone").toString().equals(""))
                        preparedStatement2.setObject(7, null);
                    else
                        preparedStatement2.setObject(7, map.get("phone"));

                    preparedStatement2.setObject(8, date);
                    preparedStatement2.setObject(9, date);
                    preparedStatement2.executeUpdate();

                    PreparedStatement preparedStatement5 = connection.prepareStatement(sql5);
                    preparedStatement5.executeQuery();
                    ResultSet resultSet = preparedStatement5.getResultSet();
                    resultSet.last();
                    // 得到刚刚添加的病人id
                    patientId = resultSet.getInt("id");
                } else {
                    // 更新病人信息
                    PreparedStatement preparedStatement4 = connection.prepareStatement(sql4);
                    preparedStatement4.setObject(1, map.get("patientname"));
                    preparedStatement4.setObject(2, map.get("sex"));
                    preparedStatement4.setObject(3, map.get("birth"));
                    if (map.get("identity").equals(""))
                        preparedStatement4.setObject(4, null);
                    else
                        preparedStatement4.setObject(4, map.get("identity").toString());
                    if (map.get("phone").toString().equals(""))
                        preparedStatement4.setObject(5, null);
                    else
                        preparedStatement4.setObject(5, map.get("phone"));

                    preparedStatement4.setObject(6, date);
                    preparedStatement4.setObject(7, patientId);
                    preparedStatement4.executeUpdate();

                }

                // 添加完病人开始挂号
                userStatement userStatement = (user.userStatement) selectionKey.attachment();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, null);
                preparedStatement.setObject(2, patientId);
                preparedStatement.setObject(3, map.get("departmentId"));
                preparedStatement.setObject(4, map.get("doctorId"));
                preparedStatement.setObject(5, map.get("regtypeid"));
                preparedStatement.setObject(6, map.get("fee"));
                preparedStatement.setObject(7, map.get("first"));
                preparedStatement.setObject(8, userStatement.getId());
                preparedStatement.setObject(9, date);
                preparedStatement.setObject(10, 1);
                preparedStatement.setObject(11, null);
                preparedStatement.setObject(12, null);
                preparedStatement.setObject(13, null);
                preparedStatement.setObject(14, null);
                preparedStatement.executeUpdate();

                // 得到刚刚挂号的流水号
                PreparedStatement preparedStatement5 = connection.prepareStatement(sql5);
                preparedStatement5.executeQuery();
                ResultSet resultSet = preparedStatement5.getResultSet();
                resultSet.last();
                int rgid = resultSet.getInt("id");
                connection.commit();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insertRegister", rgid, "挂号成功"));
            } catch (SQLException e) {
                connection.rollback();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insertRegister", -1, "挂号失败"));
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insertRegister", 0, "挂号失败"));
            e.printStackTrace();
        }

    }

    // 医保挂号，更改挂号详情
    public static void insuranceRegister(SelectionKey selectionKey, Any any) {
        String sql = "update register set insurance = 1 , securityId = ? ,cardId = ?,areaId = ? ,clinicNumber = ?,insuranceType = ?,entityCode = ?,entityName = ? where id =?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();

            try {
                PreparedStatement pre = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                String securityId = map.get("securityId").toString();
                String cardId = map.get("cardId").toString();
                pre.setObject(1, securityId);
                pre.setObject(2, cardId);
                pre.setObject(3, map.get("areaId"));
                pre.setObject(4, map.get("clinicNumber"));
                pre.setObject(8, map.get("id"));
                pre.setObject(5, map.get("insuranceType"));
                pre.setObject(6, map.get("entityCode"));
                pre.setObject(7, map.get("entityName"));
                pre.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insuranceRegister", 1, "更新医保挂号成功！").toByteArray());
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insuranceRegister", -1, "更新数据库失败！").toByteArray());

            } finally {
                connection.close();
            }
        } catch (Exception e1) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("insuranceRegister", -1, "更新数据库失败").toByteArray());
        }


    }


    // 修改挂号信息
    public static void editRegister(SelectionKey selectionKey, Any any) {
        // 1. 得到原来的挂号id
        // 2. 病人信息的重置
        // 3. 挂号信息的重置
        String sql = "UPDATE `patient` SET `name`=?, `sex`=?, `birth`=?, `identity`=?, `phone`=? WHERE `id` = ?";
        String sql2 = "UPDATE `register` SET `departmentId`=?, `doctorId`=? , `regtypeid`=? , `fee`=?  WHERE `id`=?;";
        String sql3 = "SELECT patientid FROM all_register_view where id = ?;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);
            Map map = buildMessage.AnyToMap(any);
            int rgid = Integer.parseInt(map.get("rgid").toString());
            try {
                PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
                preparedStatement3.setObject(1, rgid);
                preparedStatement3.executeQuery();
                ResultSet re = preparedStatement3.getResultSet();
                re.last();
                if (re.getRow() != 1)
                    throw new SQLException();
                int patientid = re.getInt("patientid");

                // 更新病人信息
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("patientname"));
                preparedStatement.setObject(2, map.get("sex"));
                preparedStatement.setObject(3, map.get("birth"));

                if(map.get("identity").toString().equals(""))
                    preparedStatement.setObject(4,null);
                else
                    preparedStatement.setObject(4,map.get("identity"));

                if (map.get("phone").toString().equals(""))
                    preparedStatement.setObject(5, null);
                else
                    preparedStatement.setObject(5, map.get("phone"));
                preparedStatement.setObject(6, patientid);
                preparedStatement.executeUpdate();

                // 更新挂号信息   `departmentId`=?, `doctorId`=? , `regtypeid`=? , `fee`=?  WHERE `id`=?;"
                PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                preparedStatement2.setObject(1, map.get("departmentId"));
                preparedStatement2.setObject(2, map.get("doctorId"));
                preparedStatement2.setObject(3, map.get("regtypeid"));
                preparedStatement2.setObject(4, map.get("fee"));
                preparedStatement2.setObject(5, rgid);
                preparedStatement2.executeUpdate();


                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("editRegister", 1, "success"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("editRegister", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("editRegister", -1, "fail"));
            e.printStackTrace();
        }


    }

    // 查询病人以前处方id
    public static void getHistory(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM pre_order_view where state is not null  and patientid = ? order by pid desc;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, map.get("patientid"));
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getHistory", resultSet));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getHistory", -1, "fail"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getHistory", -1, "fail"));
            e.printStackTrace();
        }
    }

    // 事务添加所有的处方 -- 划价
    public static void addPrescriptions(SelectionKey selectionKey, Any any) {
        /*
         *   1. 得到挂号id
         *   2. 得到处方id，如果有处方id ，那么就是更新处方，否则就是创建新处方
         *   3. 更新处方：删除原有的旧处方，回到创建新处方
         *
         *   药品，诊疗费截断方式为， 累加所有的值（单价 * 数量），最后再截断
         * */
        String sql = "INSERT INTO prescription (`listid`, `medicineid`,itemCode,itemGrade, `medname`,`medname2`, `inprice`, `outprice`, `type`,unitName,factoryName,feetypeName,number) VALUES ( ?, ?, ?,?, ?, ?,?,?,?,?,?,?,?);";
        String sql3 = "delete from prescription where listid = ?;";
        String sql4 = "select * from medcure_view where name = ? and useful = 1";
        String sql5 = "INSERT INTO prescriptionlist (`id`,`rgid`,`orderid`,`dose`, `medfee`, `curefee`, `state`,discription) VALUES (null, ?, null ,?, ?, ?, '1',?);";
        String sql6 = "select last_insert_id() last_id;";
        String sql7 = "select * from prescriptionlist where id = ? and state =1;";
        String sql8 = "update prescriptionlist set dose = ? , medfee = ? , curefee = ? , discription = ? where id = ?;";
        String sql9 = "delete from prescriptionlist where id = ?";
        String sql10 = "update register set illness = ? where id = ?";

        Map<Object, Map<String, Object>> medmap = new LinkedHashMap<>();
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);
            Map map = buildMessage.AnyToMap(any);
            int id = (int) map.get("pid");
            map.remove("pid");
            Object regid = map.get("regid");
            map.remove("regid");
            int dose = (int) map.get("dose");
            map.remove("dose");
            String illness = map.get("illness").toString();
            String discription = map.get("discription").toString();
            try {
                // 遍历药品
                BigDecimal medmon = new BigDecimal(0);
                BigDecimal curemon = new BigDecimal(0);

                // 药品id 列表，数量列表
                String[] medName;
                String[] medNumber;
                medName = map.get("medName").toString().split("\\|");
                medNumber = map.get("medNumber").toString().split("\\|");
                PreparedStatement preparedStatement4 = connection.prepareStatement(sql4);
                for (int i = 0; i < medName.length; i++) {
                    BigDecimal number = new BigDecimal(medNumber[i]).setScale(2, BigDecimal.ROUND_FLOOR);
                    // 查询药品信息
                    preparedStatement4.setObject(1, medName[i]);
                    preparedStatement4.executeQuery();
                    ResultSet resultSet = preparedStatement4.getResultSet();
                    resultSet.last();
                    int n = resultSet.getRow();
                    if (n == 1) {
                        // 重构药品map
                        Map<String, Object> t = new HashMap<>();
                        BigDecimal outprice = resultSet.getBigDecimal("outPrice");
                        String type = (String) resultSet.getObject("type");
                        t.put("number", number);
                        t.put("name", resultSet.getObject("name"));
                        t.put("inprice", resultSet.getBigDecimal("inprice"));
                        t.put("outprice", outprice);
                        t.put("type", type);
                        t.put("itemCode", resultSet.getObject("itemCode"));
                        t.put("itemGrade", resultSet.getObject("itemGrade"));
                        t.put("name2", resultSet.getString("name2"));
                        t.put("unitName", resultSet.getObject("unitName"));
                        t.put("factoryName", resultSet.getObject("factoryName"));
                        t.put("feetypename", resultSet.getObject("feetypename"));
                        t.put("id", resultSet.getObject("id"));
                        medmap.put(medName[i], t);
                        if (type.equals("药品费"))
                            medmon = medmon.add(number.multiply(outprice));
                        else
                            curemon = curemon.add(number.multiply(outprice));
                    } else
                        throw new SQLException("药品  " + medName[i] + "  不存在");
                }


                if (id == 0) {
                    // 如果处方id == 0,说明这个是一个新的处方
                    // 插入prescriptionlist
                    PreparedStatement preparedStatement5 = connection.prepareStatement(sql5);
                    preparedStatement5.setObject(1, regid);
                    preparedStatement5.setObject(2, dose);
                    preparedStatement5.setObject(3, medmon);
                    preparedStatement5.setObject(4, curemon);
                    preparedStatement5.setObject(5, discription);
                    preparedStatement5.executeUpdate();

                    // 得到插入的listid
                    PreparedStatement preparedStatement6 = connection.prepareStatement(sql6);
                    preparedStatement6.executeQuery();
                    ResultSet re = preparedStatement6.getResultSet();
                    re.last();
                    id = re.getInt("last_id");
                } else {
                    // 如果处方id != 0 ，说明是一个修改后的处方
                    // 1. 先判断处方是否收费，收费了就不能进行修改
                    PreparedStatement preparedStatement7 = connection.prepareStatement(sql7);
                    preparedStatement7.setObject(1, id);
                    preparedStatement7.executeQuery();
                    ResultSet resultSet = preparedStatement7.getResultSet();
                    resultSet.last();
                    int n = resultSet.getRow();
                    if (n == 1) {
                        // 如果查询到了记录就说明有符合条件的,更新处方信息
                        PreparedStatement preparedStatement8 = connection.prepareStatement(sql8);
                        preparedStatement8.setObject(1, dose);
                        preparedStatement8.setObject(2, medmon);
                        preparedStatement8.setObject(3, curemon);
                        preparedStatement8.setObject(4, discription);
                        preparedStatement8.setObject(5, id);
                        preparedStatement8.executeUpdate();

                    } else {
                        // 没有处方记录或者已经收费
                        throw new SQLException("处方已被收费，或者不存在处方记录");
                    }
                }

                // 插入处方，无论是新的还是旧的处方都先删了
                // 1. 删除原处方
                PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
                preparedStatement3.setObject(1, id);
                preparedStatement3.executeUpdate();

                if (medmap.keySet().size() == 0) {
                    // 如果没有药
                    PreparedStatement preparedStatement9 = connection.prepareStatement(sql9);
                    preparedStatement9.setObject(1, id);
                    preparedStatement9.executeUpdate();
                    connection.commit();
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPrescriptions", 1, "插入成功"));
                    return;
                }


                // 2. 循环插入
                // `listid`, `medicineid`,itemCode,itemGrade, `medname`,`medname2`, `inprice`, `outprice`, `type`,unitName,factoryName,feetypeName,number
                Map<String, Object> temp = new HashMap<>();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                for (Object key : medmap.keySet()) {
                    Map m = medmap.get(key);
                    preparedStatement.setObject(1, id);
                    preparedStatement.setObject(2, m.get("id"));
                    preparedStatement.setObject(3, m.get("itemCode"));
                    preparedStatement.setObject(4, m.get("itemGrade"));
                    preparedStatement.setObject(5, key);
                    preparedStatement.setObject(6, m.get("name2"));
                    preparedStatement.setObject(7, m.get("inprice"));
                    preparedStatement.setObject(8, m.get("outprice"));
                    preparedStatement.setObject(9, m.get("type"));
                    preparedStatement.setObject(10, m.get("unitName"));
                    preparedStatement.setObject(11, m.get("factoryName"));
                    preparedStatement.setObject(12, m.get("feetypename"));
                    preparedStatement.setObject(13, m.get("number"));
                    preparedStatement.executeUpdate();
                }

                // 插入一个(名为甘草的)凑整费，药品id为0就是凑整费！！！
                preparedStatement.setObject(1, id);
                preparedStatement.setObject(2, 0);
                preparedStatement.setObject(3, "Y-0229");
                preparedStatement.setObject(4, "甲类");
                preparedStatement.setObject(5, "甘草");
                preparedStatement.setObject(6, "甘草");
                preparedStatement.setObject(7, 0);
                preparedStatement.setObject(8, 0.01);
                preparedStatement.setObject(9, "药品费");
                preparedStatement.setObject(10, "克");
                preparedStatement.setObject(11, "凑整费");
                preparedStatement.setObject(12, "凑整费");
                preparedStatement.setObject(13, 0);
                preparedStatement.executeUpdate();

                // 更新病人的临床诊断
                PreparedStatement preparedStatement10 = connection.prepareStatement(sql10);
                preparedStatement10.setObject(1, illness);
                preparedStatement10.setObject(2, regid);
                preparedStatement10.executeUpdate();

                connection.commit();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPrescriptions", 1, "插入成功"));
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPrescriptions", -1, "保存处方失败，" + e.getMessage()));

            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (Exception e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addPrescriptions", -1, "插入失败," + e.getMessage()));
            e.printStackTrace();
        }
    }

    // 药品出入库
    synchronized public static void MedicineEnterAndOut(SelectionKey selectionKey, Any any) {

        String sql = "UPDATE medicine SET stock=? WHERE name= ?;";
        String sql2 = "SELECT max(id) FROM stockrecord;";
        String sql3 = "INSERT INTO `stockrecord` (id, medname, outprice, inprice, number, feetypename, factoryname, positionId,iscount,date) values (?,?,?,?,?,?,?,?,?,?);";
        String sql5 = "SELECT * FROM medicine_view where name = ? and type like '药品费';";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                connection.setAutoCommit(false);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
                PreparedStatement preparedStatement5 = connection.prepareStatement(sql5);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement2.executeQuery();
                ResultSet resultSet1 = preparedStatement2.getResultSet();
                resultSet1.last();
                userStatement user = (userStatement) selectionKey.attachment();
                int positionId = user.getId();
                int id = resultSet1.getInt("max(id)") + 1;
                map.remove("positionId");
                Date date = new Date();

                // 一个个药品进行插入盘点
                for (Object key : map.keySet()) {
                    preparedStatement5.setObject(1, key);
                    preparedStatement5.executeQuery();
                    resultSet1 = preparedStatement5.getResultSet();
                    resultSet1.last();
                    // 没有找到药品，那就抛出异常
                    if (resultSet1.getRow() != 1)
                        throw new SQLException();
                    BigDecimal inprice = resultSet1.getBigDecimal("inprice");
                    BigDecimal outprice = resultSet1.getBigDecimal("outprice");
                    BigDecimal stock = resultSet1.getBigDecimal("stock");
                    String feetypename = resultSet1.getString("feetypename");
                    String factoryname = resultSet1.getString("factoryname");
                    BigDecimal num = new BigDecimal(map.get(key).toString());
                    // 真实库存
                    BigDecimal trueStock = stock.add(num);

                    // 插入记录
                    preparedStatement3.setObject(1,id);
                    preparedStatement3.setObject(2,key);
                    preparedStatement3.setObject(3,outprice);
                    preparedStatement3.setObject(4,inprice);
                    preparedStatement3.setObject(5,num);
                    preparedStatement3.setObject(6,feetypename);
                    preparedStatement3.setObject(7,factoryname);
                    preparedStatement3.setObject(8,positionId);
                    preparedStatement3.setObject(9,0);
                    preparedStatement3.setObject(10,date);
                    preparedStatement3.executeUpdate();

                    // 更新药品数量
                    preparedStatement.setObject(1,trueStock);
                    preparedStatement.setObject(2,key);
                    preparedStatement.executeUpdate();
                }

                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("MedicineEnterAndOut", id, "成功"));
                connection.commit();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("MedicineEnterAndOut", -1, "添加失败"));
                connection.rollback();
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("MedicineEnterAndOut", -1, "添加失败"));
            e.printStackTrace();
        }

    }

    // 作废处方
    public static void resetUsefulByPreId(SelectionKey selectionKey, Any any) {
        String sql = "UPDATE `prescriptionlist` SET `state`='-1' WHERE orderid=? ;";
        String sql2 = "update medicine set stock = stock + ? where id = ? ;";
        String sql3 = "SELECT medicineid,sum(number) number FROM prescription where ";
        String sql4 = "SELECT * FROM `order` where id = ?;";
        String sql5 = "UPDATE vip SET remain = remain + ? , present = present+ ? ,consume = consume - ? - ?  where id = ?";
        String sql6 = "SELECT * FROM prescriptionlist where orderid= ?;";
        String sql7 = "";


        try {
            Map map = buildMessage.AnyToMap(any);
            Object id = map.get("orderid");
            Connection connection = C3P0ConnentionProvider.getConnection();
            connection.setAutoCommit(false);

            try {
                PreparedStatement p6 = connection.prepareStatement(sql6);
                p6.setObject(1, id);
                p6.executeQuery();
                ResultSet re = p6.getResultSet();
                boolean one = true;
                boolean mark = true;
                while (re.next()) {
                    int state = re.getInt("state");
                    int pid = re.getInt("id");
                    if (state == 3) {
                        mark = false;
                        // 拼接补药查询语句
                        if (one) {
                            one = false;
                            sql3 = sql3 + " listid = " + pid;
                        } else {
                            sql3 = sql3 + " or listid = " + pid;
                        }
                    }
                }
                if (mark)
                    sql3 = sql3 + " 1=0";
                sql3 = sql3 + " group by medicineid";

                Map<Integer, Float> medicine = new HashMap<>();
                PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
                preparedStatement3.executeQuery();
                ResultSet resultSet = preparedStatement3.getResultSet();
                while (resultSet.next()) {
                    medicine.put(resultSet.getInt("medicineid"), resultSet.getFloat("number"));
                }

                PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                // 循环补药
                for (Integer temp : medicine.keySet()) {
                    preparedStatement2.setObject(1, medicine.get(temp));
                    preparedStatement2.setObject(2, temp);
                    preparedStatement2.executeUpdate();
                }

                // 返还vip充值的钱
                PreparedStatement preparedStatement4 = connection.prepareStatement(sql4);
                preparedStatement4.setObject(1, id);
                preparedStatement4.executeQuery();
                resultSet = preparedStatement4.getResultSet();
                resultSet.last();
                if (resultSet.getRow() != 0) {
                    Object vipId = resultSet.getObject("vipId");
                    Object card = resultSet.getObject("card");
                    Object given = resultSet.getObject("given");
                    Object cash = resultSet.getObject("cash");
                    Object other = resultSet.getObject("other");
                    if (vipId != null) {
                        PreparedStatement preparedStatement5 = connection.prepareStatement(sql5);
                        preparedStatement5.setObject(1, card);
                        preparedStatement5.setObject(2, given);
                        preparedStatement5.setObject(3, cash);
                        preparedStatement5.setObject(4, other);
                        preparedStatement5.setObject(5, vipId);

                        preparedStatement5.executeUpdate();
                    }
                }

                // 设置已经作废
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, id);
                preparedStatement.executeUpdate();

                //

                connection.commit();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("resetUsefulByPreId", 1, "作废成功"));
            } catch (SQLException e) {
                connection.rollback();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("resetUsefulByPreId", -1, "作废失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("resetUsefulByPreId", -1, "作废失败"));
            e.printStackTrace();
        }

    }

    // 得到最大的库存id
    public static void maxIdFromStockrecord(SelectionKey selectionKey) {
        String sql = "SELECT max(id) FROM stockrecord;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                resultSet.last();
                int id = resultSet.getInt("max(id)");
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("maxIdFromStockrecord", id + 1, "查询成功"));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("maxIdFromStockrecord", 0, "查询失败"));
                e.printStackTrace();
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("maxIdFromStockrecord", 0, "查询失败"));
            e.printStackTrace();
        }

    }


    // 盘点药库
    synchronized public static void resetStock(SelectionKey selectionKey, Any any) {
        // 1. 添加
        String sql = "UPDATE medicine SET stock=? WHERE name= ?;";
        String sql2 = "SELECT max(id) FROM stockrecord;";
        String sql3 = "INSERT INTO `stockrecord` (id, medname, outprice, inprice, number, feetypename, factoryname, positionId,iscount,date) values (?,?,?,?,?,?,?,?,?,?);";
        String sql5 = "SELECT * FROM medicine_view where name = ? and type like '药品费';";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                connection.setAutoCommit(false);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
                PreparedStatement preparedStatement3 = connection.prepareStatement(sql3);
                PreparedStatement preparedStatement5 = connection.prepareStatement(sql5);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement2.executeQuery();
                ResultSet resultSet1 = preparedStatement2.getResultSet();
                resultSet1.last();
                userStatement user = (userStatement) selectionKey.attachment();
                int positionId = user.getId();
                int id = resultSet1.getInt("max(id)") + 1;
                map.remove("positionId");
                Date date = new Date();

                // 一个个药品进行插入盘点
                for (Object key : map.keySet()) {
                    preparedStatement5.setObject(1, key);
                    preparedStatement5.executeQuery();
                    resultSet1 = preparedStatement5.getResultSet();
                    resultSet1.last();
                    // 没有找到药品，那就抛出异常
                    if (resultSet1.getRow() != 1)
                        throw new SQLException();
                    BigDecimal inprice = resultSet1.getBigDecimal("inprice");
                    BigDecimal outprice = resultSet1.getBigDecimal("outprice");
                    BigDecimal stock = resultSet1.getBigDecimal("stock");
                    String feetypename = resultSet1.getString("feetypename");
                    String factoryname = resultSet1.getString("factoryname");
                    BigDecimal num = new BigDecimal(map.get(key).toString());
                    // 真实库存
                    BigDecimal trueStock = stock.add(num);

                    // 插入记录
                    preparedStatement3.setObject(1,id);
                    preparedStatement3.setObject(2,key);
                    preparedStatement3.setObject(3,outprice);
                    preparedStatement3.setObject(4,inprice);
                    preparedStatement3.setObject(5,num);
                    preparedStatement3.setObject(6,feetypename);
                    preparedStatement3.setObject(7,factoryname);
                    preparedStatement3.setObject(8,positionId);
                    preparedStatement3.setObject(9,1);
                    preparedStatement3.setObject(10,date);
                    preparedStatement3.executeUpdate();

                    // 更新药品数量
                    preparedStatement.setObject(1,trueStock);
                    preparedStatement.setObject(2,key);
                    preparedStatement.executeUpdate();
                }

                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("resetStock", id, "成功"));
                connection.commit();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("resetStock", -1, "添加失败"));
                connection.rollback();
                e.printStackTrace();
            } finally {
                connection.setAutoCommit(true);
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("resetStock", -1, "添加失败"));
            e.printStackTrace();
        }

    }

    // 对设置的 填，删，改
    public static void addSetting(SelectionKey selectionKey, Any any) {
        Map map = buildMessage.AnyToMap(any);
        String view = (String) map.get("view");
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            String sql = "INSERT INTO " + view + " VALUES(?,?);";
            PreparedStatement preapred = connection.prepareStatement(sql);
            preapred.setObject(1, map.get("id"));
            preapred.setObject(2, map.get("name"));
            try {
                preapred.executeUpdate();
                ResultSet resultSet = preapred.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addSetting", 1, "插入成功"));
                connection.close();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addSetting", -1, "插入失败"));
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addSetting", -1, "数据库连接失败"));
            System.out.println("获取数据库链接失败");
        }
    }

    public static void updateSetting(SelectionKey selectionKey, Any any) {
        Map map = buildMessage.AnyToMap(any);
        String view = (String) map.get("view");
        if (!judgeView(view)) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateSetting", -2, "表格权限不够"));
            return;
        }
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            String sql = "UPDATE " + view + " SET name = ? where id = ?;";
            PreparedStatement preapred = connection.prepareStatement(sql);
            preapred.setObject(1, map.get("name"));
            preapred.setObject(2, map.get("id"));
            try {
                preapred.executeUpdate();
                ResultSet resultSet = preapred.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateSetting", 1, "更新成功"));
                connection.close();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updateSetting", -1, "更新失败"));
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("updataSetting", -1, "数据库连接失败"));
            System.out.println("获取数据库链接失败");
        }
    }

    public static void deleteSetting(SelectionKey selectionKey, Any any) {
        Map map = buildMessage.AnyToMap(any);
        String view = (String) map.get("view");
        if (!judgeView(view)) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteSetting", -2, "表格权限不够"));
            return;
        }
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            String sql = "DELETE FROM " + view + " WHERE id =? ;";
            PreparedStatement preapred = connection.prepareStatement(sql);
            preapred.setObject(1, map.get("id"));
            try {
                preapred.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteSetting", 1, "删除成功"));
                connection.close();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteSetting", -1, "删除失败"));
                connection.close();
            }

        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteSetting", -1, "数据库连接失败"));
            System.out.println("获取数据库链接失败");
        }
    }

    // 返回所有用户
    public static void getPosition(SelectionKey selectionKey) {
        String sql = "SELECT * FROM position;";

        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getPosition", resultSet));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPosition", -1, "db fail"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPosition", -1, "connection fail"));
            e.printStackTrace();
        }

    }

    // 返回一个用户信息
    public static void getOnePosition(SelectionKey selectionKey, Any any) {
        Map map = buildMessage.AnyToMap(any);
        String name = (String) map.get("name");
        String sql = "SELECT * FROM position where name like ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, name);
            try {
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getOnePosition", resultSet));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getOnePosition", -1, "fail"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getOnePosition", -1, "fail"));
            e.printStackTrace();
        }

    }

    // 修改用户信息
    public static void changeMessage(SelectionKey selectionKey, Any any) {
        Map map = buildMessage.AnyToMap(any);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE `position` SET ");
        String id = (String) map.get("number");
        map.remove("number");
        int i = 0;
        for (Object o : map.keySet()) {
            if (i++ != map.size() - 1)
                stringBuilder.append(" " + o + " = '" + map.get(o) + "',");
            else
                stringBuilder.append(" " + o + " = '" + map.get(o) + "' ");
        }
        stringBuilder.append(" where number = ?;");
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(stringBuilder.toString());
            preparedStatement.setObject(1, id);
            try {
                preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("changeMessage", 1, "success"));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("changeMessage", -1, "db fail"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getPosition", -1, "connection fail"));
            e.printStackTrace();
        }
    }

    // 按照部门收费汇总
    public static void getCountByDepart(SelectionKey selectionKey, Any any) {
        String sql = "call count_depart_procedure(?, ?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("start"));
                preparedStatement.setObject(2, map.get("end"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                if (resultSet == null)
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCountByDepart", -1, "没有数据"));
                else
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getCountByDepart", preparedStatement.getResultSet()));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCountByDepart", -1, "失败"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCountByDepart", -1, "失败"));
            e.printStackTrace();
        }
    }

    // 按照医生收费汇总
    public static void getCountByDoctor(SelectionKey selectionKey, Any any) {
        String sql = "call count_doctor_procedure(?, ?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                Map map = buildMessage.AnyToMap(any);
                preparedStatement.setObject(1, map.get("start"));
                preparedStatement.setObject(2, map.get("end"));
                preparedStatement.executeQuery();
                ResultSet resultSet = preparedStatement.getResultSet();
                if (resultSet == null)
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCountByDoctor", -1, "没有数据"));
                else
                    ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getCountByDoctor", preparedStatement.getResultSet()));
                connection.close();
            } catch (SQLException e) {
                connection.close();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCountByDoctor", -1, "失败"));
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getCountByDoctor", -1, "失败"));
            e.printStackTrace();
        }
    }

    // 添加一个用户
    public static void addUser(SelectionKey selectionKey, Any any) {
        String sql = "INSERT INTO `position` (`name`, `number`, `password`, `power`) VALUES (?, ?, ?, ?);";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            Map map = buildMessage.AnyToMap(any);
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("name"));
                preparedStatement.setObject(2, map.get("number"));
                preparedStatement.setObject(3, map.get("password"));
                preparedStatement.setObject(4, map.get("power"));
                preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addUser", 1, "success"));
                connection.close();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addUser", -1, "fail"));
                connection.close();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("addUser", -1, "fail"));
            e.printStackTrace();
        }

    }

    // 删除一个用户
    public static void deleteUser(SelectionKey selectionKey, Any any) {
        String sql = "DELETE FROM `position` WHERE `id`=?;";
        Map map = buildMessage.AnyToMap(any);
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.executeUpdate();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteUser", 1, "success"));
                connection.close();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteUser", -1, "fail"));
                connection.close();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("deleteUser", -1, "fail"));
            e.printStackTrace();
        }
    }

    // 得到损益日期
    public static void getProfitDate(SelectionKey selectionKey) {
        String sql = "SELECT id,date FROM getandlose_view group by id  order by id desc ;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getProfitDate", preparedStatement.getResultSet()));
                connection.close();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getProfitDate", -1, "fail"));
                connection.close();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getProfitDate", -1, "fail"));
            e.printStackTrace();
        }
    }

    // 通过日期得到损益详情
    public static void getProfitByDate(SelectionKey selectionKey, Any any) {
        String sql = "SELECT * FROM getandlose_view WHERE id = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            try {
                Map map = buildMessage.AnyToMap(any);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setObject(1, map.get("id"));
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getProfitByDate", preparedStatement.getResultSet()));
                connection.close();
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getProfitByDate", -1, "fail"));
                connection.close();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getProfitByDate", -1, "fail"));
            e.printStackTrace();
        }
    }

    // 通过日期得到订单详情
    public static void getDatePres(SelectionKey selectionKey, Any any) {
        String sql = "SELECT pid, rgid,orderid, name, patientid, fee, departname, doctorname, regdate, dose, medfee, curefee, orderid, medicinediscount, medcurediscount, ifnull(card,0) card, ifnull(given,0) given, cash, other,ifnull(insurance,0) insurance, date, state,ifnull(insuranceNumber,'非医保处方') insuranceNumber,sum FROM pre_order_view " +
                "where regdate between ? and ? or date between ? and ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("start") + " 00:00:00");
            preparedStatement.setObject(2, map.get("end") + " 23:59:59");

            preparedStatement.setObject(3, map.get("start") + " 00:00:00");
            preparedStatement.setObject(4, map.get("end") + " 23:59:59");

            try {
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getDatePres", preparedStatement.getResultSet()));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDatePres", -1, "fail"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getDatePres", -1, "fail"));
            e.printStackTrace();
        }

    }

    // 得到某个人的处方记录
    public static void getOnesPres(SelectionKey selectionKey, Any any) {
        String sql = "SELECT  distinct(prescriptionid), departname, doctorname, date FROM  prerecord_view  WHERE  patientid = ?;";
        try {
            Connection connection = C3P0ConnentionProvider.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            Map map = buildMessage.AnyToMap(any);
            preparedStatement.setObject(1, map.get("patientid"));
            try {
                preparedStatement.executeQuery();
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getResult("getOnesPres", preparedStatement.getResultSet()));
            } catch (SQLException e) {
                ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getOnesPres", -1, "fail"));
            } finally {
                connection.close();
            }
        } catch (SQLException e) {
            ThreadHandlerChannel.sendMessage(selectionKey, buildMessage.getFeedback("getOnesPres", -1, "fail"));
            e.printStackTrace();
        }

    }

    public static List<List<Object>> resultToList(ResultSet resultSet) throws SQLException {
        int column = resultSet.getMetaData().getColumnCount();
        resultSet.last();
        int row = resultSet.getRow();
        resultSet.beforeFirst();
        List<List<Object>> lists = new ArrayList<>();

        while (resultSet.next()) {
            List<Object> temp = new ArrayList<>();
            for (int i = 0; i < column; i++)
                temp.add(resultSet.getObject(i + 1));
            lists.add(temp);
        }
        return lists;
    }

    public static boolean judgeView(String v) {
        String[] view =
                {
                        "unit",
                        "regtype",
                        "factory",
                        "department",
                        "feetype",
                        "integrate"
                };
        for (String temp : view) {
            if (temp.equals(v))
                return true;
        }
        return false;
    }

}
