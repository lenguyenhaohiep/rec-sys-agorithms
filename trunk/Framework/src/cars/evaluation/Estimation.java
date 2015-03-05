/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.evaluation;

import cars.alg.Algorithm;
import cars.alg.memory.FilterPoFUserBased;
import cars.alg.memory.UserBasedTopK;
import cars.alg.memory.WeightPoFUserBased;
import cars.data.structure.ItemScorePair;
import cars.data.structure.TestInput;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hiep
 */
public class Estimation {

    public static void EvaluateErrorsWithContext(Algorithm a) {
        Date t1 = new Date();

        List<TestInput> testingSet = a.dataset.testingSet;

        int numTestSet = 0;
        double RMSE = 0;
        double MAE = 0;


        for (int i = 0; i < testingSet.size(); i++) {
            int user_id = testingSet.get(i).userID;
            String context = testingSet.get(i).context;
            testingSet.get(i).recommendingItems2 = a.recommend2(user_id, context, testingSet.get(i).items);
            for (Map.Entry<Integer, ItemScorePair> entry : testingSet.get(i).pairs.entrySet()) {
                int item_id = entry.getValue().itemID;
                double realRating = entry.getValue().score;
                double ratingPrediction = a.prediction(user_id, item_id, context);

//                if (ratingPrediction > 0) {

                //Just only for KDD data
               
                if (Double.isNaN(ratingPrediction)) {
                    ratingPrediction = 0;
                }
                MAE += Math.abs(ratingPrediction - realRating);
                RMSE += Math.abs(ratingPrediction - realRating) * Math.abs(ratingPrediction - realRating);
                numTestSet++;
//                System.out.println(realRating + "\t" + ratingPrediction);
//                }
            }
        }
        MAE = MAE / numTestSet;
        RMSE = Math.sqrt(RMSE / numTestSet);
        DecimalFormat df = new DecimalFormat("0.000");

        Date t2 = new Date();
        a.evaluator.testing_Time = (t2.getTime() - t1.getTime());

        a.evaluator.MAE = MAE;
        a.evaluator.RMSE = RMSE;
        a.evaluator.computeAccuracy();
        //int[] num = new int[]{1, 10, 20, 30, 50, 100, 150, 200, 250, 300};
//        int[] num = new int[]{1, 10, 20, 30, 50, 100, 150, 200, 250, 300};

        //for (int k = 1; k <= Algorithm.N; k = k + 1) {
//        a.evaluator.topKRecommend(testingSet, a.N);
        //}

        double[] x = new double[]{1, 1.5, 2, 2.5, 3, 3.5, 4, 4.5, 5};
        for (int i = 0; i < x.length; i++) {
            a.evaluator.thresholdRecommend(testingSet, x[i]);
        }

    }

    public static void EvaluateErrors(Algorithm a) {
        Date t1 = new Date();
        List<TestInput> testingSet = a.dataset.testingSet;

        int numTestSet = 0;
        double RMSE = 0;
        double MAE = 0;
        for (int i = 0; i < testingSet.size(); i++) {
            int user_id = testingSet.get(i).userID;
            testingSet.get(i).recommendingItems = a.recommend(user_id, testingSet.get(i).items);
            for (Map.Entry<Integer, ItemScorePair> entry : testingSet.get(i).pairs.entrySet()) {
                int item_id = entry.getValue().itemID;
                double realRating = entry.getValue().score;
                double ratingPrediction = a.prediction(user_id, item_id);
//                if (ratingPrediction > 0) {
                
                 //Just only for KDD data
                
                MAE += Math.abs(ratingPrediction - realRating);
                RMSE += Math.abs(ratingPrediction - realRating) * Math.abs(ratingPrediction - realRating);
                numTestSet++;
//                    System.out.println(realRating + "=>" + ratingPrediction);
//                }
            }
        }

        MAE = MAE / numTestSet;
        RMSE = Math.sqrt(RMSE / numTestSet);
        DecimalFormat df = new DecimalFormat("0.000");
        Date t2 = new Date();
        a.evaluator.testing_Time = (t2.getTime() - t1.getTime());
        a.evaluator.MAE = MAE;
        a.evaluator.RMSE = RMSE;
        //a.evaluator.computeAccuracy();
        // for (int k = 1; k <= Algorithm.N; k = k + 1) {
        //a.evaluator.topKRecommend(testingSet, a.N);
        //}
    }
}
