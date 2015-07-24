/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package splitwords;

import global.WBObject;
import global.GlobalConfig;
import ICTCLAS.I3S.AC.ICTCLAS50;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * It needs libICTCLAS50.so, the so file must have been copied to /usr/lib
 *
 * @author daniel
 */
public class SplitWords {

    private static Logger logger;
    private GlobalConfig gConfig;
    private String regEx;
    private Pattern pattern;

    private String stopWordsPath;
    private String stopWordsPattern;

    private ArrayList<LinkedBlockingQueue<WBObject>> inputWBBlockingQList;
    private ArrayList<LinkedBlockingQueue<WBObject>> outputSplitedWBBlockingQList;

    private ArrayList<PerSplitThreadState> perThreadStates;
    private ArrayList<Thread> splitThreads;

    private ICTCLAS50 ictclas = new ICTCLAS50();

    public SplitWords(GlobalConfig gc, ArrayList<LinkedBlockingQueue<WBObject>> obtainSrcBQList) {
        logger = LogManager.getLogger(SplitWords.class);
        this.gConfig = gc;
        this.inputWBBlockingQList = obtainSrcBQList;
        this.regEx = "[\u4e00-\u9fa5]";
        this.pattern = Pattern.compile(regEx);
        this.outputSplitedWBBlockingQList = new ArrayList<>();
        this.perThreadStates = new ArrayList<>();
        this.splitThreads = new ArrayList<>();
    }

