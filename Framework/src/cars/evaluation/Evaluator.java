/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.evaluation;

import cars.data.structure.ItemScorePair;
import cars.data.structure.TestInput;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.math.stat.descriptive.rank.Max;

/**
 *
 * @author Hiep
 */
public class Evaluator {

    //parameter
    public int hits;
    public int topks;
    public int tests;
    public int k;
    //result
    public double RMSE;
    public double MAE;
    public double precision;
    public double recall;
    public double fmeasure;
    public double precision2;
    public double recall2;
    public double fmeasure2;
    public long training_Time;
    public long testing_Time;
    public int tp, fn, fp;
    public double[] aPrecision, aRecall;
    public int[] density = new int[10];
    public List<Double> P = new ArrayList<Double>();
    public List<Double> R = new ArrayList<Double>();

    public void updateDensity(double rating) {
        if (rating >= 0 && rating < 0.5) {
            density[0]++;
        }
        if (rating >= 0.5 && rating < 1) {
            density[1]++;
        }
        if (rating >= 1 && rating < 1.5) {
            density[2]++;
        }
        if (rating >= 1.5 && rating < 2) {
            density[3]++;
        }
        if (rating >= 2 && rating < 2.5) {
            density[4]++;
        }
        if (rating >= 2.5 && rating < 3) {
            density[5]++;
        }
        if (rating >= 3.0 && rating < 3.5) {
            density[6]++;
        }
        if (rating >= 3.5 && rating < 4) {
            density[7]++;
        }
        if (rating >= 4.0 && rating < 4.5) {
            density[8]++;
        }
        if (rating >= 4.5 && rating < 5) {
            density[9]++;
        }
    }

    public Evaluator(int _k) {
        k = _k;
        aPrecision = new double[_k];
        aRecall = new double[_k];
    }

    public void empty() {
        RMSE = 0;
        MAE = 0;
        precision = 0;
        recall = 0;
        fmeasure = 0;
        training_Time = 0;
        testing_Time = 0;
        hits = 0;
        topks = 0;
        tests = 0;
    }

    public void average(int m) {
        RMSE /= m;
        MAE /= m;
        precision /= m;
        recall /= m;
        fmeasure /= m;
        precision2 /= m;
        recall2 /= m;
        fmeasure2 /= m;
        training_Time /= m;
        testing_Time /= m;
        for (int i = 0; i < k; i++) {
            aRecall[i] /= m;
            aPrecision[i] /= m;
        }

        for (int i = 0; i < P.size(); i++) {
            P.set(i, P.get(i) / m);
            R.set(i, R.get(i) / m);
        }
    }

    public void computeAccuracy() {
        precision2 = ((double) tp / (tp + fp));
        recall2 = ((double) tp / (tp + fn));
        fmeasure2 = ((double) (2 * precision2 * recall2) / (precision2 + recall2));
        precision = ((double) hits / topks);
        recall = ((double) hits / tests);
        fmeasure = (2 * precision * recall) / (precision + recall);
        tp = 0;
        fp = 0;
        fn = 0;
        hits = 0;
        topks = 0;
        tests = 0;
    }

    public void computePP() {
        precision2 = tp / (tp + fp);
        recall2 = tp / (tp + fn);
        fmeasure2 = (2 * precision2 * recall2) / (precision2 + recall2);
        precision = ((double) hits / topks);
        recall = ((double) hits / tests);
        fmeasure = (2 * precision * recall) / (precision + recall);
        tp = 0;
        fp = 0;
        fn = 0;
        hits = 0;
        topks = 0;
        tests = 0;
    }

    public void print() {
        output();
    }

    public void print2() {
        output2();
    }

    public void output() {
        average(1);
        DecimalFormat df = new DecimalFormat("0.000");
        System.out.println(df.format(MAE));
        System.out.println(df.format(RMSE));
        df = new DecimalFormat("0.0000000");
        System.out.println(training_Time);
        System.out.println(testing_Time);
        System.out.print("x=[");
        for (int i = 0; i < k; i++) {
            if (aRecall[i] > 0) {
                System.out.print(df.format(aRecall[i]) + ",");
            }
        }
        System.out.print(df.format(aRecall[k - 1]) + "]");
        System.out.println();
        System.out.print("y=[");
        for (int i = 0; i < k; i++) {
            if (aPrecision[i] > 0) {
                System.out.print(df.format(aPrecision[i]) + ",");
            }
        }
        System.out.print(df.format(aPrecision[k - 1]) + "]");
        System.out.println();
    }

    public void updateParameter(int[] recommendedItem, Map<Integer, ItemScorePair> testSet) {
        int numberOfHits = 0;
        int _test = 0;
        for (Map.Entry<Integer, ItemScorePair> entry : testSet.entrySet()) {
            if (entry.getValue().score > 3) {
                _test++;
                for (int i = 0; i < recommendedItem.length; i++) {
                    if (recommendedItem[i] == entry.getValue().itemID) {
                        numberOfHits++;
                        break;
                    }
                }
            }
        }
        if (_test > 0) {
            hits += numberOfHits;
            //tests += testSet.size();
            tests += _test;
            topks += recommendedItem.length;
        }
    }

