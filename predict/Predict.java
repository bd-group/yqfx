/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package predict;


import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import libsvm.svm_node;
import org.hibernate.Session;

import global.WBObject;
import global.GlobalConfig;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import util.ClassifySQL;
import util.HibernateUtil;

/**
 *
 * @author Rain
 */
// predict -b 1 test_file data_file.model output_file
public class Predict {

    private static Logger logger;
    private GlobalConfig gConfig;

    private ArrayList<LinkedBlockingQueue<WBObject>> inputWBOBQList;
    private ArrayList<LinkedBlockingQueue<WBObject>> outputClassifiedBQList;
    
     private ArrayList<Thread> classifyThreads;
    private ArrayList<PerClassifyThreadState> perClassifyStates;

    private FeatureRebuild feat;
    private ReadWriteLock rwl = new ReentrantReadWriteLock();
   

    //public static String initTrainFileForLiblinear = "data/output/predictFileForLiblinear.txt";
//    public static String initTrainFileForLibSVM = "predictFileForLibSVM.txt";
//    public static String predictAnsFile = "predictClassfication";

    public HashMap<String, Integer> hashWordStrtoInt = new HashMap<String, Integer>();             //对词str 哈希成 int
    public HashMap<Integer, Float> hashInttoIDF = new HashMap<Integer, Float>();
    public HashMap<Integer, String> hashhasClass = new HashMap<Integer, String>();
    public ArrayList<Double> feature_min = new ArrayList<Double>();
    public ArrayList<Double> feature_max = new ArrayList<Double>();


    public de.bwaldvogel.liblinear.Predict predictLiblinear;
    public Session session;

    //统计量的值，统计之前处理的总量 用于写入数据库函数
    public long[] preCount = new long[41];
    //统计量,统计之前处理的总量 用于check函数
    public long[] preCountCheck = new long[41];
    //每秒处理数量
    public long[] tmpCount = new long[41];

    public Predict(GlobalConfig globalConfig, ArrayList<LinkedBlockingQueue<WBObject>> classifyBQList) {
        logger = LogManager.getLogger(Predict.class);
        this.gConfig = globalConfig;
        
        this.inputWBOBQList = classifyBQList;
        this.outputClassifiedBQList = new ArrayList<>();

        this.feat = new FeatureRebuild();
        this.classifyThreads = new ArrayList<>();
        this.perClassifyStates = new ArrayList<>();
        
        for (int i = 0; i < this.gConfig.maxBlockQueue; i++) {
            LinkedBlockingQueue<WBObject> res = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
            this.outputClassifiedBQList.add(res);
        }
        //数据库
        this.session = HibernateUtil.currentSession();
        init();

    }

