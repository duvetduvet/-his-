package tool;

import java.awt.*;
import java.awt.print.*;
import java.math.BigDecimal;

public class Print implements Printable
{
    /**
     * Graphic指明打印的图形环境
     * PageFormat指明打印页格式（页面大小以点为计量单位，1点为1英才的1/72，1英寸为25.4毫米。A4纸大致为595×842点）
     * pageIndex指明页号
     */

    private char str[];
    private String str1[] = new String[9];



    public Print(String patientName, String time, String registerId, BigDecimal medMon, BigDecimal cureMon,
                 BigDecimal other, String doctorName, String positionName)
    {
        str = "零零零零零零零".toCharArray();
        BigDecimal sum;
        medMon = medMon.setScale(2,BigDecimal.ROUND_FLOOR);
        cureMon = cureMon.setScale(2,BigDecimal.ROUND_FLOOR);
        sum = medMon.add(cureMon);

        str1[0] = patientName;
        str1[1] = time;
        str1[2] = registerId;
        str1[3] = medMon.toString();
        str1[4] = cureMon.toString();
        str1[5] = other.toString();

        try
        {
            toBig(sum);
            str1[6] = String.valueOf(sum);
            str1[7] = doctorName;
            str1[8] = positionName;
            test();
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            Tool.warning("打印金额过多！！！");
        }


    }

    public int print(Graphics gra, PageFormat pf, int pageIndex) throws PrinterException
    {
        System.out.println("pageIndex=" + pageIndex);
        Graphics2D g2 = (Graphics2D) gra;
        //设置打印颜色为黑色
        g2.setColor(Color.black);
        //打印起点坐标
        double x = pf.getImageableX();
        double y = pf.getImageableY();

        double x1 = 80;
        double y1 = 70;
        switch (pageIndex)
        {
            case 0:
                //设置打印字体（字体名称、样式和点大小）（字体名称可以是物理或者逻辑名称）
                //Java平台所定义的五种字体系列：Serif、SansSerif、Monospaced、Dialog 和 DialogInput
                Font font = new Font("新宋体", Font.PLAIN, 9);
                g2.setFont(font);//设置字体
                System.out.println("x=" + x);
                g2.drawString(str1[2], (float) (x + 90), (float) (y + 70));
                g2.drawString(str1[1], (float) (x + 175), (float) (y + 70));
                g2.drawString(str1[0], (float) (x + 95), (float) (y + 90));
                g2.drawString(str1[3], (float) (x + 95), (float) (y + 165));
                g2.drawString(str1[4], (float) (x + 260), (float) (y + 165));
                g2.drawString(str1[5], (float) (x + 260), (float) (y + 182));
                g2.drawString(str1[6], (float) (x + 270), (float) (y + 200));
                g2.drawString(str1[7], (float) (x + 100), (float) (y + 215));
                g2.drawString(str1[8], (float) (x + 250), (float) (y + 215));
                for (int i = 0, j = 0; i < str.length; i++, j += 25)
                {
                    g2.drawString(String.valueOf(str[i]), (float) (x + 100 + j), (float) (y + 200));
                }
                g2.drawString(".",(float)(x+336),(float)(y+260));
                return PAGE_EXISTS;
            default:
                return NO_SUCH_PAGE;
        }
    }

    public void toBig(BigDecimal num)
    {
        num = num.multiply(new BigDecimal(100)).setScale(0,BigDecimal.ROUND_FLOOR);
        int num2 = num.intValueExact();
        char[] str2 = String.valueOf(num2).toCharArray();
        int b = str.length - str2.length;
        for (int i = str2.length - 1; i >= 0; i--)
        {
            switch (str2[i])
            {
                case '0':
                    str[b + i] = '零';
                    break;
                case '1':
                    str[b + i] = '壹';
                    break;
                case '2':
                    str[b + i] = '贰';
                    break;
                case '3':
                    str[b + i] = '叁';
                    break;
                case '4':
                    str[b + i] = '肆';
                    break;
                case '5':
                    str[b + i] = '伍';
                    break;
                case '6':
                    str[b + i] = '陆';
                    break;
                case '7':
                    str[b + i] = '柒';
                    break;
                case '8':
                    str[b + i] = '捌';
                    break;
                case '9':
                    str[b + i] = '玖';
                    break;
            }
        }
    }

    public void test()
    {
        //    通俗理解就是书、文档
        Book book = new Book();
        //    设置成竖打
        PageFormat pf = new PageFormat();
        pf.setOrientation(PageFormat.PORTRAIT);
        //    通过Paper设置页面的空白边距和可打印区域。必须与实际打印纸张大小相符。
        Paper p = new Paper();
        p.setSize(336, 260);//纸张大小
        p.setImageableArea(0, 0, 336, 260);//A4(595 X 842)设置打印区域，其实0，0应该是72，72，因为A4纸的默认X,Y边距是72
        pf.setPaper(p);
        // 把 PageFormat 和 Printable 添加到书中，组成一个页面
        book.append(this, pf);
        //获取打印服务对象
        PrinterJob job = PrinterJob.getPrinterJob();
        // 设置打印类
        job.setPageable(book);
        try
        {
            boolean a = job.printDialog();
            if (a)
            {
                job.print();
            }
        }
        catch (PrinterException e)
        {
            e.printStackTrace();
        }
    }
}