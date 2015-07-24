package sensitive;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import util.HibernateUtil;
import global.WBObject;
import global.GlobalConfig;
import util.MGCModel;

public class MinGanInteface {

    private static Logger logger;
    private GlobalConfig gConfig;
    
    private ArrayList<LinkedBlockingQueue<WBObject>> inputWBOBlockingQueues;
    private ArrayList<LinkedBlockingQueue<WBObject>> outputSensitiveWBObjectList;
    
    private LinkedBlockingQueue<MinGan> pushPredefineBlockingQueue;
    private LinkedBlockingQueue<MinGan> pushDefinedBlockingQueue;
    
    private ArrayList<PerMinGanThreadState> minganThreadStates;
    private ArrayList<Thread> sensitiveThreads;
    
    private ReadWriteLock rwl = new ReentrantReadWriteLock();
   
//    Session session;
    boolean istree = false;
    Map<String, Integer> mgcmap;
    static Map<String, Integer> ydymgcmap;
    KeywordMatch keyMatch;
    static AcStateOp treeRoot;
    static String[] words;

    public MinGanInteface(GlobalConfig globalConfig,  ArrayList<LinkedBlockingQueue<WBObject>> srcBQList) {
        logger = LogManager.getLogger(MinGanInteface.class);
        
        this.gConfig = globalConfig;
        this.inputWBOBlockingQueues = srcBQList;
        this.outputSensitiveWBObjectList = new ArrayList<>();
        for (int i = 0; i < gConfig.maxBlockQueue; i++) {
            LinkedBlockingQueue<WBObject> OutWBBq = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
            outputSensitiveWBObjectList.add(OutWBBq);
        }
        
        this.pushDefinedBlockingQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
        this.pushPredefineBlockingQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
        this.sensitiveThreads = new ArrayList<>();
        this.minganThreadStates = new ArrayList<>();

        mgcmap = new HashMap<>();
        ydymgcmap = new HashMap<>();
        keyMatch = new KeywordMatch();
        treeRoot = new AcStateOp();
        init();
    }

    public ArrayList<LinkedBlockingQueue<WBObject>> getOutputMinganList() {
        return outputSensitiveWBObjectList;
    }

    public boolean MakeTree() {
        List<MGCModel> expres2 ;
        Session session2 = HibernateUtil.currentSession();
        String hqlydy = "from MGCModel where status = ?";
        Query query2 = session2.createQuery(hqlydy);
        query2.setString(0, "0");
        expres2 = query2.list();
        for (int i = 0; i < expres2.size(); i++) {
            ydymgcmap.put(expres2.get(i).getName(), expres2.get(i).getId());
        }
        List<MGCModel> expres;
        Session session = HibernateUtil.currentSession();
        String hql = "from MGCModel where status <> ?";
        Query query = session.createQuery(hql);
        query.setString(0, "-1");
        expres = query.list();
        words = new String[expres.size()];
        for (int i = 0; i < expres.size(); i++) {
            words[i] = expres.get(i).getName();
            mgcmap.put(expres.get(i).getName(), expres.get(i).getId());
        }

        AcStateOp rootAcStateOp = new AcStateOp();
        for (int i = 0; i < words.length; i++) {
            treeRoot = treeRoot.extendString(words[i], rootAcStateOp, words);
        }
        treeRoot = treeRoot.failDeal(treeRoot);
        //System.out.println(new Date());
        if (expres.isEmpty()) {
            logger.error("sensitive make tree failed!");
            return false;
        }
        logger.trace("sensitive make tree success!");
        return true;
    }

    public boolean init() {        
        if (mgcmap != null && mgcmap.size() > 0) {
            mgcmap.clear();
        }
        if (ydymgcmap != null && ydymgcmap.size() > 0) {
            ydymgcmap.clear();
        }

        sensitiveThreads.clear();

        istree = MakeTree();
        if (istree) {
            logger.trace("initialize sensitive sucess!");
        }
        else {
            logger.error("initialize sensitive failed!");
        }
        return istree;
    }

