package protocol;

import java.util.Date;

public class message
{
    public static final int MaxNumbr = 100;

    public static final int TestChanel = 0;
    public static final int LoginSuccess = 1;
    public static final int LoginFaild = 2;


    public static void main(String[] arg)
    {
        Date date = new Date();
        System.out.println(date.toString());
    }
}
