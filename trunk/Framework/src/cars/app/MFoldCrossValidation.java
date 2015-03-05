/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.alg.Algorithm;
import cars.alg.ItemSplitting;
import cars.evaluation.Evaluator;
import cars.evaluation.Estimation;
import cars.alg.memory.ContextualNeighbors;
import cars.alg.memory.FilterPoFUserBased;
import cars.alg.memory.PrefilterUserBased;
import cars.alg.memory.UserBasedTopK;
import cars.alg.memory.WeightPoFUserBased;
import cars.alg.model.FilterPoF;
import cars.alg.model.MFCACF;
import cars.alg.model.MFCF;
import cars.alg.model.Prefilter;
import cars.alg.model.WeightPoF;
import cars.data.DataManager;
import cars.data.DataSwitcher;
import cars.data.Food;
import cars.data.structure.Dataset;
import cars.data.structure.DataInput;
import cars.data.structure.ItemScorePair;
import cars.data.structure.Rating;
import cars.data.structure.TestInput;
import cars.newalg.STIGmeans;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hiep
 */
public class MFoldCrossValidation {

    public int m;
    public Evaluator[][] result;
    public List<List<DataInput>> mFoldData = new ArrayList<List<DataInput>>();
    public List<DataInput> entireData;
    public List<Integer> users;
    public int numUser;
    public int numItem;

