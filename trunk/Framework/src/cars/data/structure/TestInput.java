/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data.structure;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hiep
 */
public class TestInput {

    public int userID;
    public String context;
    //Cặp giá trị itemid-score
    public Map<Integer, ItemScorePair> pairs = new HashMap<Integer, ItemScorePair>();
    public List<float[]> recommendingItems2 = new ArrayList<float[]>();
    public int[] recommendingItems;
    public List<Integer> items = new ArrayList<Integer>();

    public int indexOfExistance(List<TestInput> lst, int userID, String context) {
        for (int i = 0; i < lst.size(); i++) {
            TestInput input = lst.get(i);
            if (userID == input.userID && context.equals(lst.get(i).context)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfExistance(List<TestInput> lst, int user_id) {
        for (int i = 0; i < lst.size(); i++) {
            TestInput input = lst.get(i);
            if (user_id == input.userID) {
                return i;
            }
        }
        return -1;
    }

    public boolean isDuplicated(List<TestInput> lst, int user_id, int item_id, String context) {
        for (int i = 0; i < lst.size(); i++) {
            TestInput input = lst.get(i);
            if (user_id == input.userID && context.equals(input.context)) {
                for (Map.Entry<Integer, ItemScorePair> entry : input.pairs.entrySet()) {
                    if (entry.getValue().itemID == item_id) {
                        System.out.print("trùng");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Integer> getRatedItems() {
        List<Integer> items = new ArrayList<Integer>();
        for (Map.Entry<Integer, ItemScorePair> entry : pairs.entrySet()) {
            items.add(entry.getValue().itemID);

        }
        return items;
    }
}
