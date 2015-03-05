/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.validation;

import cars.test.*;
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
public class testingN {

    public int m = 5;
    public Evaluator[][] result;
    public String path = "";

    public void outputResult(int algIndex) {
        Evaluator averageResult = new Evaluator(Algorithm.N);
        for (int i = 0; i < m; i++) {
            averageResult.MAE += result[algIndex][i].MAE;
            averageResult.RMSE += result[algIndex][i].RMSE;
            averageResult.training_Time += result[algIndex][i].training_Time;
            averageResult.testing_Time += result[algIndex][i].testing_Time;
        }
        averageResult.average(m);
        averageResult.output();
    }

    public static void main(String[] arg) {
        testingN t = new testingN();
        //t.runKNN();
        t.runMatrixFactorization();
        //t.runTuningKNN();
        //t.runTuningMatrixFactorization();
    }

    public void runKNN() {
        DataManager dm = new DataManager();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.readMovilenData(iFold + 1);
            int numNeighbors = 500;
            UserBasedTopK UB = new UserBasedTopK(numNeighbors);
            UB.addData(ds);
            Estimation.EvaluateErrors(UB);
            UB.evaluator.print();
            result[0][iFold] = UB.evaluator;
        }
        outputResult(0);
    }

//Chạy kiểm thử thuật toán
    public void runMatrixFactorization() {
        DataManager dm = new DataManager();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        for (int iFold = 0; iFold < m; iFold++) {
            ds.data = dm.readMovilenData(iFold + 1);
            int factor = 5;
            int maxLoop = 100;
            MFCF svd = new MFCF(factor);
            svd.addData(ds);
            svd.maxIteration = maxLoop;
            svd.run();
            Estimation.EvaluateErrors(svd);
            svd.evaluator.print();
            result[0][iFold] = svd.evaluator;
        }
        outputResult(0);
    }

    public void runTuningKNN() {
        DataManager dm = new DataManager();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        //int[] neighs = {100, 200, 300, 400, 500};
        //int[] neighs = {50, 150, 250, 350, 450};
        int[] neighs = {550,600,650,700,750};
        for (int i = 0; i < neighs.length; i++) {
            ds.data = dm.readMovilenData(6);
            int numNeighbors = neighs[i];
            UserBasedTopK UB = new UserBasedTopK(numNeighbors);
            UB.addData(ds);
            Estimation.EvaluateErrors(UB);
            UB.evaluator.print();
            result[0][i] = UB.evaluator;
        }
        outputResult(0);
    }

    public void runTuningMatrixFactorization() {
        DataManager dm = new DataManager();
        result = new Evaluator[10][m];
        DataSwitcher ds = new DataSwitcher();
        //int[] pa = {5, 10, 20, 30, 40};
        int[] pa = {50, 60, 70, 80, 100};
        for (int i = 0; i < pa.length; i++) {
            ds.data = dm.readMovilenData(6);
            int factor = pa[i];
            int maxLoop = 100;
            MFCF svd = new MFCF(factor);
            svd.addData(ds);
            svd.maxIteration = maxLoop;
            svd.run();
            Estimation.EvaluateErrors(svd);
            svd.evaluator.print();
            result[0][i] = svd.evaluator;
        }
        outputResult(0);
    }
}