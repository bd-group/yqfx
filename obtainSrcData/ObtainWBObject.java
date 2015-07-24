/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package obtainSrcData;

import global.WBObject;
import global.GlobalConfig;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */
public class ObtainWBObject implements Runnable {

    private static Logger logger;
    private GlobalConfig gConfig;

    private Consumer consumer;
    private Deserialize deserialize;

    private ArrayList<LinkedBlockingQueue<WBObject>> outputWBOBlockingQList;
    private LinkedBlockingQueue<byte[]> inputMsgQueue;

    public ObtainWBObject(GlobalConfig globalConfig) {

        this.gConfig = globalConfig;
        logger = LogManager.getLogger(ObtainWBObject.class);
        this.consumer = new Consumer(gConfig);
        this.deserialize = new Deserialize();
        
        this.outputWBOBlockingQList = new ArrayList<>();
        for (int i = 0; i < gConfig.maxBlockQueue; i++) {
            LinkedBlockingQueue<WBObject> wboBQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
            outputWBOBlockingQList.add(wboBQueue);            
        }
        
        
    }

    public ArrayList<LinkedBlockingQueue<WBObject>> getOutputWBOBlockingQList() {
        return outputWBOBlockingQList;
    }    

    @Override
    public void run() {
        consumer.start();
        this.inputMsgQueue = consumer.getSrcQueue();
        int count = 0;
        while (!Thread.currentThread().isInterrupted()) {
            //System.out.println("count " + count);
            try {
               byte[]   msg = inputMsgQueue.take();
                WBObject wbo = deserialize.deserialize2WBObject(msg);
                if (wbo != null) {
                    this.outputWBOBlockingQList.get(count % gConfig.maxBlockQueue).put(wbo);       
                    count++;
                }
            } catch (InterruptedException ex) {
                logger.debug(ex);
            }
        }
    }

   
}
