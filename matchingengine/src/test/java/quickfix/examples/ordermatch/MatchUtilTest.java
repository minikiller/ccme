package quickfix.examples.ordermatch;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MatchUtilTest {

    @org.junit.jupiter.api.Test
    void getAfterDoubleMap() {
        Map<String, List<String>> map=MatchUtil.getAfterDoubleMap();
        List<String> list1=map.get("FMG3-SEP21");

        assert list1.size()==0;
        List<String> list2=map.get("FMG3-MAR21");
        assert list2.get(0).equals("FMG3-MAR21-JUN21");
        assert list2.get(1).equals("FMG3-MAR21-SEP21");
    }

    @org.junit.jupiter.api.Test
    void getAfterSingleMap() {
        Map<String, List<String>> map= MatchUtil.getAfterSingleMap();
        assert map.size()>0;
        List<String> list=map.get("FMG3-JUN21");
        assert list.size()==1;
    }

    @org.junit.jupiter.api.Test
    void getBeforeDoubleMap() {
        Map<String, List<String>> map= MatchUtil.getBeforeDoubleMap();
        assert map.size()>0;
        List<String> list=map.get("FMG3-JUN21");
        assert list.size()==1;
    }

    @org.junit.jupiter.api.Test
    void getBeforeSingleMap() {
        Map<String, List<String>> map= MatchUtil.getBeforeSingleMap();
        assert map.size()>0;
        List<String> list=map.get("FMG3-JUN21");
        assert list.size()==1;
    }
}