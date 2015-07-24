package transput;

import global.GlobalConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.LoadConfig;

public class TransputClient {

    private static Logger logger;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private PushResults pushResults;
    private GlobalConfig gConfig;

    public TransputClient(GlobalConfig gc, PushResults psResults) {
        logger = LogManager.getLogger(TransputClient.class);
        this.pushResults = psResults;
        this.gConfig = gc;

        try {
            this.socket = new Socket(gConfig.serverIP, gConfig.serverPort);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf8"));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void runTransputClient() {
        logger.trace("run Transput client!");
        SendThread send = new SendThread(this.out, this.pushResults);
        ReceiveThread receiveThread = new ReceiveThread(this.in, this.pushResults);
        Thread ts = new Thread(send);
        Thread tr = new Thread(receiveThread);
        ts.start();
        logger.trace("send thread " + ts.toString() + " is started!");
        tr.start();
        logger.trace("receive thread " + tr.toString() + " is started!");
        Thread prThread = new Thread(this.pushResults);
        prThread.start();
        logger.trace("push result thread get result thread " + prThread.toString() + " is started!");
    }

    class SendThread implements Runnable {

        private PrintWriter out;
        private PushResults pushResults;

        public SendThread(PrintWriter pw, PushResults pr) {
            super();
            this.out = pw;
            this.pushResults = pr;
        }

        @Override
        public void run() {

            long countSecs = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (countSecs % gConfig.refreshSeconds == 0) {
                        // hot cluster result
                        if (pushResults.hotClusterResultString != null) {
                            out.println("hot");
                            //out.flush();
                            out.println(pushResults.hotClusterResultString);
//                            System.out.println(pushResults.hotClusterResultString);
                            out.flush();
                        }                        
                    }

                    // predict
                    if (pushResults.classifyResultString != null) {
                        out.println("predict");
                        //out.flush();
                        out.println(pushResults.classifyResultString);
//                        System.out.println(pushResults.classifyResultString);
                        out.flush();
                    }
                    // mingan result
                        if (pushResults.minganMainListString != null) {
                            out.println("mgmainlist");
                            //out.flush();
                            out.println(pushResults.minganMainListString);
                            out.flush();
                        }

                        // predefine mingan result
                        if (pushResults.minganPredefineResultString != null) {
                            out.println("mingan");
                            //out.flush();
                            out.println(pushResults.minganPredefineResultString);
                            out.flush();
                        }

                        // total count
                        if (pushResults.totalCountString != null) {
                            out.println("total");
                            // out.flush();
                            out.println(pushResults.totalCountString);
//                        System.out.println("total " + pushResults.totalCountString);
                            out.flush();
                        }

                        if (pushResults.minganTotalNumString != null) {
                            out.println("MgTotal");
                            // out.flush();
                            out.println(pushResults.minganTotalNumString);
//                        System.out.println("MgTotal " + pushResults.totalCountString);
                            out.flush();
                        }

                        // yys count
                        if (pushResults.yysCountString != null) {
                            out.println("yunying");
                            //out.flush();
                            out.println(pushResults.yysCountString);
//                        System.out.println("yys " + pushResults.yysCountString);
                            out.flush();
                        }

                        // sentiment count
                        if (pushResults.sentimentResulitString != null) {

                            out.println("sentiment");
                            //out.flush();
                            out.println(pushResults.sentimentResulitString);
//                        System.out.println("sentiment " + pushResults.sentimentResulitString);
                            out.flush();
                        }

                    Thread.sleep(1000);
                    countSecs++;

                } catch (InterruptedException ex) {
                    logger.error(ex);
                }
            }
        }
    }

    class ReceiveThread implements Runnable {

        private BufferedReader in;
        private PushResults pushResults;

        public ReceiveThread(BufferedReader inReader, PushResults pr) {
            super();
            this.in = inReader;
            this.pushResults = pr;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String inString = in.readLine();
                    System.out.println(inString);
                    String[] w = inString.split(",");
                    if (w[0].equals("addCi")) {
                        //System.out.println(pushResults.restartMingan());
                        if (Integer.parseInt(w[w.length - 1]) == 0) {
                            for (int i = 1; i < w.length - 1; i++) {
                                pushResults.addMinGanCi(w[i], Integer.parseInt(w[w.length - 1]));
                            }
                        } else {
                            pushResults.addMinGanCi(w[1], Integer.parseInt(w[2]));
                        }
                    }
                    if (w[0].equals("delCi")) {
                        //System.out.println(pushResults.restartMingan());
                        pushResults.delMinGanCi(w[1], Integer.parseInt(w[2]));
                    }
                    if (inString.equals("reload")) {
                        LoadConfig l = new LoadConfig(gConfig);
                        l.loadConfigFromDatabase();
                    }
                } catch (IOException ex) {
                    logger.error(ex);
                }

            }
        }
    }

}
