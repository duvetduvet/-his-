package DataBase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager
{

    //使用ThreadLocal保存Connection变量
    private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>();

    /**
     * 连接Connection
     *
     * @return
     */
    public static Connection getConnection()
    {
        //ThreadLocal取得当前线程的connection
        Connection conn = connectionHolder.get();
        //如果ThreadLocal没有绑定相应的Connection，创建一个新的Connection，
        //并将其保存到本地线程变量中。
        if (conn == null)
        {
            try
            {
                conn = C3P0ConnentionProvider.getConnection();
                //将当前线程的Connection设置到ThreadLocal
                connectionHolder.set(conn);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        return conn;

    }

    /**
     * 关闭Connection，清除集合中的Connection
     */
    public static void closeConnection()
    {
        //ThreadLocal取得当前线程的connection
        Connection conn = connectionHolder.get();
        //当前线程的connection不为空时，关闭connection.
        if (conn != null)
        {
            try
            {
                conn.close();
                //connection关闭之后，要从ThreadLocal的集合中清除Connection
                connectionHolder.remove();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

        }
    }
}
