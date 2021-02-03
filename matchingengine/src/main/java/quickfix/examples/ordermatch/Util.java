package quickfix.examples.ordermatch;

import quickfix.Message;

import java.util.*;

public class Util {
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

    /**
     * 获得大盘对数据字典
     *
     * @param
     * @return
     */
    public static Map<String, List<String>> getAfterMap() {
        Map<String, List<String>> after = new HashMap<>();
        List<String> nameList = new ArrayList<>();
        nameList.add("FMG3");
        nameList.add("HEF1");
        List<String> dateList = new ArrayList<>();
        dateList.add("JUN21");
        dateList.add("MAR21");
        dateList.add("SEP21");

        List<String> singleList = new ArrayList<>();
        List<String> doubleList = new ArrayList<>();
        for (String name : nameList) {
            for (String date : dateList) {
                String str = name + "-" + date;
                singleList.add(str);
            }
        }

        Map<Integer, String> map = new HashMap();
        int i = 0;
        for (String date : dateList) {
            map.put(i, date);
            i++;
        }

        int j = 0;
        int k = dateList.size();
        for (String name : singleList) {
            List<String> tmpList = new ArrayList<>();
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                if (j < entry.getKey()) {
                    String str = name + "-" + entry.getValue();
                    doubleList.add(str);
                    tmpList.add(str);
                }
            }
            after.put(name, tmpList);
            j++;
            if (j == k) j = 0;
        }
        return after;
    }

    public static Map<String, List<String>> getBeforeMap() {
        Map<String, List<String>> before = new HashMap<>();
        List<String> nameList = new ArrayList<>();
        nameList.add("FMG3");
        nameList.add("HEF1");
        List<String> dateList = new ArrayList<>();
        dateList.add("JUN21");
        dateList.add("MAR21");
        dateList.add("SEP21");

        List<String> singleList = new ArrayList<>();
        List<String> doubleList = new ArrayList<>();
        for (String name : nameList) {
            for (String date : dateList) {
                String str = name + "-" + date;
                singleList.add(str);
            }
        }

        Map<Integer, String> nameMap = new HashMap();
        int i = 0;
        for (String date : dateList) {
            nameMap.put(i, date);
            i++;
        }

        Map<String, Integer> map = new HashMap();
        i = 0;
        for (String date : dateList) {
            map.put(date, i);
            i++;
        }

        int j = 0;
        for (String name : singleList) {
            List<String> tmpList = new ArrayList<>();
            String[] _str=name.split("-");
            int index = map.get(_str[1]);
            while (index - j > 0) {
                String str=nameMap.get(j);
                tmpList.add(_str[0]+"-"+str+"-"+_str[1]);
                j++;
            }
            before.put(name, tmpList);
            j = 0;
        }
        return before;
    }

    //	public static List<String> getBeforeOrder(String symbol,List<String> stringList){
//
//	}
    public static void main(String[] param) {
//        String str = "FMG3-MAR21, FMG3-JUN21, FMG3-SEP21, HEF1-MAR21, HEF1-JUN21, HEF1-SEP21, FMG3-MAR21-JUN21, FMG3-MAR21-SEP21, FMG3-JUN21-SEP21, HEF1-MAR21-JUN21, HEF1-MAR21-SEP21, HEF1-JUN21-SEP21";
//        String[] strArray = str.split(",");
//        List<String> stringList = new ArrayList();
//        for (int i = 0; i < strArray.length; i++) {
//            stringList.add(strArray[i]);
//        }
        getBeforeMap();
    }
}
