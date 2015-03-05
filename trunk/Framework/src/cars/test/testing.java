/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.test;

import cars.alg.Algorithm;
import cars.alg.ItemSplitting;
import cars.evaluation.Evaluator;
import cars.evaluation.Estimation;
import cars.alg.memory.*;
import cars.alg.model.*;
import cars.data.DataManager;
import cars.data.DataSwitcher;
import cars.newalg.STI;
import cars.newalg.STIGmeans;
import java.util.Date;

/**
 *
 * @author Hiep
 */
public class testing {

    public int m = 5;
    public Evaluator[][] result;
    public String path = "";

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

    public void outputResult2(int algIndex) {
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

    public static void main(String[] arg) {
        testing t = new testing();
//        t.path = "C:\\Users\\Hiep\\Dropbox\\Recommender_Systems\\dataset_full\\hmusic.l3";
//        Algorithm.N = 5;
////        t.runMemoryAlgorithm(100, 200, 80);
//        t.runModelAlgorithm(10, 1000, 80);
//        
        testing t1 = new testing();
        t1.path = "C:\\Users\\Hiep\\Dropbox\\Recommender_Systems\\dataset_full\\comoda.dat";
        Algorithm.N = 5;
//        t.runMemoryAlgorithm(100, 200, 80);
        t1.runModelAlgorithm(10, 1000, 80);
        
        
//        testing t2 = new testing();
//        t2.path = "C:\\Users\\Hiep\\Dropbox\\Recommender_Systems\\dataset_full\\hmusic.l3";
//        Algorithm.N = 5;
////        t.runMemoryAlgorithm(100, 200, 80);
////        t2.runModelAlgorithm(10, 1000, 80);
//        t2.runModelAlgorithmWithContextsRemoved(10, 1000, 80);
//        t.runFixedSplittedData(2, 30);



    }

    public void runMemoryAlgorithm(int numNeighbors, int numContextNeighbors, int numNeighborsInContext) {
        DataManager dm = new DataManager();
        dm.dataProcessor(path, 3);
//        dm.getFullDataset();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {

            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
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

            WeightPoFUserBased WPOFUB = new WeightPoFUserBased(numNeighbors);
            WPOFUB.addData(ds);
            WPOFUB.numNeighborsInContext = numNeighborsInContext;
            Estimation.EvaluateErrorsWithContext(WPOFUB);
            result[3][iFold] = WPOFUB.evaluator;
//
//
            ItemSplitting IS = new ItemSplitting();
            IS.runUB(ds.data, ds.CP, numNeighbors);
            result[2][iFold] = IS.evaluator;
////
            FilterPoFUserBased FPOFUB = new FilterPoFUserBased(numNeighbors);
            FPOFUB.addData(ds);
            FPOFUB.threshold = (float) 0.1;
            FPOFUB.numNeighborsInContext = numNeighborsInContext;
            Estimation.EvaluateErrorsWithContext(FPOFUB);
            result[4][iFold] = FPOFUB.evaluator;

            ContextualNeighbors CN = new ContextualNeighbors(numContextNeighbors);
            CN.addData(ds);
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
    public void runModelAlgorithm(int factor, int maxLoop, int numNeighborsInContext) {
        DataManager dm = new DataManager();
        dm.dataProcessor(path, 3);
        System.out.println(new Date() + " - Read Data Complete");
//        dm.getFullDataset();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            System.out.println(new Date() + " - Fold" + iFold);
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();


//            MFCF svd = new MFCF(factor);
//            svd.addData(ds);
//            svd.maxIteration = maxLoop;
//            svd.run();
//            Estimation.EvaluateErrors(svd);
//            result[0][iFold] = svd.evaluator;
////
//            Prefilter pr = new Prefilter();
//            pr.addData(ds);
//            pr.factor = factor;
//            pr.maxIteration = maxLoop;
//            pr.run();
//            Estimation.EvaluateErrorsWithContext(pr);
//            result[1][iFold] = pr.evaluator;

            ItemSplitting IS = new ItemSplitting();
            IS.factor = factor;
            IS.maxIteration = maxLoop;
            IS.run(ds.data, ds.CP, false);
            result[2][iFold] = IS.evaluator;
//            IS.evaluator.print2();
//
//            WeightPoF wp = new WeightPoF();
//            wp.factor = factor;
//            wp.maxIteration = maxLoop;
//            wp.addData(ds);
//            wp.run();
//            wp.numNeighborsInContext = numNeighborsInContext;
//            Estimation.EvaluateErrorsWithContext(wp);
//            result[3][iFold] = wp.evaluator;
//
//            FilterPoF fp = new FilterPoF();
//            fp.addData(ds);
//            fp.factor = factor;
//            fp.maxIteration = maxLoop;
//            fp.svd = wp.svd;
//            fp.numNeighborsInContext = numNeighborsInContext;
//            fp.threshold = (float) 0.1;
//            Estimation.EvaluateErrorsWithContext(fp);
//            fp.evaluator.training_Time = wp.evaluator.training_Time;
//            result[4][iFold] = fp.evaluator;

//            MFCACF mf = new MFCACF();
//            mf.addData(ds);
//            mf.factor = factor;
//            mf.maxIteration = maxLoop;
//            mf.run();
//            Estimation.EvaluateErrorsWithContext(mf);
//            result[5][iFold] = mf.evaluator;
//            mf.evaluator.print2();
////            mf.evaluator.print();
//            ItemClustering ic = new ItemClustering();
//            ic.addData(ds);
//            ic.fullDataset = ds.data;
//            ic.contextProcessor.generateContextCombinationFromHierachy2();
//            ic.run();
//            Estimation.EvaluateErrorsWithContext(ic);
//            result[5][iFold] = ic.evaluator;


        }
        outputResult(2);
//        outputResult(5);
//        outputResult2(2);
//        outputResult(3);
//        outputResult(4);
//        outputResult2(5);

    }

    public void runModelAlgorithmWithContextsRemoved(int factor, int maxLoop, int numNeighborsInContext) {
        DataManager dm = new DataManager();
        dm.dataProcessor(path, 3);
        System.out.println(new Date() + " - Read Data Complete");
//        dm.getFullDataset();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            System.out.println(new Date() + " - Fold" + iFold);
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();


//            MFCF svd = new MFCF(factor);
//            svd.addData(ds);
//            svd.maxIteration = maxLoop;
//            svd.run();
//            Estimation.EvaluateErrors(svd);
//            result[0][iFold] = svd.evaluator;
////
//            Prefilter pr = new Prefilter();
//            pr.addData(ds);
//            pr.factor = factor;
//            pr.maxIteration = maxLoop;
//            pr.run();
//            Estimation.EvaluateErrorsWithContext(pr);
//            result[1][iFold] = pr.evaluator;

//            ItemSplitting IS = new ItemSplitting();
//            IS.factor = factor;
//            IS.maxIteration = maxLoop;
//            //true when peform ADOM
//            IS.run(ds.data, ds.CP, true);
//            result[2][iFold] = IS.evaluator;
//            IS.evaluator.print2();
//
//            WeightPoF wp = new WeightPoF();
//            wp.factor = factor;
//            wp.maxIteration = maxLoop;
//            wp.addData(ds);
//            wp.run();
//            wp.numNeighborsInContext = numNeighborsInContext;
//            Estimation.EvaluateErrorsWithContext(wp);
//            result[3][iFold] = wp.evaluator;
//
//            FilterPoF fp = new FilterPoF();
//            fp.addData(ds);
//            fp.factor = factor;
//            fp.maxIteration = maxLoop;
//            fp.svd = wp.svd;
//            fp.numNeighborsInContext = numNeighborsInContext;
//            fp.threshold = (float) 0.1;
//            Estimation.EvaluateErrorsWithContext(fp);
//            fp.evaluator.training_Time = wp.evaluator.training_Time;
//            result[4][iFold] = fp.evaluator;

            MFCACF mf = new MFCACF();
            mf.addData(ds);
            mf.factor = factor;
            mf.maxIteration = maxLoop;
            mf.dataset = mf.contextProcessor.runADOM(mf.dataset);
            mf.run();
            Estimation.EvaluateErrorsWithContext(mf);
            result[5][iFold] = mf.evaluator;
//            mf.evaluator.print2();
////            mf.evaluator.print();
//            ItemClustering ic = new ItemClustering();
//            ic.addData(ds);
//            ic.fullDataset = ds.data;
//            ic.contextProcessor.generateContextCombinationFromHierachy2();
//            ic.run();
//            Estimation.EvaluateErrorsWithContext(ic);
//            result[5][iFold] = ic.evaluator;


        }
//        outputResult(2);
        outputResult(5);
//        outputResult2(2);
//        outputResult(3);
//        outputResult(4);
//        outputResult2(5);

    }
    
