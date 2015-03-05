/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg;

import cars.evaluation.Estimation;
import cars.evaluation.Evaluator;
import cars.data.DataSwitcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Hiep
 */
public class Algorithm {

    public ContextProcessor contextProcessor;
    public String name;
    public static int N = 20;
    public int numNeighbor;
    public Evaluator evaluator = new Evaluator(N);
    public cars.data.structure.Dataset dataset;
    public int memoryFlag;

    public float prediction(int u, int i, String c) {
        return 0;
    }

    public float prediction(int u, int i) {
        return 0;
    }

    public int[] order(List<float[]> res) {
//        for (int i = 0; i < res.size() - 1; i++) {
//            for (int j = i + 1; j < res.size(); j++) {
//                if (res.get(i)[1] < res.get(j)[1]) {
//                    float index = res.get(i)[0];
//                    float val = res.get(i)[1];
//                    res.get(i)[0] = res.get(j)[0];
//                    res.get(i)[1] = res.get(j)[1];
//                    res.get(j)[0] = index;
//                    res.get(j)[1] = val;
//                }
//            }
//        }

        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                float[] a = (float[]) o1;
                float[] b = (float[]) o2;
                if (a[1] == b[1]) {
                    return 0;
                }
                if (a[1] < b[1]) {
                    return 1;
                }
                return -1;
            }
        };
        Collections.sort(res, comparator); // use the comparator as much as u want

        List<Integer> recommendingItems = new ArrayList<Integer>();
        int count = 0;
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i)[1] > 0) {
                recommendingItems.add(Math.round(res.get(i)[0]));
            } else {
                count++;
            }
        }
        int[] res2 = ArrayUtils.toPrimitive((Integer[]) recommendingItems.toArray(new Integer[recommendingItems.size()]));
        return res2;
    }

    public List<float[]> order2(List<float[]> res) {
//        for (int i = 0; i < res.size() - 1; i++) {
//            for (int j = i + 1; j < res.size(); j++) {
//                if (res.get(i)[1] < res.get(j)[1]) {
//                    float index = res.get(i)[0];
//                    float val = res.get(i)[1];
//                    res.get(i)[0] = res.get(j)[0];
//                    res.get(i)[1] = res.get(j)[1];
//                    res.get(j)[0] = index;
//                    res.get(j)[1] = val;
//                }
//            }
//        }

        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                float[] a = (float[]) o1;
                float[] b = (float[]) o2;
                if (a[1] == b[1]) {
                    return 0;
                }
                if (a[1] < b[1]) {
                    return 1;
                }
                return -1;
            }
        };
        Collections.sort(res, comparator); // use the comparator as much as u want
        return res;
    }

    public int[] sort(List<float[]> res) {
        int[] listItem;
        int max;
        max = res.size();
//        if (res.size() > N) {
//            max = N;
//        } else {
//            max = res.size();
//        }
        listItem = new int[max];
//
//        if (N == 0) {
//            return null;
//        }


        for (int i = 0; i < res.size() - 1; i++) {
            for (int j = i + 1; j < res.size(); j++) {
                if (res.get(i)[1] < res.get(j)[1]) {
                    float index = res.get(i)[0];
                    float val = res.get(i)[1];
                    res.get(i)[0] = res.get(j)[0];
                    res.get(i)[1] = res.get(j)[1];
                    res.get(j)[0] = index;
                    res.get(j)[1] = val;
                }
            }
        }
        for (int i = 0; i < max; i++) {
            listItem[i] = (Integer) Math.round((float) res.get(i)[0]);
        }
        return listItem;
    }

    public int[] recommend(int user, List<Integer> ratedItemsByUser) {
        if (memoryFlag == 1) {
            this.neighbors(user);
        }
        List<float[]> res = new ArrayList<float[]>();
        for (int i = 0; i < ratedItemsByUser.size(); i++) {
            int item = ratedItemsByUser.get(i);
//            if (dataset.mRating[user][item] == 0) {
            float[] r = new float[2];
            r[0] = item;
            r[1] = prediction(user, item);
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

    public int[] recommend(int user, String context, List<Integer> ratedItemsByUser) {
        if (memoryFlag == 1) {
            this.neighbors(user, context);
        }
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

    //Memory
    public void neighbors(int user, String context) {
    }

    public void neighbors(int user) {
    }

    public void addData(DataSwitcher ds) {
        dataset = ds.data;
        contextProcessor = ds.CP;
//        dataset.findRatedItemsByUser();
    }

    public void runEvaluation() {
        Estimation.EvaluateErrorsWithContext(this);
        System.out.println(this.name);
        evaluator.print();
    }

    public List<float[]> descending(List<float[]> res) {
        Comparator comparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                float[] a = (float[]) o1;
                float[] b = (float[]) o2;
                if (a[1] == b[1]) {
                    return 0;
                }
                if (a[1] < b[1]) {
                    return 1;
                }
                return -1;
            }
        };
        Collections.sort(res, comparator); // use the comparator as much as u want
        return res;
    }

    public List<float[]> recommend2(int user, List<Integer> ratedItemsByUser) {
        if (memoryFlag == 1) {
            this.neighbors(user);
        }
        List<float[]> res = new ArrayList<float[]>();
        for (int i = 0; i < ratedItemsByUser.size(); i++) {
            int item = ratedItemsByUser.get(i);
//            if (dataset.mRating[user][item] == 0) {
            float[] r = new float[2];
            r[0] = item;
            r[1] = prediction(user, item);
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
        return order2(res);
    }

    public List<float[]> recommend2(int user, String context, List<Integer> ratedItemsByUser) {
        if (memoryFlag == 1) {
            this.neighbors(user, context);
        }
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
        return order2(res);
    }
}
