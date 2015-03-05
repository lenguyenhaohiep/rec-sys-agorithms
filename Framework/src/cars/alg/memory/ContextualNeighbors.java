/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.memory;

import cars.alg.Algorithm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Hiep
 */
public class ContextualNeighbors extends AlgMemory {

    List<Vector> neighbors;

    public ContextualNeighbors(int numNeighbors) {
        numNeighbor = (numNeighbors);
        name = ("Contextual Neighbors CM");

    }

    public ContextualNeighbors() {
        name = ("Contextual Neighbors CM");
    }

    public float cosinSimilitary(int user1, String context1, int user2, String context2) {
        float a = 0;
        float b = 0;
        float c = 0;
        for (int i = 0; i < dataset.numItem; i++) {
            if (dataset.mRatingWithContext[user1][i] != null && dataset.mRatingWithContext[user2][i] != null) {
                if (dataset.mRatingWithContext[user1][i].contextScorePairs.containsKey(context1) && dataset.mRatingWithContext[user2][i].contextScorePairs.containsKey(context2)) {
                    a += dataset.mRatingWithContext[user1][i].contextScorePairs.get(context1) * dataset.mRatingWithContext[user2][i].contextScorePairs.get(context2);
                    b += Math.pow(dataset.mRatingWithContext[user1][i].contextScorePairs.get(context1), 2);
                    c += Math.pow(dataset.mRatingWithContext[user2][i].contextScorePairs.get(context2), 2);
                }
            }
        }
        if (b == 0 || c == 0) {
            return 0;
        }
        float cosin = (float) (a / (Math.sqrt(b) * Math.sqrt(c)));
        return cosin;
    }

    private List<Vector> selectNeighbors(List<Vector> neighbors, int num) {
        List<String> contexts = new ArrayList<String>();
        for (int i = 0; i < neighbors.size(); i++) {
            if (!contexts.contains((String) neighbors.get(i).get(1))) {
                contexts.add((String) neighbors.get(i).get(1));
            }
        }
        if (contexts.size() == 0) {
            return new ArrayList<Vector>();
        }
        int _num = num / contexts.size();
        List<Vector> res = new ArrayList<Vector>();
        for (int i = 0; i < contexts.size(); i++) {
            int count = 0;
            for (int j = 0; j < neighbors.size(); j++) {
                if (((String) neighbors.get(j).get(1)).equals(contexts.get(i))) {
                    count++;
                    res.add(neighbors.get(j));
                }
                if (count == _num) {
                    break;
                }
            }
        }
        return res;
    }

    @Override
    public void neighbors(int user, String context) {
        neighbors = new ArrayList<Vector>();
        for (int u = 0; u < dataset.numUser; u++) {

            if (u != user) {
                for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
                    float cos = cosinSimilitary(user, context, u, contextProcessor.contextsGereration[c]);
                    if (cos > 0) {
                        Vector neighbor = new Vector();
                        neighbor.add(u);
                        neighbor.add(contextProcessor.contextsGereration[c]);
                        neighbor.add(cos);
                        neighbors.add(neighbor);
                    }
                }
            }
        }


        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Vector a1 = (Vector) o1;
                Vector b1 = (Vector) o2;
                float a = (Float) a1.get(2);
                float b = (Float) b1.get(2);
                if (a == b) {
                    return 0;
                }
                if (a < b) {
                    return 1;
                }
                return -1;
            }
        };
        Collections.sort(neighbors, comparator); // use the comparator as muc


//        for (int i = 0; i < neighbors.size() - 1; i++) {
//            for (int j = i + 1; j < neighbors.size(); j++) {
//                if ((Float) neighbors.get(i).get(2) < (Float) neighbors.get(j).get(2)) {
//                    int index = (Integer) neighbors.get(i).get(0);
//                    String indexContext = (String) neighbors.get(i).get(1);
//                    Float value = (Float) neighbors.get(i).get(2);
//                    neighbors.get(i).set(0, neighbors.get(j).get(0));
//                    neighbors.get(i).set(1, neighbors.get(j).get(1));
//                    neighbors.get(i).set(2, neighbors.get(j).get(2));
//                    neighbors.get(j).set(0, index);
//                    neighbors.get(j).set(1, indexContext);
//                    neighbors.get(j).set(2, value);
//                }
//            }
//        }

//        int avg = numNeighbor / contextProcessor.contextsGereration.length;
//        List<Vector> temp = new ArrayList<Vector>();
//        for (int i = 0; i < contextProcessor.contextsGereration.length; i++) {
//            int count = 0;
//            for (int k = 0; k < neighbors.size(); k++) {
//                if (neighbors.get(k).get(1).toString().equals(contextProcessor.contextsGereration[i])) {
//                    temp.add(neighbors.get(k));
//                    count++;
//                }
//                if (count == avg) {
//                    break;
//                }
//            }
//        }
        int maxNeighbors = numNeighbor;
        //neighbors = temp;
        if (neighbors.size() < numNeighbor) {
            maxNeighbors = neighbors.size();
        }
        //System.out.print("user-" + user + " ");
        List<Vector> similarUsers = new ArrayList<Vector>();
//        for (int i = 0; i < maxNeighbors; i++) {
//            similarUsers.add(neighbors.get(i));
//            //System.out.print(similarUsers[i] + " ");
//        }

        similarUsers = selectNeighbors(neighbors, maxNeighbors);
        //System.out.println();
        neighbors = similarUsers;
    }

    @Override
    public float prediction(int u, int i, String c) {
        float sum = 0;
        float k = 0;
        for (int j = 0; j < neighbors.size(); j++) {
            if (dataset.mRatingWithContext[(Integer) neighbors.get(j).get(0)][i] != null) {
                if (dataset.mRatingWithContext[(Integer) neighbors.get(j).get(0)][i].contextScorePairs.containsKey((String) neighbors.get(j).get(1))) {
                    sum += (Float) neighbors.get(j).get(2) * dataset.mRatingWithContext[(Integer) neighbors.get(j).get(0)][i].contextScorePairs.get((String) neighbors.get(j).get(1));
                    k += Math.abs((Float) neighbors.get(j).get(2));
                }
            }
        }
        if (k == 0) {
            return 0;
        }
        return sum / k;
    }
}
