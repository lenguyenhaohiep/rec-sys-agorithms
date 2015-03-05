/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.alg.memory.UserBasedTopK;
import cars.alg.model.MFCF;
import cars.evaluation.Estimation;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import cars.data.structure.TestInput;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class Movielens100K1 extends Dataset {

    public int iFold;
    String DIR_PATH = "C:\\Users\\Hiep\\Dropbox\\Recommender_Systems\\data\\";
    String USER_PATH = DIR_PATH + "ml-100K\\u.user";
    String ITEM_PATH = DIR_PATH + "ml-100K\\u.item";
    String RATING_PATH = DIR_PATH + "ml-100K\\u" + Integer.toString(iFold) + ".base";
    String TESTING_PATH = DIR_PATH + "ml-100K\\u" + Integer.toString(iFold) + ".test";
    private ArrayList<String> mUser;
    private ArrayList<String> mItem;
    private float avgScore;

    public Movielens100K1(int iFold) {
        RATING_PATH = DIR_PATH + "ml-100K\\u.data";
    }

    /*
     * Read User Data
     */
    private void readUserData() {
        mUser = new ArrayList<String>();
        Scanner scanner;
        try {
            scanner = new Scanner(new File(USER_PATH));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String userID = line.substring(0, line.indexOf("|"));
                mUser.add(userID);
            }
            numUser = (mUser.size());
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
                String itemID = text.substring(0, text.indexOf("|"));
                mItem.add(itemID);
            }
            numItem = (mItem.size());
        } catch (Exception e) {
        }
    }

    /*
     * Read Rating Data
     */
    private void readRatingData() {
        int[] val = {0, 0, 0, 0, 0};
        numRating = 0;
        avgScore = 0;
        mRating = (new float[numUser][numItem]);
        Scanner scanner;
        try {
            scanner = new Scanner(new File(RATING_PATH));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split("\t");
                int indexUser = mUser.indexOf(elements[0].trim());
                int indexItem = mItem.indexOf(elements[1].trim());

                float score = Float.parseFloat(elements[2].trim());
                val[(int) score - 1]++;

                mRating[indexUser][indexItem] = score;
                numRating++;
                avgScore += score;
            }
            scanner.close();
            avgScore = (float) avgScore / numRating;
        } catch (Exception e) {
        }
        
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
                int user_id = mUser.indexOf((elements[0].trim()));
                int item_id = mItem.indexOf((elements[1].trim()));
                float score = Float.parseFloat(elements[2]);
                if (user_id == -1 || item_id == -1) {
                    System.out.println(user_id + "==" + item_id);
                }
                //Kiem tra ton tai
                int index = etc.indexOfExistance(testingSet, user_id);
                if (index == -1) {
                    ItemScorePair pair = new ItemScorePair();
                    pair.itemID = item_id;
                    pair.score = score;
                    etc.userID = user_id;
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

    public void readData() {
        this.readUserData();
        this.readItemData();
        this.readRatingData();
    }
    
    public static void main(String[] are){
        Movielens100K1 t= new Movielens100K1(1);
        t.readData();
    }
}
