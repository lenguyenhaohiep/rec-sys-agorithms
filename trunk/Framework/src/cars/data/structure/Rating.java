/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data.structure;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Hiep
 */
public class Rating {

    public Map<String, Float> contextScorePairs = new HashMap<String, Float>();

    public void setValueRating(float score, int[] context) {
        String key = "";
        for (int i = 0; i < context.length; i++) {
            key += context[i] + "::";
        }
        contextScorePairs.put(key, score);
//        System.out.println(key);
    }
    
    public void setValueRating(float score, String context) {
        contextScorePairs.put(context, score);
    }
    

}
