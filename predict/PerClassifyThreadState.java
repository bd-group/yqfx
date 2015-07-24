package predict;

public class PerClassifyThreadState {

    public long[] perClassCount = new long[41];
    public boolean isStoped = true;
    public boolean classifyIsExited = false;
    public long classifiedCount = 0;

}