    public boolean initalize() {
        if (gConfig.destopwords) {
            this.stopWordsPath = this.gConfig.dictDir + "/stopwords.txt";
            this.stopWordsPattern = loadStopWords(stopWordsPath);
            logger.info("split words will wipe off stopwords !");
        }

        String argu = ".";
        try {
            if (ictclas.ICTCLAS_Init(argu.getBytes("UTF8")) == false) {
                logger.error("ERROR initalize split words failed! check ICTCLAS.log, please!");
                return false;
            } else {
                // initalize output blocking queues list
                for (int i = 0; i < gConfig.maxBlockQueue; i++) {
                    LinkedBlockingQueue<WBObject> toClassifyBQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
                    outputSplitedWBBlockingQList.add(toClassifyBQueue);
                }

                // run split words threads
                for (int i = 0; i < gConfig.maxSplitWordThreadCount; i++) {
                    PerSplitThreadState ptState = new PerSplitThreadState();
                    perThreadStates.add(ptState);
                    SplitThread sThread = new SplitThread(inputWBBlockingQList.get(i % gConfig.maxBlockQueue),
                            outputSplitedWBBlockingQList.get(i % gConfig.maxBlockQueue), ptState);
                    Thread splitThread = new Thread(sThread);
                    splitThreads.add(splitThread);
                    splitThread.start();
                }
            }

            logger.debug("initalize split words threads success!");
        } catch (UnsupportedEncodingException ex) {
            java.util.logging.Logger.getLogger(SplitWords.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public ArrayList<LinkedBlockingQueue<WBObject>> getOutputSplitedBlockingQueuesList() {
        return this.outputSplitedWBBlockingQList;
    }

    public long getSplitedTotalNum() {
        long splitedCount = 0;
        for (int i = 0; i < gConfig.maxSplitWordThreadCount; i++) {
            splitedCount += perThreadStates.get(i).splitNum;
        }
        return splitedCount;
    }

    public long getFilteredTotalNum() {
        long filteredCount = 0;
        for (int i = 0; i < gConfig.maxSplitWordThreadCount; i++) {
            filteredCount += perThreadStates.get(i).filteredNum;
        }
        return filteredCount;
    }

    public Map<String, Long> getYYSCount() {
        Map<String, Long> map = new HashMap<>();

        for (int i = 0; i < gConfig.maxSplitWordThreadCount; i++) {
            if (perThreadStates.get(i).srcCountMap == null) {
                return  map;
            }
            Map<String, Long> cloneMap = new HashMap<>(perThreadStates.get(i).srcCountMap);

            for (String key : cloneMap.keySet()) {
//                if (key.equals("")) {
//                    key = "其他";
//                }
                if (map.containsKey(key)) {
//                    System.out.println(map+ "\t" + perThreadStates.get(i).srcCountMap.get(key) );
                    map.put(key, map.get(key) + cloneMap.get(key));
                } else {
                    map.put(key, cloneMap.get(key));
                }
            }

        }

        return map;
    }

    // import user dictionary 
    public void importUserDict(String userDictFile) {
        byte[] usrdirb = userDictFile.getBytes();
        int nCount = ictclas.ICTCLAS_ImportUserDictFile(usrdirb, 3);
        System.out.println("Import user's words count is:" + nCount);
        logger.trace("Import user's words count is:" + nCount);
    }

    // split native
    private String splitNative(String input) throws Exception {
        byte[] nativeByte = ictclas.ICTCLAS_ParagraphProcess(input.getBytes("UTF8"), 0, 0);
        String nativeString = new String(nativeByte, 0, nativeByte.length, "UTF8");
        nativeString = nativeString.replaceAll("\\s+", "|");
        //System.out.println(nativeString);
        return nativeString;
    }

    private String splitFilterSign(String input) throws Exception {
        byte[] nativeByte = ictclas.ICTCLAS_ParagraphProcess(input.getBytes("UTF8"), 0, 0);
        String nativeString = new String(nativeByte, 0, nativeByte.length, "UTF8");
        // wipe off punctuation
        nativeString = nativeString.replaceAll("[\\pP]", "");

        nativeString = nativeString.replaceAll("\\s+", "|");

        return nativeString;
    }

    private String splitFilterStopWords(String input) throws Exception {
        byte[] nativeByte = ictclas.ICTCLAS_ParagraphProcess(input.getBytes("UTF8"), 0, 0);
        String nativeString = new String(nativeByte, 0, nativeByte.length, "UTF8");
        // \\p is unicode property P is sign characters
        nativeString = nativeString.replaceAll("[\\pP]", "");
        nativeString = nativeString.replaceAll(stopWordsPattern, "");

        nativeString = nativeString.replaceAll("\\s+", "|");

        return nativeString;
    }

    private String loadStopWords(String filePath) {
        BufferedReader reader = null;
        String pat = "";
        try {
            File file = new File(filePath);
            reader = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                pat += line + "|";
            }
            logger.info("stopwords pattern is: " + pattern);

        } catch (FileNotFoundException ex) {
            logger.error(ex);
            java.util.logging.Logger.getLogger(SplitWords.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.error(ex);
            java.util.logging.Logger.getLogger(SplitWords.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                logger.error(ex);
                java.util.logging.Logger.getLogger(SplitWords.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return pat;
    }

    private boolean isContainsChinese(String str) {
        Matcher matcher = this.pattern.matcher(str);
        boolean flg = false;
        if (matcher.find()) {
            flg = true;
        }
        return flg;
    }

    // Multiple thread
    class SplitThread implements Runnable {

        private BlockingQueue<WBObject> srcBlockQ;
        private BlockingQueue<WBObject> classifyBlockQ;
        private PerSplitThreadState ptState;

        public SplitThread(BlockingQueue<WBObject> bq,
                BlockingQueue<WBObject> classifybq,
                PerSplitThreadState pts) {
            logger.trace("Create split thread!");

            this.srcBlockQ = bq;
            this.classifyBlockQ = classifybq;
            this.ptState = pts;
        }
        
        private String whichClient(String input) {
            if (input.contains("新浪") || input.contains("微博")) {
                return  "新浪微博";
            } else if (input.contains("iphone") || input.contains("iPhone")) {
                return "iphone客户端";
            } else if (input.contains("Android") || input.contains("android")) {
                return "Android客户端";
            } else if (input.contains("手机") 
                    || input.contains("HTC")
                    || input.contains("中兴")
                    || input.contains("三星")
                    || input.contains("诺基亚")
                    || input.contains("华为")
                    || input.contains("小米")
                    || input.contains("魅族")
                    || input.contains("coolpad")
                    || input.contains("Coolpad")
                    || input.contains("联想")) {
                return "手机客户端";
            } else if (input.contains("ipad")  || input.contains("iPad")) {
                return "ipad客户端";
            } else if (input.contains("客户端")){
                return "第三方客户端";
            } else {
                return  "第三方网站";
            }           
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WBObject so = srcBlockQ.take();
                    if (so == null) {
                        continue;
                    }
                    if (!isContainsChinese(so.mnr)) {
                        ptState.filteredNum++;
                        so.fllx = 1;
                        classifyBlockQ.put(so);
                        continue;
                    }
                    if (so.length < 5) {
                        ptState.filteredNum++;
                        so.fllx = 1;
                        classifyBlockQ.put(so);
                        continue;
                    }

                    if (gConfig.destopwords) {
                        so.setSplited(splitFilterStopWords(so.getMnr()));
                        classifyBlockQ.put(so);
                    } else {
                        so.setSplited(splitNative(so.getMnr()));
                        classifyBlockQ.put(so);
                    }

                    ptState.splitNum++;
                    String client = whichClient(so.getSrc());
                    if (ptState.srcCountMap.containsKey(client)) {
                        long count = ptState.srcCountMap.get(client) + 1;
                        ptState.srcCountMap.put(client, count);
//                        System.out.println(ptState.srcCountMap.toString());
                    } else {
                        ptState.srcCountMap.put(client, 1L);
//                        System.out.println(ptState.srcCountMap.toString());
                    }
//                    if (ptState.splitNum % 1000 == 0) {
//                        System.out.println(ptState.srcCountMap.toString());
//                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }

    }

}
