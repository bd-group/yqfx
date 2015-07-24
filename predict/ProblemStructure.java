/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package predict;

/**
 *
 * @author Rain
 */
public class ProblemStructure {
    String text;
    int len;

    public ProblemStructure() {
        text = "";
        len = 0;
    }
    
    public ProblemStructure(int l, String tt) {
        text = tt;
        len = l;
    }
}
