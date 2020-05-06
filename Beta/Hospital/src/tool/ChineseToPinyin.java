package tool;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import static tool.Regex.ChineseRegex;

public interface ChineseToPinyin {
    static String getPinYinHeadChar(String Chinese) {
        // 汉语拼音格式输出类
        HanyuPinyinOutputFormat hanYuPinOutputFormat = new HanyuPinyinOutputFormat();
        // 输出设置为大写，音标方式等
        hanYuPinOutputFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        hanYuPinOutputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hanYuPinOutputFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
        StringBuilder pinyinString = new StringBuilder();
        char[] chars = Chinese.toCharArray();
        for (char c : chars) {
            // 如果是中文则转化为拼音
            if (String.valueOf(c).matches(ChineseRegex)) {
                try {
                    String[] pinyinStringArray = PinyinHelper.toHanyuPinyinStringArray(c, hanYuPinOutputFormat);
                    pinyinString.append(pinyinStringArray[0].charAt(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                pinyinString.append(c);
            }
        }
        return pinyinString.toString();
    }
}
