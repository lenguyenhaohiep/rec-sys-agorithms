/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.studyadvisor.modeling;

import cars.newalg.*;
import cars.alg.Algorithm;
import cars.alg.model.MFCF;
import cars.alg.stat.Clustering;
import cars.alg.stat.ClustersData;
import cars.data.DataManager;
import cars.data.DataSwitcher;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import cars.evaluation.Estimation;
import cars.evaluation.Evaluator;
import com.sun.jmx.remote.internal.ArrayQueue;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hiep
 */
public class CARSAlg extends Algorithm {

    static public int m;
    static public Evaluator[][] result;
    double[][] data;
    List<ItemProfile> itemProfiles = new ArrayList<ItemProfile>();
    public Dataset tempDataset;
    public MFCF mf = new MFCF(10);
    ClustersData clusters;
    Map<String, Double> biasContext = new HashMap<String, Double>();
    Map<String, Double> weighs = new HashMap<String, Double>();
    Map<String, Integer> newItems = new HashMap<String, Integer>();
    int n;
    boolean[][] graph;
    double[][] g_weigh;
    public List<String> itemContextCombination = new ArrayList<String>();
    public List<Integer> Traces = new ArrayList<Integer>();
    public List<Integer> Marks = new ArrayList<Integer>();

    private List<Double> convertToList(double[] x) {
        List<Double> lst = new ArrayList<Double>();
        for (int i = 0; i < x.length; i++) {
            lst.add(x[i]);
        }
        return lst;
    }

    public static void outputResult2(int algIndex) {
        Evaluator averageResult = new Evaluator(Algorithm.N);
        for (int i = 0; i < m; i++) {
            averageResult.MAE += result[algIndex][i].MAE;
            averageResult.RMSE += result[algIndex][i].RMSE;
            for (int k = 0; k < result[algIndex][i].P.size(); k++) {
                if (averageResult.P.size() <= k) {
                    averageResult.P.add(result[algIndex][i].P.get(k));
                    averageResult.R.add(result[algIndex][i].R.get(k));
                } else {
                    double P = averageResult.P.get(k);
                    double R = averageResult.R.get(k);
                    averageResult.P.set(k, P + result[algIndex][i].P.get(k));
                    averageResult.R.set(k, R + result[algIndex][i].R.get(k));
                }
            }
        }
        averageResult.average(m);
        averageResult.output2();
    }

    public static void outputResult(int algIndex) {
        Evaluator averageResult = new Evaluator(Algorithm.N);
        for (int i = 0; i < m; i++) {
            averageResult.MAE += result[algIndex][i].MAE;
            averageResult.RMSE += result[algIndex][i].RMSE;
            averageResult.recall += result[algIndex][i].recall;
            averageResult.precision += result[algIndex][i].precision;
            for (int k = 0; k < result[algIndex][i].k; k++) {
                averageResult.aPrecision[k] += result[algIndex][i].aPrecision[k];
                averageResult.aRecall[k] += result[algIndex][i].aRecall[k];
            }
            averageResult.training_Time += result[algIndex][i].training_Time;
            averageResult.testing_Time += result[algIndex][i].testing_Time;
        }
        averageResult.average(m);
        averageResult.output();
    }

    private void generateNewItemID() {
//        for (int i = 0; i < tempDataset.numItem; i++) {
//            for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
//                String item = i + "=>" + contextProcessor.contextsGereration[c];
//                newItems.put(item, findNewItemID(i, contextProcessor.contextsGereration[c], clusters.k_Cluster_label));
//            }
//        }
        for (int i = 0; i < itemContextCombination.size(); i++) {
            String item = itemContextCombination.get(i);
            newItems.put(item, findNewItemID(Integer.parseInt(item.split("=>")[0]), item.split("=>")[1], clusters.k_Cluster_label));
        }
    }

