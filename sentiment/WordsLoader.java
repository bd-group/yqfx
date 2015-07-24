package sentiment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WordsLoader {

    private static Logger logger;
    private Map<String, Double> posSenWordsMap;

    private Map<String, Double> negSenWordsMap;

    // 加载词的PMI分值
    private Map<String, Double> posPMIScores;

    private Map<String, Double> negPMIScores;

    public WordsLoader() {
        logger = LogManager.getLogger(WordsLoader.class);
        loadWords();
    }

    public Map<String, Double> getPosSenWordsMap() {
        return posSenWordsMap;
    }

    public Map<String, Double> getNegSenWordsMap() {
        return negSenWordsMap;
    }

    public Map<String, Double> getPosPMIScores() {
        return posPMIScores;
    }

    public Map<String, Double> getNegPMIScores() {
        return negPMIScores;
    }

    public boolean isPosSenWord(String word) {
        return posSenWordsMap.containsKey(word);
    }

    public boolean isNegSenWord(String word) {
        return negSenWordsMap.containsKey(word);
    }

    public double getPosPMIScoreOfWord(String word) {
        if (posPMIScores.containsKey(word)) {
            return posPMIScores.get(word);
        }
        return 0.0D;
    }

    public double getNegPMIScoreOfWord(String word) {
        if (negPMIScores.containsKey(word)) {
            return negPMIScores.get(word);
        }
        return 0.0D;
    }

    private Map<String, Double> loadWordsWithWeight(String path) {
        Map<String, Double> results = new HashMap<String, Double>();
        String line = null;
        String word = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(path));
            BufferedReader br = new BufferedReader(is);
            while ((line = br.readLine()) != null) {
                word = line.trim();
                results.put(word, 0.0D);
            }
            br.close();
            is.close();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

        return results;
    }

    private Map<String, Double> loadPMIScores(String path) {
        Map<String, Double> results = new HashMap<String, Double>();
        String line = null;
        String word = null;
        double weight = 0.0D;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(path));
            BufferedReader br = new BufferedReader(is);
            while ((line = br.readLine()) != null) {
                String[] array = line.trim().split("=");
                if (array.length != 2) {
                    continue;
                }
                word = array[0];
                weight = Double.parseDouble(array[1]);
                results.put(word, weight);
            }
            br.close();
            is.close();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

        return results;
    }

//	private Set<String> loadWords(String path) {
//    	Set<String> results = new HashSet<String>();
//    	String line = null;
//    	String word = null;
//    	try {
//    		InputStreamReader is = new InputStreamReader(new FileInputStream(path),"UTF-8");             
//            BufferedReader br = new BufferedReader(is);
//            while((line = br.readLine()) != null){  
//                  word = line.trim();               
//                  results.add(word);  
//            }
//            br.close();
//            is.close();
//    	} catch(Exception e) {
//    		
//    	}
//    	
//    	return results;
//    }
    private void loadWords() {

        posSenWordsMap = loadWordsWithWeight(Constants.POS_SEN_DICT);

        negSenWordsMap = loadWordsWithWeight(Constants.NEG_SEN_DICT);

        posPMIScores = loadPMIScores(Constants.POS_PMI_SCORE);
        negPMIScores = loadPMIScores(Constants.NEG_PMI_SCORE);

    }

}
