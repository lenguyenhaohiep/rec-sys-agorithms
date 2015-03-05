/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.data.structure.TestInput;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import cars.data.structure.Rating;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class Food extends Dataset {

    public void readData(int k) {
        try {
            Scanner scanner = new Scanner(new File("D:\\dataset\\food.dataset" + k));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("::")) {
                    String[] elements = line.split("::");
                    numUser = Integer.parseInt(elements[0]);
                    numItem = Integer.parseInt(elements[1]);
                    mRatingWithContext = new cars.data.structure.Rating[numUser][numItem];
                    contextDimension = 1;
                    numContextAtDimension = new int[contextDimension];
                    numContextAtDimension[0] = Integer.parseInt(elements[2]);

                } else {
                    String[] elements = line.split(" ");
                    int u = Integer.parseInt(elements[0]);
                    int i = Integer.parseInt(elements[1]);
                    int c = Integer.parseInt(elements[2]);
                    if (mRatingWithContext[u][i] == null) {
                        mRatingWithContext[u][i] = new Rating();
                    }
                    mRatingWithContext[u][i].setValueRating(Float.parseFloat(elements[3]), new int[]{c});
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void readTest2(int k) {
        testingSet = new ArrayList<TestInput>();
        try {
            Scanner scanner = new Scanner(new File("D:\\dataset\\food.testset" + k));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split(" ");
                TestInput etc = new TestInput();
                int[] test = new int[4];
                int user_id = Integer.parseInt(elements[0]);
                int item_id = Integer.parseInt(elements[1]);
                float score = Float.parseFloat(elements[3]);
                String context = elements[2] + "::";

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
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void reduceContextDimension() {
        mRating = new float[numUser][numItem];
        for (int u = 0; u < numUser; u++) {
            for (int i = 0; i < numItem; i++) {
                float sum = 0;
                int count = 0;
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        sum += entry.getValue();
                        count++;
                    }
                }

                if (count == 0) {
                    mRating[u][i] = 0;
                } else {
                    mRating[u][i] = (float) sum / count;
                }
            }
        }
        //System.out.println(sum / count);

        //System.out.println(u + " " + i + " " + mRating.get(u, i));

    }

    public void read(int i) {
        //this.contextDimension = 1;
        //this.numContextAtDimension = new int[]{3};
        this.readData(i);
        this.readTest2(i);
    }
}
