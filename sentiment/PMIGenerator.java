package sentiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PMIGenerator {

    /*每条ID及分词后的结果*/
    private Map<Long, List<String>> data2Words = null;
    /*词语及其对应的文档ID*/
    private Map<String, List<Long>> wordIDList = null;
    private int totalWords = 0;

    // load words dict
    private WordsLoader wordsLoader = null;

    public PMIGenerator() {
        data2Words = new HashMap<Long, List<String>>();
        wordIDList = new HashMap<String, List<Long>>();
        wordsLoader = new WordsLoader();
    }

    /**
     * 从语料中初始化矩阵 矩阵的行为评论，矩阵的列为词到文档的索引 这里并没有对词进行Id化，简单起见，只是过滤了停用词等
     *
     * @param path
     */
    public void initFromCorpus(String path) {
        String line = null;
        String comment = null;
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                comment = line.trim();
                //可能要判断一下文本内容，是否空白符，长度等等
                List<String> words = Arrays.asList(comment.trim().split("\\s+"));
                System.out.println(comment);
                if (words.size() <= 1) {
                    continue;
                }
                totalWords += words.size();

                compute(words);
            }
            br.close();
            fr.close();
        } catch (Exception e) {

        }
    }

    private void compute(List<String> words) {
        // TODO Auto-generated method stub
        long cId = data2Words.size() + 1;
        data2Words.put(cId, words);
        for (String word : words) {
            if (wordIDList.containsKey(word)) {
                List<Long> dataIds = wordIDList.get(word);
                dataIds.add(cId);
            } else {
                List<Long> dataIds = new ArrayList<Long>();
                dataIds.add(cId);
                wordIDList.put(word, dataIds);
            }
        }
    }

    public double getPMIScore(String word1, String word2) {
        double score = 0.0D;
        List<Long> word1IDList = wordIDList.get(word1);
        List<Long> word2IDList = wordIDList.get(word2);

        if (word1IDList == null || word2IDList == null) {
            return score;
        }
        if (word1IDList.size() == 0 || word2IDList.size() == 0) {
            return score;
        }

        int word1Freq = word1IDList.size();
        int word2Freq = word2IDList.size();
        double word1Prob = div(word1Freq, totalWords, 8);
        double word2Prob = div(word2Freq, totalWords, 8);
        Map<Long, Integer> word1Map = list2Map(word1IDList);
        Map<Long, Integer> word2Map = list2Map(word2IDList);

        int uniFreq = 0;
        for (Map.Entry<Long, Integer> entry : word1Map.entrySet()) {
            long id = entry.getKey();
            if (word2Map.containsKey(id)) {
                uniFreq += Math.min(entry.getValue(), word2Map.get(id));
            }
        }
        if (uniFreq == 0) {
            return score;
        }
        double uniProb = div(uniFreq, totalWords, 8);

        return Math.log(div(uniProb, word1Prob * word2Prob, 8));
    }

    public void computePMI(String posPath, String negPath) {
        Map<String, Double> posWordsMap = wordsLoader.getPosSenWordsMap();
        Map<String, Double> negWordsMap = wordsLoader.getNegSenWordsMap();

        try {
            computePMIScore(posPath, posWordsMap);
            computePMIScore(negPath, negWordsMap);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    /**
     * 计算与情感词的pmi分数并输出到文件
     *
     * @param path
     * @throws IOException
     */
    private void computePMIScore(String path, Map<String, Double> senWordsMap) throws IOException {
        // TODO Auto-generated method stub
        FileWriter fw = new FileWriter(path);
        Map<String, Double> scores = new HashMap<String, Double>();
        double weight = 0.0D;
        for (Map.Entry<String, Double> entry : senWordsMap.entrySet()) {
            String word = entry.getKey();
            List<String> coWords = getCoWords(word);
            if (coWords.isEmpty()) {
                continue;
            }
            for (String coWord : coWords) {
                weight = getPMIScore(word, coWord);
                if (scores.containsKey(coWord)) {
                    weight += scores.get(coWord);
                }
                scores.put(coWord, weight);
            }
        }
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            weight = entry.getValue() / senWordsMap.size();

            fw.write(entry.getKey() + "=" + weight + "\n");
        }
        fw.close();
    }

    private List<String> getCoWords(String word) {
        // TODO Auto-generated method stub
        Set<String> words = new HashSet<String>();
        List<Long> commentIds = wordIDList.get(word);
        if (commentIds == null || commentIds.size() == 0) {
            return new ArrayList<String>();
        }
        for (Long id : commentIds) {
            words.addAll(data2Words.get(id));
        }
        return new ArrayList<String>(words);
    }

    private Map<Long, Integer> list2Map(List<Long> ids) {
        // TODO Auto-generated method stub
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        for (Long id : ids) {
            if (map.containsKey(id)) {
                map.put(id, map.get(id) + 1);
            } else {
                map.put(id, 1);
            }
        }
        return map;
    }

    private double div(double d1, double d2, int scale) {
        // TODO Auto-generated method stub
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static void main(String[] args) throws Exception {
        PMIGenerator pmi = new PMIGenerator();
        String corpus = "corpus.txt";
        pmi.initFromCorpus(corpus);
        pmi.computePMI("postive.txt", "negative.txt");
        System.exit(0);
    }
}
