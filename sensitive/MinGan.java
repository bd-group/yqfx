package sensitive;

import java.util.Date;

public class MinGan {

    private String id;
    private String number;
    private String comment;
    private String mgc_id;
    private Date date;
    private String address;
    private String mgc_comment;
    private int classify;
    private int sentiment;

    public MinGan(String id, String number, String comment, String mgcId,
            Date date, String address, String mgcComment) {
        super();
        this.id = id;
        this.number = number;
        this.comment = comment;
        mgc_id = mgcId;
        this.date = date;
        this.address = address;
        mgc_comment = mgcComment;
    }

    public MinGan() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMgc_id() {
        return mgc_id;
    }

    public void setMgc_id(String mgcId) {
        mgc_id = mgcId;
    }

    public String getMgc_comment() {
        return mgc_comment;
    }

    public void setMgc_comment(String mgcComment) {
        mgc_comment = mgcComment;
    }

    public int getClassify() {
        return classify;
    }

    public void setClassify(int classify) {
        this.classify = classify;
    }

    public int getSentiment() {
        return sentiment;
    }

    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }

    @Override
    public String toString() {
        return "MinGan [address=" + address + ", comment=" + comment
                + ", date=" + date + ", id=" + id + ", mgc_comment="
                + mgc_comment + ", mgc_id=" + mgc_id + ", number=" + number
                + ", classify=" + classify + ", sentiment" + sentiment + "]";
    }

}
