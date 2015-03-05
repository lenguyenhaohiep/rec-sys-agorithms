/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author Hiep
 */
public class ContextualPostFiltering extends Algorithm {

    public int factor;
    public int numNeighborsInContext;
    public int[] neighborsInContext;

    public double cosinSimilitary(int user1, int user2, String context) {
        double a = 0;
        double b = 0;
        double c = 0;
        for (int i = 0; i < dataset.numItem; i++) {
            if (dataset.mRatingWithContext[user1][i] != null && dataset.mRatingWithContext[user2][i] != null) {
                if (dataset.mRatingWithContext[user1][i].contextScorePairs.containsKey(context)
                        && dataset.mRatingWithContext[user2][i].contextScorePairs.containsKey(context)) {
                    if (dataset.mRatingWithContext[user1][i].contextScorePairs.get(context) > 0 && dataset.mRatingWithContext[user2][i].contextScorePairs.get(context) > 0) {
                        //a += mRating.get(user1, i) * mRating.get(user2, i);
                        //b += Math.pow(mRating.get(user1, i), 2);
                        //c += Math.pow(mRating.get(user2, i), 2);
                        a += dataset.mRatingWithContext[user1][i].contextScorePairs.get(context) * dataset.mRatingWithContext[user2][i].contextScorePairs.get(context);
                        b += Math.pow(dataset.mRatingWithContext[user1][i].contextScorePairs.get(context), 2);
                        c += Math.pow(dataset.mRatingWithContext[user2][i].contextScorePairs.get(context), 2);
                    }
                }
            }
        }
        if (b == 0 || c == 0) {
            return 0;
        }
        double cosin = a / (Math.sqrt(b) * Math.sqrt(c));
        return cosin;
    }

    public int[] neighborsOfUser(int user, String c) {
        List<Vector> neighbors = new ArrayList<Vector>();
        for (int u = 0; u < dataset.numUser; u++) {
            if (u != user) {
                Vector neighbor = new Vector();
                neighbor.add(u);
                neighbor.add(cosinSimilitary(user, u, c));
                neighbors.add(neighbor);
            }
        }

        for (int i = 0; i < neighbors.size() - 1; i++) {
            for (int j = i + 1; j < neighbors.size(); j++) {
                if ((Double) neighbors.get(i).get(1) < (Double) neighbors.get(j).get(1)) {
                    int index = (Integer) neighbors.get(i).get(0);
                    double value = (Double) neighbors.get(i).get(1);
                    neighbors.get(i).set(0, neighbors.get(j).get(0));
                    neighbors.get(i).set(1, neighbors.get(j).get(1));
                    neighbors.get(j).set(0, index);
                    neighbors.get(j).set(1, value);
                }
            }
        }

        int maxNeighbors = numNeighborsInContext;
        if (neighbors.size() < numNeighborsInContext) {
            maxNeighbors = neighbors.size();
        }
        //System.out.print("user-" + user + " ");

        int count = 0;
        List<Integer> nei = new ArrayList<Integer>();
        for (int i = 0; i < maxNeighbors; i++) {
            if ((Double) neighbors.get(i).get(1) > 0) {
                //similarUsers[count] = (Integer) neighbors.get(i).get(0);
                nei.add((Integer) neighbors.get(i).get(0));
                count++;
                if (count == maxNeighbors) {
                    break;
                }
            }
            //System.out.print(similarUsers[i] + " ");
        }
        int[] similarUsers = new int[nei.size()];
        for (int i = 0; i < nei.size(); i++) {
            similarUsers[i] = nei.get(i);
        }
        //System.out.println();
        return similarUsers;
    }

    public int neighborsRateItemInContext(int[] users, int i, String c) {
        int count = 0;
        for (int j = 0; j < users.length; j++) {
            if (dataset.mRatingWithContext[users[j]][i] != null) {
                if (dataset.mRatingWithContext[users[j]][i].contextScorePairs.containsKey(c)) {
                    if (dataset.mRatingWithContext[users[j]][i].contextScorePairs.get(c) > 3) {
                        count++;
                    }
                    //System.out.println("count = " + count);
                }
            }
        }
        return count;
    }

    public float contextualProbability(int u, int i, String c) {

        if (neighborsInContext.length == 0) {
            return 0;
        }
        int corateNeighborsInContext = neighborsRateItemInContext(neighborsInContext, i, c);

        //System.out.println(corateNeighborsInContext + "====" + neighbors.length);

        float probability = (float) corateNeighborsInContext / neighborsInContext.length;

        //System.out.println(probability);
        return probability;
    }

    public void run() {
    }

    /**
     * @return the factor
     */
    public int getFactor() {
        return factor;
    }

    /**
     * @param factor the factor to set
     */
    public void setFactor(int factor) {
        this.factor = factor;
    }
}
