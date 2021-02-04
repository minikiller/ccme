package quickfix.examples.ordermatch;

import quickfix.Message;
import quickfix.examples.executor.Util;

import java.util.*;

public class MatchUtil {
    private static final List<String> nameList = Util.getNameList();
    private static final List<String> dateList = Util.getDateList();
    private static final List<String> singleList = createSingleList();
    private static final Map<String, Integer> dateMap = getDateMap();
    private static final Map<Integer, String> indexMap = getIndexMap();

    public static String get32UUID() {
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        return uuid;
    }

    public static String generateID() {
        return Long.toString(System.currentTimeMillis());
    }

    public static void printMsg(Message msg) {
        char delimiter = 1;
        String str = msg.toString();
        str = str.replace(delimiter, '|');
        System.out.println("get message is " + str);
    }

    private static List<String> createSingleList() {
        List<String> singleList = new ArrayList<>();
        for (String name : nameList) {

            for (String date : dateList) {
                String str = name + "-" + date;
                singleList.add(str);
            }

        }
        return singleList;
    }

    private static Map getIndexMap() {
        Map<Integer, String> map = new HashMap();
        int i = 0;
        for (String date : dateList) {
            map.put(i, date);
            i++;
        }
        return map;
    }

    private static Map getDateMap() {
        Map<String, Integer> map = new HashMap();
        int i = 0;
        for (String date : dateList) {
            map.put(date, i);
            i++;
        }
        return map;
    }

    /**
     * 返回map，基于单脚单为key，value为当前单脚单时间序列之后symbol list
     * 格式： <s-d1,[s-d2,s-d3]>
     *
     * @return
     */
    public static Map<String, List<String>> getAfterDoubleMap() {
        Map<String, List<String>> after = new HashMap<>();


        int j = 0;
        int k = dateList.size();
        for (String name : singleList) {
            List<String> doubleList = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : indexMap.entrySet()) {
                if (j < entry.getKey()) {
                    String str = name + "-" + entry.getValue();
                    doubleList.add(str);
                }
            }
            after.put(name, doubleList);
            j++;
            if (j == k) j = 0;
        }
        return after;
    }

    /**
     * 返回单脚单的时间后序单脚单列表
     * 例如 <s-d1,[s-d2,s-d3]>
     *
     * @return
     */
    public static Map<String, List<String>> getAfterSingleMap() {
        Map<String, List<String>> after = new HashMap<>();

        int j = 0;
        int k = dateList.size();
        for (String name : singleList) {
            String[] single = name.split("-");
            List<String> doubleList = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : indexMap.entrySet()) {
                if (j < entry.getKey()) {
                    String str = single[0] + "-" + entry.getValue();
                    doubleList.add(str);
                }
            }
            after.put(name, doubleList);
            j++;
            if (j == k) j = 0;
        }
        return after;
    }


    /**
     * 返回map，基于单脚单为key，value为当前单脚单时间序列之前symbol list
     * 格式： <s-d3,[s-d1,s-d2]>
     *
     * @return
     */
    public static Map<String, List<String>> getBeforeDoubleMap() {
        Map<String, List<String>> before = new HashMap<>();

        int j = 0;
        for (String name : singleList) {
            List<String> tmpList = new ArrayList<>();
            String[] _str = name.split("-");
            int index = dateMap.get(_str[1]);
            while (index - j > 0) {
                String str = indexMap.get(j);
                tmpList.add(_str[0] + "-" + str + "-" + _str[1]);
                j++;
            }
            before.put(name, tmpList);
            j = 0;
        }
        return before;
    }

    /**
     * 返回单脚单的时间前序单脚单列表
     * 例如 <d3,[d2,d1]>
     *
     * @return
     */
    public static Map<String, List<String>> getBeforeSingleMap() {
        Map<String, List<String>> before = new HashMap<>();

        int j = 0;
        for (String name : singleList) {
            List<String> tmpList = new ArrayList<>();
            String[] _str = name.split("-");
            int index = dateMap.get(_str[1]);
            while (index - j > 0) {
                String str = indexMap.get(j);
                tmpList.add(_str[0] + "-" + str);
                j++;
            }
            before.put(name, tmpList);
            j = 0;
        }
        return before;
    }

    /**
     * 输入两个single，返回一个double symbol
     * @param s_d1
     * @param s_d2
     * @return s_d1_d2
     */

    public static String getDoubleSymbol(String s_d1, String s_d2) {
        String[] str1 = s_d1.split("-");
        String[] str2 = s_d2.split("-");
        Integer first = dateMap.get(str1[1]);
        Integer second = dateMap.get(str2[1]);
        if (first > second)
            return str1[0] + "-" + str2[1] + "-" + str1[1];
        else
            return str1[0] + "-" + str1[1] + "-" + str2[1];
    }

    public static void main(String[] param) {
//        String str = "FMG3-MAR21, FMG3-JUN21, FMG3-SEP21, HEF1-MAR21, HEF1-JUN21, HEF1-SEP21, FMG3-MAR21-JUN21, FMG3-MAR21-SEP21, FMG3-JUN21-SEP21, HEF1-MAR21-JUN21, HEF1-MAR21-SEP21, HEF1-JUN21-SEP21";
//        String[] strArray = str.split(",");
//        List<String> stringList = new ArrayList();
//        for (int i = 0; i < strArray.length; i++) {
//            stringList.add(strArray[i]);
//        }
        getBeforeDoubleMap();
        getAfterDoubleMap();
    }
}
