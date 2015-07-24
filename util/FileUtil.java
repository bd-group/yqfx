/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//import org.apache.log4j.Logger;

/**
 *
 * @author mc
 */
public class FileUtil {

    //private static Logger logger = Logger.getLogger(FileUtil.class);
    /**
     * 将文本每一行存入list
     *
     * @param modeFilePath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> lineToList(String modeFilePath) throws FileNotFoundException, IOException {
        List<String> ret = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader(modeFilePath));
        String temp = null;
        while ((temp = br.readLine()) != null) {
            //BufferedReader.readLine()读取文件第一行会出现bug，首行第一个字符会是一个空字符 
            char s = temp.trim().charAt(0);
            if (s == 65279 && temp.length() > 1) {
                temp = temp.substring(1);
            }
            ret.add(temp);
        }

        if (ret.size() == 0) {
            throw new RuntimeException("关键词列表为空！");
        }

        br.close();
        return ret;
    }

    /**
     * 读取文件内容
     *
     * @param targertFile
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String fileToStr(File file) throws UnsupportedEncodingException, IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
        String temp;
        while ((temp = br.readLine()) != null) {
            sb.append(temp);
        }
        return sb.toString();
    }

    /**
     * 将字符串写入目标文件
     *
     * @param content
     * @param fileName
     * @throws IOException
     */
    public static void write(String content, String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            System.out.println("目标文件所在路径不存在，准备创建。。。");
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        int i = 0;
        while (file.exists()) {
            file = new File(fileName + "_" + i++);
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(content);
        bw.close();
    }

    /**
     * 读入文档集（此文档集存储在一个文档中，每一行代表一个文档），将每一行放入字符串数组中
     *
     * @param inputFileName
     * @return
     */
    public static String[] readInput(String inputFileName) {
        List<String> ret = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "utf8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                ret.add(temp);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String[] fileString = new String[ret.size()];
        return (String[]) ret.toArray(fileString);
    }

    public static String[] readInputList(String inputPathName) {
        List<String> ret = new ArrayList<String>();
        File file = new File(inputPathName);
        System.out.println(file + "|---");
        for (File f : file.listFiles()) {
            if (!f.getName().endsWith(".txt")) {
                continue;
            }
            String[] tmpStr = readInput(f.getPath());
            String SS = "" + inputPathName.substring(inputPathName.length() - 2, inputPathName.length()) + "\t";
            for (String stt : tmpStr) {
                SS += stt;
            }

            ret.add(SS);

        }

        String[] fileString = new String[ret.size()];
        return (String[]) ret.toArray(fileString);
    }

    public static String[] readInputFolder(String inputPath) {
        List<String> ret = new ArrayList<String>();
        File file = new File(inputPath);
//        File[] file = new File[9];
//        file[0] = new File(inputPath + "/C000000");
//        file[1] = new File(inputPath + "/C000001");
//        file[2] = new File(inputPath + "/C000002");
//        file[3] = new File(inputPath + "/C000003");
//        file[4] = new File(inputPath + "/C000004");
//        file[5] = new File(inputPath + "/C000005");
//        file[6] = new File(inputPath + "/C000006");
//        file[7] = new File(inputPath + "/C000007");
//        file[8] = new File(inputPath + "/C000008");

//        ret.addAll(Arrays.asList(readInputList(file[7].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[6].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[5].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[8].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[4].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[3].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[0].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[2].getPath())));
//        ret.addAll(Arrays.asList(readInputList(file[1].getPath())));
        for (File f : file.listFiles()) {//
            if (!f.isDirectory()) {
                continue;
            }
            ret.addAll(Arrays.asList(readInputList(f.getPath())));
        }

        String[] fileString = new String[ret.size()];
        return (String[]) ret.toArray(fileString);

    }

    public static float[] readInputFloat(String inputFileName) {
        List<Float> ret = new ArrayList<Float>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFileName), "utf-8"));

            String temp;
            while ((temp = br.readLine()) != null) {
                ret.add(Float.parseFloat(temp));
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        float[] f = new float[ret.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = ret.get(i);
        }
        return f;
    }

    public static void writeOutput(String[] outputContent, String outputFileName) throws IOException {
        File f = new File(outputFileName);
        if (!f.getParentFile().exists()) {
            System.out.println("目标文件所在路径不存在，准备创建。。。");
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < outputContent.length; i++) {
                bw.write(outputContent[i]);
                bw.newLine();
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeOutput(float[] outputContent, String outputFileName) {
        //将outputContent中的内容写入文件outputFileName中
        File f = new File(outputFileName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < outputContent.length; i++) {
                bw.write(String.valueOf(outputContent[i]));
                bw.newLine();
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void ouputFileForLibsvm(int[] y, float[][] outputContent, String outputFileName) {

        File f = new File(outputFileName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < outputContent.length; i++) {
                bw.write(y[i] + "");
                for (int j = 0; j < outputContent[i].length; j++) {
                    double d = outputContent[i][j];
                    if (d != 0) {
                        bw.write(" " + j + ":" + d);
                    }

                }
                bw.newLine();
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void ouputFileForLiblinear(int[] y, float[][] outputContent, String outputFileName) {

        File f = new File(outputFileName);
        int featurenum = outputContent[0].length;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < outputContent.length; i++) {
                bw.write(y[i] + "");
                for (int j = 0; j < outputContent[i].length; j++) {
                    double d = outputContent[i][j];
                    if (d != 0) {
                        bw.write(" " + (j + 1) + ":" + d);
                    }

                }
                bw.write(" " + (featurenum + 1) + ":" + 1.0);
                bw.newLine();
            }
            bw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
