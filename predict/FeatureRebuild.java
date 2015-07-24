/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package predict;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import libsvm.svm_node;

/**
 *
 * @author Rain
 */
public class FeatureRebuild {

	// private String [] text;
    public int[] y;
    public int featurenum = 0;
    public int clasnum = 0;
    public int docnum = 0;
    private double lower = 0;
    private double upper = 1.0;

    private void print() {
        System.out.println("文集大小: " + docnum + "\n特征数目：" + featurenum
                + "\n类别数目：" + clasnum);
    }

    public svm_node[] processSingal(HashMap<Integer, Float> hashInttoIDF,
            HashMap<String, Integer> hashWordStrtoInt,
            ArrayList<Double> feature_min, ArrayList<Double> feature_max,
            ProblemStructure st) {

        //print();
        return featureCalculateSNode(hashInttoIDF, hashWordStrtoInt,
                feature_min, feature_max, st);

    }

    public svm_node[] featureCalculateSNode(
            HashMap<Integer, Float> hashInttoIDF,
            HashMap<String, Integer> hashWordStrtoInt,
            ArrayList<Double> feature_min, ArrayList<Double> feature_max,
            ProblemStructure st) {
        double[] x;
        String[] textt;

        HashMap<Integer, Integer> hashWordHasContain = new HashMap<Integer, Integer>();

        featurenum = hashInttoIDF.size() + 1;//维度从 1 开始计数

        docnum = 1;
        int featureI = 0;
        // 特征向量组 +1 (长度)
        x = new double[featurenum + 1];
        for (int i = 0; i < x.length; i++) {
            x[i] = 0;
        }
        svm_node[] xx; // 特征向量组 +1 (长度)

        x[featurenum] = (double) st.len;
        // System.err.println(pp[i].text);
        if (st.text.isEmpty()) {
            x[0] = 0;
        }
        textt = st.text.split("\\|");

        hashWordHasContain.clear();
        for (int j = 0; j < textt.length; j++) {

            if (!hashWordStrtoInt.containsKey(textt[j])) {
				// System.err.println(i+"-"+j);
                // System.err.println("-"+textt[i][j]+"-");
                continue;
            }

            if (!hashWordHasContain.containsKey(hashWordStrtoInt.get(textt[j]))) {
                hashWordHasContain.put(hashWordStrtoInt.get(textt[j]), 1);
            } else {
                int num = hashWordHasContain
                        .get(hashWordStrtoInt.get(textt[j]));
                hashWordHasContain.put(hashWordStrtoInt.get(textt[j]), num + 1);
            }
        }

        for (Map.Entry entry : hashWordHasContain.entrySet()) {
            Integer key = (Integer) entry.getKey();
            Integer val = (Integer) entry.getValue();
            x[key] = (double) (val * hashInttoIDF.get(key)); //tf*idf
            // idf
            featureI++;
        }

        /* pass 3: scale */
        xx = new svm_node[featureI + 1];
        int f = 0;
        for (int j = 0; j < x.length; j++) {
            double d = x[j];
            if (d == 0) {
                continue;
            }
            if (d <= feature_min.get(j)) {
                d = lower;
            } else if (d >= feature_max.get(j)) {
                d = upper;
            } else {
                d = lower + (upper - lower) * (d - feature_min.get(j))
                        / (feature_max.get(j) - feature_min.get(j));
            }

            x[j] = d;

            try {
                xx[f] = new svm_node();
                xx[f].index = j;
                xx[f].value = d;
                f++;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return xx;
    }
}
