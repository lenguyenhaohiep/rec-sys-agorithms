/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.memory;

import cars.alg.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hiep
 */
public class PostFilterUserBased extends ContextualPostFiltering {

    public float threshold = (float) 0.1;
    public UserBasedTopK usbtk = new UserBasedTopK();

    public PostFilterUserBased() {
        this.memoryFlag = 1;
        usbtk.numNeighbor = this.numNeighbor;
        usbtk.dataset = this.dataset;
    }

    public void run2DRS() {
        usbtk.numNeighbor = this.numNeighbor;
        usbtk.dataset = this.dataset;
    }

    @Override
    public int[] recommend(int user, String context, List<Integer> ratedItemsByUser) {
        neighborsInContext = neighborsOfUser(user, context);
        usbtk.numNeighbor = this.numNeighbor;
        usbtk.dataset = this.dataset;
        usbtk.neighbors(user);
        List<float[]> res = new ArrayList<float[]>();
        for (int i = 0; i < ratedItemsByUser.size(); i++) {
            int item = ratedItemsByUser.get(i);
//            if (dataset.mRatingWithContext[user][item] == null) {
//            if (dataset.mRating[user][item] == 0) {
                float[] r = new float[2];
                r[0] = item;
                r[1] = prediction(user, item, context);
                res.add(r);
//            }
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
