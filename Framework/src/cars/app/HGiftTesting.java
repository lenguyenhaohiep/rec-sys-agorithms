/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.alg.ItemSplitting;
import cars.alg.memory.UserBasedTopK;
import cars.alg.model.MFCACF;
import cars.alg.model.MFCF;
import cars.data.DataSwitcher;
import cars.data.HGift;
import cars.data.HMusic;
import cars.data.structure.Dataset;
import cars.evaluation.Estimation;
import cars.evaluation.Evaluator;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hiep
 */
public class HGiftTesting extends MFoldCrossValidation {

    @Override
    public void outputResult(int algIndex) {
        Evaluator averageResult = new Evaluator(numItem);
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

    public static void main(String args[]) {
        HGift data = new HGift();
        try {
            data.processData();
        } catch (Exception ex) {
        }
        HGiftTesting hTest = new HGiftTesting();
        hTest.m = 5;
        hTest.mFoldData = data.mFold;
        hTest.numItem = data.numItem;
        hTest.numUser = data.numUser;
        hTest.run();
    }

    public void run() {
        result = new Evaluator[10][m];
        for (int iFold = 0; iFold < m; iFold++) {
            DataSwitcher ds = new DataSwitcher();
            ds.DataChoosing(3);
            ds.data.numUser = numUser;
            ds.data.numItem = numItem;
            Dataset data = ds.data;

            tranformToTestSet(data, iFold);
            transformToTrainSet(data, iFold);

            data.reduceContextDimension();
            ds.data = data;
            ds.CP.data = ds.data;
            ds.CP.generateContextCombinationFromHierachy();

//            MFCF mf = new MFCF(10);
//            mf.addData(ds);
//            mf.factor = 10;
//            mf.maxIteration = 1000;
//            mf.run();
//            Estimation.EvaluateErrors(mf);
//            result[2][iFold] = mf.evaluator;
//            
//            ItemSplitting is = new ItemSplitting();
//            is.addData(ds);
//            is.factor = 20;
//            is.maxIteration = 1000;
//            is.run(ds.data, ds.CP, false);
//            result[0][iFold] = is.evaluator;

//            MFCACF camf = new MFCACF();
//            camf.addData(ds);
//            camf.factor = 10;
//            camf.maxIteration = 1000;
//            camf.run();
//            Estimation.EvaluateErrorsWithContext(camf);
//            result[1][iFold] = camf.evaluator;
//
//            MFCACF mf2 = new MFCACF();
//            mf2.addData(ds);
//            mf2.dataset = mf2.contextProcessor.runGSG(mf2.dataset);
////            mf2.contextProcessor.runGFSG(mf2.dataset, 3, (float) 0.95);
////            mf2.contextProcessor.generateContextCombinationFromHierachyGFSG();
////            mf2.dataset = mf2.contextProcessor.convertToNewDataset(mf2.dataset);
////            mf2.contextProcessor.updateHeirachyAfterConverting();
//            mf2.factor = 10;
//            mf2.maxIteration = 1000;
//            mf2.run();
//            Estimation.EvaluateErrorsWithContext(mf2);
//            result[2][iFold] = mf2.evaluator;
            
            ItemSplitting is = new ItemSplitting();
            is.addData(ds);
            is.factor = 10;
            is.maxIteration = 1000;
            is.run(ds.data, ds.CP, false);
            Estimation.EvaluateErrorsWithContext(is);
            result[1][iFold] = is.evaluator;
//            is.evaluator.print();

            ItemSplitting is2 = new ItemSplitting();
            is2.addData(ds);
            is2.factor = 10;
            is2.maxIteration = 1000;
            is2.dataset = is2.contextProcessor.runGSG(is2.dataset);
            is2.run(is2.dataset, is2.contextProcessor, false);
            result[2][iFold] = is2.evaluator;
//            is2.evaluator.print();

        }
//        outputResult(0);
        outputResult(1);
        outputResult(2);
    }
}