    private void runFixedSplittedData(int factor, int iterator) {
        DataManager dm = new DataManager();
        System.out.println(new Date() + " - Start");

        DataSwitcher ds = new DataSwitcher();
        result = new Evaluator[10][m];

        ds.data = dm.getDataFixedSplitter(path, "D:\\algebra_train.txt", "D:\\algebra_test.txt");;
        ds.data.reduceContextDimension();
        ds.CP = dm.getContextProcessor();
        ds.data.findRatedItemsByUser();
        ds.CP.data = ds.data;
        ds.CP.generateContextCombinationFromHierachy();

        System.out.println(new Date() + " - Read Data Complete");

//        ItemSplitting IS = new ItemSplitting();
//        IS.factor = factor;
//        IS.maxIteration = iterator;
//        IS.run(ds.data, ds.CP, false);
//        IS.evaluator.print2();
//
//        MFCACF mf = new MFCACF();
//        mf.addData(ds);
//        mf.factor = factor;
//        mf.maxIteration = iterator;
//        mf.run();
//        Estimation.EvaluateErrorsWithContext(mf);
//        mf.evaluator.print2();

        STI ic = new STI();
        ic.itemContextCombination = dm.itemContextCombination;
        ic.tempDataset = ds.data;
        ds.CP.generateContextCombinationFromHierachy2();
        ic.addData(ds);
        ic._minRating = 0;
        ic._maxRating = 1;
        ic.factor = factor;
        ic.iterator = iterator;
        ic.run();

//        STIGmeans sti_gmeans = new STIGmeans();
//        sti_gmeans.tempDataset = ds.data;
//        sti_gmeans.addData(ds);
//        sti_gmeans.run();      


    }
}