    private void computeBiasContext() {
        for (int u = 0; u < tempDataset.numUser; u++) {
            for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
                double val = 0.0;
                String key = u + "=>" + contextProcessor.contextsGereration[c];
                double sumAll = 0, sumContext = 0;
                int countAll = 0, countContext = 0;
                for (int i = 0; i < tempDataset.numItem; i++) {
                    if (tempDataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : tempDataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            countAll++;
                            sumAll += entry.getValue();
                            if (entry.getKey().equals(contextProcessor.contextsGereration[c])) {
                                countContext++;
                                sumContext += entry.getValue();
                            }
                        }
                    }
                }
                if (sumContext > 0) {
                    val = (sumContext / countContext) - (sumAll / countAll);
                }
                biasContext.put(key, val);

            }
        }
    }

    private double[][] convertToSingleList(List<ItemProfile> profiles) {
        double[][] res = new double[profiles.size()][dataset.numUser];
        for (int i = 0; i < profiles.size(); i++) {
            res[i] = profiles.get(i).ratings;
        }
        return res;
    }

    private boolean checkEqual(double[] a, double[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean checkExist(int itemid, String context, List<ItemProfile> lst) {
        for (int i = 0; i < lst.size(); i++) {
            if (lst.get(i).itemID == itemid && lst.get(i).context.equals(context)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkContains(List<ItemProfile> lstData, ItemProfile ip) {
        boolean check = false;
        for (int i = 0; i < lstData.size(); i++) {
            if (checkEqual(lstData.get(i).ratings, ip.ratings)) {
                return true;
            }
        }
        return check;
    }

    private List<ItemProfile> convertToListProfiles(Dataset _dataset) {
        List<ItemProfile> lstData = new ArrayList<ItemProfile>();

        for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
            for (int i = 0; i < _dataset.numItem; i++) {
                ItemProfile profile = new ItemProfile();
                profile.itemID = i;
                profile.context = contextProcessor.contextsGereration[c];
                for (int u = 0; u < _dataset.numUser; u++) {
                    if (_dataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : _dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            if (entry.getKey().equals(profile.context)) {
                                if (profile.ratings == null) {
                                    profile.ratings = new double[_dataset.numUser];
                                }
                                profile.ratings[u] = entry.getValue();
                            }
                        }
                    }
                }
                if (profile.ratings != null) {
//                    if (!checkContains(lstData, profile)) {
                    lstData.add(profile);
//                    }
                } else {
                    profile.ratings = new double[_dataset.numUser];
                    lstData.add(profile);
                }
            }
        }
        return lstData;
    }

    private double[] computeNewItemVector(List<List<Double>> cluster) {
        int n = cluster.get(0).size();
        int[] numbers = new int[n];
        double[] vals = new double[n];
        for (int i = 0; i < cluster.size(); i++) {
            for (int j = 0; j < n; j++) {
                if (cluster.get(i).get(j) > 0) {
                    vals[j] = vals[j] + cluster.get(i).get(j);
                    numbers[j]++;
                }
            }
        }
        for (int i = 0; i < n; i++) {
            if (numbers[i] > 0) {
                vals[i] /= numbers[i];
            }
        }
        return vals;

    }

    private Dataset convertoNewDataset(ClustersData clusterData) {
        Dataset data = new Dataset();
        data.numUser = dataset.numUser;
        data.numItem = clusterData.clusters.size();
        data.mRating = new float[data.numUser][data.numItem];
        for (int i = 0; i < data.numItem; i++) {
            if (clusterData.clusters.get(i).size() > 0) {
                double[] center = computeNewItemVector(clusterData.clusters.get(i));
                for (int u = 0; u < data.numUser; u++) {
                    data.mRating[u][i] = (float) center[u];
                }
            }
        }

        data.testingSet = dataset.testingSet;
//        for (int i = 0; i < data.testingSet.size(); i++) {
//            String context = data.testingSet.get(i).context;
//            for (Map.Entry<Integer, ItemScorePair> entry : data.testingSet.get(i).pairs.entrySet()) {
//                entry.getValue().itemID = findNewItemID(entry.getValue().itemID, context, clusterData.k_Cluster_label);
//            }
//
//        }
        return data;
    }
    int count = 0;

    @Override
    public float prediction(int u, int i, String c) {
        int newID = -1;
        try {
            count++;
            newID = newItems.get(i + "=>" + c);
        } catch (Exception ex) {
        }
        float predictiveValue = (mf.prediction(u, newID) + biasContext.get(u + "=>" + c).floatValue());
//        System.out.print(mf.prediction(u, newID) + "---" + biasContext.get(u + "=>" + c).floatValue() + "   ");
        if (predictiveValue < 1) {
            return 1;
        }
        if (predictiveValue < 5) {
            return predictiveValue;
        }
        return 5;
    }

    public void run() {
        itemProfiles = convertToListProfiles(tempDataset);
        System.out.println("STI - Item Profiles " + new Date());
        data = convertToSingleList(itemProfiles);
        System.out.println("STI - Convert Single List " + new Date());
        Clustering clustering = new Clustering();
        clusters = graphCluster(data, data.length);
        System.out.println("STI - Cluster " + new Date());
        dataset = convertoNewDataset(clusters);
        
        System.out.println("STI - Generate ID " + new Date());
        mf.dataset = dataset;
        mf.run();
    }

    public static void main(String[] arg) {
        DataManager dm = new DataManager();
        result = new Evaluator[1][10];
        dm.dataProcessor("E:\\dataset\\hmusic.l3", 3);
        m = 5;
        Algorithm.N = 10;
        for (int i = 0; i < m; i++) {
            dm.itemContextCombination.clear();
            DataSwitcher ds = new DataSwitcher();
            ds.data = dm.getDataset(i);
            ds.CP = dm.getContextProcessor();
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy2();
            STI ic = new STI();
            ic.itemContextCombination = dm.itemContextCombination;
            ic.tempDataset = dm.getDataset(i);
            ic.addData(ds);
            ic.run();
            result[0][i] = ic.evaluator;
        }
        outputResult2(0);
    }

    private int findNewItemID(int itemID, String context, List<List<Integer>> k_Cluster) {
        int iProfile = 0;
        for (int i = 0; i < itemProfiles.size(); i++) {
            if (itemProfiles.get(i).itemID == itemID && itemProfiles.get(i).context.equals(context)) {
                iProfile = i;
                break;
            }
        }
        for (int i = 0; i < k_Cluster.size(); i++) {
            if (k_Cluster.get(i).contains(iProfile)) {
                return i;
            }
        }
        return -1;
    }

    private ClustersData graphCluster(double[][] data, int length) {
        ClustersData res = new ClustersData();
        n = length;
        graph = new boolean[length][length];
        g_weigh = new double[length][length];
        int count = 0;
        for (int i = 0; i < length; i++) {
            double max = 1000000000;
            int index = i;
            for (int j = 0; j < length; j++) {
                if (i != j) {
                    double weigh = 0;
                    if (g_weigh[i][j] == 0) {
                        weigh = computeWeigh(data[i], data[j]);
                        g_weigh[i][j] = weigh;
                        g_weigh[j][i] = weigh;
                        count++;
                    } else {
                        weigh = g_weigh[i][j];
                    }
                    if (weigh < max) {
                        max = weigh;
                        index = j;
                    }
                } else {
                    graph[i][j] = true;
                }
            }
            graph[i][index] = true;
            graph[index][i] = true;
        }
        res.k_Cluster_label = findConnectedComponents();
        res.clusters = new ArrayList<List<List<Double>>>();
        for (int i = 0; i < res.k_Cluster_label.size(); i++) {
            List<List<Double>> c = new ArrayList<List<Double>>();
            for (int j = 0; j < res.k_Cluster_label.get(i).size(); j++) {
                c.add(convertToList(data[res.k_Cluster_label.get(i).get(j)]));
            }
            res.clusters.add(c);
        }
        return res;
    }

    private double computeWeigh2(double[] a, double[] b) {
        double avg1 = 0, avg2 = 0;
        int count = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0) {
                avg1 += a[i];
                count++;
            }
        }
        avg1 = avg1 / count;
        count = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] > 0) {
                avg2 += b[i];
                count++;
            }
        }
        avg2 = avg2 / count;
        double x = 0, y = 0, z = 0;
        double weigh1 = 0, weigh2 = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0 && b[i] > 0) {
                x += (a[i] - avg1) * (b[i] - avg2);
                y += (a[i] - avg1) * (a[i] - avg1);
                z += (b[i] - avg2) * (b[i] - avg2);
            }
        }
        if (y == 0 || z == 0) {
            weigh1 = 0;
            for (int i = 0; i < a.length; i++) {
                weigh2 += Math.pow(a[i] - b[i], 2);
            }
            if (weigh2 == 0) {
                return 1;
            }
        } else {
            weigh1 = (x / (Math.sqrt(z) * Math.sqrt(y)));
        }
        return weigh1;
    }

    private double computeWeigh(double[] a, double[] b) {
        double x = 0, y = 0, z = 0;
        double weigh1 = 0, weigh2 = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0 && b[i] > 0) {
                x += a[i] * b[i];
                y += a[i] * a[i];
                z += b[i] * b[i];
            }
        }
        if (y == 0 || z == 0) {
            weigh1 = 1;
        } else {
            weigh1 = (1 - x / (Math.sqrt(z) * Math.sqrt(y)));
        }
