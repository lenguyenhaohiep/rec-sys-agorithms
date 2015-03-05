/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.alg.model.MFCF;
import cars.evaluation.Estimation;
import cars.data.structure.DataInput;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import cars.data.structure.Rating;
import cars.data.structure.TestInput;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class Movielens extends Dataset {

    String DIR_PATH = "D:\\";
    String USER_PATH = DIR_PATH + "ml-100K\\u.user";
    String ITEM_PATH = DIR_PATH + "ml-100K\\u.item";
    String RATING_PATH = DIR_PATH + "ml-100K\\u5.test";
    String TESTING_PATH = DIR_PATH + "ml-100K\\u3.test";
    private List<String> mUser;
    private List<String> mItem;
    private List<Integer> mAge;
    private List<Integer> mGender;
    private float avgScore;
    private List<DataInput> entireData = new ArrayList<DataInput>();
    private int m_fold = 5;
    List<List<DataInput>> folds = new ArrayList<List<DataInput>>();

    public void generateSemiSyntheticDataset(float alpha, float beta) {
        List<Integer> itemFraction = new ArrayList<Integer>();


        int numItemFraction = Math.round(alpha * mItem.size());
        while (itemFraction.size() < numItemFraction) {
            Random r = new Random();
            int i = r.nextInt(mItem.size());
            while (!itemFraction.contains(i)) {
                itemFraction.add(i);
                i = r.nextInt(mItem.size());
            }
        }
        for (int fold = 0; fold < m_fold; fold++) {
            List<DataInput> _entireData = folds.get(fold);
            List<Integer> ratingID = new ArrayList<Integer>();
            List<Integer> ratingFraction = new ArrayList<Integer>();
            for (int i = 0; i < _entireData.size(); i++) {
                if (itemFraction.contains(_entireData.get(i).itemID)) {
                    ratingID.add(i);
                }
            }
            int numRatingFraction = Math.round(beta * ratingID.size());
            while (ratingFraction.size() < numRatingFraction) {
                Random r = new Random();
                int i = r.nextInt(ratingID.size());
                while (!ratingFraction.contains(i)) {
                    ratingFraction.add(i);
                }
            }

            for (int i = 0; i < ratingFraction.size(); i++) {
                Random r = new Random();
                int c = r.nextInt(2);

                if (_entireData.get(ratingFraction.get(i)).score == 1.0 && c == 0) {
                    c = 1;
                }

                if (_entireData.get(ratingFraction.get(i)).score == 5.0 && c == 1) {
                    c = 0;
                }
                _entireData.get(ratingFraction.get(i)).context = c + "::" + _entireData.get(ratingFraction.get(i)).context;
                if (c == 0) {
                    _entireData.get(ratingFraction.get(i)).score = _entireData.get(ratingFraction.get(i)).score - 1;
                } else {
                    _entireData.get(ratingFraction.get(i)).score = _entireData.get(ratingFraction.get(i)).score + 1;
                }
            }

            for (int i = 0; i < _entireData.size(); i++) {
                if (_entireData.get(i).context.length() < 4) {
                    _entireData.get(i).context = mGender.get(_entireData.get(i).userID) + "::" + _entireData.get(i).context;
//                System.out.println(_entireData.get(i).context);
                }
            }
            //DataInput di = new DataInput();
            //di.sortUser(_entireData);
//            mRatingWithContext = new Rating[numUser][numItem];
//            for (int i = 0; i < _entireData.size(); i++) {
//                if (mRatingWithContext[_entireData.get(i).userID][_entireData.get(i).itemID] == null) {
//                    mRatingWithContext[_entireData.get(i).userID][_entireData.get(i).itemID] = new Rating();
//                }
//                mRatingWithContext[_entireData.get(i).userID][_entireData.get(i).itemID].setValueRating(_entireData.get(i).score, _entireData.get(i).context);
//            }
            folds.set(fold, _entireData);
        }
    }

    public void readUserData() {
        mUser = new ArrayList<String>();
        mAge = new ArrayList<Integer>();
        mGender = new ArrayList<Integer>();
        Scanner scanner;
        try {
            scanner = new Scanner(new File(USER_PATH));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.replace('|', '\t').split("\t");
                String userID = line.substring(0, line.indexOf("|"));
                String a = line.substring(line.indexOf("|") + 1, line.indexOf("|") + 3);
                int age = Integer.parseInt(elements[1]);
                String gender = elements[2];
                if (gender.equals("M")) {
                    mGender.add(0);
                } else {
                    mGender.add(1);
                }
                if (age < 18) {
                    mAge.add(0);
                }
                if (age >= 18 && age <= 50) {
                    mAge.add(1);
                }
                if (age > 50) {
                    mAge.add(2);
                }
                //if (!mUser.contains(userID)) {
                mUser.add(userID);
                // }
            }
            numUser = mUser.size();
            scanner.close();
        } catch (FileNotFoundException ex) {
        }
    }

    /*
     * Read Item Data
     */
    private void readItemData() {
        mItem = new ArrayList<String>();
        try {
            BufferedReader reader = null;
            reader = new BufferedReader(new FileReader(new File(ITEM_PATH)));
            String text = null;

            while ((text = reader.readLine()) != null) {
                String[] elements = text.replace('|', '\t').split("\t");
                String itemID = text.substring(0, text.indexOf("|"));
                // if (!mItem.contains(itemID)) {
                mItem.add(itemID);
                // }
            }
            numItem = mItem.size();
        } catch (Exception e) {
        }
    }

    /*
     * Read Rating Data
     */
    private List<DataInput> readRatingData(int datafile) {
        List<DataInput> _entireData = new ArrayList<DataInput>();
        mRatingWithContext = new Rating[numUser][numItem];
        numRating = 0;
        avgScore = 0;
        mRating = new float[numUser][numItem];
        Scanner scanner;
        try {
            scanner = new Scanner(new File("D:\\ml-100k\\u" + datafile + ".test"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split("\t");
                int indexUser = mUser.indexOf(elements[0].trim());
                int indexItem = mItem.indexOf(elements[1].trim());
                float score = Float.parseFloat(elements[2].trim());
                mRating[indexUser][indexItem] = score;
                if (mRatingWithContext[indexUser][indexItem] == null) {
                    mRatingWithContext[indexUser][indexItem] = new Rating();
                }
                mRatingWithContext[indexUser][indexItem].setValueRating(Float.parseFloat(elements[2]), new int[]{mGender.get(indexUser), mAge.get(indexUser)});
                numRating++;
                avgScore += score;
                DataInput di = new DataInput();
                di.userID = indexUser;
                di.itemID = indexItem;
                di.score = Float.parseFloat(elements[2]);
                di.context = mAge.get(indexUser).toString() + "::" + mGender.get(indexUser).toString() + "::";
                _entireData.add(di);

            }
            scanner.close();
            avgScore = (float) avgScore / numRating;
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return _entireData;
    }

    public void readTestingSet() {

        Scanner scanner;
        try {
            scanner = new Scanner(new File(TESTING_PATH));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split("\t");
                TestInput etc = new TestInput();
                int[] test = new int[4];
                int user_id = mUser.indexOf((elements[0]));
                int item_id = mItem.indexOf((elements[1]));
                float score = Float.parseFloat(elements[2]);

                String context = mGender.get(user_id) + "::" + mAge.get(user_id) + "::";
                //Kiem tra ton tai
                int index = etc.indexOfExistance(testingSet, user_id);
                if (index == -1) {
                    ItemScorePair pair = new ItemScorePair();
                    pair.itemID = item_id;
                    pair.score = score;
                    etc.userID = user_id;
                    etc.context = context;
                    etc.pairs = new HashMap<Integer, ItemScorePair>();
                    etc.pairs.put(0, pair);
                    testingSet.add(etc);
                } else {
                    int size = testingSet.get(index).pairs.size();
                    ItemScorePair pair = new ItemScorePair();
                    pair.itemID = item_id;
                    pair.score = score;
                    testingSet.get(index).pairs.put(size, pair);
                }

            }
            scanner.close();
        } catch (Exception e) {
        }
    }

    public void output(int k) throws FileNotFoundException {
        String dir = "D:\\MV\\movielensSemiS.fold" + k;
        PrintWriter out = new PrintWriter(new File(dir));
        int count = 0;
        List<DataInput> _entireData = folds.get(k);
        for (int i = 0; i < _entireData.size(); i++) {
            out.println(_entireData.get(i).toString());

        }

        out.close();
    }

    public void readData() {
        this.readUserData();
        this.readItemData();
        for (int i = 1; i <= 5; i++) {
            folds.add(this.readRatingData(i));
        }
//        this.generateSemiSyntheticDataset((float) 0, (float) 0);
        try {
            this.output();
            //this.readTestingSet();
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) {
        Movielens m = new Movielens();
        m.readData();
//        m.readUserData();
//        m.readDataFromFile();
//        m.splitTestingSet();
//        m.generateSemiSyntheticDataset((float) 0.9, (float) 0.9);
//        try {
//            m.output();
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Movielens.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public void readDataFromFile() {
        numUser = 943;
        numItem = 1682;
        mRatingWithContext = new cars.data.structure.Rating[numUser][numItem];
        try {
            Scanner scanner = new Scanner(new File("D:\\data\\MSSS.entire"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("::")) {
                    String[] elements = line.split("::");
                    numUser = Integer.parseInt(elements[0]);
                    numItem = Integer.parseInt(elements[1]);
                    mRatingWithContext = new cars.data.structure.Rating[numUser][numItem];
                    contextDimension = 2;
                    numContextAtDimension = new int[contextDimension];
                    numContextAtDimension[0] = Integer.parseInt(elements[2]);
                    numContextAtDimension[1] = Integer.parseInt(elements[3]);

                } else {
                    String[] elements = line.split("\t");
                    int u = Integer.parseInt(elements[0]);
                    int i = Integer.parseInt(elements[1]);
                    int c = Integer.parseInt(elements[2]);
                    int f = Integer.parseInt(elements[3]);
                    if (mRatingWithContext[u][i] == null) {
                        mRatingWithContext[u][i] = new Rating();
                    }
                    mRatingWithContext[u][i].setValueRating(Float.parseFloat(elements[4]), new int[]{c, f});
                    numRating++;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void splitTestingSet() {
        testingSet = new ArrayList<TestInput>();
        int numtestset = 0;
        int count2 = 0;
        while (((double) numtestset / numRating) < 0.2) {
            for (int u = 0; u < numUser; u++) {
                boolean check = false;
                int countu = 0;
                while (countu < 20) {
                    Random r = new Random();
                    int iGender = r.nextInt(2);
                    int iAge = mAge.get(u);
                    String context = iGender + "::" + iAge + "::";
                    int count = 0;
                    for (int i = 0; i < numItem; i++) {
                        if (mRatingWithContext[u][i] != null) {
                            if (mRatingWithContext[u][i].contextScorePairs.containsKey(context)) {
                                count++;
                                TestInput etc = new TestInput();
                                int user_id = u;
                                int item_id = i;
                                float score = mRatingWithContext[u][i].contextScorePairs.get(context);

                                //Kiem tra ton tai
                                int index = etc.indexOfExistance(testingSet, user_id, context);
                                if (index == -1) {
                                    ItemScorePair pair = new ItemScorePair();
                                    pair.itemID = item_id;
                                    pair.score = score;
                                    etc.userID = user_id;
                                    etc.context = context;
                                    etc.pairs = new HashMap<Integer, ItemScorePair>();
                                    etc.pairs.put(0, pair);
                                    testingSet.add(etc);
                                } else {
                                    int size = testingSet.get(index).pairs.size();
                                    ItemScorePair pair = new ItemScorePair();
                                    pair.itemID = item_id;
                                    pair.score = score;
                                    testingSet.get(index).pairs.put(size, pair);
                                }

                                mRatingWithContext[u][i].contextScorePairs.remove(context);
                                numtestset++;
                                countu++;
                                if (count == 10) {
                                    break;
                                }

                            }
                        }
                    }
                }
            }

        }
    }

    private void output() throws FileNotFoundException {
        String dir = "D:\\MV\\m.dat";
        PrintWriter out = new PrintWriter(new File(dir));
        for (int k = 0; k < 5; k++) {
            for (int i = 0; i < folds.get(k).size(); i++) {
                int u = folds.get(k).get(i).userID;
                int item = folds.get(k).get(i).itemID;
                out.println(u + "\t" + item + "\t" + folds.get(k).get(i).context.replace("::", "\t") + folds.get(k).get(i).score);
            }

        }
        out.close();

    }
}
