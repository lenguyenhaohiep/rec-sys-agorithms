/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.data.structure.DataInput;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import cars.data.structure.Rating;
import cars.data.structure.TestInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
public class AIST extends Dataset {

    List<List<DataInput>> mFold = new ArrayList<List<DataInput>>();

    public void readData() {
        try {
            Scanner scanner = new Scanner(new File("D:\\data\\food.entire"));
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
                    String[] elements = line.split(" ");
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

    public void split() {
        mFold.add(new ArrayList<DataInput>());
        mFold.add(new ArrayList<DataInput>());
        mFold.add(new ArrayList<DataInput>());
        mFold.add(new ArrayList<DataInput>());
        mFold.add(new ArrayList<DataInput>());
        for (int u = 0; u < numUser; u++) {
            List<DataInput> lstRatingByUser = new ArrayList<DataInput>();
            for (int i = 0; i < numItem; i++) {
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        DataInput di = new DataInput();
                        di.userID = u;
                        di.itemID = i;
                        di.context = entry.getKey();
                        di.score = entry.getValue();
                        lstRatingByUser.add(di);
                    }

                }
            }

            Comparator comparator = new Comparator() {
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
            Collections.sort(lstRatingByUser, comparator);

            int[] sizes = new int[5];
            int[] indexs = new int[5];
            for (int k = 0; k < 5; k++) {
                sizes[k] = mFold.get(k).size();
                indexs[k] = k;
            }

            for (int i = 0; i < 4; i++) {
                for (int j = i + 1; j < 5; j++) {
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
                int index = i % 5;
                mFold.get(indexs[index]).add(lstRatingByUser.get(i));
            }
        }
    }

    public void outputFold() throws FileNotFoundException {
        for (int k = 0; k < 5; k++) {
            String dir = "D:\\data\\food.fold" + k;
            PrintWriter out = new PrintWriter(new File(dir));
            int count = 0;
            for (int i = 0; i < mFold.get(k).size(); i++) {
                DataInput di = mFold.get(k).get(i);
                out.println(di.userID + "\t" + di.itemID + "\t" + di.context.replace("::", "\t") + di.score);
            }
            out.close();
        }
    }

    public static void main(String arg[]) {
//        AIST a = new AIST();
//        a.readData();
//        a.split();
//        try {
//            a.outputFold();
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(AIST.class.getName()).log(Level.SEVERE, null, ex);
//        }
        Map<String, Float> a = new HashMap<String, Float>();
        a.put("1", (float) 3);
        System.out.print(a.get("2"));
    }
}
