/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data.structure;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hiep
 */
public class DataInput {

    public int userID;
    public int itemID;
    public float score;
    public String context;

    public void sort(List<DataInput> lst) {
        for (int i = 0; i < lst.size() - 1; i++) {
            for (int j = i + 1; j < lst.size(); j++) {
                if (lst.get(i).context.compareTo(lst.get(j).context) > 0) {
                    DataInput temp = new DataInput();
                    temp = lst.get(i);
                    lst.set(i, lst.get(j));
                    lst.set(j, temp);
                }
            }
        }
    }

    public void sortUser(List<DataInput> lst) {
        for (int i = 0; i < lst.size() - 1; i++) {
            for (int j = i + 1; j < lst.size(); j++) {
                if (lst.get(i).userID > lst.get(j).userID) {
                    DataInput temp = new DataInput();
                    temp = lst.get(i);
                    lst.set(i, lst.get(j));
                    lst.set(j, temp);
                }
            }
        }
    }

    public void split(List<DataInput> temp, List<List<DataInput>> mFoldData) {
        List<String> contexts = new ArrayList<String>();
        List<Integer> counts = new ArrayList<Integer>();
        for (int i = 0; i < temp.size(); i++) {
            if (!contexts.contains(temp.get(i).context)) {
                contexts.add(temp.get(i).context);
            }
        }
        for (int c = 0; c < contexts.size(); c++) {
            int count = 0;
            for (int i = 0; i < temp.size(); i++) {
                if (temp.get(i).context.equals(contexts.get(c))) {
                    count++;
                }
            }
            counts.add(count);
        }
    }

    @Override
    public String toString() {
        return userID + "\t" + itemID + "\t" + context.replace("::", "\t") + score;
    }
}
