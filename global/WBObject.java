/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package global;

import org.apache.avro.generic.GenericRecord;
import predict.ProblemStructure;

/**
 *
 * @author daniel
 */
public class WBObject {
    
    public String mid;                          // message id
    public String uid;                          // user id
//    public String uname;                   // user name
//    public String sname;                   // screen name
//    public String purl;                       // profile url
//    public String rtid;                        // retweet id
    public String mnr;                      // message content
//    public String murl;                     // message url
    public String src;                         // message souce like yysID
//    public String pic;                          // picture url
//    public String aud;                      // audio url
//    public String vid;                         // vedio url
//    public int rtCount;                     // retweet count
//    public int commentCount;        // comment count
    public long postTime;               // post time 
//    public String nameCard;             // @username
    public int length;
    public String splited;
    public int fllx;                            // which classify
    public int qgz;                            // sentiment score
    public int isMg = 0;                           // is sensitive
    public int[] mgWords;               // sensitive words list
    public long jcsj;                          // generate time
    public GenericRecord record;
    
    public WBObject() {
        
    }

    public WBObject(String mid, String uid, String mnr, String src, long postTime, GenericRecord record) {
        this.mid = mid;
        this.uid = uid;
        this.mnr = mnr;
        this.length = mnr.length();
        this.src = src;
        this.postTime = postTime;
        this.record = record;
    }
    
    public ProblemStructure toProblemStructure( ) {
        ProblemStructure s = new ProblemStructure(this.length, this.splited);
         return s;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getMnr() {
        return mnr;
    }

    public void setMnr(String mnr) {
        this.mnr = mnr;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public long getPostTime() {
        return postTime;
    }

    public void setPostTime(long postTime) {
        this.postTime = postTime;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getSplited() {
        return splited;
    }

    public void setSplited(String splited) {
        this.splited = splited;
    }

    public int getFllx() {
        return fllx;
    }

    public void setFllx(int fllx) {
        this.fllx = fllx;
    }

    public int getQgz() {
        return qgz;
    }

    public void setQgz(int qgz) {
        this.qgz = qgz;
    }

    public int getIsMg() {
        return isMg;
    }

    public void setIsMg(int isMg) {
        this.isMg = isMg;
    }

    public int[] getMgWords() {
        return mgWords;
    }

    public void setMgWords(int[] mgWords) {
        this.mgWords = mgWords;
    }

    public long getJcsj() {
        return jcsj;
    }

    public void setJcsj(long jcsj) {
        this.jcsj = jcsj;
    }

    public GenericRecord getRecord() {
        return record;
    }

    public void setRecord(GenericRecord record) {
        this.record = record;
    }
    
    

    @Override
    public String toString() {
        return "WBObject{" + "mid=" + mid + ", uid=" + uid + ", mnr=" + mnr + ", src=" + src + ", postTime=" 
                + postTime + ", length=" + length + ", splited=" + splited + ", fllx=" + fllx + ", qgz=" + qgz 
                + ", isMg=" + isMg + ", mgWords=" + mgWords + ", jcsj=" + jcsj + ", record=" + record + '}';
    }
    
    
    


    
    
    
    
}
