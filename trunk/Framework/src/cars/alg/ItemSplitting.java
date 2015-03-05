/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg;

import cars.alg.memory.UserBasedTopK;
import cars.alg.model.MFCF;
import cars.evaluation.Estimation;
import cars.data.DataSwitcher;
import cars.data.structure.NewItem;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class ItemSplitting extends Algorithm {

    public int factor;
    public int maxIteration;
    public Dataset transformedDS;
    private Algorithm traditionalRS;
    public MFCF svd = new MFCF(factor);
    private int flagUB;
    public Map<String, NewItem> lstNewItem = new HashMap<String, NewItem>();

    //Tách giá trị đánh giá 
    public List<Double> ratingsBelongToContext(int itemID, int contextID, int valueInHeirachy, boolean condition) {
        List<Double> r = new ArrayList<Double>();
        for (int u = 0; u < dataset.numUser; u++) {
            if (dataset.mRatingWithContext[u][itemID] != null) {
                float sum = 0;
                int count = 0;
                for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][itemID].contextScorePairs.entrySet()) {
                    int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(entry.getKey());
                    boolean check;
                    if (contextID == 1){
                        int a=0;
                    }
                    check = (arrayContext[contextID] == valueInHeirachy);
                    if (check == condition) {
                        sum += entry.getValue();
                        count++;
                    }
                }
                if (count != 0) {
                    r.add((double) sum / count);
                } else {
                    r.add(0.0);
                }
            } else {
                r.add(0.0);
            }
        }
        return r;
    }

    public void splitItem(int iItem) {
        //Duyệt từng ngữ cảnh
        double min = 100;
        int context_index = 0;
        int context_value = 0;
        int numContextDimension = contextProcessor.hierachy.size();
        for (int iContext = 0; iContext < numContextDimension; iContext++) {
            //Duyệt các giá trị mức 1
            int numContexyLevel1 = contextProcessor.hierachy.get(iContext).size();
            for (int iLv1 = 0; iLv1 < numContexyLevel1; iLv1++) {
                //chọn những đánh giá thuộc
                List<Double> sample1 = ratingsBelongToContext(iItem, iContext, iLv1, true);
                List<Double> sample2 = ratingsBelongToContext(iItem, iContext, iLv1, false);
                double valueTtest = contextProcessor.computePairTTest(sample1, sample2);
                if (valueTtest < min) {
                    min = valueTtest;
                    context_index = iContext;
                    context_value = iLv1;
                }
            }
        }
        //Thêm mới 2 sản phẩm vào danh sách những sản phẩm mới
        NewItem newItemGroup1 = new NewItem(iItem, context_index, context_value, 1);
        NewItem newItemGroup2 = new NewItem(iItem, context_index, context_value, 2);
        newItemGroup1.newID = lstNewItem.size();
        newItemGroup2.newID = lstNewItem.size() + 1;
        lstNewItem.put(iItem + "1", newItemGroup1);
        lstNewItem.put(iItem + "2", newItemGroup2);
    }

    //Chuyễn đỗi thành dữ liệu với sản phẩm mới
    public void trainingTestTransform() {
        transformedDS = new Dataset();
        for (int iItem = 0; iItem < dataset.numItem; iItem++) {
            splitItem(iItem);
        }
        float[][] mTransformedRating = new float[dataset.numUser][lstNewItem.size()];
        int numNewRating = 0;
        for (int u = 0; u < dataset.numUser; u++) {
            for (Map.Entry<String, NewItem> _entry : lstNewItem.entrySet()) {
                int i_old = _entry.getValue().itemID;
                boolean condition = (_entry.getValue().group == 1);
                if (dataset.mRatingWithContext[u][i_old] != null) {
                    float sum = 0;
                    int count = 0;
                    for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i_old].contextScorePairs.entrySet()) {
                        int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(entry.getKey());
                        boolean check = (arrayContext[_entry.getValue().contextID] == _entry.getValue().valContext);
                        if (check == condition) {
                            sum += entry.getValue();
                            count++;
                        }
                    }
                    if (count > 0) {
                        mTransformedRating[u][_entry.getValue().newID] = sum / count;
                        numNewRating++;
                    }
                }
            }
        }

        transformedDS.numRating = (numNewRating);
        transformedDS.numItem = (lstNewItem.size());
        transformedDS.mRating = (mTransformedRating);
        transformedDS.numUser = this.dataset.numUser;
        transformedDS.numItem = this.dataset.numItem * 2;
        transformedDS.testingSet = this.dataset.testingSet;
    }

    @Override
    public float prediction(int userid, int itemid, String context) {

        NewItem item1 = (lstNewItem.get(itemid + "1"));
        NewItem item2 = (lstNewItem.get(itemid + "2"));

        int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(context);
        boolean check = (arrayContext[item1.contextID] == item1.valContext);
        if (check) {
            return traditionalRS.prediction(userid, item1.newID);
        } else {
            return traditionalRS.prediction(userid, item2.newID);
        }
    }

    @Override
    public int[] recommend(int user, String context, List<Integer> ratedItemsByUser) {
        if (this.flagUB == 1) {
            traditionalRS.neighbors(user);
        }
        List<float[]> res = new ArrayList<float[]>();
        for (int i = 0; i < ratedItemsByUser.size(); i++) {
            int item = ratedItemsByUser.get(i);
//            if (dataset.mRatingWithContext[user][item] == null) {
            float[] r = new float[2];
            r[0] = item;
            r[1] = prediction(user, item, context);
            res.add(r);
//            } else {
//                if (!dataset.mRatingWithContext[user][item].contextScorePairs.containsKey(context)) {
//                    float[] r = new float[2];
//                    r[0] = item;
//                    r[1] = prediction(user, item, context);
//                    res.add(r);
//                }
//            }
        }
        return order(res);
    }

    //chuyển đổi dữ liệu kiểm thử
    public void testingSetTransform() {
        for (int i = 0; i < dataset.testingSet.size(); i++) {
            for (Map.Entry<Integer, ItemScorePair> entry : dataset.testingSet.get(i).pairs.entrySet()) {
                for (int k = 0; k < lstNewItem.size(); k++) {
                    boolean condition = (lstNewItem.get(k).group == 1);
                    if (lstNewItem.get(k).itemID == entry.getValue().itemID) {
                        String context = dataset.testingSet.get(i).context;
                        int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(context);
                        boolean check = (arrayContext[lstNewItem.get(k).contextID] == lstNewItem.get(k).valContext);
                        if (check == condition) {
                            ItemScorePair newPair = new ItemScorePair();
                            newPair.itemID = (k);
                            newPair.score = (entry.getValue().score);
                            entry.setValue(newPair);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void run(Dataset data, ContextProcessor CP, boolean process) {
        Date t1 = new Date();
        this.dataset = data;
//        this.dataset.findRatedItemsByUser();
        this.contextProcessor = CP;
        if (process) {
//            this.contextProcessor.runGFSG(dataset, 3, (float) 0.95);
            this.dataset = this.contextProcessor.runADOM(dataset);
        }
        trainingTestTransform();
        //testingSetTransform();
        svd = new MFCF(factor);
        svd.dataset = this.transformedDS;
        svd.maxIteration = maxIteration;
        svd.run();
        this.traditionalRS = svd;
        Estimation.EvaluateErrorsWithContext(this);
        //this.evaluator = svd.evaluator;
        Date t2 = new Date();
        evaluator.training_Time = (t2.getTime() - t1.getTime()) / 1000;
    }

    public void runUB(Dataset data, ContextProcessor CP, int numNeighbors) {
        this.dataset = data;
//        this.dataset.findRatedItemsByUser();
        this.contextProcessor = CP;
        this.flagUB = 1;
        trainingTestTransform();

        //testingSetTransform();
        UserBasedTopK ub = new UserBasedTopK(numNeighbors);
        ub.dataset = this.transformedDS;
        this.traditionalRS = ub;
        Estimation.EvaluateErrorsWithContext(this);
    }

    @Override
    public void runEvaluation() {
        System.out.println(this.name);
        evaluator.print();
    }

    public static void main(String[] arg) {
    }
}