    public void outputResult(int algIndex) {
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

    //Chuyển 1 phần thành testset
    public void tranformToTestSet(Dataset data, int mFold) {
        data.testingSet = new ArrayList<TestInput>();
        for (int i = 0; i < m; i++) {
            if (i == mFold) {
                for (int k = 0; k < mFoldData.get(i).size(); k++) {
                    DataInput di = mFoldData.get(i).get(k);
                    TestInput etc = new TestInput();
                    int user_id = di.userID;
                    int item_id = di.itemID;
                    float score = di.score;
                    String context = di.context;

                    //Kiem tra ton tai
                    int index = etc.indexOfExistance(data.testingSet, user_id, context);
                    if (index == -1) {
                        ItemScorePair pair = new ItemScorePair();
                        pair.itemID = item_id;
                        pair.score = score;
                        etc.userID = user_id;
                        etc.context = context;
                        etc.pairs = new HashMap<Integer, ItemScorePair>();
                        etc.pairs.put(0, pair);
                        data.testingSet.add(etc);
                    } else {
                        int size = data.testingSet.get(index).pairs.size();
                        ItemScorePair pair = new ItemScorePair();
                        pair.itemID = item_id;
                        pair.score = score;
                        data.testingSet.get(index).pairs.put(size, pair);
                    }
                }
            }
        }
    }

    //Chuyển m-1 thành traing set
    public void transformToTrainSet(Dataset data, int mFold) {
        data.mRatingWithContext = new Rating[numUser][numItem];
        for (int i = 0; i < m; i++) {
            if (i != mFold) {
                for (int k = 0; k < mFoldData.get(i).size(); k++) {
                    DataInput inp = mFoldData.get(i).get(k);
                    if (data.mRatingWithContext == null) {
                        data.mRatingWithContext = new Rating[numUser][numItem];
                    }
                    if (data.mRatingWithContext[inp.userID][inp.itemID] == null) {
                        data.mRatingWithContext[inp.userID][inp.itemID] = new Rating();
                    }
                    data.mRatingWithContext[inp.userID][inp.itemID].setValueRating(inp.score, inp.context);
                }
            }
        }
    }

    public void readFoodData() {
        entireData = new ArrayList<DataInput>();
        users = new ArrayList<Integer>();
        try {
            Scanner scanner = new Scanner(new File("D:\\data\\food.entire"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (!line.contains("::")) {
                    String[] elements = line.split(" ");
                    int u = Integer.parseInt(elements[0]);
                    int i = Integer.parseInt(elements[1]);
                    int c1 = Integer.parseInt(elements[2]);
                    int c2 = Integer.parseInt(elements[3]);
                    DataInput di = new DataInput();
                    di.context = c1 + "::" + c2 + "::";
                    di.itemID = i;
                    di.userID = u;
                    di.score = Float.parseFloat(elements[4]);
                    entireData.add(di);
                    if (!users.contains(u)) {
                        users.add(u);
                    }
                }

            }
        } catch (Exception ex) {
        }

    }

    public void dataSplitter() {
        DataInput dataInput = new DataInput();
        //dataInput.sortUser(entireData);
//        for (int u = 0; u < users.size(); u++) {
//            List<DataInput> temp = new ArrayList<DataInput>();
//            for (int i = 0; i < entireData.size(); i++) {
//                if (entireData.get(i).userID == users.get(u)) {
//                    temp.add(entireData.get(i));
//                }
//            }
//            dataInput.sort(temp);
//            dataInput.split(temp, mFoldData);
//        }
        mFoldData = new ArrayList<List<DataInput>>();
        for (int i = 0; i < 5; i++) {
            mFoldData.add(new ArrayList<DataInput>());
        }
        for (int i = 0; i < entireData.size(); i = i + 5) {
            boolean free[] = new boolean[5];
            int count = 0;
            for (int j = 0; j < 5; j++) {
                Random r = new Random();
                int k;
                do {
                    k = r.nextInt(5);
                } while (free[k] == true);
                free[k] = true;
                if (i + k < entireData.size()) {
                    mFoldData.get(j).add(entireData.get(i + k));
                }
            }
        }

    }

    public void output(int fold, int k, List<List<DataInput>> mFold) throws FileNotFoundException {
        String dir = "D:\\data\\food\\data" + fold + "\\food.dataset" + k;
        PrintWriter out = new PrintWriter(new File(dir));
        int count = 0;
        for (int i = 0; i < m; i++) {
            if (i != k) {
                for (int j = 0; j < mFold.get(i).size(); j++) {
                    out.println(mFold.get(i).get(j).toString());
                }
            }
        }

        out.close();
        dir = "D:\\data\\food\\data" + fold + "\\food.testset" + k;
        out = new PrintWriter(new File(dir));
        for (int i = 0; i < m; i++) {
            if (i == k) {
                DataInput di = new DataInput();
                di.sortUser(mFold.get(i));
                for (int j = 0; j < mFold.get(i).size(); j++) {
                    out.println(mFold.get(i).get(j).toString());
                }
            }
        }
        out.close();
    }

    private void readDataset(int dataIndex, int iFold, Dataset data) {
        data.mRatingWithContext = new Rating[data.numUser][data.numItem];

        for (int k = 0; k < 5; k++) {
            if (k != iFold) {
                String dir = "D:\\data\\food.fold" + k;
                try {
                    Scanner scanner = new Scanner(new File(dir));
                    while (scanner.hasNext()) {
                        String line = scanner.nextLine();
                        String[] elements = line.split("\t");
                        int u = Integer.parseInt(elements[0]);
                        int i = Integer.parseInt(elements[1]);
                        int c = Integer.parseInt(elements[2]);
                        int f = Integer.parseInt(elements[3]);
                        if (data.mRatingWithContext[u][i] == null) {
                            data.mRatingWithContext[u][i] = new Rating();
                        }
                        data.mRatingWithContext[u][i].setValueRating(Float.parseFloat(elements[4]), new int[]{c, f});
                        data.numRating++;
                    }
                    scanner.close();

                } catch (Exception ex) {
                }
            }
        }
        String dir_ = "D:\\data\\food.fold" + iFold;
        data.testingSet = new ArrayList<TestInput>();
        try {
            Scanner scanner = new Scanner(new File(dir_));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split("\t");
                TestInput etc = new TestInput();
                int[] test = new int[4];
                int user_id = Integer.parseInt(elements[0]);
                int item_id = Integer.parseInt(elements[1]);
                float score = Float.parseFloat(elements[4]);
                String context = elements[2] + "::" + elements[3] + "::";

                //Kiem tra ton tai
                int index = etc.indexOfExistance(data.testingSet, user_id, context);
                if (index == -1) {
                    ItemScorePair pair = new ItemScorePair();
                    pair.itemID = item_id;
                    pair.score = score;
                    etc.userID = user_id;
                    etc.context = context;
                    etc.pairs = new HashMap<Integer, ItemScorePair>();
                    etc.pairs.put(0, pair);
                    data.testingSet.add(etc);
                } else {
                    int size = data.testingSet.get(index).pairs.size();
                    ItemScorePair pair = new ItemScorePair();
                    pair.itemID = item_id;
                    pair.score = score;
                    data.testingSet.get(index).pairs.put(size, pair);
                }
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] arg) {
        MFoldCrossValidation m = new MFoldCrossValidation();
        m.m = 5;
        Algorithm.N = 20;
//        m.runAlgorithm(4);
        m.runAlgorithm2(4);
    }

    public void runAlgorithm(int dataIndex) {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\food.dat", 3);
        Algorithm.N = 3;
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        int numNeighbors = 120;
        for (int iFold = 0; iFold < m; iFold++) {
            System.out.println(new Date());
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

            UserBasedTopK UB = new UserBasedTopK(numNeighbors);
            UB.addData(ds);
            Estimation.EvaluateErrors(UB);
            result[0][iFold] = UB.evaluator;

            PrefilterUserBased FUB = new PrefilterUserBased(numNeighbors);
            FUB.addData(ds);
            FUB.extractDataByContext();
            Estimation.EvaluateErrorsWithContext(FUB);
            result[1][iFold] = FUB.evaluator;

            ItemSplitting IS = new ItemSplitting();
            IS.runUB(ds.data, ds.CP, numNeighbors);
            result[2][iFold] = IS.evaluator;
//
            WeightPoFUserBased WPOFUB = new WeightPoFUserBased(numNeighbors);
            WPOFUB.addData(ds);
            WPOFUB.numNeighborsInContext = 160;
            Estimation.EvaluateErrorsWithContext(WPOFUB);
            result[3][iFold] = WPOFUB.evaluator;

            FilterPoFUserBased FPOFUB = new FilterPoFUserBased(numNeighbors);
            FPOFUB.addData(ds);
            FPOFUB.threshold = (float) 0.1;
            FPOFUB.numNeighborsInContext = 160;
            Estimation.EvaluateErrorsWithContext(FPOFUB);
            result[4][iFold] = FPOFUB.evaluator;
////
            ContextualNeighbors CN = new ContextualNeighbors(600);
            CN.addData(ds);
            CN.contextProcessor.generateContextCombinationFromHierachy2();
            CN.evaluator = new Evaluator(CN.N);
            CN.evaluator.empty();
            Estimation.EvaluateErrorsWithContext(CN);
            result[5][iFold] = CN.evaluator;
        }
        outputResult(0);
        outputResult(1);
        outputResult(2);
        outputResult(3);
        outputResult(4);
        outputResult(5);
    }

//Chạy kiểm thử thuật toán
    public void runAlgorithm2(int dataIndex) {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\food.dat", 3);
        Algorithm.N = dm.getMaxItemsTestSet();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            System.out.println(new Date());
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();


            MFCF svd = new MFCF(10);
            svd.addData(ds);
            svd.maxIteration = 1000;
            svd.run();
            Estimation.EvaluateErrors(svd);
            result[0][iFold] = svd.evaluator;

//            Prefilter pr = new Prefilter();
//            pr.addData(ds);
//            pr.factor = 10;
//            pr.maxIteration = 1000;
//            pr.run();
//            Estimation.EvaluateErrorsWithContext(pr);
//            result[1][iFold] = pr.evaluator;
//
//            ItemSplitting IS = new ItemSplitting();
//            IS.factor = 10;
//            IS.maxIteration = 1000;
//            IS.run(ds.data, ds.CP, false);
//            result[2][iFold] = IS.evaluator;
//
//            WeightPoF wp = new WeightPoF();
//            wp.factor = 10;
//            wp.maxIteration = 100;
//            wp.addData(ds);
//            wp.run();
//            wp.numNeighborsInContext = 160;
//            Estimation.EvaluateErrorsWithContext(wp);
//            result[3][iFold] = wp.evaluator;
//
//            FilterPoF fp = new FilterPoF();
//            fp.addData(ds);
//            fp.factor = 10;
//            fp.maxIteration = 1000;
//            fp.svd = wp.svd;
//            fp.numNeighborsInContext = 160;
//            fp.threshold = (float) 0.1;
//            Estimation.EvaluateErrorsWithContext(fp);
//            fp.evaluator.training_Time = wp.evaluator.training_Time;
//            result[4][iFold] = fp.evaluator;
//
//            MFCACF mf = new MFCACF();
//            mf.addData(ds);
//            mf.factor = 10;
//            mf.maxIteration = 1000;
//            mf.run();
//            Estimation.EvaluateErrorsWithContext(mf);
//            result[5][iFold] = mf.evaluator;
//            System.out.println(iFold);
            STIGmeans ic = new STIGmeans();
            ic.addData(ds);
            ic.contextProcessor.generateContextCombinationFromHierachy2();
            
            ic.run();
            Estimation.EvaluateErrorsWithContext(ic);
            result[1][iFold] = ic.evaluator;

        }
        outputResult(0);
        outputResult(1);
//        outputResult(2);
//        outputResult(3);
//        outputResult(4);
//        outputResult(5);

    }
    //Chạy kiểm thử với dữ liệu Movielens
}
