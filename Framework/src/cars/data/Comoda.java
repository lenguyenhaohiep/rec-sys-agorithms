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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Hiep
 */
public class Comoda extends Dataset {

    List<String> mTime = new ArrayList<String>();
    List<String> mDayType = new ArrayList<String>();
    List<String> mSeason = new ArrayList<String>();
    List<String> mLocation = new ArrayList<String>();
    List<String> mWeather = new ArrayList<String>();
    List<String> mSocial = new ArrayList<String>();
    List<String> mEndEmo = new ArrayList<String>();
    List<String> mDominantEmo = new ArrayList<String>();
    List<String> mMood = new ArrayList<String>();
    List<String> mPhysical = new ArrayList<String>();
    List<String> mDecision = new ArrayList<String>();
    List<String> mInteraction = new ArrayList<String>();
    List<String> mUser = new ArrayList<String>();
    List<String> mItem = new ArrayList<String>();
    List<List<DataInput>> mFold = new ArrayList<List<DataInput>>();
    List<List<DataInput>> mRatingByUser = new ArrayList<List<DataInput>>();

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

    public int max(int[] a) {
        int m = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > m) {
                m = a[i];
            }
        }
        return m;
    }

    public void readData() {
        try {
            Scanner scanner = new Scanner(new File("D:\\CoMoDa.csv"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("userID")) {
                } else {
                    String[] elements = line.split(",");
                    String user, item;
                    user = elements[0];
                    item = elements[1];

                    if (!mUser.contains(user)) {
                        mUser.add(user);
                    }
                    if (!mItem.contains(item)) {
                        mItem.add(item);
                    }

                    String time, daytype, season, location, weather, social, endEmo, dominantEmo, mood, physical, decision, interaction;
                    time = elements[7];
                    daytype = elements[8];
                    season = elements[9];
                    location = elements[10];
                    weather = elements[11];
                    social = elements[12];
                    endEmo = elements[13];
                    dominantEmo = elements[14];
                    mood = elements[15];
                    physical = elements[15];
                    decision = elements[17];
                    interaction = elements[18];
                    if (!mTime.contains(time)) {
                        mTime.add(time);
                    }
                    if (!mDayType.contains(daytype)) {
                        mDayType.add(daytype);
                    }
                    if (!mSeason.contains(season)) {
                        mSeason.add(season);
                    }
                    if (!mLocation.contains(location)) {
                        mLocation.add(location);
                    }
                    if (!mWeather.contains(weather)) {
                        mWeather.add(weather);
                    }
                    if (!mSocial.contains(social)) {
                        mSocial.add(social);
                    }
                    if (!mEndEmo.contains(endEmo)) {
                        mEndEmo.add(endEmo);
                    }
                    if (!mDominantEmo.contains(dominantEmo)) {
                        mDominantEmo.add(dominantEmo);
                    }
                    if (!mMood.contains(mood)) {
                        mMood.add(mood);
                    }
                    if (!mPhysical.contains(physical)) {
                        mPhysical.add(physical);
                    }
                    if (!mDecision.contains(decision)) {
                        mDecision.add(decision);
                    }
                    if (!mInteraction.contains(interaction)) {
                        mInteraction.add(interaction);
                    }
                }
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
        mTime.remove("-1");
        mDayType.remove("-1");
        mSeason.remove("-1");
        mLocation.remove("-1");
        mWeather.remove("-1");
        mSocial.remove("-1");
        mEndEmo.remove("-1");
        mDominantEmo.remove("-1");
        mMood.remove("-1");
        mPhysical.remove("-1");
        mDecision.remove("-1");
        mInteraction.remove("-1");

        int[] aTime = new int[mTime.size()];
        int[] aDayTime = new int[mDayType.size()];
        int[] aSeason = new int[mSeason.size()];
        int[] aLocation = new int[mLocation.size()];
        int[] aWeather = new int[mWeather.size()];
        int[] aSocial = new int[mSocial.size()];
        int[] aEndEmo = new int[mEndEmo.size()];
        int[] aDominantEmo = new int[mDominantEmo.size()];
        int[] aMood = new int[mMood.size()];
        int[] aPhysical = new int[mPhysical.size()];
        int[] aDecision = new int[mDecision.size()];
        int[] aInteraction = new int[mInteraction.size()];

        numUser = mUser.size();
        numItem = mItem.size();
        mRatingWithContext = new Rating[mUser.size()][mItem.size()];

        try {
            Scanner scanner = new Scanner(new File("D:\\CoMoDa.csv"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("userID")) {
                } else {
                    String[] elements = line.split(",");
                    String user, item;
                    user = elements[0];
                    item = elements[1];

                    if (!mUser.contains(user)) {
                        mUser.add(user);
                    }
                    if (!mItem.contains(item)) {
                        mItem.add(item);
                    }

                    String time, daytype, season, location, weather, social, endEmo, dominantEmo, mood, physical, decision, interaction;
                    time = elements[7];
                    daytype = elements[8];
                    season = elements[9];
                    location = elements[10];
                    weather = elements[11];
                    social = elements[12];
                    endEmo = elements[13];
                    dominantEmo = elements[14];
                    mood = elements[15];
                    physical = elements[16];
                    decision = elements[17];
                    interaction = elements[18];

                }

            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Scanner scanner = new Scanner(new File("D:\\CoMoDa.csv"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("userID")) {
                } else {
                    String[] elements = line.split(",");
                    String time, daytype, season, location, weather, social, endEmo, dominantEmo, mood, physical, decision, interaction;
                    int c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12;

                    time = elements[7];

                    if (!time.equals("-1")) {
                        c1 = mTime.indexOf(time);
                    } else {
                        c1 = max(aTime);
                    }
                    daytype = elements[8];
                    if (!daytype.equals("-1")) {
                        c2 = mDayType.indexOf(daytype);
                    } else {
                        c2 = max(aDayTime);
                    }
                    season = elements[9];
                    if (!season.equals("-1")) {
                        c3 = mSeason.indexOf(season);
                    } else {
                        c3 = max(aSeason);
                    }
                    location = elements[10];
                    if (!location.equals("-1")) {
                        c4 = mLocation.indexOf(location);
                    } else {
                        c4 = max(aLocation);
                    }
                    weather = elements[11];
                    if (!weather.equals("-1")) {
                        c5 = mWeather.indexOf(weather);
                    } else {
                        c5 = max(aWeather);
                    }
                    social = elements[12];
                    if (!social.equals("-1")) {
                        c6 = mSocial.indexOf(social);
                    } else {
                        c6 = max(aSocial);
                    }
                    endEmo = elements[13];
                    if (!endEmo.equals("-1")) {
                        c7 = mEndEmo.indexOf(endEmo);
                    } else {
                        c7 = max(aEndEmo);
                    }
                    dominantEmo = elements[14];
                    if (!dominantEmo.equals("-1")) {
                        c8 = mDominantEmo.indexOf(dominantEmo);
                    } else {
                        c8 = max(aDominantEmo);
                    }
                    mood = elements[15];
                    if (!mood.equals("-1")) {
                        c9 = mMood.indexOf(mood);
                    } else {
                        c9 = max(aMood);
                    }
                    physical = elements[16];
                    if (!physical.equals("-1")) {
                        c10 = mPhysical.indexOf(physical);
                    } else {
                        c10 = max(aPhysical);
                    }

                    decision = elements[17];
                    if (!decision.equals("-1")) {
                        c11 = mDecision.indexOf(decision);
                    } else {
                        c11 = max(aDecision);
                    }
                    interaction = elements[18];
                    if (!interaction.equals("-1")) {
                        c12 = mInteraction.indexOf(interaction);
                    } else {
                        c12 = max(aInteraction);
                    }



                    int u = mUser.indexOf(elements[0]);
                    int i = mItem.indexOf(elements[1]);

                    if (mRatingWithContext[u][i] == null) {
                        mRatingWithContext[u][i] = new Rating();
                    }
                    mRatingWithContext[u][i].setValueRating(Float.parseFloat(elements[2]), new int[]{c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12});
                    numRating++;
                }

            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void outputFold() throws FileNotFoundException {
        for (int k = 0; k < 5; k++) {
            String dir = "D:\\data\\comoda.fold" + k;
            PrintWriter out = new PrintWriter(new File(dir));
            int count = 0;
            for (int i = 0; i < mFold.get(k).size(); i++) {
                DataInput di = mFold.get(k).get(i);
                out.println(di.userID + "\t" + di.itemID + "\t" + di.context.replace("::", "\t") + di.score);
            }
            out.close();
        }
    }

    public void output(int k) throws FileNotFoundException {
        String dir = "D:\\data\\comoda.train" + k;
        PrintWriter out = new PrintWriter(new File(dir));
        int count = 0;
        for (int u = 0; u < numUser; u++) {
            for (int i = 0; i < numItem; i++) {
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        out.println(u + "\t" + i + "\t" + entry.getKey().replace("::", "\t") + entry.getValue());
                    }
                }
            }
        }
        out.close();
        dir = "D:\\data\\comoda.test" + k;
        out = new PrintWriter(new File(dir));
        for (int i = 0; i < testingSet.size(); i++) {
            for (Map.Entry<Integer, ItemScorePair> entry : testingSet.get(i).pairs.entrySet()) {
                out.println(testingSet.get(i).userID + "\t" + entry.getValue().itemID + "\t" + testingSet.get(i).context.replace("::", "\t") + entry.getValue().score);
            }
        }
        out.close();
    }

    public void splitTestSet() {
        for (int u = 0; u < numUser; u++) {
            Random r = new Random();
            //int i = r.nextInt(numItem);
            int count;
            if (numRatingByUser[u] < 3) {
                count = 0;
            } else {
                count = (int) Math.round(0.2 * numRatingByUser[u]) + 1;
            }
            boolean check = false;
            int count2 = 0;
            if (count > 0) {
                for (int i = 0; i < numItem; i++) {
                    if (mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            if (check(entry.getKey(), i)) {
                                TestInput etc = new TestInput();
                                int user_id = u;
                                int item_id = i;
                                float score = entry.getValue();

                                //Kiem tra ton tai
                                int index = etc.indexOfExistance(testingSet, user_id, entry.getKey());
                                if (index == -1) {
                                    ItemScorePair pair = new ItemScorePair();
                                    pair.itemID = item_id;
                                    pair.score = score;
                                    etc.userID = user_id;
                                    etc.context = entry.getKey();
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
                                String context = entry.getKey();
                                mRatingWithContext[u][i].contextScorePairs.remove(context);
                                count2++;
                                break;
                            }
                        }
                    }

                }
            }

        }
    }

    public void readData(int fold) {
        numUser = 121;
        numItem = 1232;
        mRatingWithContext = new Rating[numUser][numItem];
        try {
            Scanner scanner = new Scanner(new File("D:\\data\\comoda.train" + fold));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("userID")) {
                } else {
                    String[] elements = line.split("\t");
                    int user, item;
                    user = Integer.parseInt(elements[0]);
                    item = Integer.parseInt(elements[1]);
                    String context = "";
                    for (int i = 2; i <= 13; i++) {
                        context += elements[i] + "::";
                    }

                    if (mRatingWithContext[user][item] == null) {
                        mRatingWithContext[user][item] = new Rating();
                    }
                    mRatingWithContext[user][item].setValueRating(Float.parseFloat(elements[14]), context);

                }
            }
            scanner.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Scanner scanner = new Scanner(new File("D:\\data\\comoda.test" + fold));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                String[] elements = line.split("\t");
                TestInput etc = new TestInput();
                int[] test = new int[4];
                int user_id = Integer.parseInt(elements[0]);
                int item_id = Integer.parseInt(elements[1]);
                float score = Float.parseFloat(elements[14]);
                String context = "";
                for (int i = 2; i <= 13; i++) {
                    context += elements[i] + "::";
                }

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

    public static void main(String[] args) throws FileNotFoundException {
        Comoda comoda = new Comoda();
        comoda.readData();
        comoda.computeAvgRatingByUser();
        comoda.split();
        comoda.outputFold();
//        comoda.splitTestSet();
//        try {
//            comoda.output(1);
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Comoda.class.getName()).log(Level.SEVERE, null, ex);
//        }

    }

    private boolean check(String key, int itemid) {
        int count = 0;
        for (int u = 0; u < numUser; u++) {
            if (mRatingWithContext[u][itemid] != null) {
                for (Map.Entry<String, Float> entry : mRatingWithContext[u][itemid].contextScorePairs.entrySet()) {
                    if (entry.getKey().equals(key)) {
                        count++;
                        if (count > 2) {
                            return true;
                        }
                    }
                }
            }

        }
        return (count > 2);
    }
}
