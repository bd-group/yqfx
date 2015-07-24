/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package check;

import cluster.Center;
import cluster.HotCluster;
import global.GlobalConfig;
import global.WBObject;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import obtainSrcData.ObtainWBObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import predict.Predict;
import sensitive.MinGanInteface;
import sentiment.JudgeSentiment;
import splitwords.SplitWords;
import transput.Transmitter;
import transput.TransputHistoryHots;


/**
 *
 * @author daniel
 */
public class CheckStates implements  Runnable{
    
     private static Logger logger;
     private GlobalConfig gConfig;
     private SplitWords splitWords;
     private ObtainWBObject obwbobj;
     private Predict predict;
     private MinGanInteface mgi;
     private JudgeSentiment js;
     private HotCluster hc;
     private TransputHistoryHots thh;
     
     public CheckStates(GlobalConfig gc, ObtainWBObject owbo,
                                        SplitWords sw, Predict p,
                                        MinGanInteface mgInteface, JudgeSentiment judgeSentiment,
                                        HotCluster hotCluster, TransputHistoryHots thHots) {
         logger = LogManager.getLogger(CheckStates.class);
         this.gConfig = gc;
         this.obwbobj = owbo;
         this.splitWords = sw;
         this.predict = p;
         this.mgi = mgInteface;
         this.js = judgeSentiment;
         this.hc = hotCluster;
         this.thh = thHots;         
     }
     
     @Override
     public void run() {
         while (!Thread.currentThread().isInterrupted()) {
             
             ArrayList<LinkedBlockingQueue<WBObject>> wbBlockList = obwbobj.getOutputWBOBlockingQList();            
             for (int i = 0; i < wbBlockList.size(); i++) {                 
                 logger.info("Obtain WBObject blocking queue " + i + " size is:\t" + wbBlockList.get(i).size());
             }
             
             ArrayList<LinkedBlockingQueue<WBObject>> spBlockList = splitWords.getOutputSplitedBlockingQueuesList();
             for (int i = 0; i < spBlockList.size(); i++) {                 
                 logger.info("Split blocking queue " + i + " size is:\t" + spBlockList.get(i).size());
             }
             
             ArrayList<LinkedBlockingQueue<WBObject>> predictBlockList = predict.getOutputClassifiedBQList();
             for (int i = 0; i < predictBlockList.size(); i++) {                 
                 logger.info("Predict (Classify) blocking queue " + i + " size is:\t" + predictBlockList.get(i).size());
             }
             
             ArrayList<LinkedBlockingQueue<WBObject>> mgiBlockList = mgi.getOutputMinganList();
             for (int i = 0; i < mgiBlockList.size(); i++) {                 
                 logger.info("MinGanInterface (sensitive blocking queue " + i + " size is:\t" + mgiBlockList.get(i).size());
             }
             
             ArrayList<LinkedBlockingQueue<WBObject>> jsBlockList = js.getOutputSentimentedBlockingQueues();
             for (int i = 0; i < jsBlockList.size(); i++) {                 
                 logger.info("Sentiment (Transmitter) blocking queue " + i + " size is:\t" + jsBlockList.get(i).size());
             }
             
             ArrayList<LinkedBlockingQueue<WBObject>> jshBlockList = js.getOutputForClusterBlockingQueues();
             for (int i = 0; i < jshBlockList.size(); i++) {                 
                 logger.info("Sentimet for cluster blocking queue " + i + " size is:\t" + jshBlockList.get(i).size());
             }
             
                                   
             logger.info("hot cluster history blocking queue size is:\t" + hc.getHistoryHotCenters().size());
             
             try {
                 Thread.currentThread().sleep(2000);
             } catch (InterruptedException ex) {
                 java.util.logging.Logger.getLogger(CheckStates.class.getName()).log(Level.SEVERE, null, ex);
             }
             
         }
         
     }
    
    
}
