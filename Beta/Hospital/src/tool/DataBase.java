package tool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DataBase {
    private static Connection con = null;
    private static Statement sql;

    // 连接数据库
    public static void connect() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String url = "jdbc:mysql://39.108.133.117:3306/hospital?useSSL=true&CharacterEncoding=utf-8";
        String user = "root";
        String password = "Aa123456..";
        con = DriverManager.getConnection(url, user, password);
        sql = con.createStatement();
        System.out.println("DataBase has been connected.");
    }

    // 更新操作
    public static void update(String query) throws Exception {
        sql.executeUpdate(query);
    }

    // 查询操作，将数据表转化为对象数组
    public static Object[][] select(String query) throws Exception {
        ResultSet rs = sql.executeQuery(query);
        rs.last();
        int rows = rs.getRow();
        int columns = rs.getMetaData().getColumnCount();
        Object[][] objects = new Object[rows][columns];
        rs.beforeFirst();
        for (int i = 0; rs.next(); i++) {
            for (int j = 0; j < columns; j++) {
                objects[i][j] = rs.getObject(j + 1);
            }
        }
        return objects;
    }

    // 关闭连接
    public static void close() throws Exception {
        con.close();
        System.out.println("Database has been closed.");
    }
}