//        for (int i = 0; i < a.length; i++) {
//            weigh2 += Math.pow(a[i] - b[i], 2);
//        }
//        weigh2 = Math.sqrt(weigh2);
        return weigh1 + weigh2;
    }

    public List<List<Integer>> warShall() {

        boolean[] free = new boolean[n];
        for (int i = 0; i < n; i++) {
            free[i] = true;
        }
        for (int k = 0; k < n; k++) {
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    graph[u][v] = graph[u][v] || graph[u][k] && graph[k][v];
                }
            }
        }
        List<List<Integer>> level2 = new ArrayList<List<Integer>>();
        for (int u = 0; u < n; u++) {
            if (free[u]) {
                List<Integer> level1 = new ArrayList<Integer>();
                for (int v = 0; v < n; v++) {
                    if (graph[u][v]) {
                        //System.out.print(v + "-");
                        free[v] = false;
                        level1.add(v);
                    }
                }
                level2.add(level1);
                //System.out.println();
            }
        }
        return level2;
    }

    private List<Integer> BFS(int s) {
        int front = 0, rear = 0, u, v;
        boolean[] trace = new boolean[n];
        int[] queue = new int[n];
        queue[0] = s;
        for (int i = 0; i < n; i++) {
            trace[i] = true;
        }
        trace[s] = false;
        do {
            u = queue[front];
            front++;
            for (v = 0; v < n; v++) {
                if (graph[u][v] && trace[v]) {
                    rear++;
                    queue[rear] = v;
                    trace[v] = false;
                }
            }
        } while ((front <= rear) && (rear < n - 1));
        List<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            if (!trace[i]) {
                res.add(i);
            }
        }
        return res;

    }

    private List<List<Integer>> findConnectedComponents() {
        boolean[] free = new boolean[n];
        for (int i = 0; i < n; i++) {
            free[i] = true;
        }
        List<List<Integer>> level2 = new ArrayList<List<Integer>>();
        for (int u = 0; u < n; u++) {
            if (free[u]) {
                List<Integer> level1 = new ArrayList<Integer>();
                level1 = BFS(u);
                level2.add(level1);
                for (int i = 0; i < level1.size(); i++) {
                    free[level1.get(i)] = false;
                }
            }
        }
        return level2;
    }

    private boolean checkInTest(String context, int itemID) {
        for (int i = 0; i < dataset.testingSet.size(); i++) {
            if (dataset.testingSet.get(i).context.equals(context)) {
                for (Map.Entry<Integer, ItemScorePair> entry : dataset.testingSet.get(i).pairs.entrySet()) {
                    if (entry.getValue().itemID == itemID) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
