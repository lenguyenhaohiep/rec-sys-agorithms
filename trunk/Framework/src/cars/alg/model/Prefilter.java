/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.model;

import cars.alg.Algorithm;
import cars.alg.MatrixFunc;
import java.util.Date;
import java.util.Map;
import java.util.Vector;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class Prefilter extends AlgModel {
    /*
     * Input
     */

    public int factor;
    public int maxIteration = 10000;
    /*
     * Output
     */
    float[] avgscores;
    float[][] biasUser;
    float[][] biasItem;
    Vector[][] userFactorVector;
    Vector[][] itemFactorVector;

    public Prefilter() {
        this.name = "Pre-Fitlering with SVD";
    }

    public void initialize() {
        int numContextCombination = contextProcessor.contextsGereration.length;
        biasUser = new float[numContextCombination][dataset.numUser];
        biasItem = new float[numContextCombination][dataset.numItem];
        userFactorVector = new Vector[numContextCombination][dataset.numUser];
        itemFactorVector = new Vector[numContextCombination][dataset.numItem];
        avgscores = new float[numContextCombination];
    }

    @Override
    public float prediction(int u, int i, String context) {
        int c = contextProcessor.getIndexOfContextInHeirachy(context);
        float[] Pu = MatrixFunc.fromVector(userFactorVector[c][u]);
        float[] Qi = MatrixFunc.fromVector(itemFactorVector[c][i]);
        float predictedRating = (float) 0.0;
        predictedRating = avgscores[c] + biasUser[c][u] + biasItem[c][i] + MatrixFunc.dot(Pu, Qi);
        if (predictedRating == Float.NaN) {
            predictedRating = 0;
        }
        return predictedRating;

    }

    public void run() {
        Date t1 = new Date();
        initialize();
        int newItemModel = 0;
        for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
            float[][] mRating = new float[dataset.numUser][dataset.numItem];
            int numRatingInContext = 0;
            float avgScoreInContext = 0;
            for (int u = 0; u < dataset.numUser; u++) {
                for (int i = 0; i < dataset.numItem; i++) {
                    int count = 0;
                    double sum = 0.0;
                    if (dataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            if (contextProcessor.checkContextBelongsToHierachy(c, entry.getKey())) {
                                count++;
                                sum += entry.getValue();
                                numRatingInContext++;
                                avgScoreInContext += entry.getValue();
                            }
                        }
                    }
                    if (count == 0) {
                        mRating[u][i] = 0;
                    } else {
                        mRating[u][i] = (float) sum / count;
                    }
                }
            }
            if (numRatingInContext > 0) {
                avgScoreInContext = avgScoreInContext / numRatingInContext;
                newItemModel = c;
            } else {
                avgScoreInContext = 0;
            }

            MFCF svd = new MFCF(factor);
            svd.maxIteration = maxIteration;
            svd.dataset = this.dataset;
            //svd.numItem = numItem;
            svd.numRating = numRatingInContext;
            //svd.numUser = numUser;
            svd.avgScore = avgScoreInContext;
            //svd.mRating = mRating;
            svd.dataset.mRating = mRating;
            svd.run();

            avgscores[c] = avgScoreInContext;
            biasItem[c] = svd.biasItem;
            biasUser[c] = svd.biasUser;
            itemFactorVector[c] = svd.itemFactorVector;
            userFactorVector[c] = svd.userFactorVector;

        }
//        for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
//            if (avgscores[c] == 0) {
//                avgscores[c] = avgscores[newItemModel];
//                biasItem[c] = biasItem[newItemModel];
//                biasUser[c] = biasUser[newItemModel];
//                itemFactorVector[c] = itemFactorVector[newItemModel];
//                userFactorVector[c] = userFactorVector[newItemModel];
//            }
//        }
        Date t2 = new Date();
        evaluator.training_Time = (t2.getTime() - t1.getTime()) / 1000;
    }
}