    public boolean addmgc(String ss, int iswhat) {
        String[] sswo = new String[words.length + 1];
        if (iswhat == 0) {
            String[] delw = ss.split("-");
            for (int i = 0; i < words.length; i++) {
                if (delw[0].equals(words[i])) {
                    if (ydymgcmap.keySet().contains(delw[0])) {
                        return true;
                    }
                    ydymgcmap.put(delw[0], Integer.parseInt(delw[1]));
                    return true;
                }
                sswo[i] = words[i];
            }
            sswo[words.length] = delw[0];
            words = sswo;
            ydymgcmap.put(delw[0], Integer.parseInt(delw[1]));
            if (!mgcmap.keySet().contains(delw[0])) {
                mgcmap.put(delw[0], Integer.parseInt(delw[1]));
            }
            treeRoot = treeRoot.extendString(delw[0], treeRoot, words);
            // System.out.println("你好你的程序树重新加载了》》》》》请稍等。。。。。。。。。。。"+words[i]);
        } else {
            ydymgcmap.remove(ss);
        }

        return true;

    }

    public boolean delmgc(String ss, int iswhat) {
        int isin = -1;
        String[] sswo = new String[words.length - 1];
        int j = 0;
        for (int i = 0; i < words.length; i++) {

            if (ss.equals(words[i])) {
                isin = i;
                break;
            }
        }
        if (isin != -1) {
            for (int i = 0; i < words.length; i++) {

                if (i != isin) {
                    sswo[j] = words[i];
                    j++;
                }
            }
        }
        if (isin == -1) {
            System.out.println("in not exit");
            return false;
        }

        words = sswo;
        try {
            rwl.writeLock().lock();
            if (iswhat == 0) {
                ydymgcmap.remove(ss);
            }

            treeRoot = treeRoot.delmgc(ss, treeRoot);
        } finally {
            rwl.writeLock().unlock();
        }
        if (mgcmap.keySet().contains(ss)) {
            mgcmap.remove(ss);
        }

        return true;
    }


    private Map<Integer, ArrayList<String>> checkSensitive(WBObject wbObject, KeywordMatch kMatch) {
        
        Map<Integer, ArrayList<String>> matc;
        matc = kMatch.match(wbObject.getMnr(), treeRoot, ydymgcmap);
        return matc;
    }
    
    public long getMgDefinedTotalNum() {
        long total = 0;
        for (int i = 0; i < gConfig.maxSensitiveThreadCount; i++) {
            total += minganThreadStates.get(i).isMinganNum;
        }
        return  total;
    }

    public ArrayList<MinGan> getMinGans() {
        ArrayList<MinGan> res = new ArrayList<>();
        while (true) {
            MinGan mGan = pushDefinedBlockingQueue.poll();

            if (mGan != null) {
                res.add(mGan);
            } else {
                break;
            }
        }
        return res;
    }

    public ArrayList<MinGan> getPredefineResult() {
        ArrayList<MinGan> res = new ArrayList<>();
        while (true) {
            MinGan mGan = pushPredefineBlockingQueue.poll();

            if (mGan != null) {
                res.add(mGan);
            } else {
                break;
            }
        }

        return res;

    }

    class MinGanThread implements Runnable {

        private LinkedBlockingQueue<WBObject> srcBlockingQueue;
        private LinkedBlockingQueue<WBObject> outBQueue;
        private PerMinGanThreadState state;

        public MinGanThread(LinkedBlockingQueue<WBObject> src,
                LinkedBlockingQueue<WBObject> des,                
                PerMinGanThreadState minganState) {
            this.srcBlockingQueue = src;
            this.outBQueue = des;           
            this.state = minganState;
        }

