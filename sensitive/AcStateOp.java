package sensitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedList;

class AcStateOp {

    public char value;
    public AcStateOp fail;
    public AcStateOp father;
    public HashMap<Object, AcStateOp> children;
    public ArrayList<String> outPut;
    public int nodeCount = 0;

    public HashMap<Object, AcStateOp> getChildren() {
        return children;
    }

    public ArrayList getOutPut() {
        return outPut;
    }

    public AcStateOp getFail() {
        return fail;
    }

    public AcStateOp() {
        this.value = '\0';
        this.outPut = new ArrayList<String>();
        this.children = new HashMap(10000, 0.75f);//这个有希望该井
    }

    private AcStateOp extendChar(char c, AcStateOp parent, AcStateOp root) {
        AcStateOp node = new AcStateOp();
        node.nodeCount++;
        node.value = c;
        node.fail = root;
        node.father = parent;
        parent.children.put(c, node);
        return node;
    }

    public AcStateOp extendString(String word, AcStateOp root, String[] srTerms) {
        String srTemp = "";
        //如果root节点不包含word的char[0]的数

        if (!root.children.containsKey(word.charAt(0))) {
            AcStateOp newPathFirstNode = extendChar(word.charAt(0), root, root);
            //当前的为初始的，把父类设定子类设定
            //  System.out.println("newPathFirstNode"+newPathFirstNode.nodeCount+"-______________________>"+word);
            AcStateOp currentNode = newPathFirstNode;
            srTemp = srTemp.trim() + word.charAt(0);
            if (word.length() == 1) {
                currentNode.getOutPut().add(word);
            }
            for (int i = 1; i < word.length(); i++) {
                AcStateOp newNode = extendChar(word.charAt(i), currentNode, root);
                //默认children为本身，下面是修改过程，fail为以后修改
                //  currentNode.children.put(word.charAt(i), newNode);
                //已经设定过
                currentNode = newNode;
                srTemp = srTemp.trim() + word.charAt(i);
                for (int j = 0; j < srTerms.length; j++) {
                    //System.out.println(srTemp+"sssssssssssssss"+srTerms.length);    
                    if (srTemp.equals(srTerms[j])) {
                    	//下面若新生成的这个词的里面包含关键词的话，将关键词 加到output 等以后判断来返回output
                        //  System.out.println(srTemp);  

                        //currentNode.getOutPut().add(srTerms[j]);
                        for (int s = 0; s < srTerms.length; s++) {
                            if (srTemp.contains(srTerms[s])) {
                                currentNode.getOutPut().add(srTerms[s]);
                            }
                        }
                        //System.out.println(currentNode.getOutPut());
                        break;
                    }
                }
            }
        } else {
            AcStateOp currentNode = root.children.get(word.charAt(0));
            currentNode.nodeCount++;
            srTemp += word.charAt(0);
            if (word.length() == 1) {
                currentNode.getOutPut().add(word);  //++++++++++++++这个多余，因为关键词也可能是一个
            }
            for (int i = 1; i < word.length(); i++) {
                if (currentNode.children.containsKey(word.charAt(i))) {
                    currentNode = currentNode.children.get(word.charAt(i));
                    currentNode.nodeCount++;
                } else {
                    currentNode = extendChar(word.charAt(i), currentNode, root);
                    //+++++++++++++++这一步是不是没有和上面一样指定child
                    //这样如果第一个字符没有child那么第二个字符currentNode 直接等于了第二个字符，并没有把第一个字符的
                    //child 设置成第二个字符
                }
                srTemp += word.charAt(i);
                for (int j = 0; j < srTerms.length; j++) {
                    if (srTemp.equals(srTerms[j])) {
                    	//System.out.println(srTemp); 
                        //  if (!currentNode.getOutPut().equals(srTerms[j])) {
                        //System.out.println(srTemp);   
                        if (currentNode.getOutPut().isEmpty()) {
                            for (int s = 0; s < srTerms.length; s++) {
                                if (srTemp.contains(srTerms[s])) {
                                    currentNode.getOutPut().add(srTerms[s]);
                                }
                            }
	                            //currentNode.getOutPut().add(srTerms[j]);
                            //System.out.println(currentNode.getOutPut());
                            break;
                        }
                        //  }
                    }
                }
            }
        }
        return root;
    }

