/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package run;

import check.CheckStates;
import cluster.HotCluster;
import global.GlobalConfig;
import obtainSrcData.ObtainWBObject;
import predict.Predict;
import sensitive.MinGanInteface;
import sentiment.JudgeSentiment;
import splitwords.SplitWords;
import transput.PushResults;
import transput.Transmitter;
import transput.TransputClient;
import transput.TransputHistoryHots;
import util.LoadConfig;


/**
 *
 * @author daniel
 */
public class RunEntry {
    private static GlobalConfig gConfig = new GlobalConfig();
    
    public static void main(String[] args) {
       
     
        
        LoadConfig loadConfig = new LoadConfig("./Configure.xml", gConfig);        
        System.out.println(gConfig.toString());
        // obtain threads
        ObtainWBObject odxo = new ObtainWBObject(gConfig);
         Thread obtainThread = new Thread(odxo);
        obtainThread.start();
        // split threads
        SplitWords s = new SplitWords(gConfig, odxo.getOutputWBOBlockingQList());
        s.initalize();
        // predict threads
        Predict predict = new Predict(gConfig, s.getOutputSplitedBlockingQueuesList());
        predict.runClassify();
        // judgeSentiment threads
        JudgeSentiment js = new JudgeSentiment(gConfig, predict.getOutputClassifiedBQList());
        js.runSentiment();
        // mingan threads
        MinGanInteface mgi = new MinGanInteface(gConfig, js.getOutputSentimentedBlockingQueues());
        mgi.runSensitive();
        // hot cluster threads
        HotCluster hc = new HotCluster(gConfig, js.getOutputForClusterBlockingQueues());
        hc.runCluster(gConfig.clusterThrehold);
        // run transmitter threads
        Transmitter transmitter = new Transmitter(gConfig, mgi.getOutputMinganList());
        transmitter.runTransmitter();
        // run push results thread
        PushResults pr = new PushResults(gConfig, hc, predict, js, mgi, s);
        Thread prThread = new Thread(pr);
        prThread.start();
        // run transput hot history
        TransputHistoryHots thh = new TransputHistoryHots(gConfig, hc.getHistoryHotCenters());
        Thread thhThread = new Thread(thh);
        thhThread.start();
        
        // run transput client thread
        TransputClient tc = new TransputClient(gConfig, pr);
        tc.runTransputClient();
        
        // run check thread
        CheckStates checkStates = new CheckStates(gConfig, odxo, s, predict, mgi, js, hc, thh);
        Thread checkThread = new Thread(checkStates);
//        checkThread.start();
      
    }
}
