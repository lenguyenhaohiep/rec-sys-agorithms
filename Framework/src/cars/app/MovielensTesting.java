/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

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
import cars.data.DataSwitcher;
import cars.data.structure.DataInput;
import cars.data.structure.Dataset;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Hiep
 */
public class MovielensTesting extends MFoldCrossValidation {

    @Override
    public void outputResult(int algIndex) {
        Evaluator averageResult = new Evaluator(1682);
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

    public void readMovieData(int k) {
        List<DataInput> entireData = new ArrayList<DataInput>();
        users = new ArrayList<Integer>();
        try {
            Scanner scanner = new Scanner(new File("D:\\MVSyncthetic\\movielensSemiS.fold" + k));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (!line.contains("::")) {
                    String[] elements = line.split("\t");
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
        mFoldData.add(entireData);

    }

    public static void main(String[] arg) {
        MovielensTesting m = new MovielensTesting();
        m.m = 5;
        m.numUser = 943;
        m.numItem = 1682;
        for (int i = 0; i <= 4; i++) {
            m.readMovieData(i);
        }
        m.m = 5;
//        m.runAlgorithm(0);
        m.runAlgorithm2(0);

    }

    @Override
    public void runAlgorithm(int dataIndex) {

        result = new Evaluator[10][m];
        int numNeighbors = 650;

        for (int iFold = 0; iFold < m; iFold++) {
            DataSwitcher ds = new DataSwitcher();
            ds.DataChoosing(4);
            numUser = ds.data.numUser;
            numItem = ds.data.numItem;
            Dataset data = ds.data;

            tranformToTestSet(data, iFold);
            transformToTrainSet(data, iFold);

            data.reduceContextDimension();
            ds.data = data;
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

            UserBasedTopK UB = new UserBasedTopK(numNeighbors);
            UB.addData(ds);
            Estimation.EvaluateErrors(UB);
            result[0][iFold] = UB.evaluator;
//            UB.evaluator.print();
////
            PrefilterUserBased FUB = new PrefilterUserBased(numNeighbors);
            FUB.addData(ds);
            FUB.extractDataByContext();
            Estimation.EvaluateErrorsWithContext(FUB);
//            FUB.evaluator.print();
            result[1][iFold] = FUB.evaluator;
////
            ItemSplitting IS = new ItemSplitting();
            IS.runUB(ds.data, ds.CP, numNeighbors);
//            IS.evaluator.print();
            result[2][iFold] = IS.evaluator;
//
//
////////                System.out.println(num);
//            WeightPoFUserBased WPOFUB = new WeightPoFUserBased(650);
//            WPOFUB.addData(ds);
//            WPOFUB.numNeighborsInContext = 700;
//            Estimation.EvaluateErrorsWithContext(WPOFUB);
//            result[3][iFold] = WPOFUB.evaluator;
//            WPOFUB.evaluator.print();
//
//////
//            FilterPoFUserBased FPOFUB = new FilterPoFUserBased(650);
//            FPOFUB.addData(ds);
//            FPOFUB.numNeighborsInContext = 700;
//            FPOFUB.threshold = (float) 0.05;
//            Estimation.EvaluateErrorsWithContext(FPOFUB);
//            result[4][iFold] = FPOFUB.evaluator;
////            }
//            }
////            for (int num3 = 900; num3 <= 10000; num3 = num3 + 100) {
//
//                System.out.println(1000);
//            ContextualNeighbors CN = new ContextualNeighbors(900);
//            CN.addData(ds);
//            CN.evaluator = new Evaluator(CN.N);
//            CN.evaluator.empty();
//            Estimation.EvaluateErrorsWithContext(CN);
//                CN.evaluator.print();
//            result[0][iFold] = CN.evaluator;
//            }
        }
        outputResult(0);
        outputResult(1);
        outputResult(2);
//        outputResult(3);
//        outputResult(4);
//        outputResult(5);

    }

    //Chạy kiểm thử thuật toán
    @Override
    public void runAlgorithm2(int dataIndex) {

        result = new Evaluator[10][m];
        for (int iFold = 0; iFold < m; iFold++) {
            DataSwitcher ds = new DataSwitcher();
            ds.DataChoosing(4);
            numUser = ds.data.numUser;
            numItem = ds.data.numItem;
            Dataset data = ds.data;
            tranformToTestSet(data, iFold);
            transformToTrainSet(data, iFold);


            data.reduceContextDimension();
            ds.data = data;
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();
//            for (int factor = 10; factor <= 100; factor = factor + 10) {
//            MFCF svd = new MFCF(5);
//            svd.maxIteration = 1000;
//            svd.addData(ds);
//            svd.run();
//            Estimation.EvaluateErrors(svd);
////            svd.evaluator.print();
//            result[0][iFold] = svd.evaluator;
////            }
//
//            Prefilter pr = new Prefilter();
//            pr.addData(ds);
//            pr.factor = 5;
//            pr.maxIteration = 1000;
//            pr.run();
//            Estimation.EvaluateErrorsWithContext(pr);
////            pr.evaluator.print();
//            result[1][iFold] = pr.evaluator;
////
            ItemSplitting IS = new ItemSplitting();
            IS.factor = 5;
            IS.maxIteration = 1000;
            IS.run(ds.data, ds.CP, false);
            result[2][iFold] = IS.evaluator;
//            IS.evaluator.print();

//            WeightPoF wp = new WeightPoF();
//            wp.factor = 5;
//            wp.maxIteration = 1000;
//            wp.addData(ds);
//            wp.run();
//            wp.numNeighborsInContext = 700;
//            Estimation.EvaluateErrorsWithContext(wp);
//            result[0][iFold] = wp.evaluator;
//////            wp.evaluator.print();
//
//            FilterPoF fp = new FilterPoF();
//            fp.addData(ds);
//            fp.factor = 5;
//            fp.maxIteration = 1000;
//            fp.numNeighborsInContext = 700;
//            fp.threshold = (float) 0.1;
//            fp.svd = wp.svd;
//            Estimation.EvaluateErrorsWithContext(fp);
////            
//            fp.evaluator.training_Time = wp.evaluator.training_Time;
//            result[1][iFold] = fp.evaluator;

//            fp.evaluator.print();
//            
//            MFCACF mf = new MFCACF();
//            mf.addData(ds);
//            mf.factor = 5;
//            mf.maxIteration = 1000;
//            mf.run();
//            Estimation.EvaluateErrorsWithContext(mf);
////            mf.evaluator.print();
//
//            result[5][iFold] = mf.evaluator;


        }
//        outputResult(0);
//        outputResult(1);
        outputResult(2);
//        outputResult(5);

    }
}
