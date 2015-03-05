/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.alg.Algorithm;
import cars.alg.ItemSplitting;
import cars.alg.memory.ContextualNeighbors;
import cars.alg.memory.PrefilterUserBased;
import cars.alg.memory.UserBasedTopK;
import cars.alg.model.FilterPoF;
import cars.alg.model.MFCACF;
import cars.alg.model.Prefilter;
import cars.alg.model.WeightPoF;
import cars.data.DataManager;
import cars.data.DataSwitcher;
import cars.data.structure.DataInput;
import cars.evaluation.Estimation;
import cars.evaluation.Evaluator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Hiep
 */
public class ComodaTesting extends MFoldCrossValidation {

    @Override
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

    public void readData(int k) {
        List<DataInput> _entireData = new ArrayList<DataInput>();
        users = new ArrayList<Integer>();
        try {
            Scanner scanner = new Scanner(new File("D:\\data\\comoda.fold" + k));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (!line.contains("::")) {
                    String[] elements = line.split("\t");
                    int user, item;
                    user = Integer.parseInt(elements[0]);
                    item = Integer.parseInt(elements[1]);
                    String context = "";
                    for (int i = 2; i <= 13; i++) {
                        context += elements[i] + "::";
                    }
                    DataInput di = new DataInput();
                    di.context = context;
                    di.itemID = item;
                    di.userID = user;
                    di.score = Float.parseFloat(elements[14]);
                    _entireData.add(di);
                }

            }
        } catch (Exception ex) {
        }
        mFoldData.add(_entireData);

    }

    public static void main(String[] arg) {
        Algorithm.N = 10;
        ComodaTesting com = new ComodaTesting();
        com.m = 5;
        
        com.runItemSplitting();
        com.runCAMF();
//        com.runPreFilter();
//        com.runPostFilter();
//        com.runPostWeigh();
//        com.runMemory();
    }

    public void runCAMF() {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\hmusic.dat", 3);
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

//            MFCACF mf = new MFCACF();
//            mf.addData(ds);
//            mf.factor = 10;
//            mf.maxIteration = 1000;
//            mf.run();
//            Estimation.EvaluateErrorsWithContext(mf);

//            MFCACF mf2 = new MFCACF();
//            mf2.addData(ds);
//            mf2.dataset = mf2.contextProcessor.runGSG(mf2.dataset);
//            mf2.factor = 10;
//            mf2.maxIteration = 1000;
//            mf2.run();
//            Estimation.EvaluateErrorsWithContext(mf2);

            MFCACF mf3 = new MFCACF();
            mf3.addData(ds);
            mf3.dataset = mf3.contextProcessor.runADOM(mf3.dataset);
            mf3.factor = 5;
            mf3.maxIteration = 1000;
            mf3.run();
            Estimation.EvaluateErrorsWithContext(mf3);


//            result[2][iFold] = mf.evaluator;
//            result[3][iFold] = mf2.evaluator;
            result[5][iFold] = mf3.evaluator;


        }
//        outputResult(2);
//        outputResult(3);
        outputResult(5);

    }

    public void runPreFilter() {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\hmusic.set5", 3);
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

            Prefilter pr = new Prefilter();
            pr.addData(ds);
            pr.factor = 10;
            pr.maxIteration = 1000;
            pr.run();
            Estimation.EvaluateErrorsWithContext(pr);
            result[2][iFold] = pr.evaluator;

            Prefilter pr2 = new Prefilter();
            pr2.addData(ds);
            pr2.dataset = pr2.contextProcessor.runGSG(pr2.dataset);
            pr2.factor = 10;
            pr2.maxIteration = 1000;
            pr2.run();
            Estimation.EvaluateErrorsWithContext(pr2);
            result[3][iFold] = pr2.evaluator;

            Prefilter pr3 = new Prefilter();
            pr3.addData(ds);
            pr3.dataset = pr3.contextProcessor.runADOM(pr3.dataset);
            pr3.factor = 10;
            pr3.maxIteration = 1000;
            pr3.run();
            Estimation.EvaluateErrorsWithContext(pr3);
            result[5][iFold] = pr3.evaluator;

        }
        outputResult(2);
        outputResult(3);
        outputResult(5);

    }

    public void runItemSplitting() {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\hmusic.dat", 3);
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();

        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

//            ItemSplitting IS = new ItemSplitting();
//            IS.factor = 10;
//            IS.maxIteration = 1;
//            IS.run(ds.data, ds.CP, false);
//            result[2][iFold] = IS.evaluator;


//            ItemSplitting IS2 = new ItemSplitting();
//            IS2.factor = 10;
//            IS2.maxIteration = 1;
//            IS2.addData(ds);
//            IS2.dataset = IS2.contextProcessor.runGSG(IS2.dataset);
//            IS2.run(IS2.dataset, IS2.contextProcessor, false);
//            result[3][iFold] = IS2.evaluator;

            ItemSplitting mf3 = new ItemSplitting();
            mf3.addData(ds);
            mf3.dataset = mf3.contextProcessor.runADOM(mf3.dataset);
            mf3.factor = 5;
            mf3.maxIteration = 1000;
            mf3.run(mf3.dataset, mf3.contextProcessor, false);
            result[5][iFold] = mf3.evaluator;

        }
