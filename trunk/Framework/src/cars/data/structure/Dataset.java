/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data.structure;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.ejml.data.DenseMatrix64F;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class Dataset {
    /*
     * Dataset
     */

    public float[] avgRatingByUser;
    public int[] numRatingByUser;
    public int numUser;
    public int numItem;
    public int numRating;
    public int contextDimension;
    public int[] numContextAtDimension;
    public float[][] mRating;
    public Rating[][] mRatingWithContext;
    public List<TestInput> testingSet = new ArrayList<TestInput>();
    public List<Integer> ratedItemsByUser = new ArrayList<Integer>();
    public int maxSelectedNumberItemsToTest;

    public void reduceContextDimension() {
        this.computeAvgRatingByUser();
        mRating = new float[numUser][numItem];
        for (int u = 0; u < numUser; u++) {
            for (int i = 0; i < numItem; i++) {
                float sum = 0;
                int count = 0;
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        sum += entry.getValue();
                        count++;
                    }
                }

                if (count == 0) {
                    mRating[u][i] = 0;
                } else {
                    mRating[u][i] = (float) sum / count;
                }
            }
        }
    }

    public boolean checkCaseInTestSet(int userid, int itemid) {
        for (TestInput t : testingSet) {
            if (t.userID == userid) {
                for (Map.Entry<Integer, ItemScorePair> p : t.pairs.entrySet()) {
                    if (p.getValue().itemID == itemid) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void computeAvgRatingByUser() {
        avgRatingByUser = new float[numUser];
        numRatingByUser = new int[numUser];
        for (int u = 0; u < numUser; u++) {
            int count = 0;
            int sumRating = 0;
            for (int i = 0; i < numItem; i++) {
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        count++;
                        sumRating += entry.getValue();
                    }
                }
            }
            if (count > 0) {
                avgRatingByUser[u] = (float) (sumRating) / count;
                numRatingByUser[u] = count;
            } else {
                avgRatingByUser[u] = 0;
                numRatingByUser[u] = 0;
            }
//            if (count < 3) {
//                System.out.println(u);
//            }
        }
    }

    public void findRatedItemsByUser() {
        for (int k = 0; k < testingSet.size(); k++) {
            for (Map.Entry<Integer, ItemScorePair> entry : testingSet.get(k).pairs.entrySet()) {
                if (entry.getValue().score >= 3) {
                    testingSet.get(k).items.add(entry.getValue().itemID);
                }
            }

            Collections.sort(testingSet.get(k).items);
        }

    }
//    public void findRatedItemsByUser() {
//        for (int k = 0; k < testingSet.size(); k++) {
//            for (Map.Entry<Integer, ItemScorePair> entry : testingSet.get(k).pairs.entrySet()) {
//                if (!ratedItemsByUser.contains(entry.getValue().itemID)) {
//                    ratedItemsByUser.add(entry.getValue().itemID);
//                }
//            }
//        }
//        for (int k = 0; k < testingSet.size(); k++) {
//            int count = 0;
//            for (int i = 0; i < ratedItemsByUser.size(); i++) {
//                int item = ratedItemsByUser.get(i);
//                if (mRatingWithContext[testingSet.get(k).userID][item] == null) {
//                    if (!testingSet.get(k).items.contains(item)) {
//                        testingSet.get(k).items.add(item);
//                        count++;
//                    }
//                } else {
//                    if (!mRatingWithContext[testingSet.get(k).userID][item].contextScorePairs.containsKey(testingSet.get(k).context)) {
//                        if (!testingSet.get(k).items.contains(item)) {
//                            testingSet.get(k).items.add(item);
//                            count++;
//                        }
//                    }
//                }
//            }
//            Collections.sort(testingSet.get(k).items);
//        }
//
//    }

    public void print(List<Integer> lsr) {
        //Collections.sort(lsr);
        for (int i = 0; i < lsr.size(); i++) {
            System.out.print(lsr.get(i) + "  ");
        }
        System.out.println();
    }

    public Dataset combineTestAndTrainSet() {
        Dataset _new = this;
        for (int i = 0; i < _new.testingSet.size(); i++) {
            int user_id = _new.testingSet.get(i).userID;
            for (Map.Entry<Integer, ItemScorePair> entry : _new.testingSet.get(i).pairs.entrySet()) {
                int item_id = entry.getValue().itemID;
                float realRating = entry.getValue().score;
                if (_new.mRatingWithContext[user_id][item_id] == null) {
                    _new.mRatingWithContext[user_id][item_id] = new Rating();
                }
                numRating++;
                _new.mRatingWithContext[user_id][item_id].setValueRating(realRating, _new.testingSet.get(i).context);
            }
        }
        return _new;
    }
}
