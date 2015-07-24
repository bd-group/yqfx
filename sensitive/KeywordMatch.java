package sensitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/*
 *
 * @author wkp
 */
public class KeywordMatch {

    private final ReentrantReadWriteLock keywordMatchLock = new ReentrantReadWriteLock();// added

    public Map<Integer, ArrayList<String>> match(String text, AcStateOp root, Map<String, Integer> ydymgcmap) {
		// 如果表达式中含有关键字，那么他那个就会变成1 ，index表示选择第几个表达式
        //添加返回关键词功能		
        Map<Integer, ArrayList<String>> matchExpression = new HashMap<Integer, ArrayList<String>>();
        ArrayList<String> mgclist = new ArrayList<String>();
        ArrayList<String> ydylist = new ArrayList<String>();
        keywordMatchLock.readLock().lock();
        char[] source = text.toCharArray();
        AcStateOp current = root;
        char currentChar = '\0';
		//String LogicExpression = keywordLogicExpression.get(index);
        // 得到对应的关键字表达式
        //System.out.println("cicic"+"============"+LogicExpression);//乖乖第一行不读取
        for (int i = 0; i < text.length(); i++) {
            currentChar = source[i];
            if (current.getChildren().containsKey(currentChar)) {
                //System.out.println(currentChar+"============"+text);
                current = current.getChildren().get(currentChar);
				//System.out.println(current.getOutPut()+"============");//为什么进来过一次就
                // 如果有的话就取
                if (!current.getOutPut().isEmpty()) {
                    //这里等待修改，因为这里的问题是，可能会有很多交叉的地方，可能需要你做多次判断分离出有ydy的地方亲
                    for (int ss = 0; ss < current.getOutPut().size(); ss++) {
                        if (ydymgcmap.containsKey(current.getOutPut().get(ss))) {
						//System.out.println(current.getOutPut()+"============");
                            //for (int j = 0; j < current.getOutPut().size(); j++) {
                            //LogicExpression = replace(LogicExpression,(String) current.getOutPut().get(j),"1");
                            //加入返回关键词
                            ydylist.add((String) current.getOutPut().get(ss));
                            //}
                        } else {
					//System.out.println(current.getOutPut()+"============");
                            //for (int j = 0; j < current.getOutPut().size(); j++) {
                            // 包含关键字才变成1呢亲
                            //LogicExpression = replace(LogicExpression,(String) current.getOutPut().get(0), "1");
                            // 把对应的包含的关键词变成1
                            mgclist.add((String) current.getOutPut().get(ss));
							//System.out.println(current.getOutPut()+"============");
                            //}
                        }
                    }
                }
                continue;
            } else {
                if (current == root) {
                    continue;
                } else {
                    current = current.getFail();
                    while (true) {
                        if (current.getChildren().containsKey(currentChar)) {
                            current = current.getChildren().get(currentChar);
                            if (!current.getOutPut().isEmpty()) {
                                for (int ss = 0; ss < current.getOutPut().size(); ss++) {
                                    if (ydymgcmap.containsKey(current.getOutPut().get(ss))) {
									//mgclist.remove(0);
                                        //mgclist.add(0,"1");
                                        ///for (int j = 0; j < current.getOutPut().size(); j++) {
                                        //LogicExpression = replace(LogicExpression,(String) current.getOutPut().get(j),"1");
                                        //加入返回关键词
                                        ydylist.add((String) current.getOutPut().get(ss));
                                        //}
                                    } else {
									//for (int j = 0; j < current.getOutPut().size(); j++) {
                                        //LogicExpression = replace(LogicExpression,(String) current.getOutPut().get(j),"1");
                                        //加入返回关键词
                                        mgclist.add((String) current.getOutPut().get(ss));
                                        //}
                                    }
                                }
                            }
                            break;
                        } else if (current == root) {
                            break;
                        } else {
                            current = current.getFail();
							// ++++++感觉有问题为什么要向上找，上面没有了，跳到 他的fail，结果他的fail也没有
                            //跳到对应的但是有跳到别处的，为什么还要往下找呢，直接root不是可以吗
                        }
                    }
                }
            }
        }
        keywordMatchLock.readLock().unlock();
        matchExpression.put(0, ydylist);
        matchExpression.put(1, mgclist);
        return matchExpression;
    }
}
