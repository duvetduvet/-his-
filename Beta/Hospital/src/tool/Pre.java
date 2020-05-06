package tool;

import com.google.protobuf.Any;
import org.omg.PortableInterceptor.INACTIVE;

import java.awt.*;
import java.awt.print.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class Pre implements Printable
{
    private int pid = -1;
    private int rgid = -1;
    private String patientname = null;
    private String sex = null;
    private String docname = null;
    private String depname = null;
    private int dose = 0;

    public String getMedical_Diagnosis() {
        return medical_Diagnosis;
    }

    public void setMedical_Diagnosis(String medical_Diagnosis) {
        this.medical_Diagnosis = medical_Diagnosis;
    }

    public String getDoctor_Advice() {
        return doctor_Advice;
    }

    public void setDoctor_Advice(String doctor_Advice) {
        this.doctor_Advice = doctor_Advice;
    }

    private int age = 0;
    private Date date = null;           // 处方日期
    private BigDecimal money = BigDecimal.ZERO;
    private int state = 0;
    private  String medical_Diagnosis = null; //临床诊断
    private  String doctor_Advice = null; //医嘱
    private  String doctor_Advice_1 = null;
    private String[] hide = {"袋子","加工费","济配","凑整费"};
    private List<medicine> medcineList = new ArrayList<>();

    public int getPid()
    {
        return pid;
    }

    public int getRgid()
    {
        return rgid;
    }

    public String getPatientname()
    {
        return patientname;
    }

    public String getSex()
    {
        return sex;
    }

    public String getDocname()
    {
        return docname;
    }

    public String getDepname()
    {
        return depname;
    }

    public int getDose()
    {
        return dose;
    }

    public Date getDate()
    {
        return date;
    }

    public BigDecimal getMoney()
    {
        return money;
    }

    public int getState()
    {
        return state;
    }

    public List<medicine> getMedcineList()
    {
        return medcineList;
    }

    public Pre(int pid, int rgid, String patientname, String sex, int age, String docname, String depname, int dose, BigDecimal money,
               int state, Date date ,String medical_Diagnosis,String doctor_Advice)
    {
        this.pid = pid;
        this.patientname = patientname;
        this.sex = sex;
        this.docname = docname;
        this.depname = depname;
        this.dose = dose;
        this.money = money;
        this.date = date;
        this.rgid = rgid;
        this.state = state;
        this.age = age;
        this.doctor_Advice = doctor_Advice;
        this.medical_Diagnosis = medical_Diagnosis;

    }

    public Pre(int pid)
    {
        this.pid = pid;
    }

    public void setMedcineList(List<medicine> medcineList)
    {
        this.medcineList = medcineList;
    }

    public void clearMedcineList()
    {
        medcineList.clear();
    }

    public boolean check()
    {
        if (medcineList == null || medcineList.size() == 0)
            return false;
        return true;
    }

    public void filter()
    {
        List<medicine> newList = new LinkedList<>();
        for(medicine t : medcineList)
        {
            if (t.name.startsWith(hide[0]) || t.name.startsWith(hide[1]) || t.name.startsWith(hide[2]))
                continue;
            else
                newList.add(t);
        }
        medcineList = newList;
    }

    public boolean print()
    {
        if(check() == false)
        {
            Tool.warning(pid + " 药品详情为空！！");
            return false;
        }
        Book book = new Book();
        //    设置成竖打
        PageFormat pf = new PageFormat();
        pf.setOrientation(PageFormat.PORTRAIT);
        //    通过Paper设置页面的空白边距和可打印区域。必须与实际打印纸张大小相符。
        Paper p = new Paper();
        p.setSize(421, 595);//纸张大小
        p.setImageableArea(0, 0, 421, 595);//A4(595 X 842)设置打印区域，其实0，0应该是72，72，因为A4纸的默认X,Y边距是72
        pf.setPaper(p);
        // 把 PageFormat 和 Printable 添加到书中，组成一个页面
        if (medcineList.size() > 42)
        {
            book.append(this, pf, 2);
        }
        else
        {
            book.append(this, pf, 1);
        }
        //获取打印服务对象
        PrinterJob job = PrinterJob.getPrinterJob();
        // 设置打印类
        job.setPageable(book);
        try
        {
            //可以用printDialog显示打印对话框，在用户确认后打印；也可以直接打印
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
        return true;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException
    {
        Component c = null;
        //转换成Graphics2D
        Graphics2D g2 = (Graphics2D) graphics;
        //设置打印颜色为黑色
        g2.setColor(Color.black);
        //打印起点坐标
        double x = pageFormat.getImageableX();
        double y = pageFormat.getImageableY();
        Font font = new Font("华文行楷", Font.BOLD, 25);
        g2.setFont(font);//设置字体
        g2.drawString("江西省赣州济世堂处方笺", (float) (x +10), (float) (y + 60));
        // 过滤药品
        filter();
        if (pageIndex >= 0)
        {
            Font font1 = new Font("新宋体", Font.PLAIN, 14);
            g2.setFont(font1);//设置字体
            BasicStroke bs = new BasicStroke(0.0f);
            g2.setStroke(bs);
            g2.drawLine(300, 20, 400, 20);
            g2.drawString("普 通 处 方", 310, 36);
            g2.drawString(Integer.toString(pid), 345, 58);
            g2.drawLine(300, 20, 300, 80);
            g2.drawLine(300, 80, 400, 80);
            g2.drawLine(400, 20, 400, 80);
            g2.drawLine(300, 38, 400, 38);
            g2.drawLine(300, 62, 400, 62);
            g2.drawString("当 日 有 效", 310, 78);
            g2.drawLine(10, 120, 405, 120);
            g2.drawString("门诊/住院病例号：", 12, 118);
            g2.drawString(Integer.toString(rgid), 130, 118);
            g2.drawString("年   月   日", 310, 118);
            // 分别为年、月、日
            g2.drawString(Integer.toString( date.getYear()+1900),282,118);
            g2.drawString(Integer.toString(date.getMonth()+1),330,118);
            g2.drawString(Integer.toString(date.getDate()),360,118);
            g2.drawLine(10, 144, 405, 144);
            g2.drawString("姓名：         性别：     年龄：   岁    科别： ", 10, 140);
            //姓名
            g2.drawString(patientname, 48, 140);
            //性别
            g2.drawString(sex, 160, 140);
            //年龄
            g2.drawString(Integer.toString(age), 240, 140);
            //科室
            g2.drawString(depname, 340, 140);
            //临床诊断
            g2.drawLine(10, 168, 405, 168);
            g2.drawString("临床诊断：", 10, 164);
            g2.drawString(medical_Diagnosis,90,164);
            g2.drawLine(110, 120, 110, 144);
            g2.drawLine(190, 120, 190, 144);
            g2.drawLine(300, 120, 300, 144);
            g2.drawString("医师：", 20, 532);
            //医生姓名
            g2.drawString(docname, 60, 532);
            g2.drawString("调配：", 145, 532);
            g2.drawString("复核：", 260, 532);
            g2.drawLine(10, 535, 405, 535);
            g2.drawString("药品金额：                                        元", 20, 556);
            //费用
            g2.drawString(money.toString(), 310, 556);
            g2.drawLine(10, 560, 405, 560);
        }
        //剂量
        if (dose > 0)
        {
            Font font2 = new Font("新宋体", Font.PLAIN, 30);
            g2.setFont(font2);//设置字体
            g2.drawString('X' + Integer.toString(dose), 330, 522);
        }
        if (pageIndex == 0)
        {
            //设置打印字体（字体名称、样式和点大小）（字体名称可以是物理或者逻辑名称）
            //Java平台所定义的五种字体系列：Serif、SansSerif、Monospaced、Dialog 和 DialogInput
            Font font3 = new Font("新宋体", Font.PLAIN, 12);
            g2.setFont(font3);//设置字体
            BasicStroke bs = new BasicStroke(0.0f);
            g2.setStroke(bs);
            g2.drawLine(197, 575, 205, 575);
            g2.drawString("1", 208, 578);
            g2.drawLine(218, 575, 226, 575);
            //int[][] j = {{10, 90, 120}, {150, 230, 260}, {290, 370, 400}};
            int[][] j = {{10, 90}, {150, 230}, {290, 370}};
            int[] k = new int[14];
            int temp = 184, q = 0, w = 0;
            for (int i = 0; i < 14; i++)
            {
                k[i] = temp;
                temp = temp + 24;
            }

            for (int i = 0; i<medcineList.size() && i<42; i++)
            {

                if ((i + 1) % 3 == 1)
                    q = 0;
                if ((i + 1) % 3 == 2)
                    q = 1;
                if ((i + 1) % 3 == 0)
                    q = 2;
                if (medcineList.get(i).name.length() < 7)
                    g2.drawString(medcineList.get(i).name, j[q][0], k[w]);
                else
                    g2.drawString(medcineList.get(i).name.substring(0,6) + "..", j[q][0], k[w]);
                String str = Integer.toString((int)(medcineList.get(i).number/dose))+medcineList.get(i).unitname;
                //g2.drawString(Integer.toString((int)(medcineList.get(i).number/dose)), j[q][1], k[w]);
                //g2.drawString(medcineList.get(i).unitname, j[q][2], k[w]);
                g2.drawString(str,j[q][1],k[w]);
                if ((i+1) % 3 == 0 )
                {
                    w = w + 1;
                }
            }

            int n = 0;
            if(medcineList.size()%3==0)
                n = (int) (medcineList.size() / 3);
            else
                n = (int) (medcineList.size() / 3)+1;
            int begin = 0;
            int doctor_Advice_length = doctor_Advice.length();
            int end = 32;
            if(n+(int) (doctor_Advice.length()/32)<=14){
                while(n < 14){
                    if(32 > doctor_Advice_length){
                        g2.drawString(doctor_Advice.substring(begin,doctor_Advice.length()),10,k[n]);
                        break;
                    }else {
                        g2.drawString(doctor_Advice.substring(begin,end),10,k[n]);
                    }
                    n++;
                    doctor_Advice_length = doctor_Advice_length-32;
                    begin = end+1;
                    end = begin+32;
                }
            }else {
                while(n < 14){
                    if(32 > doctor_Advice_length){
                        g2.drawString(doctor_Advice.substring(begin,doctor_Advice.length()),10,k[n]);
                        break;
                    }else {
                        g2.drawString(doctor_Advice.substring(begin,end),10,k[n]);
                    }
                    n++;
                    doctor_Advice_length = doctor_Advice_length-32;
                    begin = end+1;
                    end = begin+32;
                }
                int a = 14-(int) medcineList.size()/3;
                doctor_Advice_1 = doctor_Advice.substring(a*32+1,doctor_Advice.length());
            }
            return PAGE_EXISTS;
        }
        if (pageIndex == 1)
        {
            Font font4 = new Font("新宋体", Font.PLAIN, 12);
            g2.setFont(font4);//设置字体
            BasicStroke bs1 = new BasicStroke(0.0f);//设置线条
            g2.setStroke(bs1);
            g2.drawLine(197, 575, 205, 575);
            g2.drawString("2", 208, 578);
            g2.drawLine(218, 575, 226, 575);
            //int[][] j = {{10, 94, 110}, {150, 234, 250}, {280, 368, 395}};
            int[][] j = {{10, 90}, {150, 230}, {290, 370}};
            int[] k = new int[14];
            int temp = 184, q = 0, w = 0;
            for (int i = 0; i < 14; i++)
            {
                k[i] = temp;
                temp = temp + 24;
            }
            if(medcineList.size()>42){
                for (int i = 42; i<medcineList.size(); i++)
                {
                    if ((i + 1) % 3 == 1)
                        q = 0;
                    if ((i + 1) % 3 == 2)
                        q = 1;
                    if ((i + 1) % 3 == 0)
                        q = 2;
                    if (medcineList.get(i).name.length() < 7)
                        g2.drawString(medcineList.get(i).name, j[q][0], k[w]);
                    else
                        g2.drawString(medcineList.get(i).name.substring(0, 6) + "..", j[q][0], k[w]);
                    String str = Integer.toString((int)(medcineList.get(i).number/dose))+medcineList.get(i).unitname;
                    //g2.drawString(Integer.toString((int)(medcineList.get(i).number/dose)), j[q][1], k[w]);
                    //g2.drawString(medcineList.get(i).unitname, j[q][2], k[w]);
                    g2.drawString(str,j[q][1],k[w]);
                    if ((i+1) % 3 == 0)
                        w = w + 1;
                }
                int n = 0;
                if(medcineList.size()%3==0)
                    n = (int) (medcineList.size() / 3);
                else
                    n = (int) (medcineList.size() / 3)+1;
                int begin = 0;
                int doctor_Advice_length = doctor_Advice_1.length();
                int end = 32;
                if(n+(int) (doctor_Advice.length()/35)>14) {
                    while (n < 14) {
                        if (32 > doctor_Advice_length) {
                            g2.drawString(doctor_Advice_1.substring(begin,doctor_Advice.length()), 10, k[n]);
                            break;
                        } else {
                            g2.drawString(doctor_Advice_1.substring(begin, end), 10, k[n]);
                        }
                        n++;
                        doctor_Advice_length = doctor_Advice_length - 32;
                        begin = end + 1;
                        end = begin + 32;
                    }
                }
            }
            return PAGE_EXISTS;
        }
        if (pageIndex != 0 && pageIndex != 1)
        {
            return PAGE_EXISTS;
        }
        return PAGE_EXISTS;
    }
}


