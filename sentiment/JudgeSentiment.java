package sentiment;

import global.WBObject;
import global.GlobalConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sentiment.Constants.*;
import java.util.concurrent.BlockingQueue;

public class JudgeSentiment {

    private static Logger logger;
    private WordsLoader wordsLoader;
    private static final String POS_TAG = "positive";
    private static final String NEG_TAG = "negative";

    private GlobalConfig gConfig;

    private ArrayList<LinkedBlockingQueue<WBObject>> inputSplitedObjectBlockingQueues;
    private ArrayList<LinkedBlockingQueue<WBObject>> outputForClusterBlockingQueues;    
    private ArrayList<LinkedBlockingQueue<WBObject>> outputSentimentedBlockingQueues;
    
    private ArrayList<Thread> sentimentThreads;
    private ArrayList<PerSentimentThreadState> sentimentStates;

    public JudgeSentiment(GlobalConfig globalConfig, ArrayList<LinkedBlockingQueue<WBObject>> inputWBOBQList) {
        
        logger = LogManager.getLogger(JudgeSentiment.class);
        this.gConfig = globalConfig;
        this.wordsLoader = new WordsLoader();
        this.inputSplitedObjectBlockingQueues = inputWBOBQList;
        
        this.sentimentThreads = new ArrayList<>();
        this.sentimentStates = new ArrayList<>();
        
        this.outputForClusterBlockingQueues = new ArrayList<>();
        this.outputSentimentedBlockingQueues = new ArrayList<>();
        for (int i = 0; i < gConfig.maxBlockQueue; i++) {
            LinkedBlockingQueue<WBObject> clusterBQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
            outputForClusterBlockingQueues.add(clusterBQueue);
            LinkedBlockingQueue<WBObject> resultBlockingQueue = new LinkedBlockingQueue<>(gConfig.maxBlockingQueueCapacity);
            outputSentimentedBlockingQueues.add(resultBlockingQueue);
        }
    }
   
