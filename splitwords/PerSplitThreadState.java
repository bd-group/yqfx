/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitwords;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author daniel
 */
public class PerSplitThreadState {
    
    public long splitNum = 0;
    public long filteredNum = 0;
//    public long ydCount = 0;
//    public long ltCount = 0;
//    public long dianxCount = 0;
//    public long unknownCount = 0;
    public Map<String, Long> srcCountMap = new HashMap<>();
}
