/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.alg.ContextProcessor;
import cars.data.structure.DataInput;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import cars.data.structure.Rating;
import cars.data.structure.TestInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hiep
 */
public class DataManager {

    private int numUser;
    private int numItem;
    private int numContextDimension;
    private List<List<String>> contexts;
    private List<String> contextsGenreration = new ArrayList<String>();
    private List<String> users = new ArrayList<String>();
    private List<String> items = new ArrayList<String>();
    private List<List<DataInput>> mFold = new ArrayList<List<DataInput>>();
    private List<DataInput> entireData = new ArrayList<DataInput>();
    public int numFold = 5;
    public List<String> itemContextCombination = new ArrayList<String>();

    public void readEntireData(String filePath) {
        try {
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split(" ");
                if (elements.length == 1) {
                    elements = null;
                    elements = line.split("\t");
                }
                if (contexts == null) {
                    contexts = new ArrayList<List<String>>();
                    for (int c = 0; c < elements.length - 3; c++) {
                        contexts.add(new ArrayList<String>());
                    }
                }
                if (!users.contains(elements[0])) {
                    users.add(elements[0]);
                }
                if (!items.contains(elements[1])) {
                    items.add(elements[1]);
                }
                DataInput di = new DataInput();
                di.score = Float.parseFloat(elements[elements.length - 1]);
                di.itemID = items.indexOf(elements[1]);
                di.userID = users.indexOf(elements[0]);

                String context = "";
                for (int c = 2; c < elements.length - 1; c++) {
                    if (!contexts.get(c - 2).contains(elements[c])) {
                        contexts.get(c - 2).add(elements[c]);
                    }
                    context += contexts.get(c - 2).indexOf(elements[c]) + "::";
                }
                if (!contextsGenreration.contains(context)) {
                    contextsGenreration.add(context);
                }
                di.context = context;
                String combination = di.itemID + "=>" + di.context;
                if (!itemContextCombination.contains(combination)) {
                    itemContextCombination.add(combination);
                }
                entireData.add(di);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Update Number of Users, Items, Contexts
        numUser = users.size();
        numItem = items.size();
        numContextDimension = contexts.size();
    }

    private Comparator getComparatorByCriteria(int criteria) {
        Comparator comparator;

        //sort by items
        if (criteria == 1) {
            comparator = new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    DataInput a = (DataInput) o1;
                    DataInput b = (DataInput) o2;
                    if (a.itemID > b.itemID) {
                        return 1;
                    }
                    if (a.itemID == b.itemID) {
                        return 0;
                    }
                    return -1;
                }
            };
        } //sort by context
        else if (criteria == 2) {
            comparator = new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    DataInput a = (DataInput) o1;
                    DataInput b = (DataInput) o2;
                    return a.context.compareTo(b.context);
                }
            };
        } else {
            //Sort by ratings
            comparator = new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    DataInput a = (DataInput) o1;
                    DataInput b = (DataInput) o2;
                    if (a.score > b.score) {
                        return 1;
                    }
                    if (a.score == b.score) {
                        return 0;
                    }
                    return -1;
                }
            };
        }
        return comparator;
    }

    private void dataSplitter(int criteria) {
        for (int iFold = 0; iFold < numFold; iFold++) {
            mFold.add(new ArrayList<DataInput>());
        }
        for (int c = 0; c < contextsGenreration.size(); c++) {
            List<DataInput> lstRatingByUser = new ArrayList<DataInput>();
            for (int i = 0; i < entireData.size(); i++) {
                if (entireData.get(i).context.equals(contextsGenreration.get(c))) {
                    lstRatingByUser.add(entireData.get(i));
                }

            }

            if (criteria != 4) {
                Collections.sort(lstRatingByUser, getComparatorByCriteria(criteria));
            }

            int[] sizes = new int[numFold];
            int[] indexs = new int[numFold];
            for (int k = 0; k < numFold; k++) {
                sizes[k] = mFold.get(k).size();
                indexs[k] = k;
            }

            for (int i = 0; i < numFold - 1; i++) {
                for (int j = i + 1; j < numFold; j++) {
                    if (sizes[i] > sizes[j]) {
                        int temp2 = sizes[i];
                        sizes[i] = sizes[j];
                        sizes[j] = temp2;
                        int temp1 = indexs[i];
                        indexs[i] = indexs[j];
                        indexs[j] = temp1;
                    }
                }
            }
            for (int i = 0; i < lstRatingByUser.size(); i++) {
                int index = i % numFold;
                mFold.get(indexs[index]).add(lstRatingByUser.get(i));
            }
        }
    }

    public void dataProcessor(String filePath, int criteriaSlitting) {
        readEntireData(filePath);
        dataSplitter(criteriaSlitting);
    }

    public Dataset getDataset(int iFold) {
        Dataset data = new Dataset();
        data.numItem = numItem;
        data.numUser = numUser;
        data.mRatingWithContext = new Rating[numUser][numItem];
        for (int k = 0; k < numFold; k++) {
            if (k != iFold) {
                for (int i = 0; i < mFold.get(k).size(); i++) {

                    int u = mFold.get(k).get(i).userID;
                    int item = mFold.get(k).get(i).itemID;
                    if (data.mRatingWithContext[u][item] == null) {
                        data.mRatingWithContext[u][item] = new Rating();
                    }
                    data.mRatingWithContext[u][item].setValueRating(mFold.get(k).get(i).score, mFold.get(k).get(i).context);
                    data.numRating++;
                    String combination = item + "=>" + mFold.get(k).get(i).context;
                    if (!itemContextCombination.contains(combination)) {
                        itemContextCombination.add(combination);
                    }
                }
            } else {
                for (int i = 0; i < mFold.get(k).size(); i++) {

                    TestInput etc = new TestInput();
                    int user_id = mFold.get(k).get(i).userID;
                    int item_id = mFold.get(k).get(i).itemID;
                    float score = mFold.get(k).get(i).score;
                    String context = mFold.get(k).get(i).context;
                    String combination = item_id + "=>" + mFold.get(k).get(i).context;
                    if (!itemContextCombination.contains(combination)) {
                        itemContextCombination.add(combination);
                    }
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
        return data;
    }

    public Map<String, Rating> getDatasetMap(int iFold) {
        Map<String, Rating> map = new HashMap<String, Rating>();
        for (int k = 0; k < numFold; k++) {
            if (k != iFold) {
                for (int i = 0; i < mFold.get(k).size(); i++) {

                    int u = mFold.get(k).get(i).userID;
                    int item = mFold.get(k).get(i).itemID;
                    if (!map.containsKey(u + "-" + item)) {
                        map.put(u + "-" + item, new Rating());

                    }

                    map.get(u + "-" + item).setValueRating(mFold.get(k).get(i).score, mFold.get(k).get(i).context);

                }
            }
        }
        return map;
    }

    public List<TestInput> getTestsetList(int iFold) {
        List<TestInput> res = new ArrayList<TestInput>();
        for (int k = 0; k < numFold; k++) {
            if (k == iFold) {
                for (int i = 0; i < mFold.get(k).size(); i++) {

                    TestInput etc = new TestInput();
                    int user_id = mFold.get(k).get(i).userID;
                    int item_id = mFold.get(k).get(i).itemID;
                    float score = mFold.get(k).get(i).score;
                    String context = mFold.get(k).get(i).context;

                    //Kiem tra ton taitestingSet,
                    int index = etc.indexOfExistance(res, user_id, context);
                    if (index == -1) {
                        ItemScorePair pair = new ItemScorePair();
                        pair.itemID = item_id;
                        pair.score = score;
                        etc.userID = user_id;
                        etc.context = context;
                        etc.pairs = new HashMap<Integer, ItemScorePair>();
                        etc.pairs.put(0, pair);
                        res.add(etc);
                    } else {
                        int size = res.get(index).pairs.size();
                        ItemScorePair pair = new ItemScorePair();
                        pair.itemID = item_id;
                        pair.score = score;
                        res.get(index).pairs.put(size, pair);
                    }
                }
            }
        }
        return res;
    }

    public List<DataInput> getDatasetList(int iFold) {
        List<DataInput> res = new ArrayList<DataInput>();
        for (int k = 0; k < numFold; k++) {
//            if (k != iFold) {
            res.addAll(mFold.get(k));
//            }
        }
        return res;
    }

    public ContextProcessor getContextProcessor() {
        ContextProcessor cp = new ContextProcessor();
        cp.hierachy = new ArrayList<List<List<Integer>>>();
        for (int i = 0; i < contexts.size(); i++) {
            List<List<Integer>> level1 = new ArrayList<List<Integer>>();
            for (int j = 0; j < contexts.get(i).size(); j++) {
                List<Integer> level2 = new ArrayList<Integer>();
                level2.add(j);
                level1.add(level2);
            }
            cp.hierachy.add(level1);
        }
        cp.numAttrAtContext = new int[contexts.size()];
        for (int i = 0; i < cp.numAttrAtContext.length; i++) {
            cp.numAttrAtContext[i] = contexts.get(i).size();
        }
        return cp;
    }

    public int getMaxItemsTestSet() {
        int maxItems = 0;
        for (int iFold = 0; iFold < numFold; iFold++) {
            List<Integer> _items = new ArrayList<Integer>();
            for (int i = 0; i < mFold.get(iFold).size(); i++) {
                if (!_items.contains(mFold.get(iFold).get(i).itemID)) {
                    _items.add(mFold.get(iFold).get(i).itemID);
                }
            }
            if (maxItems < _items.size()) {
                maxItems = _items.size();
            }
        }
        return maxItems;
    }

    public Dataset getFullDataset() {
        Dataset data = new Dataset();
        data.numItem = numItem;
        data.numUser = numUser;
        data.mRatingWithContext = new Rating[numUser][numItem];
        for (int k = 0; k < numFold; k++) {

            for (int i = 0; i < mFold.get(k).size(); i++) {

                int u = mFold.get(k).get(i).userID;
                int item = mFold.get(k).get(i).itemID;
                if (data.mRatingWithContext[u][item] == null) {
                    data.mRatingWithContext[u][item] = new Rating();
                }
                data.mRatingWithContext[u][item].setValueRating(mFold.get(k).get(i).score, mFold.get(k).get(i).context);
                data.numRating++;
//                System.out.println(u + "\t" + item + "\t" + mFold.get(k).get(i).context.replace("::", "\t") + mFold.get(k).get(i).score);
            }

        }
        return data;
    }

    public Rating[][] readTrainData(String path) {
        Dataset data = new Dataset();
        data.numItem = numItem;
        data.numUser = numUser;
        data.mRatingWithContext = new Rating[numUser][numItem];
        try {
            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split(" ");
                if (elements.length == 1) {
                    elements = null;
                    elements = line.split("\t");
                }
                if (contexts == null) {
                    contexts = new ArrayList<List<String>>();
                    for (int c = 0; c < elements.length - 3; c++) {
                        contexts.add(new ArrayList<String>());
                    }
                }
                if (!users.contains(elements[0])) {
                    users.add(elements[0]);
                }
                if (!items.contains(elements[1])) {
                    items.add(elements[1]);
                }
                DataInput di = new DataInput();
                di.score = Float.parseFloat(elements[elements.length - 1]);
                di.itemID = items.indexOf(elements[1]);
                di.userID = users.indexOf(elements[0]);

                String context = "";
                for (int c = 2; c < elements.length - 1; c++) {
                    if (!contexts.get(c - 2).contains(elements[c])) {
                        contexts.get(c - 2).add(elements[c]);
                    }
                    context += contexts.get(c - 2).indexOf(elements[c]) + "::";
                }
                if (!contextsGenreration.contains(context)) {
                    contextsGenreration.add(context);
                }
                di.context = context;
                if (data.mRatingWithContext[di.userID][di.itemID] == null) {
                    data.mRatingWithContext[di.userID][di.itemID] = new Rating();
                    data.numRating++;
                }
                data.mRatingWithContext[di.userID][di.itemID].setValueRating(di.score, di.context);

                String combination = di.itemID + "=>" + di.context;
                if (!itemContextCombination.contains(combination)) {
                    itemContextCombination.add(combination);
                }

            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(data.numRating);
        return data.mRatingWithContext;
    }

    public List<TestInput> readTestData(String path) {
        Dataset data = new Dataset();
        data.numItem = numItem;
        data.numUser = numUser;
        data.mRatingWithContext = new Rating[numUser][numItem];
        try {
            Scanner scanner = new Scanner(new File(path));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split(" ");
                if (elements.length == 1) {
                    elements = null;
                    elements = line.split("\t");
                }
                if (contexts == null) {
                    contexts = new ArrayList<List<String>>();
                    for (int c = 0; c < elements.length - 3; c++) {
                        contexts.add(new ArrayList<String>());
                    }
                }
                if (!users.contains(elements[0])) {
                    users.add(elements[0]);
                }
                if (!items.contains(elements[1])) {
                    items.add(elements[1]);
                }
                DataInput di = new DataInput();
                di.score = Float.parseFloat(elements[elements.length - 1]);
                di.itemID = items.indexOf(elements[1]);
                di.userID = users.indexOf(elements[0]);

                String context = "";
                for (int c = 2; c < elements.length - 1; c++) {
                    if (!contexts.get(c - 2).contains(elements[c])) {
                        contexts.get(c - 2).add(elements[c]);
                    }
                    context += contexts.get(c - 2).indexOf(elements[c]) + "::";
                }
                if (!contextsGenreration.contains(context)) {
                    contextsGenreration.add(context);
                }
                di.context = context;
                TestInput etc = new TestInput();
                int user_id = di.userID;
                int item_id = di.itemID;
                float score = di.score;
                context = di.context;
                String combination = item_id + "=>" + di.context;
                if (!itemContextCombination.contains(combination)) {
                    itemContextCombination.add(combination);
                }
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
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data.testingSet;
    }

    public Dataset getDataFixedSplitter(String path1, String path2, String path3) {
        Dataset data = new Dataset();
        this.readEntireData(path1);
        data.numItem = numItem;
        data.numUser = numUser;
        data.mRatingWithContext = this.readTrainData(path2);
        data.testingSet = this.readTestData(path3);
        return data;
    }

    public Dataset readMovilenData(int i) {
        Movielens100K m = new Movielens100K(i);
        m.iFold = i;
        m.readData();
        return m;
    }
}
