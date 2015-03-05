/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.alg.ContextProcessor;
import cars.alg.memory.ContextualNeighbors;
import cars.alg.memory.FilterPoFUserBased;
import cars.alg.memory.UserBasedTopK;
import cars.alg.memory.PrefilterUserBased;
import cars.alg.memory.WeightPoFUserBased;
import cars.alg.model.MFCF;
import cars.alg.model.Prefilter;
import cars.data.DataSwitcher;
import cars.data.HGift;

/**
 *
 * @author Hiep
 */
public class TestMemoryBased {

    public static void main(String[] arg) {
        DataSwitcher ds = new DataSwitcher();
        ds.DataChoosing(7);
        ds.data.reduceContextDimension();
//        MFCF svd = new MFCF(5);
//        svd.addData(ds);
//        svd.run();
//        svd.runEvaluation();
//        //svd.evaluator.print();
//        Prefilter pr = new Prefilter();
//        pr.addData(ds);
//        pr.factor = 5;
//        pr.run();
//        pr.runEvaluation();
//        pr.evaluator.print();

//
        for (int i = 10; i <= 120; i = i + 10) {
//            System.out.println(i);
//            UserBasedTopK UB = new UserBasedTopK(i);
//            UB.addData(ds);
//            UB.runEvaluation();
            WeightPoFUserBased WPOFUB = new WeightPoFUserBased(i);
            WPOFUB.numNeighborsInContext = i;
            WPOFUB.addData(ds);
            WPOFUB.runEvaluation();
        }
//        FUB.evaluator.print();


////
//        FilterPoFUserBased FPOFUB = new FilterPoFUserBased(100);
////        ds.data.reduceContextDimension();
//        FPOFUB.addData(ds);
//        FPOFUB.numNeighborsInContext = 100;
//
//        FPOFUB.runEvaluation();
//        FPOFUB.evaluator.print();

//        for (int numNeighbors = 80; numNeighbors <= 80; numNeighbors = numNeighbors + 10) {
//            System.out.println("N=" + numNeighbors);
//
//
//            UserBasedTopK UB = new UserBasedTopK(numNeighbors);
//            UB.addData(ds);
//            UB.runEvaluation();
//
//            PrefilterUserBased FUB = new PrefilterUserBased(numNeighbors);
//            FUB.addData(ds);
//            FUB.extractDataByContext();
//            FUB.runEvaluation();
//
//            WeightPoFUserBased WPOFUB = new WeightPoFUserBased(numNeighbors);
//            WPOFUB.addData(ds);
//            WPOFUB.runEvaluation();
//
//            FilterPoFUserBased FPOFUB = new FilterPoFUserBased(numNeighbors);
//            FPOFUB.addData(ds);
//            FPOFUB.runEvaluation();
//
//        ContextualNeighbors CN = new ContextualNeighbors(100);
//        CN.addData(ds);
//        CN.runEvaluation();
//        }

    }
}
