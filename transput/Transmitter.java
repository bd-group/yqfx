/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transput;

import global.WBObject;
import global.GlobalConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */
public class Transmitter {
    
    private static Logger logger;
    private GlobalConfig gConfig;
    private ArrayList<LinkedBlockingQueue<WBObject>> inputWBObjectsBQList;
    private ArrayList<Thread> transmitThreads;
    private Schema docSchema;
    private Schema docsSchema;
    
    public Transmitter(GlobalConfig gc, ArrayList<LinkedBlockingQueue<WBObject>> input) {
        
        try {
            logger = LogManager.getLogger(Transmitter.class);
            this.gConfig = gc;
            this.inputWBObjectsBQList = input;
            this.transmitThreads = new ArrayList<>();
            Protocol docProtocol = Protocol.parse(new File("lib/doc.json"));
            this.docSchema = docProtocol.getType("doc");
            Protocol docsProtocol = Protocol.parse(new File("lib/docs.json"));
            this.docsSchema = docsProtocol.getType("docs");
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
    
    public void runTransmitter() {
        for (int i = 0; i < gConfig.maxTransmitThreadCount; i++) {
            Transmit t = new Transmit(this.inputWBObjectsBQList.get(i % gConfig.maxBlockQueue));
            Thread tTrhead = new Thread(t);
            transmitThreads.add(tTrhead);
            tTrhead.start();
            logger.trace("run transmit thread " + i + " " + tTrhead.toString());
        }
    }

    private GenericRecord getPackage(WBObject wb) {
        GenericRecord docRecord = wb.getRecord();

        GenericRecord docsRecord = new GenericData.Record(docsSchema);
        GenericArray docSet = new GenericData.Array<>(3, docsSchema.getField("doc_set").schema());

        Map<Utf8, Integer> int_sMap = (HashMap<Utf8, Integer>) docRecord.get("int_s");
//        Map<Utf8, String> string_sMap = (HashMap<Utf8, String>) docRecord.get("string_s");
        Map<Utf8, Long> long_sMap = (HashMap<Utf8, Long>) docRecord.get("long_s");
        Map<Utf8, GenericArray> int_aMap = (HashMap<Utf8, GenericArray>) docRecord.get("int_a");
        if (int_aMap == null) {
//            System.out.println("get int_a map is null");
            int_aMap = new HashMap<>();
        }

        Schema int_aSchema = docSchema.getField("int_a").schema().getTypes().get(0).getValueType();
        if ((wb.getMgWords()) != null) {
            GenericArray<Integer> mgcIdArray = new GenericData.Array<>(wb.getMgWords().length, int_aSchema);
            for (int a : wb.getMgWords()) {
                mgcIdArray.add(a);
            }
            int_aMap.put(new Utf8("c_mgword"), mgcIdArray);
        }

        int_sMap.put(new Utf8("c_qgz"), wb.getQgz());
        int_sMap.put(new Utf8("c_fllx"), wb.getFllx());
        int_sMap.put(new Utf8("c_ismg"), wb.getIsMg());
        Date d = new Date();
        long_sMap.put(new Utf8("c_jcsj"), d.getTime()/1000);
//        System.out.println("jcsj " + d.getTime()/1000);

//        System.out.println(docRecord.toString());

        try {
            DatumWriter<GenericRecord> docWriter = new GenericDatumWriter<>(docSchema);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            docWriter.write(docRecord, encoder);
            encoder.flush();
            out.close();
            docSet.add(ByteBuffer.wrap(out.toByteArray()));
        } catch (Exception e) {
            logger.info(e);
        }

        Map<String, String> user_desc = new HashMap<>();
        Map<String, String> doc_desc = new HashMap<>();
        doc_desc.put("version", "1.0.0");
        doc_desc.put("c_mid", "string_s");
        doc_desc.put("c_uid", "string_s");
        doc_desc.put("c_uname", "string_s");
        doc_desc.put("c_sname", "string_s");
        doc_desc.put("c_purl", "string_s");
        doc_desc.put("c_rtid", "string_s");
        doc_desc.put("c_mnr", "string_s");
        doc_desc.put("c_murl", "string_s");
        doc_desc.put("c_src", "string_s");
        doc_desc.put("c_pic", "string_s");
        doc_desc.put("c_aud", "string_s");
        doc_desc.put("c_vid", "string_s");
        doc_desc.put("c_rc", "int_s");
        doc_desc.put("c_cc", "int_s");
        doc_desc.put("c_ctime", "long_s");
        doc_desc.put("c_nc", "string_s");
        doc_desc.put("c_fllx", "int_s");
        doc_desc.put("c_qgz", "int_s");
        doc_desc.put("c_jcsj", "long_s");
        doc_desc.put("c_ismg", "int_s");
        doc_desc.put("c_mgword", "int_a");

        user_desc.put("author", "wbyq");

        docsRecord.put("doc_schema_name", gConfig.TransmitTableName);
        docsRecord.put("doc_desc", doc_desc);
        docsRecord.put("user_desc", user_desc);
        docsRecord.put("doc_set", docSet);

        return docsRecord;
    }

    class Transmit implements Runnable {
        
        private LinkedBlockingQueue<WBObject> inputBQueue;
        

        public Transmit(LinkedBlockingQueue<WBObject> minganResultQueue) {
            this.inputBQueue = minganResultQueue;
        }

        @Override
        public void run() {
            GenericRecord packageGenericRecord;
            HttpClient httpClient = new DefaultHttpClient();
            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    WBObject wb = inputBQueue.take();
//                    System.err.println("take " + count);
                    packageGenericRecord = getPackage(wb);
                    DatumWriter<GenericRecord> docWriter = new GenericDatumWriter<>(packageGenericRecord.getSchema());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
                    docWriter.write(packageGenericRecord, encoder);
                    encoder.flush();
                    out.close();
                    byte[] smsg = out.toByteArray();
                    HttpPost httppost = new HttpPost(gConfig.httpPost);

                    InputStreamEntity reqEntity = new InputStreamEntity(new ByteArrayInputStream(smsg), -1);
                    reqEntity.setContentType("binary/octet-stream");
                    reqEntity.setChunked(true);
                    httppost.setEntity(reqEntity);
                    HttpResponse response = httpClient.execute(httppost);
                    System.err.println(response.getStatusLine());
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        logger.error(response.getStatusLine() + "  \t" + smsg.length);
                    } else {
//                        System.out.println("count " + count);
                        count++;
                    }
                  
                    httppost.releaseConnection();
//                    if (count == 25) {
//                        Thread.currentThread().interrupt();
//                        System.out.println("transmitter is stoped");
//                    }

                } catch (Exception ex) {
//                    logger.error(ex);
                    
                }
            }

        }

    }

}
