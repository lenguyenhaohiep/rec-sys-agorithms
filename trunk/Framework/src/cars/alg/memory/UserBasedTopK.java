/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.memory;

import cars.evaluation.Estimation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Hiep
 */
public class UserBasedTopK extends AlgMemory {
    /*
     * input
     */

    public int numRating;

    public UserBasedTopK(int numNeighbors) {
        this.numNeighbor = numNeighbors;
        this.name = "User-based Top K\t";
    }

    public UserBasedTopK() {
        this.name = "User-based Top K\t";
    }

    public float cosinSimilitary(int user1, int user2) {
        float a = 0;
        float b = 0;
        float c = 0;
        for (int i = 0; i < dataset.numItem; i++) {
            if (dataset.mRating[user1][i] > 0 && dataset.mRating[user2][i] > 0) {
                a += dataset.mRating[user1][i] * dataset.mRating[user2][i];
                b += Math.pow(dataset.mRating[user1][i], 2);
                c += Math.pow(dataset.mRating[user2][i], 2);
            }
        }
        if (b == 0 || c == 0) {
            return 0;
        }
        float cosin = (float) (a / (Math.sqrt(b) * Math.sqrt(c)));
        return cosin;
    }

    @Override
    public void neighbors(int user) {
        neighbors = new ArrayList<Vector>();
        for (int u = 0; u < dataset.numUser; u++) {
            if (u != user) {
                float cos = cosinSimilitary(user, u);
                if (cos > 0) {
                    Vector neighbor = new Vector();
                    neighbor.add(u);
                    neighbor.add(cos);
                    neighbors.add(neighbor);
                }
            }
        }
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Vector a1 = (Vector) o1;
                Vector b1 = (Vector) o2;
                float a = (Float) a1.get(1);
                float b = (Float) b1.get(1);
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

        int maxNeighbors = numNeighbor;
        if (neighbors.size() < numNeighbor) {
            maxNeighbors = neighbors.size();
        }
        //System.out.print("user-" + user + " ");
        List<Vector> similarUsers = new ArrayList<Vector>();
        for (int i = 0; i < maxNeighbors; i++) {
            similarUsers.add(neighbors.get(i));
            //System.out.print(similarUsers[i] + " ");
        }
        //System.out.println();
        neighbors = similarUsers;
    }

    @Override
    public float prediction(int u, int i) {
        //double predict = svd.ratingSVDPrediction(u, i);
        //System.out.print("user-item-context:" + u + "," + i + "," + c + ":");
        //neighbors(u);
        float sum = 0;
        float k = 0;
        for (int j = 0; j < neighbors.size(); j++) {
            if (dataset.mRating[(Integer) neighbors.get(j).get(0)][i] > 0) {
                sum += (Float) neighbors.get(j).get(1) * dataset.mRating[(Integer) neighbors.get(j).get(0)][i];
                k += Math.abs((Float) neighbors.get(j).get(1));
            }
        }
        if (k == 0) {
            return 0;
        }
        return sum / k;
    }

    public float prediction2(int u, int i) {
        //double predict = svd.ratingSVDPrediction(u, i);
        //System.out.print("user-item-context:" + u + "," + i + "," + c + ":");
        //neighbors(u);
        float sum = 0;
        float k = 0;
        for (int j = 0; j < neighbors.size(); j++) {
            if (dataset.mRating[(Integer) neighbors.get(j).get(0)][i] > 0) {
                sum += (Float) neighbors.get(j).get(1) * dataset.mRating[(Integer) neighbors.get(j).get(0)][i];
                k += Math.abs((Float) neighbors.get(j).get(1));
            }
        }
        if (k == 0) {
            return 0;
        }
        System.out.println(sum / k);
        return sum / k;
    }

    @Override
    public void runEvaluation() {
        Estimation.EvaluateErrors(this);
        System.out.println(this.name);
        evaluator.print();
    }
}
