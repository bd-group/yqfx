package util;

import java.util.Calendar;
import java.util.Date;

public class ClassifySQL {

    private String id;
    int classify;
    private Date time;
    private Date timenull;
    long count;

    public ClassifySQL(String id, int classify, Calendar time, Calendar timenull, long count) {
        super();
        this.id = id;
        this.classify = classify;
        this.time = time.getTime();
        this.timenull = timenull.getTime();
        this.count = count;
    }

    public ClassifySQL() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Date getTimenull() {
        return timenull;
    }

    public void setTimenull(Date timenull) {
        this.timenull = timenull;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getClassify() {
        return classify;
    }

    public void setClassify(int classify) {
        this.classify = classify;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "ClassifySQL [id=" + id + ", classify=" + classify + ", time="
                + time + ", timenull=" + timenull + ", count=" + count + "]";
    }

}
