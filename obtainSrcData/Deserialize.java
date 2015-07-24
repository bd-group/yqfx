/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package obtainSrcData;

import global.WBObject;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;

/**
 *
 * @author daniel
 */
public class Deserialize {

    private Schema docSchema;
    private Schema docsSchema;
    private static Logger logger;

    public Deserialize() {
        try {
            logger = LogManager.getLogger(Deserialize.class);
            Protocol docProtocol = Protocol.parse(new File("lib/doc.json"));
            this.docSchema = docProtocol.getType("doc");
            Protocol docsProtocol = Protocol.parse(new File("lib/docs.json"));
            this.docsSchema = docsProtocol.getType("docs");
        } catch (IOException ex) {
            logger.debug(ex);
        }
    }

    public WBObject deserialize2WBObject(byte[] docs) {

        GenericRecord docsRecord;
        WBObject wbObject = null;

        try {
            DatumReader<GenericRecord> docsReader = new GenericDatumReader<>(docsSchema);
            ByteArrayInputStream in = new ByteArrayInputStream(docs);
            Decoder decoder = DecoderFactory.get().binaryDecoder(in, null);
            docsRecord = docsReader.read(null, decoder);

            GenericArray docSet = (GenericData.Array<GenericRecord>) docsRecord.get("doc_set");

            if (docSet == null) {
                return wbObject;
            } else if (docSet.size() < 0) {
                return wbObject;
            } else {
                Iterator<ByteBuffer> itr = docSet.iterator();

                while (itr.hasNext()) {
                    try {
                        byte[] n = ((ByteBuffer) itr.next()).array();                       
                        ByteArrayInputStream docbis = new ByteArrayInputStream(n);
                        BinaryDecoder docbd = new DecoderFactory().binaryDecoder(docbis, null);

                        DatumReader<GenericRecord> docReader = new GenericDatumReader<>(docSchema);

                        GenericRecord docRecord = docReader.read(null, docbd);

                        Map<Utf8, Utf8> str_sMap = (HashMap<Utf8, Utf8>) docRecord.get("string_s");
                        Map<Utf8, Long> long_sMap = (HashMap<Utf8, Long>) docRecord.get("long_s");
                        Map<Utf8, Integer> int_sMap = (HashMap<Utf8, Integer>) docRecord.get("int_s");
//                        Map<Utf8, GenericArray> str_aMap = (HashMap<Utf8, GenericArray>) docRecord.get("string_a");
//                        Map<Utf8, GenericArray> long_aMap = (HashMap<Utf8, GenericArray>) docRecord.get("long_a");

                        if (str_sMap != null && long_sMap != null ) {

                            if (str_sMap.containsKey(new Utf8("c_mid"))                                   
                                    && str_sMap.containsKey(new Utf8("c_uid"))
                                    && str_sMap.containsKey(new Utf8("c_mnr"))
                                    && str_sMap.containsKey(new Utf8("c_src"))
                                    && long_sMap.containsKey(new Utf8("c_ctime"))) {

                                wbObject = new WBObject(str_sMap.get(new Utf8("c_mid")).toString(),
                                        str_sMap.get(new Utf8("c_uid")).toString(),
                                        str_sMap.get(new Utf8("c_mnr")).toString(),
                                        str_sMap.get(new Utf8("c_src")).toString(),
                                        long_sMap.get(new Utf8("c_ctime")),                                       
                                        docRecord);
                            } else {
                                logger.info("c_ydzyysid OR c_time OR c_dxxh OR c_ydz OR c_dxnr has null value!");
                            }
                        } else {
                            logger.info("avro str_sMap OR long_sMap OR int_sMap is null!");
                        }

                    } catch (Exception e) {
                        logger.info("error avro struct",e);
                    }

                }
            }

        } catch (IOException ex) {
            logger.debug(ex);
        }

        return wbObject;

    }

}