    public void runSentiment() {
        sentimentStates.clear();
        sentimentThreads.clear();
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            PerSentimentThreadState psts = new PerSentimentThreadState();
            sentimentStates.add(psts);            
            SentimentThread sentimentThread = new SentimentThread(inputSplitedObjectBlockingQueues.get(i % gConfig.maxBlockQueue),
                    outputForClusterBlockingQueues.get(i % gConfig.maxBlockQueue),
                    outputSentimentedBlockingQueues.get(i % gConfig.maxBlockQueue), psts);
            Thread sentTread = new Thread(sentimentThread);
            sentimentThreads.add(sentTread);
            sentTread.start();
            logger.trace("run sentiment thread " + i + " "+ sentTread.toString());
        }
    }

    public long[] getSentimentCount() {
        long[] count = new long[4];
        for (int i = 0; i < count.length; i++) {
            count[i] = 0;
        }
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            count[0] += sentimentStates.get(i).pos;
        }
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            count[1] += sentimentStates.get(i).neg;
        }
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            count[2] += sentimentStates.get(i).neutral;
        }
        for (int i = 0; i < gConfig.maxClassifyThreadCount; i++) {
            count[3] += sentimentStates.get(i).unkonwn;
        }
        return count;
    }

    public ArrayList<LinkedBlockingQueue<WBObject>> getOutputForClusterBlockingQueues() {
        return outputForClusterBlockingQueues;
    }

    public ArrayList<LinkedBlockingQueue<WBObject>> getOutputSentimentedBlockingQueues() {
        return outputSentimentedBlockingQueues;
    }
    
    private Map<String, Double> getJudgments(String sentence) {
        Map<String, Double> result = new HashMap<>();
        result.put(POS_TAG, 0.0D);
        result.put(NEG_TAG, 0.0D);
        if (sentence.trim().isEmpty()) {
            return result;
        }

        List<String> rawWords = Arrays.asList(sentence.split("|"));
        if (rawWords.isEmpty()) {
            return result;
        }

        Map<String, Double> rawScore = judgeTermsList(rawWords);
        result = addScore(result, rawScore);

        return result;
    }

    private Map<String, Double> addScore(Map<String, Double> first,
            Map<String, Double> second) {
        // TODO Auto-generated method stub
        Map<String, Double> result = new HashMap<String, Double>();
        result.put(POS_TAG, 0.0D);
        result.put(NEG_TAG, 0.0D);
        double score = 0.0D;
        if (first.containsKey(POS_TAG) && second.containsKey(POS_TAG)) {
            score = first.get(POS_TAG) + second.get(POS_TAG);
            result.put(POS_TAG, score);
        }
        if (first.containsKey(NEG_TAG) && second.containsKey(NEG_TAG)) {
            score = first.get(NEG_TAG) + second.get(NEG_TAG);
            result.put(NEG_TAG, score);
        }
        return result;
    }

    private Map<String, Double> judgeTermsList(List<String> rawWordsList) {
        // TODO Auto-generated method stub
        int size = rawWordsList.size();
        double pos = 0.0D;
        double neg = 0.0D;

        for (int i = 0; i < size; i++) {
            WordType wType = getWordType(rawWordsList.get(i));

            switch (wType) {
                case NEUTRAL_WORD:
                    double weight = getWordWeight(rawWordsList.get(i));
                    if (weight < 0) {
                        neg -= weight;
                    } else {
                        pos += weight;
                    }
                    break;
                case POS:
                    pos += Constants.NORMAL_WEIGHT;
                    break;
                case NEG:
                    neg += Constants.NORMAL_WEIGHT;
                    break;
                default:
                    break;
            }
        }

        Map<String, Double> result = new HashMap<String, Double>();
        result.put(POS_TAG, pos);
        result.put(NEG_TAG, neg);
        return result;

    }

    private double getWordWeight(String word) {
        // TODO Auto-generated method stub
        WordType wType = getWordType(word);
        if (wType == WordType.POS || wType == WordType.NEG) {
            return Constants.NORMAL_WEIGHT;
        }
        if (wType == WordType.NEUTRAL_WORD) {
            double posPMIScore = wordsLoader.getPosPMIScoreOfWord(word);
            double negPMIScore = wordsLoader.getNegPMIScoreOfWord(word);
            if (posPMIScore == 0.0D) {
                return -Constants.NORMAL_WEIGHT * negPMIScore;
            }
            if (negPMIScore == 0.0D) {
                return Constants.NORMAL_WEIGHT * posPMIScore;
            }
            if (posPMIScore / negPMIScore < 0.68D) {
                return -Constants.NORMAL_WEIGHT * (negPMIScore - posPMIScore);
            }
            if (negPMIScore / posPMIScore < 0.68D) {
                return Constants.NORMAL_WEIGHT * (posPMIScore - negPMIScore);
            }
        }
        return 0.0D;
    }

    private WordType getWordType(String word) {
        // TODO Auto-generated method stub
        if (wordsLoader.isPosSenWord(word)) {
            return WordType.POS;
        }

        if (wordsLoader.isNegSenWord(word)) {
            return WordType.NEG;
        }

        return WordType.NEUTRAL_WORD;
    }

    private SentimentType judgeSenType(Map<String, Double> score) {
        SentimentType result = SentimentType.NEUTRAL;
        if (score.isEmpty()) {
            return result;
        }
        double pos = score.get(POS_TAG) + 1.0D;
        double neg = score.get(NEG_TAG) + 0.00001D;
        double res = (Math.abs(pos - neg) / (pos + neg));
        if (res > Constants.SEN_THRESHOLD) {
            if (pos >= neg) {
                result = SentimentType.POSITIVE;
            } else {
                result = SentimentType.NEGATIVE;
            }
        } else {
            result = SentimentType.NEUTRAL;
        }

        return result;
    }

    class SentimentThread implements Runnable {

        private LinkedBlockingQueue<WBObject> srcBlockingQueue;
        private LinkedBlockingQueue<WBObject> resultBlockingQueue;
        private BlockingQueue<WBObject> clusterBlockQ;
        private PerSentimentThreadState pSenStates;

        public SentimentThread(LinkedBlockingQueue<WBObject> srcBQ,
                LinkedBlockingQueue<WBObject> clusterBQ,
                LinkedBlockingQueue<WBObject> rBQ,
                PerSentimentThreadState pst) {
            
            logger.trace("Create judge sentiment thread!");
            this.srcBlockingQueue = srcBQ;
            this.clusterBlockQ = clusterBQ;
            this.resultBlockingQueue = rBQ;
            this.pSenStates = pst;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WBObject so = srcBlockingQueue.take();
                    if (so.fllx == 1) {
                        so.setQgz(4);
                        resultBlockingQueue.put(so);
                        continue;
                    }
                    SentimentType type = judgeSenType(getJudgments(so.getSplited()));
                    so.setQgz(type.value());
                    
                    resultBlockingQueue.put(so);
                    clusterBlockQ.put(so);                   
                    
                    switch (type.value()) {
                        case 1:
                            pSenStates.pos++;
                            break;
                        case 2:
                            pSenStates.neg++;
                            break;
                        case 3:
                            pSenStates.neutral++;
                            break;
                        default:
                            pSenStates.unkonwn++;
                            break;
                    }
                } catch (InterruptedException e) {
                    logger.error(e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

 

}
