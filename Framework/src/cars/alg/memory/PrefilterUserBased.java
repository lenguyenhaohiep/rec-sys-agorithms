/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.memory;

import cars.alg.Algorithm;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class PrefilterUserBased extends AlgMemory {
    /*
     * Input
     */

    float[][][] mRatingByContext;
    UserBasedTopK usbtk = new UserBasedTopK();

    public PrefilterUserBased(int numNeighbors) {
        numNeighbor = (numNeighbors);
        name = ("Pre-Filtering with User-based Top K");
    }

    public PrefilterUserBased() {
        name = ("Pre-Filtering with User-based Top K");
    }

    public void extractDataByContext() {
        mRatingByContext = new float[contextProcessor.contextsGereration.length][][];
        for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
            mRatingByContext[c] = new float[dataset.numUser][dataset.numItem];
            for (int u = 0; u < dataset.numUser; u++) {
                for (int i = 0; i < dataset.numItem; i++) {
                    if (dataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            if (contextProcessor.checkContextBelongsToHierachy(c, entry.getKey())) {
                                //System.out.println(contextProcessor.contextsGereration[c] + " " + u + " " + i);
                                mRatingByContext[c][u][i] = entry.getValue();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public float prediction(int u, int i, String context) {
        return usbtk.prediction(u, i);
    }


    @Override
    public int[] recommend(int user, String context, List<Integer> ratedItemsByUser) {
        int c = contextProcessor.getIndexOfContextInHeirachy(context);
        //usbtk.numItem = this.numItem;
        //usbtk.numUser = this.numUser;
        usbtk.numNeighbor = numNeighbor;
        //usbtk.mRating = this.mRatingByContext[c];
        usbtk.dataset = this.dataset;
        usbtk.dataset.mRating = this.mRatingByContext[c];
        usbtk.neighbors = this.neighbors;
        usbtk.numNeighbor = this.numNeighbor;
        usbtk.neighbors(user);
        List<float[]> res = new ArrayList<float[]>();
        for (int i = 0; i < ratedItemsByUser.size(); i++) {
            int item = ratedItemsByUser.get(i);
//            if (dataset.mRatingWithContext[user][item] == null) {
                float[] r = new float[2];
                r[0] = item;
                r[1] = prediction(user, item, context);
                res.add(r);
//            } else {
//                if (!dataset.mRatingWithContext[user][item].contextScorePairs.containsKey(context)) {
//                    float[] r = new float[2];
//                    r[0] = item;
//                    r[1] = prediction(user, item, context);
//                    res.add(r);
//                }
//            }
        }
        return order(res);
    }
}
