/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transput;

import cluster.Center;
import java.util.logging.Level;
import java.util.logging.Logger;
import global.GlobalConfig;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 *
 * @author daniel
 */
public class TransputHistoryHots implements Runnable {

    private GlobalConfig gConfig;
    private LinkedBlockingQueue<Center> historyCenters;
    private static org.apache.logging.log4j.Logger logger;

    private Schema docSchema;
    private Schema docsSchema;

    public TransputHistoryHots(GlobalConfig gc, LinkedBlockingQueue<Center> history) {
        try {
            logger = LogManager.getLogger(TransputHistoryHots.class);
            this.gConfig = gc;
            this.historyCenters = history;
            Protocol docProtocol = Protocol.parse(new File("lib/doc.json"));
            this.docSchema = docProtocol.getType("doc");
            Protocol docsProtocol = Protocol.parse(new File("lib/docs.json"));
            this.docsSchema = docsProtocol.getType("docs");
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @Override
    public void run() {
        GenericRecord packageGenericRecord;
        HttpClient httpClient = new DefaultHttpClient();
        int count = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Center c = historyCenters.take();                
                packageGenericRecord = getPackage(c);
//                System.err.println(packageGenericRecord.toString());
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

                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    logger.error(response.getStatusLine() + "  \t" + smsg.length);
                } else {
//                        System.out.println("success count " + count);
                    count++;
                }

                httppost.releaseConnection();
            } catch (InterruptedException ex) {
                Logger.getLogger(TransputHistoryHots.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(TransputHistoryHots.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private GenericRecord getPackage(Center center) {
        GenericRecord docRecord = new GenericData.Record(docSchema);

        GenericRecord docsRecord = new GenericData.Record(docsSchema);
        GenericArray docSet = new GenericData.Array<>(3, docsSchema.getField("doc_set").schema());

        Map<String, Integer> int_sMap = new HashMap<>();
        Map<String, String> string_sMap = new HashMap<>();
        Map<String, Long> long_sMap = new HashMap<>();
        Map<String, Double> double_sMap = new HashMap<>();
        Map<String, GenericArray> string_aMap = new HashMap<>();

        Schema string_aSchema = docSchema.getField("string_a").schema().getTypes().get(0).getValueType();
        if (!center.idSet.equals("")) {
            String[] ids = center.idSet.split(",");
            GenericArray<String> idSetArray = new GenericData.Array<>(ids.length, string_aSchema);
            idSetArray.addAll(Arrays.asList(ids));
            string_aMap.put("c_idset", idSetArray);
        }

        int_sMap.put("c_count", center.Count);
        int_sMap.put("c_classify", center.classify);
        int_sMap.put("c_sentiment", center.sentiment);
        Date d = new Date();
        long_sMap.put("c_time", d.getTime() / 1000);
        long_sMap.put("c_ishottime", center.isHotTimeDate.getTime() / 1000);
        long_sMap.put("c_isnothottime", center.isNotHotTimeDate.getTime() / 1000);
        double_sMap.put("c_hotscore", center.hotScore);
        string_sMap.put("c_id", center.id);
        string_sMap.put("c_clustername", center.clusterName);
        string_sMap.put("c_yysid", center.yysId);

        docRecord.put("int_s", int_sMap);
        docRecord.put("long_s", long_sMap);
        docRecord.put("double_s", double_sMap);
        docRecord.put("string_s", string_sMap);
        docRecord.put("string_a", string_aMap);
        
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
        doc_desc.put("c_time", "long_s");
        doc_desc.put("c_id", "string_s");
        doc_desc.put("c_clustername", "string_s");
        doc_desc.put("c_count", "int_s");
        doc_desc.put("c_hotscore", "double_s");
        doc_desc.put("c_classify", "int_s");
        doc_desc.put("c_sentiment", "int_s");
        doc_desc.put("c_yysid", "string_s");
        doc_desc.put("c_ishottime", "long_s");
        doc_desc.put("c_isnothottime", "long_s");
        doc_desc.put("c_idset", "string_a");

        user_desc.put("author", "wbyq");

        docsRecord.put("doc_schema_name", "dxyq.t_hots");
        docsRecord.put("doc_desc", doc_desc);
        docsRecord.put("user_desc", user_desc);
        docsRecord.put("doc_set", docSet);

        return docsRecord;
    }

}
