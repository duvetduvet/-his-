package tool;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//文本文件操作类
public class FileOperator {
    //读取文件
    public static BufferedReader readFile(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "utf-8");
        return new BufferedReader(inputStreamReader);
    }

    //将文件内容转换成二维对象数组
    public static Object[][] fileToObjects(BufferedReader bufferedReader) throws Exception {
        List<String> list = new ArrayList<>();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            list.add(line.trim());
        }
        Object objects[][] = new Object[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            objects[i] = list.get(i).split("#");
        }
        bufferedReader.close();
        return objects;
    }

    //写入文件
    public static void writeFile(String info, String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
        bufferedWriter.write(info);
        bufferedWriter.close();
        outputStreamWriter.close();
        fileOutputStream.close();
    }

    public static void writeObjects(Object[][] objects) {

    }
}