    public void updateParameter(int userid, float avg, int[] recommendedItem, Map<Integer, ItemScorePair> testSet) {
        int numberOfHits = 0;
        //tests += testSet.size();
        if (recommendedItem == null) {
            return;
        }
        topks += recommendedItem.length;
        for (Map.Entry<Integer, ItemScorePair> entry : testSet.entrySet()) {
            if (entry.getValue().score > 3) {
                tests++;
                boolean check = false;
                for (int i = 0; i < recommendedItem.length; i++) {
                    if (recommendedItem[i] == entry.getValue().itemID) {
                        numberOfHits++;
                        check = true;
                        break;
                    }
                }

                if ((entry.getValue().score >= avg && entry.getValue().score >= 3) && check) {
                    tp++;
                }
                if ((entry.getValue().score < avg || entry.getValue().score < 3) && check) {
                    fp++;
                }
                if ((entry.getValue().score >= avg && entry.getValue().score >= 3) && !check) {
                    fn++;
                }
            }

        }

        hits += numberOfHits;

    }

    public int countHit(List<Integer> testsetItems, List<Integer> recommendItems) {
        int _hits = 0;
        for (int i = 0; i < recommendItems.size(); i++) {
            for (int j = 0; j < testsetItems.size(); j++) {
//                System.out.print(recommendItems.get(i) + "-" + testsetItems.get(j));
                //
                if (recommendItems.get(i).equals(testsetItems.get(j))) {
                    _hits++;
//                    System.out.print("Hit");
                    break;
                }
//                System.out.println();
            }
        }
        return _hits;
    }

    public List<Integer> getGoodItemFromTestSet(TestInput t) {
        List<Integer> goodItems = new ArrayList<Integer>();
        for (Map.Entry<Integer, ItemScorePair> entry : t.pairs.entrySet()) {
            if (entry.getValue().score > 3) {
                goodItems.add(entry.getValue().itemID);
            }
        }
        return goodItems;
    }

    //Tính Precison-Recall Dựa trên tất cả các sản phẩm tốt dựa trên một ngưỡng định sẵn
    public void topKRecommend(List<TestInput> testSet, int K) {
        topks = hits = tests = 0;
        double _precision = 0;
        double _recall = 0;
        int count = 0;
        for (int i = 0; i < testSet.size(); i++) {
            List<Integer> testItems = getGoodItemFromTestSet(testSet.get(i));
            List<Integer> recommendingItems = getTopKFromRecommedingSet(testSet.get(i), K);
            if (testItems.size() > 0) {
//                if (recommendingItems.size() > 0) {
                int _top = recommendingItems.size();
                int _test = testItems.size();
                int _hit = countHit(testItems, recommendingItems);
                topks += k;
                tests += _test;
                hits += _hit;
                if (_top > 0) {
                    _precision += (double) _hit / _top;
                    _recall += (double) _hit / _test;
                    count++;
                }
//                }
//                if (hits == tests - 1) {

//                }
//                System.out.println(topks);
            }
        }
//        precision = ((double) hits / topks);
//        recall = ((double) hits / tests);
//        System.out.println("1-" + precision + "==" + recall);
        precision = _precision / count;
        recall = _recall / count;
//        System.out.println("2-" + precision + "==" + recall);
//        System.out.println("hits=" + hits + ",tests= " + tests + ", topks=" + topks);
//        System.out.println(precision + "\t" + recall);
        aPrecision[K - 1] = precision;
        aRecall[K - 1] = recall;
//        System.out.println("+++++++++++++++++++++++++++=");
    }

    private List<Integer> getTopKFromRecommedingSet(TestInput t, int K) {
        List<Integer> goodItems = new ArrayList<Integer>();
        int count = 0;
        for (int i = 0; i < t.recommendingItems.length; i++) {
            count++;
            goodItems.add(Math.round(t.recommendingItems[i]));
            if (count == K) {
                break;
            }
        }
        return goodItems;
    }

    private List<Integer> getThresholdRecommedingSet(TestInput t, double d) {
        List<Integer> goodItems = new ArrayList<Integer>();
        int count = 0;
        for (int i = 0; i < t.recommendingItems2.size(); i++) {
            count++;
            if (t.recommendingItems2.get(i)[1] >= d) {
                goodItems.add(Math.round(t.recommendingItems2.get(i)[0]));
            }
        }
        return goodItems;
    }

    void thresholdRecommend(List<TestInput> testSet, double d) {
        topks = hits = tests = 0;
        double _precision = 0;
        double _recall = 0;
        int count = 0;
        for (int i = 0; i < testSet.size(); i++) {
            List<Integer> testItems = getGoodItemFromTestSet(testSet.get(i));
            List<Integer> recommendingItems = getThresholdRecommedingSet(testSet.get(i), d);
            if (testItems.size() > 0) {
                int _top = recommendingItems.size();
                int _test = testItems.size();
                int _hit = countHit(testItems, recommendingItems);
                topks += k;
                tests += _test;
                hits += _hit;
                if (_top > 0) {
                    if (_test > 0) {
                        _precision += (double) _hit / _top;
                        _recall += (double) _hit / _test;
                    }
                    count++;
                }

            }
        }
        precision = _precision / count;
        recall = _recall / count;
        P.add(precision);
        R.add(recall);
    }

    public void output2() {
        average(1);
        DecimalFormat df = new DecimalFormat("0.000");
        System.out.println(df.format(MAE));
        System.out.println(df.format(RMSE));
//        df = new DecimalFormat("0.0000000");
//        System.out.print("Recall \t");
//        for (int i = 0; i < R.size(); i++) {
//            System.out.print(df.format(R.get(i)) + "\t");
//        }
//        System.out.println();
//        System.out.print("Precision \t");
//        for (int i = 0; i < P.size(); i++) {
//            System.out.print(df.format(P.get(i)) + "\t");
//        }
//        System.out.println();
    }
}
