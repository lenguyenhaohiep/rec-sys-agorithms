/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.newalg;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hiep
 */
public class STIGmeans extends Algorithm {

    static public int m;
    static public Evaluator[][] result;
    double[][] data;
    List<ItemProfile> itemProfiles = new ArrayList<ItemProfile>();
    public Dataset tempDataset;
    public MFCF mf = new MFCF(10);
    ClustersData clusters;
    Map<String, Double> biasContext = new HashMap<String, Double>();
    Map<String, Integer> newItems = new HashMap<String, Integer>();

    private List<Double> convertToList(double[] x) {
        List<Double> lst = new ArrayList<Double>();
        for (int i = 0; i < x.length; i++) {
            lst.add(x[i]);
        }
        return lst;
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
        for (int i = 0; i < tempDataset.numItem; i++) {
            for (int c = 0; c < contextProcessor.contextsGereration.length; c++) {
                String item = i + "=>" + contextProcessor.contextsGereration[c];
                newItems.put(item, findNewItemID(i, contextProcessor.contextsGereration[c], clusters.k_Cluster_label));
            }
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
        data.numItem = clusterData.centers.size();
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

    @Override
    public float prediction(int u, int i, String c) {
        //int newID = findNewItemID(i, c, clusters.k_Cluster_label);
        int newID = newItems.get(i + "=>" + c);
        float predictiveValue = (mf.prediction(u, newID) + biasContext.get(u + "=>" + c).floatValue());
        if (predictiveValue < 1) {
            return 1;
        }
        if (predictiveValue < 5) {
            return predictiveValue;
        }
        return 5;
    }

    public void run() {
        computeBiasContext();
        itemProfiles = convertToListProfiles(tempDataset);
        data = convertToSingleList(itemProfiles);
        Clustering clustering = new Clustering();
        clusters = clustering.gmeansCluster(data, 0.001, data.length);
        dataset = convertoNewDataset(clusters);
        generateNewItemID();
        System.out.println("cluster completed");
//        Estimation.EvaluateErrorsWithContext(this);
//        this.evaluator.print();
        mf.dataset = dataset;
        mf.run();
        Estimation.EvaluateErrorsWithContext(this);
        evaluator.print();
    }

    public static void main(String[] arg) {
        DataManager dm = new DataManager();
        result = new Evaluator[1][10];
        dm.dataProcessor("D:\\Recommender_Systems\\dataset_full\\hmusic.l3", 3);
        m = 5;
        Algorithm.N = 5;
        for (int i = 0; i < m; i++) {
            DataSwitcher ds = new DataSwitcher();
            ds.data = dm.getDataset(i);
            ds.CP = dm.getContextProcessor();
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy2();
            STIGmeans ic = new STIGmeans();
            //ic.fullDataset = dm.getFullDataset();
            ic.tempDataset = dm.getDataset(i);
            ic.addData(ds);
            ic.run();
            result[0][i] = ic.evaluator;
        }
        outputResult(0);
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
}