//        outputResult(2);
//        outputResult(3);
        outputResult(5);

    }

    private void runPostFilter() {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\hgift.dataset1", 3);
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();

        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

            int numNei = 50;

            FilterPoF fp1 = new FilterPoF();
            fp1.factor = 10;
            fp1.maxIteration = 1000;
            fp1.numNeighborsInContext = numNei;
            fp1.addData(ds);
            fp1.run();
            Estimation.EvaluateErrorsWithContext(fp1);
            result[2][iFold] = fp1.evaluator;

            FilterPoF fp2 = new FilterPoF();
            fp2.factor = 10;
            fp2.maxIteration = 1000;
            fp2.addData(ds);
            fp2.dataset = fp2.contextProcessor.runGSG(fp2.dataset);
            fp2.dataset.reduceContextDimension();
            fp2.numNeighborsInContext = numNei;
            fp2.run();
            Estimation.EvaluateErrorsWithContext(fp2);
            result[3][iFold] = fp2.evaluator;

            FilterPoF fp3 = new FilterPoF();
            fp3.factor = 10;
            fp3.maxIteration = 1000;
            fp3.numNeighborsInContext = numNei;
            fp3.addData(ds);
            fp3.dataset = fp2.contextProcessor.runADOM(fp3.dataset);
            fp3.dataset.reduceContextDimension();
            fp3.run();
            Estimation.EvaluateErrorsWithContext(fp3);
            result[5][iFold] = fp3.evaluator;

        }
        outputResult(2);
        outputResult(3);
        outputResult(5);
    }

    private void runPostWeigh() {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\comoda.dat", 3);
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();

        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

            int numNei = 50;

            WeightPoF fp1 = new WeightPoF();
            fp1.factor = 10;
            fp1.maxIteration = 1000;
            fp1.numNeighborsInContext = numNei;
            fp1.addData(ds);
            fp1.run();
            Estimation.EvaluateErrorsWithContext(fp1);
            result[2][iFold] = fp1.evaluator;

            WeightPoF fp2 = new WeightPoF();
            fp2.factor = 10;
            fp2.maxIteration = 1000;
            fp2.addData(ds);
            fp2.dataset = fp2.contextProcessor.runGSG(fp2.dataset);
            fp2.dataset.reduceContextDimension();
            fp2.numNeighborsInContext = numNei;
            fp2.run();
            Estimation.EvaluateErrorsWithContext(fp2);
            result[3][iFold] = fp2.evaluator;

            WeightPoF fp3 = new WeightPoF();
            fp3.factor = 10;
            fp3.maxIteration = 1000;
            fp3.numNeighborsInContext = numNei;
            fp3.addData(ds);
            fp3.dataset = fp2.contextProcessor.runADOM(fp3.dataset);
            fp3.dataset.reduceContextDimension();
            fp3.run();
            Estimation.EvaluateErrorsWithContext(fp3);
            result[5][iFold] = fp3.evaluator;

        }
        outputResult(2);
        outputResult(3);
        outputResult(5);
    }

    public void runMemory() {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\comoda.dat", 3);
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.getDataset(iFold);
            ds.data.reduceContextDimension();
            ds.CP = dm.getContextProcessor();
            ds.data.maxSelectedNumberItemsToTest = 5;
            ds.data.findRatedItemsByUser();
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

            int nei = 5000;
//            UserBasedTopK ub = new UserBasedTopK();
//            ub.addData(ds);
//            ub.numNeighbor = nei;
//            Estimation.EvaluateErrors(ub);


//            ContextualNeighbors cn = new ContextualNeighbors();
//            cn.addData(ds);
//            cn.numNeighbor = nei;
//            Estimation.EvaluateErrorsWithContext(cn);
//            result[2][iFold] = ub.evaluator;
//            result[3][iFold] = cn.evaluator;

            PrefilterUserBased fub = new PrefilterUserBased();
            fub.addData(ds);
            fub.extractDataByContext();
            fub.numNeighbor = nei;
            Estimation.EvaluateErrorsWithContext(fub);
            result[2][iFold] = fub.evaluator;
        }
        outputResult(2);
//        outputResult(3);

    }
}
