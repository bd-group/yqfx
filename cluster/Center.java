package cluster;

import java.util.Date;
import java.util.HashSet;


public class Center {

    public String id;
    public String clusterName;
    public HashSet<String> wordsSet;
    public int hashcode;
    public String idSet;
    public int Count;
    public int siftCount;
    public double hotScore;
    public int classify;
    public int sentiment;
    public String yysId;  
    public Date generateDate;
    public Date updateDate;
    public Date isHotTimeDate;
    public Date isNotHotTimeDate;

    @Override
    public String toString() {
        return "Center{" + "id=" + id + ", clusterName=" + clusterName + ", wordsSet=" + wordsSet 
                + ", hashcode=" + hashcode + ", idSet=" + idSet + ", Count=" + Count + ", siftCount=" 
                + siftCount + ", hotScore=" + hotScore + ", classify=" + classify + ", sentiment=" + sentiment 
                + ", yysId=" + yysId + ", generateDate=" + generateDate + ", updateDate=" + updateDate 
                + ", isHotTimeDate=" + isHotTimeDate + ", isNotHotTimeDate=" + isNotHotTimeDate + '}';
    }

   
    
    
}