    public AcStateOp delmgc(String word, AcStateOp root) {
        AcStateOp crt = null;
        if (root.getChildren().containsKey(word.charAt(0))) {
            // System.out.println("你鸭子这东西不存在啊。。。"+root.getChildren().get(word.charAt(0)).nodeCount);
            if (root.getChildren().get(word.charAt(0)).nodeCount == 1) {

                root.children.remove(word.charAt(0));
                // System.out.println("你鸭子这东西不存在啊。。。");
                return root;
            } else {
                crt = root.getChildren().get(word.charAt(0));
                crt.nodeCount--;
            }
        } else {
            //System.out.println("你鸭子这东西不存在啊。。。");
            return root;
        }
        for (int i = 1; i < word.length(); i++) {

            if (crt.getChildren().containsKey(word.charAt(i))) {
                if (crt.getChildren().get(word.charAt(i)).nodeCount == 1) {
                    crt.children.remove(word.charAt(i));
                    return root;
                } else {
                    crt = crt.getChildren().get(word.charAt(i));
                    crt.nodeCount--;
                }
            } else {
                //System.out.println("你鸭子这东西不存在啊。。。");
                return root;
            }
        }

        return root;

    }

    public AcStateOp failDeal(AcStateOp root) {
        AcStateOp current = new AcStateOp();
        AcStateOp temp = new AcStateOp();
        AcStateOp childNode = new AcStateOp();
        Object key = null;
        LinkedList queue = new LinkedList();
        Set<Object> keys = current.children.keySet();
        Iterator<Object> iter = keys.iterator();
        queue.add(root);
        //可是设置为根节点
        int traversedNode = 0;
        while (traversedNode != nodeCount + 1 && queue.size() != 0) {
            // System.out.println(queue.size());
            current = (AcStateOp) queue.remove();
            //取里面的第一个并在里面删除
            current.fail = root;
            //没看出有什么用
            keys = current.children.keySet();
            iter = keys.iterator();
            //获得所有的root名字的开始节点下的所有节点
            while (iter.hasNext()) {
                key = iter.next();
                childNode = (AcStateOp) current.children.get(key);
                queue.add(childNode);
                //把他们添加到queue里面//每次remove后添加一堆这样就按照顺序下去了
            }
            key = current.value;
            if (current.father == root || current == root) {
                //因为默认已经是root了
            } else {
                temp = current.father;
                while (true) {
                    //判断current的f()的值 
                    if (temp.fail == root) {
                        //如果它father的f为0
                        if (root.children.containsKey(key)) {
                            //判断g(0,current.value)是否存在
                            current.fail = root.children.get(key);
                            //因为这里已经不是二级了到3层以上了
                            //如果存在的话它的 f跳转为 对应的key的节点
                            break;
                        } else {
                            current.fail = root;
                            //如果g(0,current.value)不存在的话，跳转根节点
                            break;
                        }
                    } else if (temp.fail.getChildren().containsKey(key)) {
                        //如果它的前面那个跳转的不是根节点，而且又跳转的节点，并且跳转的节点有对应g（f(temp.fail),current.value）有值
                        current.fail = temp.fail.getChildren().get(key);
                        //所以对应的跳转是
                        break;
                    }
                    //  temp = temp.father;
                    temp = temp.fail;
//                  current.fail =root;
//                  break;
                    //++++++++++++++++++++没有往前推，直到遇到匹配的也行，不过没必要感觉，是不是应该直接设置成root呢、
                }
            }
            traversedNode++;
        }
        return root;
    }
}
