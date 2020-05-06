package tool;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// xml文件操作类
public class XmlOperator {
    // 读取xml文件
    public static Document readXml(String path) {
        Document document = null;
        try {
            //创建对象
            SAXReader reader = new SAXReader();
            //加载xml文件，获取document对象
            document = reader.read(new File(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    // 获取根节点
    public static List<Element> getRoot(Document document) {
        List<Element> list = new ArrayList<>();
        list.add(document.getRootElement());
        return list;
    }

    // 获取子节点
    public static List<Element> getElements(List<Element> elements) {
        List<Element> list = new ArrayList<>();
        for (Element element : elements) {
            //获取迭代器
            Iterator iterator = element.elementIterator();
            //遍历迭代器，获取根节点
            while (iterator.hasNext()) {
                list.add((Element) iterator.next());
            }
        }
        return list;
    }

    // 获取节点名称
    public static List<String> getName(List<Element> elements) {
        List<String> list = new ArrayList<>();
        //遍历elements获取每个节点的名称
        for (Element element : elements) {
            list.add(element.getName().trim());
        }
        return list;
    }

    // 获取节点文本
    public static List<String> getText(List<Element> elements) {
        List<String> list = new ArrayList<>();
        //遍历elements获取每个节点的值
        for (Element element : elements) {
            list.add(element.getStringValue().trim());
        }
        return list;
    }

    // 获取节点属性值
    public static List<String> getProperty(List<Element> elements) {
        List<String> list = new ArrayList<>();
        // 遍历elements获取每个节点的属性值
        for (Element element : elements) {
            list.add(element.attributeValue("path"));
        }
        return list;
    }

    // 修改xml
    public static void updateDoc(Document document, String xPath, String child, String text) {
        Element element = (Element) document.selectSingleNode(xPath);
        //修改element节点的子元素pro的文本为text
        element.element(child).setText(text);
    }

    //将修改保存至配置文件
    public static void writeXml(Document document, String path) {
        try {
            //将document中的内容写入文件中，转换字符流
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, "utf-8");
            XMLWriter writer = new XMLWriter(outputStreamWriter);
            writer.write(document);
            writer.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
