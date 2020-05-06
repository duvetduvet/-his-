package user;

import DataBase.C3P0ConnentionProvider;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mysql.jdbc.Driver;

import java.beans.PropertyVetoException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.List;

public class userStatement
{
    private int id = 0;
    private String power = null;
    private List<String> list;

    public boolean mark = true;
    public ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
    public ByteBuffer buffer = ByteBuffer.allocate(10240);
    public int index = 0;
    public byte[] bytes;
    public int size = 0;

    public boolean isLogin()
    {
        if (id != 0)
        {
            return true;
        }
        else
            return false;
    }

    public int getId()
    {
        if (isLogin())
            return id;
        else
            return 0;
    }

    public void setPower(String power)
    {
        this.power = power;
    }

    public void setList(List<String> list)
    {
        this.list = list;
    }

    public String getPower()
    {
        return power;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public boolean judge(String functionName)
    {
        if (isLogin())
        {
            if (list == null)
                return false;
            if (list.contains(functionName))
                return true;
            else
                return false;
        }
        else
            return false;
    }

    public static void main(String[] atr) throws PropertyVetoException, SQLException
    {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl("jdbc:mysql://47.93.186.15:3306/hospital?useUnicode=true&characterEncoding=utf-8&useSSL=false");
        ds.setUser("root");
        ds.setPassword("123456");
        ds.setDriverClass("com.mysql.jdbc.Driver");
        ds.setAcquireIncrement(5);
        ds.setInitialPoolSize(20);
        ds.setMinPoolSize(2);
        ds.setMaxPoolSize(50);
        Connection con = ds.getConnection();
        PreparedStatement preapred = con.prepareStatement("select * from staff where id = ? and password = ?");
        preapred.setInt(1, 1);
        preapred.setString(2, "123456");

        preapred.executeQuery();
        ResultSet resultSet = preapred.getResultSet();
        resultSet.last();
        System.out.println(resultSet.getRow());

        System.out.println("con:" + con);
        con.close();
        new C3P0ConnentionProvider();
    }
}
