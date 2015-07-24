/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package global;

/**
 *
 * @author daniel
 */
public class GlobalConfig {
    public boolean destopwords;
    public String configFilePath;
    public String dictDir;   
    public String modefFileForliblinear;
    public String tfidfFile;
    public String rangeFile;
    public String classFile;
    public String logConfigPath;

    public int maxSplitWordThreadCount;
    public int maxClassifyThreadCount; // equal to sentiment thread count
    public int maxClusterThreadCount;
    public int maxSensitiveThreadCount;
    public int maxTransmitThreadCount;
    public int maxBlockQueue;
    
    public int maxBlockingQueueCapacity;
    public int clusterThrehold;

    public String serverIP;
    public int serverPort;
    
    public String httpPost;
    public String consumerNameSrv;
    public String consumerInstanceName;
    public String MQTopicName;
    public String TransmitTableName;    

    public int refreshSeconds;
    public int displayCount;

    @Override
    public String toString() {
        return "GlobalConfig{" + "destopwords=" + destopwords + ", configFilePath=" + configFilePath 
                + ", dictDir=" + dictDir + ", modefFileForliblinear=" + modefFileForliblinear + ", tfidfFile=" 
                + tfidfFile + ", rangeFile=" + rangeFile + ", classFile=" + classFile + ", logConfigPath=" 
                + logConfigPath + ", maxSplitWordThreadCount=" + maxSplitWordThreadCount 
                + ", maxClassifyThreadCount=" + maxClassifyThreadCount + ", maxClusterThreadCount=" 
                + maxClusterThreadCount + ", maxSensitiveThreadCount=" + maxSensitiveThreadCount 
                + ", maxTransmitThreadCount=" + maxTransmitThreadCount + ", maxBlockQueue=" 
                + maxBlockQueue + ", maxBlockingQueueCapacity=" + maxBlockingQueueCapacity 
                + ", clusterThrehold=" + clusterThrehold + ", serverIP=" + serverIP + ", serverPort=" 
                + serverPort + ", httpPost=" + httpPost + ", consumerNameSrv=" + consumerNameSrv 
                + ", consumerInstanceName=" + consumerInstanceName + ", MQTopicName=" 
                + MQTopicName + ", TransmitTableName=" + TransmitTableName + ", refreshSeconds=" 
                + refreshSeconds + ", displayCount=" + displayCount + '}';
    }

    

 

    
    
}
