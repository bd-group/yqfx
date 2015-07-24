package util;

import java.util.Date;

public class XiTongPeiZhi {

    private int id;
    private int houtai_re;
    private int redian_t;
    private int mingan_t;
    private int qinggan_time;
    private int yunyings_time;
    private int fenlei_time;
    private int xitonggaik_t;
    private Date peizhi_time;

    public XiTongPeiZhi() {
        super();
        // TODO Auto-generated constructor stub
    }

    public XiTongPeiZhi(int id, int houtaiRe, int redianT, int minganT,
            int qingganTime, int yunyingsTime, int fenleiTime, int xitonggaikT,
            Date peizhiTime) {
        super();
        this.id = id;
        houtai_re = houtaiRe;
        redian_t = redianT;
        mingan_t = minganT;
        qinggan_time = qingganTime;
        yunyings_time = yunyingsTime;
        fenlei_time = fenleiTime;
        xitonggaik_t = xitonggaikT;
        peizhi_time = peizhiTime;
    }

    public int getYunyings_time() {
        return yunyings_time;
    }

    public void setYunyings_time(int yunyingsTime) {
        yunyings_time = yunyingsTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHoutai_re() {
        return houtai_re;
    }

    public void setHoutai_re(int houtaiRe) {
        houtai_re = houtaiRe;
    }

    public int getRedian_t() {
        return redian_t;
    }

    public void setRedian_t(int redianT) {
        redian_t = redianT;
    }

    public int getMingan_t() {
        return mingan_t;
    }

    public void setMingan_t(int minganT) {
        mingan_t = minganT;
    }

    public int getQinggan_time() {
        return qinggan_time;
    }

    public void setQinggan_time(int qingganTime) {
        qinggan_time = qingganTime;
    }

    public int getFenlei_time() {
        return fenlei_time;
    }

    public void setFenlei_time(int fenleiTime) {
        fenlei_time = fenleiTime;
    }

    public int getXitonggaik_t() {
        return xitonggaik_t;
    }

    public void setXitonggaik_t(int xitonggaikT) {
        xitonggaik_t = xitonggaikT;
    }

    public Date getPeizhi_time() {
        return peizhi_time;
    }

    public void setPeizhi_time(Date peizhiTime) {
        peizhi_time = peizhiTime;
    }

    @Override
    public String toString() {
        return "XiTongPeiZhi [fenlei_time=" + fenlei_time + ", houtai_re="
                + houtai_re + ", id=" + id + ", mingan_t=" + mingan_t
                + ", peizhi_time=" + peizhi_time + ", qinggan_time="
                + qinggan_time + ", redian_t=" + redian_t + ", xitonggaik_t="
                + xitonggaik_t + "]";
    }

}
