/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.evaluation.Estimation;
import cars.alg.ContextProcessor;
import cars.alg.model.FilterPoF;
import cars.alg.model.MFCACF;
import cars.alg.model.Prefilter;
import cars.alg.model.MFCF;
import cars.alg.model.WeightPoF;
import cars.data.HGift;
import cars.data.HMusic;

/**
 *
 * @author Hiep
 */
public class Test1 {

    public static void main(String[] arg) {

        for (int i = 0; i < 10; i++) {
//            AISTFood Dset = new AISTFood();
//            try {
//                Dset.read(i);
//            } catch (Exception ex) {
//            }
//            Dset.reduceContextDimension();
//            HGift Dset = new HGift();
//            try {
//                Dset.processData();
//            } catch (Exception ex) {
//            }
//
            HMusic Dset = new HMusic();
            try {
                Dset.processData();
            } catch (Exception ex) {
            }

            Dset.reduceContextDimension();
//            MFCF svd = new MFCF(5);
//            svd.numItem = Dset.numItem;
//            svd.numRating = Dset.numRating;
//            svd.numUser = Dset.numUser;
//            svd.mRating = Dset.mRating;
//            svd.run();
//            Estimation.EvaluateErrors(svd, Dset.testingSet);
//
//            ContextProcessor c = new ContextProcessor();
//            c.hierachy = new int[][][]{
//                {{0}, {1}, {2}}};
//            c.generateContextCombinationFromHierachy();
            ContextProcessor c2 = new ContextProcessor();
            c2.initializeContextHierachy(new int[][][]{
                        {{0}, {1}, {2}, {3}, {4}, {5}, {6}}});
            c2.generateContextCombinationFromHierachy();
//
//
            ContextProcessor c = new ContextProcessor();
//            c.hierachy = new int[][][]{
//                {{0}, {1}, {2}, {3}, {4}, {5}},
//                {{0}, {1}},
//                {{0}, {1}, {2}, {3}},
//                {{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}}
//            };
//            c.hierachy = new int[][][]{
//                {{0}, {1, 4}, {2, 6}, {3, 5}}};
            //c.identifyHeirachy();
//            c.generateContextCombinationFromHierachy();
            c.numAttrAtContext = new int[]{7};
            c.initializeContextHierachy(new int[][][]{
                        {{0}, {1}, {2}, {3}, {4}, {5}, {6}}});
            c.runGFSG(Dset, 3, (float)0.95);
            c.generateContextCombinationFromHierachy();

//            Prefilter pr = new Prefilter();
//            pr.name = "old";
//            pr.contextProcessor = c2;
//            pr.numItem = Dset.numItem;
//            pr.numUser = Dset.numUser;
//            pr.factor = 5;
//            pr.mRatingWithContext = Dset.mRatingWithContext;
//            pr.run();
//            Estimation.EvaluateErrorsWithContext(pr, Dset.testingSet);
//            Prefilter pr2 = new Prefilter();
//            pr2.name = "New";
//            pr2.contextProcessor = c;
//            pr2.numItem = Dset.numItem;
//            pr2.numUser = Dset.numUser;
//            pr2.factor = 5;
//            pr2.mRatingWithContext = Dset.mRatingWithContext;
//            pr2.run();
//            Estimation.EvaluateErrorsWithContext(pr2, Dset.testingSet);

//            WeightPoF w = new WeightPoF();
//            w.contextProcessor = c;
//            w.numItem = Dset.numItem;
//            w.numUser = Dset.numUser;
//            w.factor = 5;
//            w.numNeighbor = 5;
//            w.mRatingWithContext = Dset.mRatingWithContext;
//            w.mRating = Dset.mRating;
//            w.run();
//            Estimation.EvaluateErrorsWithContext(w, Dset.testingSet);
//
//            FilterPoF f = new FilterPoF();
//            f.contextProcessor = c;
//            f.numItem = Dset.numItem;
//            f.numUser = Dset.numUser;
//            f.factor = 5;
//            f.numNeighbor = 5;
//            f.mRatingWithContext = Dset.mRatingWithContext;
//            f.mRating = Dset.mRating;
//            f.run();
//            Estimation.EvaluateErrorsWithContext(f, Dset.testingSet);
            MFCACF mf = new MFCACF();
            mf.contextProcessor = c2;
            mf.name = "MF Old";
            mf.factor = 5;
            mf.dataset = Dset;
            mf.run();
            Estimation.EvaluateErrorsWithContext(mf);
            mf.contextProcessor = c;
            mf.run();
            mf.name = "MF new";
            Estimation.EvaluateErrorsWithContext(mf);

        }
    }
}
