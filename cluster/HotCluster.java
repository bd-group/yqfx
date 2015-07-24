package cluster;

import global.WBObject;
import global.GlobalConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HotCluster {

    private static Logger logger;
    private GlobalConfig gConfig;

    private int percent = 50;
    private double threshold = percent / 100;
    private ReadWriteLock rwl = new ReentrantReadWriteLock();

    private ArrayList<PerClusterThreadState> perThreadStates;
    private ArrayList<PerClusterThreadState> perHelpThreadStates;

    private ArrayList<Thread> clusterThreads;
    private ArrayList<Thread> helpClusterThreads;
    private ArrayList<Thread> redistributionThreads;

    private ArrayList<LinkedBlockingQueue<WBObject>> inputObjectBlockingQueues;
    private ArrayList<ArrayList<ArrayList<Center>>> resultCenterLists;
    private ArrayList<LinkedBlockingQueue<Message>> messageBlockingQueues;
    
    private LinkedBlockingQueue<Center> historyHotCenters;
    
    private String regEx;
    private Pattern pattern; 

    public HotCluster(GlobalConfig globalConfig, ArrayList<LinkedBlockingQueue<WBObject>> soBQList) {

        logger = LogManager.getLogger(HotCluster.class);
        this.gConfig = globalConfig;
        this.inputObjectBlockingQueues = soBQList;
        
        this.perThreadStates = new ArrayList<>();
        this.perHelpThreadStates = new ArrayList<>();
        this.clusterThreads = new ArrayList<>();
        this.helpClusterThreads = new ArrayList<>();
        this.redistributionThreads = new ArrayList<>();
        
        this.resultCenterLists = new ArrayList<>();
        this.messageBlockingQueues = new ArrayList<>();
        
        this.historyHotCenters = new LinkedBlockingQueue<>();
        
        this.regEx = "[\u4e00-\u9fa5]";
        this.pattern = Pattern.compile(regEx);
    }

    public ArrayList<LinkedBlockingQueue<Message>> getMessageBlockingQueues() {
        return messageBlockingQueues;
    }

    public LinkedBlockingQueue<Center> getHistoryHotCenters() {
        return historyHotCenters;
    }
    
    public void getClusterStates() {
        long clusterCount = 0;
        long jaccardCount = 0;
        long ignbylenCount = 0;
        long dupCount = 0;
        for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {
            clusterCount += perThreadStates.get(i).clusterNum;
            //clusterCount += perHelpThreadStates.get(i).clusterNum;

            jaccardCount += perThreadStates.get(i).jaccardNum;
            //jaccardCount += perHelpThreadStates.get(i).jaccardNum;

            ignbylenCount += perThreadStates.get(i).ignbylenNum;
            //ignbylenCount += perHelpThreadStates.get(i).ignbylenNum;

            dupCount += perThreadStates.get(i).dupNum;
            //dupCount += perHelpThreadStates.get(i).dupNum;
        }
        System.out.println("Cluster Number is: " + clusterCount + "; Compute Jaccard Number is: "
                + jaccardCount + "; ignore by length Number is: " + ignbylenCount
                + "; duplication Number is " + dupCount);
    }

    public long checkClusteredNum() {
        long clusteredCount = 0;
        for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {
            clusteredCount += perThreadStates.get(i).clusterNum;
                //clusteredCount += perHelpThreadStates.get(i).clusterNum;
            //System.out.println("Cluster thread " + i + " has clustered " + perThreadStates.get(i).clusterNum);
        }
        return clusteredCount;
    }

   public void runCluster(int threhold) {
        // set threhold
        setSimilarityThreshold(threhold);
        // initialize message blocking queue list
        for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {
            LinkedBlockingQueue<Message> lbQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
            messageBlockingQueues.add(lbQueue);
        }
        // run redistribution threads
        for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {
            RedistributionThread redistributionThread = new RedistributionThread(inputObjectBlockingQueues.get(i % gConfig.maxBlockQueue), 
                    messageBlockingQueues);
            Thread reThread = new Thread(redistributionThread);
            redistributionThreads.add(reThread);
            reThread.start();
            logger.trace("run cluster redistribution thread " + i + " " + reThread.toString());
        }
        // run cluster threads and help cluster threads
        for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {
            PerClusterThreadState pState = new PerClusterThreadState();
            perThreadStates.add(pState);

            ArrayList<ArrayList<Center>> reList = initCenterList();
            resultCenterLists.add(reList);

            ClusterThread clusterThread = new ClusterThread(messageBlockingQueues.get(i), pState, reList);
            Thread cThread = new Thread(clusterThread);
            clusterThreads.add(cThread);
            cThread.start();
            logger.trace("run cluster main thread " + i + " " + cThread.toString());
            HelpClusterThread clusterThreadHelp = new HelpClusterThread(messageBlockingQueues.get(i), pState, reList);
            Thread cThreadHelp = new Thread(clusterThreadHelp);
            helpClusterThreads.add(cThreadHelp);
            logger.trace("run cluster help thread " + i + " " + clusterThreadHelp.toString());
        }
    }
    private ArrayList<ArrayList<Center>> initCenterList() {
        ArrayList<ArrayList<Center>> list = new ArrayList<>();
        for (int i = 0; i < 41; i++) {
            ArrayList<Center> reList = new ArrayList<>();
            list.add(reList);
        }
        return list;
    }

//    public ArrayList<Center> getHotsList() {
//        if (resultCenterLists.isEmpty()) {
//            return null;
//        }
//        ArrayList<ArrayList<Center>> hotList = new ArrayList<>();
//        for (int i = 0; i < 41; i++) {
//            ArrayList<Center> reList = new ArrayList<>();
//            hotList.add(reList);
//        }
//        for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {
//            ArrayList<ArrayList<Center>> list = resultCenterLists.get(i);
//            if (list != null && list.size() > 0) {               
//                for (int j = 0; j < 41; j++) {
//                    hotList.get(j).addAll(list.get(j));
//                }             
//            }
//        }
//
//        Comparator<Center> comparator = new Comparator<Center>() {
//            @Override
//            public int compare(Center c1, Center c2) {
//                return c2.siftCount - c1.siftCount;
//            }
//        };
//        ArrayList<Center> resultList = new ArrayList<>();
//        for (int i = 0; i < 41; i++) {
//            if (hotList.get(i).size() > 1) {
//                System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
//                Collections.sort(hotList.get(i), comparator);
//            }
//            if (hotList.get(i).size() < 10) {
//                resultList.addAll(hotList.get(i));
//            } else {
//                resultList.addAll(hotList.get(i).subList(0, 10));
//            }
//        }
//        return resultList; 
//    }
    
     public ArrayList<Center> getHotsList() {
        if (resultCenterLists.isEmpty()) {
            return null;
        }
        ArrayList<Center> hotList = new ArrayList<>();
       
        for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {
            ArrayList<ArrayList<Center>> list = resultCenterLists.get(i);
            if (list != null && list.size() > 0) {               
                for (int j = 0; j < 41; j++) {
                    hotList.addAll(list.get(j));
                }             
            }
        }

        Comparator<Center> comparator = new Comparator<Center>() {

             @Override
            public int compare(Center c1, Center c2) {
                if (c2.hotScore - c1.hotScore > 0) {
                    return 1;
                }
                else if (c2.hotScore - c1.hotScore < 0) {
                    
                    return -1;
                }
                else {
                return  0;
            }
            }
        };
        
        ArrayList<Center> resultList = new ArrayList<>();
        Collections.sort(hotList, comparator);
            
            if (hotList.size() < gConfig.displayCount) {
                resultList.addAll(hotList);
            } else {
                resultList.addAll(hotList.subList(0, gConfig.displayCount));
            }
        
        return resultList; 
    }

    private void setSimilarityThreshold(int t) {
        if (t > 100) {
            t = 100;
        }
        if (t < 1) {
            t = 1;
        }
        this.percent = t;
    }

    class RedistributionThread implements Runnable {

        private LinkedBlockingQueue<WBObject> srcQueue;
        private ArrayList<LinkedBlockingQueue<Message>> desQueues;

        public RedistributionThread(LinkedBlockingQueue<WBObject> splitedBQ,
                ArrayList<LinkedBlockingQueue<Message>> messageBQs) {
            logger.trace("Create redistribution thread!");
            this.srcQueue = splitedBQ;
            this.desQueues = messageBQs;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WBObject so = srcQueue.take();
                    for (int i = 0; i < gConfig.maxClusterThreadCount; i++) {    					
                        if (so.getLength() <= 70) {
                            Message m2put = splitObject2Message(so);
                            if (m2put != null) {
                                desQueues.get(0).put(m2put);
                            }
                            break;
                        }
                        if (so.getLength() <= 140) {
                            Message m2put = splitObject2Message(so);
                            if (m2put != null) {
                                desQueues.get(1).put(m2put);
                            }
                            break;
                        }
                        if (so.getLength() <= 210) {
                            Message m2put = splitObject2Message(so);
                            if (m2put != null) {
                                desQueues.get(2).put(m2put);
                            }
                            break;
                        } else {
                            Message m2put = splitObject2Message(so);
                            if (m2put != null) {
                                desQueues.get(3).put(m2put);
                            }
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    logger.error(e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    private boolean isContainsChinese(String str) {
        Matcher matcher = this.pattern.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }

    private Message splitObject2Message(WBObject so) {
        Message m = new Message();
        m.id = so.getMid();
        m.hashcode = so.getSplited().hashCode();
        m.contentString = so.getMnr();
        String[] sp = so.getSplited().split("\\|");

        HashSet<String> wSet = new HashSet<>();
        for (String str : sp) {
            if (str.trim().isEmpty()) {
                continue;
            }
            if (isContainsChinese(str)) {
                wSet.add(str);
            }            
        }
        m.wordsSet = wSet;
        m.classify = so.getFllx();
        m.sentiment = so.getQgz();
        m.yysId = so.getSrc();
        if (wSet.size() < 1) {
            m = null;
        }
        return m;
    }

    private void computeCluster(Message message, ArrayList<Center> centerList, PerClusterThreadState pcts) {
        if (centerList.isEmpty()) {
            rwl.writeLock().lock();
            try {
                centerList.add(message2Center(message));
            } finally {
                rwl.writeLock().unlock();
            }
        } else {
            int iCur = 0, count = centerList.size();
            boolean isSimi = false;
            while (iCur < count) {
                if (message.wordsSet.size() * this.percent <= (centerList.get(iCur).wordsSet.size() * 100)) {
                    isSimi = updateCenter(message, centerList, iCur, pcts);
                    if (isSimi) {
                        break;
                    }
                } else {
                    pcts.ignbylenNum++;
                }
                iCur++;
            }// while

            if (!isSimi) {
                rwl.writeLock().lock();
                try {
                    centerList.add(message2Center(message));
                } finally {
                    rwl.writeLock().unlock();
                }
            }

        }// else
    }

    private Center message2Center(Message message) {
        Center center = new Center();
        center.clusterName = message.contentString;
        center.hashcode = message.hashcode;
        center.wordsSet = message.wordsSet;
        center.idSet = message.id;
        center.id = message.id;
        center.Count = 1;
        center.siftCount = 1;
        center.classify = message.classify;
        center.sentiment = message.sentiment;
        center.yysId = message.yysId;
        center.generateDate = new Date();
        center.updateDate = center.generateDate;
        return center;
    }

    private boolean updateCenter(Message s, ArrayList<Center> centerList, int idx, PerClusterThreadState pcts) {
        
        boolean isSimilar = false;
        Center c = centerList.get(idx);

        if (s.hashcode == c.hashcode) {
            pcts.dupNum++;
            rwl.writeLock().lock();
            try {
                c.idSet += ",";
                c.idSet += s.id;
                c.Count += 1;
                c.siftCount += 1;
                c.updateDate = new Date();
                isSimilar = true;
            } finally {
                rwl.writeLock().unlock();
            }
        } else {
            pcts.jaccardNum++;
            if (jaccardCoefficient(c.wordsSet, s.wordsSet, percent)) {
                rwl.writeLock().lock();
                try {
                    c.idSet += ",";
                    c.idSet += s.id;
                    c.Count += 1;
                    c.siftCount += 1;
                    c.updateDate = new Date();
                    isSimilar = true;
                } finally {
                    rwl.writeLock().unlock();
                }
            } else {
                isSimilar = false;
            }
        }
//        System.out.println(c.toString());
        return isSimilar;
    }

    private boolean jaccardCoefficient(HashSet<String> target, HashSet<String> tobeCluster, int percent) {

        int intersectionCount = 0;
        for (String s : target) {
            if (tobeCluster.contains(s)) {
                intersectionCount++;
            }
        }
        return intersectionCount * 100 >= (target.size() + tobeCluster.size() - intersectionCount) * percent;
    }

    class ClusterThread implements Runnable {

        private LinkedBlockingQueue<Message> clusterSrcQueue;
        private PerClusterThreadState pctState;
        private ArrayList<ArrayList<Center>> resultList;
        private Date startTime;

        public ClusterThread(LinkedBlockingQueue<Message> blockQ,
                PerClusterThreadState p, ArrayList<ArrayList<Center>> re) {
            logger.trace("Create cluster thread!");
            this.clusterSrcQueue = blockQ;
            this.pctState = p;
            this.resultList = re;
        }

        @Override
        public void run() {
            this.startTime = new Date();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Message message = clusterSrcQueue.take();

                    if (message != null && message.wordsSet.size() > 0) {
                        computeCluster(message, resultList.get(message.classify), pctState);                        
                    }
                    pctState.clusterNum++;
                    pctState.classifyCountVector[message.classify]++;
                    
                    if (pctState.clusterNum % 10000 == 0) {
   
                        rwl.writeLock().lock();
                        rwl.readLock().lock();
                        try {
                            long time = 60000;
                            for (int i = 0; i < 41; i++) {
                                switch (i) {
                                    case 1:
                                        time = 1;
                                        break;
                                    case 34:
                                        time = 60000;
                                        break;
                                    case 15:
                                    case 17:
                                    case 18:
                                    case 19:
                                    case 20:
                                    case 21:
                                    case 22:
                                    case 23:
                                    case 24:
                                    case 25:
                                        time = 600000;
                                        break;
                                    default:
                                        time = 180000;
                                        break;
                                }
                                if (!resultList.get(i).isEmpty()) {
                                    resultList.set(i, siftCenterList(resultList.get(i), time));
                                }
                            }
                            
                        } finally {
                            rwl.readLock().unlock();
                            rwl.writeLock().unlock();
                        }                       
                    }
                } catch (Exception e) {
                    logger.error(e);
                    Thread.currentThread().interrupt();
                }
            }
            pctState.clusterIsExited = true;
        }

        private ArrayList<Center> siftCenterList(ArrayList<Center> acl, long timeMS) {
            if (acl == null) {
                return  new ArrayList<>();
            }
            if (acl.size() < 1) {
                return acl;
            }
//            System.out.println("classify is " + acl.get(0).classify + ", center list size is " + acl.size());
            int weight = 20;
            switch (acl.get(0).classify) {
                case 1:
                    weight = 1;
                    break;
                case 34:
                    weight = 4;
                    break;
                case 15:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                    weight = 40;
                    break;
                default:
                    weight = 20;
                    break;          
                
            }
                Date d = new Date();   
                ArrayList<Center> newList = new ArrayList<>();
                for (Center c : acl) {
                    int subSiftCout = c.siftCount - 1;
                    
//                    if (subSiftCout > 0) {
//                        c.siftCount = subSiftCout;
//                     
//                        newList.add(c);
//                    }
//                    else if ( c.classify != 34 && (d.getTime() - c.updateDate.getTime()) < timeMS) {
//                        c.siftCount = subSiftCout;
//                      
//                        newList.add(c);
//                    }
                     if (d.getTime() - c.updateDate.getTime() < timeMS) {
                        c.siftCount = subSiftCout;
                     
                        newList.add(c);
                    }
                    else if ( c.classify != 34 && (subSiftCout > 0)) {
                        c.siftCount = subSiftCout;
                      
                        newList.add(c);
                    } else if (c.classify != 34 && c.Count > 10) {
                        try {
                            c.isNotHotTimeDate = d;
                            c.isHotTimeDate = c.generateDate;
                            historyHotCenters.put(c);
                        } catch (InterruptedException ex) {
                            java.util.logging.Logger.getLogger(HotCluster.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                for (Center c : newList) {
                    double factor = Math.log10(c.Count) * weight;
                    long existingTime = (d.getTime() - c.generateDate.getTime()) / 1000;
                    long lastUpdate = (d.getTime() - c.updateDate.getTime()) / 1000;
                    double denominator = Math.pow( (existingTime + 1) - ( (existingTime - lastUpdate) / 2), 1.5);
                    c.hotScore = factor / denominator;
//                    System.out.println("hot score is : " + c.hotScore);
                }

//                Comparator<Center> comparator = new Comparator<Center>() {
//                    @Override
//                    public int compare(Center c1, Center c2) {
//                        return c2.siftCount - c1.siftCount;
//                    }
//                };
//                Collections.sort(newList, comparator);
                acl = newList;
//                System.out.println("AFTER classify is " + acl.get(0).classify + ", center list size is " + acl.size());
                return acl;
          
        }
        
    }

    class HelpClusterThread implements Runnable {

        private LinkedBlockingQueue<Message> clusterSrcQueue;
        private PerClusterThreadState pctState;
        private ArrayList<ArrayList<Center>> resultList;
        private Date startTime;

        public HelpClusterThread(LinkedBlockingQueue<Message> blockQ,
                PerClusterThreadState p, ArrayList<ArrayList<Center>> re) {
//            System.out.println("Create Help cluster thread!");
            this.clusterSrcQueue = blockQ;
            this.pctState = p;
            this.resultList = re;
        }

        @Override
        public void run() {
            this.startTime = new Date();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Message message = clusterSrcQueue.take();

                    if (message != null && message.wordsSet.size() > 0) {
                        computeCluster(message, resultList.get(message.classify), pctState);
                        //pctState.clusterNum++;
                    }
                    pctState.clusterNum++;
                    pctState.classifyCountVector[message.classify]++;
                } catch (Exception e) {

                    Thread.currentThread().interrupt();
                }
            }
            pctState.clusterIsExited = true;
        }

    }

}
    
