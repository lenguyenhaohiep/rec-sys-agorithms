/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.alg.ItemSplitting;
import cars.alg.model.FilterPoF;
import cars.alg.model.MFCACF;
import cars.alg.model.MFCF;
import cars.alg.model.Prefilter;
import cars.alg.model.WeightPoF;
import cars.data.DataSwitcher;

/**
 *
 * @author Hiep
 */
public class TestModelBased {

    public static void main(String[] arg) {
        DataSwitcher ds = new DataSwitcher();
        ds.DataChoosing(3);
        ds.data.reduceContextDimension();

//        MFCF svd = new MFCF(20);
//        svd.addData(ds);
//        svd.maxIteration = 10000;
//        svd.run();
//        svd.runEvaluation();
//
//
//        Prefilter pr = new Prefilter();
//        pr.addData(ds);
//        pr.factor = 20;
//        pr.maxIteration = 1000;
//        pr.run();
//        pr.runEvaluation();
//
//        Prefilter pr2 = new Prefilter();
//        pr2.addData(ds);
//        pr2.contextProcessor.runGFSG(pr2.dataset, 3, (float) 0.95);
//        pr2.contextProcessor.generateContextCombinationFromHierachyGFSG();
//        pr2.dataset = pr2.contextProcessor.convertToNewDataset(ds.data);
//        pr2.dataset.findRatedItemsByUser();
//        pr2.factor = 5;
//        pr2.factor = 1000;
//        pr2.run();
//        pr2.runEvaluation();
//
//        WeightPoF w = new WeightPoF();
//        w.addData(ds);
//        w.factor = 5;
//        w.numNeighbor = 50;
//        w.numNeighborsInContext = 50;
//        w.run();
//        w.runEvaluation();
////
//        FilterPoF f = new FilterPoF();
//        f.addData(ds);
//        f.factor = 5;
//        f.numNeighbor = 50;
//        f.numNeighborsInContext = 50;
//        f.svd = w.svd;
//        //f.run();
//        f.runEvaluation();
////////

        ItemSplitting is = new ItemSplitting();
        is.addData(ds);
        is.factor = 20;
        is.maxIteration = 10000;
        is.run(ds.data, ds.CP, false);
        is.runEvaluation();
//        MFCACF mf = new MFCACF();
//        mf.addData(ds);
//        mf.factor = 20;
//        mf.maxIteration = 10000;
//        mf.run();
//        mf.runEvaluation();

//        MFCACF mf2 = new MFCACF();
//        mf2.addData(ds);
//        mf2.contextProcessor.runGFSG(mf2.dataset, 3, (float) 0.95);
//        mf2.contextProcessor.generateContextCombinationFromHierachyGFSG();
//        mf2.dataset = mf2.contextProcessor.convertToNewDataset(ds.data);
//        mf2.contextProcessor.updateHeirachyAfterConverting();
//        mf2.factor = 20;
//        mf2.run();
//        mf2.runEvaluation();
    }
}