    private void init() {

        BufferedReader br = null;
        try {
            //初始化 idf & range 文件
            br = new BufferedReader(new InputStreamReader(new FileInputStream(gConfig.tfidfFile), "utf-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                String[] sss = temp.split("\t");
                /*
                *   不用+1
                */
                hashWordStrtoInt.put(sss[0], Integer.parseInt(sss[1]));
                hashInttoIDF.put(Integer.parseInt(sss[1]), Float.parseFloat(sss[2]));
            }  
            br.close();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(gConfig.rangeFile), "utf-8"));
            br.readLine();
            br.readLine();
            feature_min.add(0.0);
            feature_max.add(1.0);
            while ((temp = br.readLine()) != null) {
                String[] sss = temp.split(" ");
                feature_min.add(Double.parseDouble(sss[1]));
                feature_max.add(Double.parseDouble(sss[2]));
            }   //加入类别
            br.close();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(gConfig.classFile), "utf-8"));
            while ((temp = br.readLine()) != null) {
                String[] sss = temp.split("\t");
                this.hashhasClass.put(Integer.parseInt(sss[0]), sss[1]);
            }   
            //初始化 Predict modelfile      
            this.predictLiblinear = new de.bwaldvogel.liblinear.Predict(this.gConfig);
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex);
            java.util.logging.Logger.getLogger(Predict.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            logger.error(ex);
            java.util.logging.Logger.getLogger(Predict.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.error(ex);
            java.util.logging.Logger.getLogger(Predict.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                logger.error(ex);
                java.util.logging.Logger.getLogger(Predict.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    //启动分类线程
    public void runClassify() {
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            PerClassifyThreadState pcs = new PerClassifyThreadState();
            perClassifyStates.add(pcs);
            PredictThread predictThread = new PredictThread(inputWBOBQList.get(i % gConfig.maxBlockQueue),
                    outputClassifiedBQList.get(i % gConfig.maxBlockQueue), pcs);
            Thread sentTread = new Thread(predictThread);
            classifyThreads.add(sentTread);
            sentTread.start();
            logger.trace("run classify thread " + i + " " + sentTread.toString());
        }
        Write2DatabaseThread write2DatabaseThread = new Write2DatabaseThread(this.perClassifyStates);
        Thread writeThread = new Thread(write2DatabaseThread);
        writeThread.start();

    }

    //实时推送数据
    public ArrayList<ClassifySQL> getPredictResult() {
        ArrayList<ClassifySQL> p = new ArrayList<>();
        Calendar time = Calendar.getInstance();

        for (int i = 1; i < tmpCount.length; i++) {
            p.add(new ClassifySQL((time.getTime().toString() + i), i, time, time, tmpCount[i]));
            //System.out.println(tmpCount[i]);
        }

        return p;
    }

    public ArrayList<LinkedBlockingQueue<WBObject>> getOutputClassifiedBQList() {
        return outputClassifiedBQList;
    }

    public long checkClassifyCount() {
        long count = 0;
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            count += perClassifyStates.get(i).classifiedCount;
        }
        return count;
    }

    //分类主线程
    class PredictThread implements Runnable {

        private LinkedBlockingQueue<WBObject> srcBlockingQueue;
        private LinkedBlockingQueue<WBObject> resultBlockingQueue;
        private PerClassifyThreadState perClassifyThreadState;

        public PredictThread(LinkedBlockingQueue<WBObject> srcBQ, LinkedBlockingQueue<WBObject> rBQ, PerClassifyThreadState pcs) {
            logger.trace("Create classify predict thread!");
            this.srcBlockingQueue = srcBQ;
            this.resultBlockingQueue = rBQ;
            this.perClassifyThreadState = pcs;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WBObject so = srcBlockingQueue.take();
                    if (so.fllx == 1) {
                        perClassifyThreadState.perClassCount[1]++;
                        resultBlockingQueue.put(so);
                        continue;
                    }
                    //选择 predictLiblinear 或者 predict 函数
                    double classify = predictLiblinear(so.toProblemStructure(), so);
                    Double d = classify;
                    so.setFllx(d.intValue());                 
                    resultBlockingQueue.put(so);
                    // count every class
                    perClassifyThreadState.perClassCount[d.intValue()]++;
                    perClassifyThreadState.classifiedCount++;

                } catch (InterruptedException | IOException e) {
                    logger.error(e);
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    //分类函数
    private double predictLiblinear(ProblemStructure s, WBObject wbo) throws IOException {
        if (s.len <= 4) {
            return 1;
        }

        svm_node[] d = feat.featureCalculateSNode(hashInttoIDF, hashWordStrtoInt, feature_min, feature_max, s);
        //wbo.setSvm_node_vector(d);

        Feature[] dd = new FeatureNode[d.length];
        for (int i = 0; i < d.length; i++) {
            dd[i] = new FeatureNode(d[i].index, d[i].value);
        }
        double v = 0;
        v = predictLiblinear.predictSignal(dd);
//        System.out.println(v + " " + hashhasClass.get((int)v) + " : \t" + s.text);
        return v;
    }

    //写数据库线程
    class Write2DatabaseThread implements Runnable {

        private ArrayList<PerClassifyThreadState> pcStatesList;

        //private Date date;
        public Write2DatabaseThread(ArrayList<PerClassifyThreadState> pcsList) {
            logger.trace("Create write to database thread");
            this.pcStatesList = pcsList;
        }

        @Override
        public void run() {
            int sec = -1;

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(100);
                    Calendar date = Calendar.getInstance();
                    int sc = date.get(Calendar.SECOND);
                    if (sec != sc) {
                        writeStatistics2Database(pcStatesList, date);
                        sec = sc;
                    }
                    if (date.get(Calendar.SECOND) == 0
                            && date.get(Calendar.MINUTE) == 0
                            && date.get(Calendar.HOUR) == 0) {
                        //清理
                        cleanPerState(pcStatesList);
                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    logger.error(e);
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

    //写数据库函数
    private void writeToSQL(int classify, long count, Calendar time) {
//        Transaction gTransaction = session.beginTransaction();
//        gTransaction.begin();
        session.save(new ClassifySQL((time.getTime().toString() + classify), classify, time, time, count));
        session.beginTransaction().commit();
    }

    //写入数据库统计量
    private void writeStatistics2Database(ArrayList<PerClassifyThreadState> pcsList, Calendar date) {
        long[] totalCount = new long[41];

        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            for (int j = 0; j < pcsList.get(i).perClassCount.length; j++) {
                totalCount[j] += pcsList.get(i).perClassCount[j];
            }

        }
        //System.out.println("\n" + date.getTime() + " 分类器处理数据 : ");
        for (int i = 0; i < preCount.length; i++) {
            tmpCount[i] = totalCount[i] - preCount[i];
            preCount[i] = totalCount[i];
            //System.out.print(preCount[i] + "|" + tmpCount[i] + " ");
        }

        for (int i = 0; i < totalCount.length; i++) {
            writeToSQL(i, tmpCount[i], date);
        }
    }

    //检查每个线程 输出
    public void checkThread(Calendar date) {
        long[] totalCount = new long[41];
        long all = 0;

        System.out.println("\n分类器状态 ： " + !this.perClassifyStates.get(0).classifyIsExited);
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            System.out.println("分类线程 " + i + "状态：" + !this.perClassifyStates.get(i).isStoped);
        }
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            for (int j = 0; j < this.perClassifyStates.get(i).perClassCount.length; j++) {
                totalCount[j] += this.perClassifyStates.get(i).perClassCount[j];
            }
            System.out.println("\n线程 " + i);
            for (int j = 0; j < preCount.length; j++) {
                System.out.print("\t类别：" + j + "  数量：" + this.perClassifyStates.get(i).perClassCount[j]);
                if (j % 3 == 0) {
                    System.out.println("");
                }
            }
        }
        for (int i = 1; i < totalCount.length; i++) {
            all += (totalCount[i] - preCountCheck[i]);
            System.out.print("\n类别: " + i + " 共: " + (totalCount[i] - preCountCheck[i]));
            preCountCheck[i] = totalCount[i];

        }
        System.err.println("\n" + date.getTime() + " 秒内共处理: " + all);

    }

    //清理线程计数
    public void cleanPerState(ArrayList<PerClassifyThreadState> pcsList) {
        rwl.writeLock().lock();
        try {
            for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
                pcsList.get(i).perClassCount = new long[41];
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

}
