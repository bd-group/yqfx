package cluster;

public class PerClusterThreadState {

    public long clusterNum = 0;
    public long jaccardNum = 0;
    public long hclusterNum = 0;
    public long ignbylenNum = 0;
    public long dupNum = 0;
    public int[] classifyCountVector = new int[41];
    public boolean isStoped = true;
    public boolean clusterIsExited = false;

}
