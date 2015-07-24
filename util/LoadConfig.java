/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import global.GlobalConfig;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.w3c.dom.Document;

/**
 *
 * @author daniel
 */
public class LoadConfig {
    
    private final GlobalConfig gConfig;
    private static Logger logger ;
    
    public LoadConfig(String configFilePathString, GlobalConfig gc) {
         this.gConfig = gc;
        this.gConfig.configFilePath = configFilePathString;
        PropertyConfigurator.configure("log4j.properties");
        loadConfigFile();
        loadConfigFromDatabase();
    }
    
    public LoadConfig(GlobalConfig gc) {
        this.gConfig = gc;
    }
    
    public final void loadConfigFile() {
        String filePath = this.gConfig.configFilePath;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbBuilder = dbFactory.newDocumentBuilder();
            Document doc = dbBuilder.parse(filePath);

            this.gConfig.dictDir = doc.getElementsByTagName("DataPath").item(0).getFirstChild().getNodeValue();
            //this.gConfig.modelFile = doc.getElementsByTagName("ModelFile").item(0).getFirstChild().getNodeValue();
            this.gConfig.modefFileForliblinear = doc.getElementsByTagName("ModelFileForLiblinear").item(0).getFirstChild().getNodeValue();

            this.gConfig.tfidfFile = doc.getElementsByTagName("TFIDFFile").item(0).getFirstChild().getNodeValue();
            this.gConfig.classFile = doc.getElementsByTagName("classFile").item(0).getFirstChild().getNodeValue();

            this.gConfig.rangeFile = doc.getElementsByTagName("rangeFile").item(0).getFirstChild().getNodeValue();
            this.gConfig.serverIP = doc.getElementsByTagName("serverIP").item(0).getFirstChild().getNodeValue();
            
            this.gConfig.logConfigPath = doc.getElementsByTagName("logConfig").item(0).getFirstChild().getNodeValue();
            this.gConfig.consumerNameSrv = doc.getElementsByTagName("consumerNameSrv").item(0).getFirstChild().getNodeValue();
            this.gConfig.httpPost = doc.getElementsByTagName("httpPost").item(0).getFirstChild().getNodeValue();
            this.gConfig.MQTopicName = doc.getElementsByTagName("MQTopicName").item(0).getFirstChild().getNodeValue();
            this.gConfig.consumerInstanceName = doc.getElementsByTagName("consumerInstanceName").item(0).getFirstChild().getNodeValue();
            this.gConfig.TransmitTableName = doc.getElementsByTagName("TransmitToTableName").item(0).getFirstChild().getNodeValue();
            this.gConfig.destopwords = Boolean.parseBoolean(doc.getElementsByTagName("destopwords").item(0).getFirstChild().getNodeValue());
            
            String s = doc.getElementsByTagName("MaxSplitThreadCount").item(0).getFirstChild().getNodeValue();
            this.gConfig.maxSplitWordThreadCount = Integer.parseInt(s);
            String classify = doc.getElementsByTagName("MaxClassifyThreadCount").item(0).getFirstChild().getNodeValue();
            this.gConfig.maxClassifyThreadCount = Integer.parseInt(classify);
            String cluster = doc.getElementsByTagName("MaxClusterThreadCount").item(0).getFirstChild().getNodeValue();
            this.gConfig.maxClusterThreadCount = Integer.parseInt(cluster);
            String sensitive = doc.getElementsByTagName("MaxSensitiveThreadCount").item(0).getFirstChild().getNodeValue();
            this.gConfig.maxSensitiveThreadCount = Integer.parseInt(sensitive);
            String bq = doc.getElementsByTagName("MaxBlockQueue").item(0).getFirstChild().getNodeValue();
            this.gConfig.maxBlockQueue = Integer.parseInt(bq);
            String bqCapacity = doc.getElementsByTagName("maxBlockingQueueCapacity").item(0).getFirstChild().getNodeValue();
            this.gConfig.maxBlockingQueueCapacity = Integer.parseInt(bqCapacity);
            String transmit = doc.getElementsByTagName("MaxTransmitThreadCount").item(0).getFirstChild().getNodeValue();
            this.gConfig.maxTransmitThreadCount = Integer.parseInt(transmit);
            
            String port = doc.getElementsByTagName("serverPort").item(0).getFirstChild().getNodeValue();
            this.gConfig.serverPort = Integer.parseInt(port);
            String threhold = doc.getElementsByTagName("clusterThrehold").item(0).getFirstChild().getNodeValue();
            this.gConfig.clusterThrehold = Integer.parseInt(threhold);
           
            System.setProperty("log4j.configurationFile", this.gConfig.logConfigPath);
            logger = LogManager.getLogger(LoadConfig.class);
            logger.info("load configure file success!");                
            
        } catch (Exception ex) {
            logger.error(ex);
            //ex.printStackTrace();
        }        
    }
    
     public final void loadConfigFromDatabase() {
        XiTongPeiZhi peiZhi = getXtpz();
        gConfig.displayCount = peiZhi.getRedian_t();
        gConfig.refreshSeconds = peiZhi.getHoutai_re();
        logger.trace("display count is: " + gConfig.displayCount 
                + " refresh seconds is: " + gConfig.refreshSeconds);
        logger.info("load configure from database success!");
    }
     
     private XiTongPeiZhi getXtpz() {
            ArrayList<XiTongPeiZhi> li = new ArrayList<XiTongPeiZhi>();
            Session session = HibernateUtil.currentSession();//生成Session实例
            Query query = session.createQuery("FROM XiTongPeiZhi order by peizhi_time desc");
            query.setFirstResult(0);
            query.setMaxResults(1);
            li = (ArrayList<XiTongPeiZhi>) query.list();
            session.close();
            return li.get(0);
    }
    
}
