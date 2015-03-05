/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.model;

import cars.alg.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Hiep
 */
public class PostFilter extends ContextualPostFiltering {
    /*
     * Input
     */

    public MFCF svd;
    public int maxIteration = 10000;

    @Override
    public void run() {
        Date t1 = new Date();
        svd = new MFCF(factor);
        svd.maxIteration = maxIteration;
        svd.dataset = this.dataset;
        svd.run();
        Date t2 = new Date();
        evaluator.training_Time = ((t2.getTime() - t1.getTime()) / 1000);
    }

    @Override
    public int[] recommend(int user, String context, List<Integer> ratedItemsByUser) {
        neighborsInContext = neighborsOfUser(user, context);
        List<float[]> res = new ArrayList<float[]>();
        for (int i = 0; i < ratedItemsByUser.size(); i++) {
            int item = ratedItemsByUser.get(i);
//            if (dataset.mRating[user][item] == 0) {
            float[] r = new float[2];
            r[0] = item;
            r[1] = prediction(user, item, context);
            res.add(r);
//            } else {
//                if (dataset.checkCaseInTestSet(user, item) == true) {
//                    float[] r = new float[2];
//                    r[0] = item;
//                    r[1] = prediction(user, item);
//                    res.add(r);
//
//                }
//            }
        }
        return order(res);
    }
}