        @Override
        public void run() {
            WBObject wbObject = null;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    wbObject = srcBlockingQueue.take();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.error(ex);
                }
                state.processNum++;
                if (istree && wbObject != null) {

                    Map<Integer, ArrayList<String>> matc = checkSensitive(wbObject, keyMatch);
                    int[] id = new int[matc.get(1).size()];
                    if (!matc.get(1).isEmpty()) {
                        MinGan mg = new MinGan();

                        String mgc = "";
                        //count++;
                        int index = 0;
                        for (String nr : matc.get(1)) {

                            if (!nr.trim().isEmpty()) {
                                mgc += ',' + nr;
                                id[index] = mgcmap.get(nr);
                                index++;

                            }
                        }
                        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒 E ");
//                        //Date d = new Date(wbObject.get_timestamp().longValue() * 1000);
                        Date d = new Date();
                        try {
                            Date dt = sdFormat.parse(sdFormat.format(d));
                            mg.setDate(dt);
                        } catch (ParseException e) {
                            logger.error(e);
                        }

                        mg.setId(wbObject.getMid());
                        mg.setComment(wbObject.getMnr());
                        //mg.setMgcId(id.substring(1));
                        mg.setMgc_comment(mgc.substring(1));
                        mg.setSentiment(wbObject.getQgz());
                        mg.setClassify(wbObject.getFllx());
                        //System.out.println(mg.getClassify());

                        if (id != null) {
                            wbObject.setIsMg(1);
                            wbObject.setMgWords(id);
                        } else {
                            logger.debug("min gan ci id is null!");
                        }                       
//
                        try {
                            pushDefinedBlockingQueue.put(mg);
                            state.isMinganNum++;
                        } catch (InterruptedException ex) {
                            logger.error(ex);
                        }
                    }
                    
                    if (!matc.get(0).isEmpty()) {
                        MinGan mg = new MinGan();
                        String idsString = "";
                        String mgc = "";
                        //count++;
                        for (String nr : matc.get(0)) {
                            if (!nr.trim().isEmpty()) {
                                mgc += ',' + nr;
                                idsString += "," + mgcmap.get(nr);
                            }
                        }
                        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date d = new Date(wbObject.getPostTime()* 1000);
                        try {
                            Date dt = sdFormat.parse(sdFormat.format(d));
                            mg.setDate(dt);
                        } catch (ParseException e) {
                            logger.error(e);
                        }
                        //mg.setNumber("255555");
                        mg.setId(wbObject.getMid());
                        mg.setComment(wbObject.getMnr());
                        mg.setMgc_id(idsString.substring(1));
                        mg.setMgc_comment(mgc.substring(1));
                        try {
                            pushPredefineBlockingQueue.put(mg);
                            //System.out.println(mg.toString());
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            //Logger.getLogger(MinGanInteface.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        state.isPredefineNum++;

                    }
                    
                    try {
                        outBQueue.put(wbObject);
                        //System.out.println(wbObject.toString());
                    } catch (InterruptedException ex) {
                        logger.error(ex);
                    }

                }
            }
        }
    }

    public void runSensitive() {
        minganThreadStates.clear();
        sensitiveThreads.clear();
      
        for (int i = 0; i < gConfig.maxSensitiveThreadCount; i++) {
            PerMinGanThreadState minganState = new PerMinGanThreadState();
            minganThreadStates.add(minganState);
            MinGanThread sensitiveThread = new MinGanThread(inputWBOBlockingQueues.get(i % gConfig.maxBlockQueue),
                    outputSensitiveWBObjectList.get(i % gConfig.maxBlockQueue),
                    minganState);
            Thread sentTread = new Thread(sensitiveThread);
            sensitiveThreads.add(sentTread);
            sentTread.start();
            logger.trace("start sensitive thread " + i + " " + sentTread.toString() );
        }        
    }

    public void finish() {
        System.out.print("看看进来没" + "++++++++++++++");
        for (int i = 0; i < gConfig.maxSensitiveThreadCount; i++) {
            sensitiveThreads.get(i).interrupt();
        }
    }
}
