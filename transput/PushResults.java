/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transput;

import cluster.Center;
import cluster.HotCluster;
import com.google.gson.Gson;
import global.GlobalConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import predict.Predict;
import sensitive.MinGan;
import sensitive.MinGanInteface;
import sentiment.JudgeSentiment;
import splitwords.SplitWords;
import util.ClassifySQL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */
public class PushResults implements Runnable {

    private static Logger logger;
    private GlobalConfig gConfig;
    private HotCluster hotCluster;
    private Predict predict;
    private JudgeSentiment sentiment;
    private MinGanInteface mingan;
    private SplitWords splitWords;
    private long currentTotal = 0;
    private long currentMgTotal = 0;
    private Map<String, Long> currentYYSCount;
    private long[] currentQGCount = new long[4];

    public String hotClusterResultString;
    public String classifyResultString;
    public String sentimentResulitString;
    public String minganPredefineResultString;
    public String totalCountString;
    public String yysCountString;
    public String minganMainListString;
    public String minganTotalNumString;

    public PushResults(GlobalConfig gc, HotCluster hc, Predict p,
            JudgeSentiment js, MinGanInteface mgi,
            SplitWords spw) {

        logger = LogManager.getLogger(PushResults.class);
        this.gConfig = gc;
        this.hotCluster = hc;
        this.predict = p;
        this.sentiment = js;
        this.mingan = mgi;
        this.splitWords = spw;
        this.currentYYSCount = new HashMap<>();
    }

    @Override
    public void run() {
        try {
            long countSecs = 0;
            while (!Thread.currentThread().isInterrupted()) {
                if (countSecs % gConfig.refreshSeconds == 0) {
                    this.hotClusterResultString = getHotResult();
                }
                this.sentimentResulitString = getSentimentCount();
                this.minganPredefineResultString = getPredefineMinganResult();
                this.minganMainListString = getMinganResult();
                this.yysCountString = getYYSCount();
                this.totalCountString = getTotalCount();
                this.minganTotalNumString = getMgTotalCount();
                this.classifyResultString = getPredictResult();

                countSecs++;
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }

    }

    public String getHotResult() {
        Gson gson = new Gson();
        ArrayList<Center> result = hotCluster.getHotsList();
        if (result == null) {
            return "";
        }
        return gson.toJson(result);
    }

    public String getPredictResult() {
        Gson gson = new Gson();
        ArrayList<ClassifySQL> result = predict.getPredictResult();
        return gson.toJson(result);
    }

    public String getMinganResult() {
        Gson gson = new Gson();
        ArrayList<MinGan> result = mingan.getMinGans();
        return gson.toJson(result);
    }

    public String getPredefineMinganResult() {
        Gson gson = new Gson();
        ArrayList<MinGan> result = mingan.getPredefineResult();
        return gson.toJson(result);
    }

    public String getTotalCount() {
        Gson gson = new Gson();
        long r = splitWords.getSplitedTotalNum();
        long result = 0;
        if (r >= currentTotal) {
            result = r - currentTotal;
            currentTotal = r;
        }
        //System.out.println("current Total " + currentTotal);
        return gson.toJson(result);
    }

    public String getMgTotalCount() {
        Gson gson = new Gson();
        long tc = mingan.getMgDefinedTotalNum();
        long res = 0;
        if (tc >= currentMgTotal) {
            res = tc - currentMgTotal;
            currentMgTotal = tc;
        }
        return gson.toJson(res);
    }

    public String getYYSCount() {
        Gson gson = new Gson();
        Map<String, Long> map = splitWords.getYYSCount();
        Map<String, Long> resultMap = new HashMap<>();
        if (currentYYSCount.isEmpty()) {
//            String resString = map.toString();
//            System.out.println("空值 \t" + resString.substring(1, resString.length() - 1));
            currentYYSCount = map;
//            return gson.toJson(resString.substring(1, resString.length() - 1));
            return gson.toJson(map);
        }

        for (String key : map.keySet()) {
            if (currentYYSCount.containsKey(key)) {
                if (currentYYSCount.get(key) <= map.get(key)) {
                    resultMap.put(key, map.get(key) - currentYYSCount.get(key));
                } else {
                    resultMap.put(key, 0L);
                }
            } else {
                resultMap.put(key, map.get(key));
            }
        }

        currentYYSCount = map;
//        String resultString = resultMap.toString();
//        System.out.println("结果 \t" + resultMap.toString());

        return gson.toJson(resultMap);
    }

    public String getSentimentCount() {
        Gson gson = new Gson();
        long[] r = sentiment.getSentimentCount();
        long[] result = new long[4];
        if (r[0] >= currentQGCount[0] && r[1] >= currentQGCount[1]
                && r[2] >= currentQGCount[2] && r[3] >= currentQGCount[3]) {
            result[0] = r[0] - currentQGCount[0];
            currentQGCount[0] = r[0];
            result[1] = r[1] - currentQGCount[1];
            currentQGCount[1] = r[1];
            result[2] = r[2] - currentQGCount[2];
            currentQGCount[2] = r[2];
            result[3] = r[3] - currentQGCount[3];
            currentQGCount[3] = r[3];
        }
        return gson.toJson(result);
    }

    public void addMinGanCi(String s, int iswhat) {
        mingan.addmgc(s, iswhat);
    }

    public void delMinGanCi(String s, int iswhat) {
        mingan.delmgc(s, iswhat);
    }

}
