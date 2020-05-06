package DataBase;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.mysql.jdbc.Driver;
import protocol.Protocol;

public class C3P0ConnentionProvider
{
    private static ComboPooledDataSource ds;

    static
    {
        ds = new ComboPooledDataSource();
        try
        {
            InputStream inputStream = C3P0ConnentionProvider.class.getClassLoader().
                    getResourceAsStream("jdbc.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            ds.setJdbcUrl(properties.getProperty("url"));
            ds.setUser(properties.getProperty("user"));
            ds.setPassword(properties.getProperty("password"));
            ds.setDriverClass("com.mysql.jdbc.Driver");
            ds.setAcquireIncrement(2);
            ds.setInitialPoolSize(10);
            ds.setMinPoolSize(2);
            ds.setMaxPoolSize(300);
            ds.setMaxIdleTime(60);
            ds.setNumHelperThreads(3);
            ds.setIdleConnectionTestPeriod(60);
            ds.setTestConnectionOnCheckin(true);
        }
        catch (PropertyVetoException e)
        {
            e.printStackTrace();
            System.out.println("Null com.mysql.jdbc.Driver!");
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            System.out.println("配置文件未正常加载");
        }


    }

    public static synchronized Connection getConnection() throws SQLException
    {
        final Connection conn = ds.getConnection();
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return conn;
    }

    public static void main(String[] arg) throws SQLException
    {
       /*Connection connection = C3P0ConnentionProvider.getConnection();
        System.out.println(connection);
        // SELECT * FROM hospital.doctor;
        PreparedStatement preapred = connection.prepareStatement("SELECT * FROM hospital.doctor");
        preapred.execute();
        ResultSet resultSet = preapred.getResultSet();
        List<Object> list;
        list = Protocol.convertList(resultSet);

        connection.close();*/
        Connection connection = C3P0ConnentionProvider.getConnection();
        String sql = "INSERT INTO doctor (`id`, `name`, `sex`, `birth`, `identity`, `phone`, `address`, `departmentId`) VALUES (?,?,?,?,?,?,?,?)";
        PreparedStatement preapred = connection.prepareStatement(sql);
        preapred.setObject(1, null);
        preapred.setObject(2, null);
        preapred.setObject(3, null);
        preapred.setObject(4, null);
        preapred.setObject(5, null);
        preapred.setObject(6, null);
        preapred.setObject(7, null);
        preapred.setObject(8, null);
        preapred.executeUpdate();
        preapred.getResultSet();
        connection.close();
    }
}