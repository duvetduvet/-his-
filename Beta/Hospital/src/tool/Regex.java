package tool;

// 正则表达式
public interface Regex {
    // 身份证号
    String idRegex = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)";
    // 电话号码(手机号码，3-4位区号，7-8位直播号码，1-4位分机号)
    String phoneRegex = "((\\d{11})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|" + "(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|" + "\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$)";
    // 纯数字
    String cardRegex = "^[0-9]\\d*$";
    // 非负浮点数
    String floatRegex = "^\\d+(\\.\\d+)?$";
    // 中文
    String ChineseRegex = "[\\u4e00-\\u9fa5]";
}
