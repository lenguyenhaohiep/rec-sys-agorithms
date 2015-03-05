/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.data.structure.TestInput;
import cars.data.structure.Rating;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class HMusic extends Dataset {

    List<String> users = new ArrayList<String>();
    List<String> emotions = new ArrayList<String>();
    List<String> items = new ArrayList<String>();
    List<String> time = new ArrayList<String>();
    public List<List<cars.data.structure.DataInput>> mFold = new ArrayList<List<cars.data.structure.DataInput>>();
    public List<List<cars.data.structure.DataInput>> mRatingByUser = new ArrayList<List<cars.data.structure.DataInput>>();
    double[][][][] mR;
    String dbUrl = "jdbc:mysql://localhost:3306/newest";
    String dbClass = "com.mysql.jdbc.Driver";
    Connection con;

    public void split() {
        mFold.add(new ArrayList<cars.data.structure.DataInput>());
        mFold.add(new ArrayList<cars.data.structure.DataInput>());
        mFold.add(new ArrayList<cars.data.structure.DataInput>());
        mFold.add(new ArrayList<cars.data.structure.DataInput>());
        mFold.add(new ArrayList<cars.data.structure.DataInput>());
        for (int u = 0; u < numUser; u++) {
            List<cars.data.structure.DataInput> lstRatingByUser = new ArrayList<cars.data.structure.DataInput>();
            for (int i = 0; i < numItem; i++) {
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        cars.data.structure.DataInput di = new cars.data.structure.DataInput();
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

    public HMusic() {
//        time.add("AM");
//        time.add("PM");
        time.add("Sáng");
        time.add("Trưa");
        time.add("Chiều");
        time.add("Tối");
        time.add("Khuya");
    }

    private void connect() throws SQLException, ClassNotFoundException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbUrl, "root", "");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void readUser() {
        try {
            String sql = "(select uid,count(*) from rating  group by uid having count(*) >= 3)";
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                users.add(res.getString(1));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void readItem() {
        try {
            String sql = "select distinct(iid) from rating where uid in (select uid from (select uid,count(*) from rating group by uid having count(*) >= 3) r)";
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                items.add(res.getString(1));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void readEmotion() {
        try {
            String sql = "select id from context";
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                emotions.add(res.getString(1));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private int getIndexContext(Timestamp timestamp) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        //Date date = sdf.parse(timestamp);
        //int time = date.getHours();
        int time = timestamp.getHours();
        //Sang: 0-10, trua: 10-12, chieu 1-5, 60-12
        if (time >= 6 && time < 10) {
            return this.time.indexOf("Sáng");
        }
        if (time >= 10 && time < 14) {
            return this.time.indexOf("Trưa");
        }
        if (time >= 14 && time < 18) {
            return this.time.indexOf("Chiều");
        }
        if (time >= 18 && time < 22) {
            return this.time.indexOf("Tối");
        }
        return this.time.indexOf("Khuya");

//        if (time >= 0 && time < 12) {
//            return this.time.indexOf("AM");
//        }
//        return this.time.indexOf("PM");
    }

    private void readRating() {
        numRating = 0;
        Statement st;
        ResultSet res = null;
        try {
            numItem = items.size();
            numUser = users.size();
            mR = new double[numUser][numItem][5][7];
            mRatingWithContext = new Rating[numUser][numItem];
            String sql = "select UID,IID,CID,OCCUR,SCORE from Rating where uid in (select uid from (select uid,count(*) from rating group by uid having count(*) >=3) r)";
            st = con.createStatement();
            res = st.executeQuery(sql);
            while (res.next()) {
                int iUser = users.indexOf(res.getString(1));
                int iItem = items.indexOf(res.getString(2));
                int iEmotion = emotions.indexOf(res.getString(3));
                int iTime = getIndexContext(res.getTimestamp(4));
                int score = res.getInt(5);
                mR[iUser][iItem][iTime][iEmotion] = score;
                if (mRatingWithContext[iUser][iItem] == null) {
                    mRatingWithContext[iUser][iItem] = new Rating();
                }
                mRatingWithContext[iUser][iItem].setValueRating(score, new int[]{iEmotion, iTime});
                numRating++;
            }
        } catch (Exception e) {
        }
    }

    public boolean check(int i, String c) {
        int count = 0;
        for (int u = 0; u < numUser; u++) {
            if (mRatingWithContext[u][i] != null) {
                if (mRatingWithContext[u][i].contextScorePairs.containsKey(c)) {
                    count++;
                    if (count == 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void splitTestingSet() {
        testingSet = new ArrayList<TestInput>();
        int numtestset = 0;
        for (int u = 0; u < numUser; u++) {
            if ((double) numtestset / numRating >= 0.2) {
                return;
            }
            boolean check = false;
            int count2 = 0;
            while (check == false) {
                Random r = new Random();
                count2++;
                int c = r.nextInt(7);
                int c2 = r.nextInt(5);
                if (count2 == 1000000) {
                    check = true;
                }

                int count = 0;
                for (int i = 0; i < numItem; i++) {
                    if (mRatingWithContext[u][i] != null) {
                        if (mRatingWithContext[u][i].contextScorePairs.containsKey(Integer.toString(c) + "::" + c2 + "::")) {
                            if (check(i, Integer.toString(c) + "::" + c2 + "::")) {
                                count++;

                                TestInput etc = new TestInput();
                                int user_id = u;
                                int item_id = i;
                                float score = mRatingWithContext[u][i].contextScorePairs.get(Integer.toString(c) + "::" + c2 + "::");
                                String context = Integer.toString(c) + "::" + c2 + "::";

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

                                mRatingWithContext[u][i].contextScorePairs.remove(Integer.toString(c) + "::" + c2 + "::");

                                numtestset++;
                                if (count == 3) {
                                    check = true;
                                    break;
                                }

                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void reduceContextDimension() {
        computeAvgRatingByUser();
        mRating = new float[numUser][numItem];
        for (int u = 0; u < numUser; u++) {
            for (int i = 0; i < numItem; i++) {
                double sum = 0.0;
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
                    mRating[u][i] = (float) (sum) / count;
                }
            }
        }
        //System.out.println(sum / count);

        //System.out.println(u + " " + i + " " + mRating.get(u, i));

    }

    public void output(int k) throws FileNotFoundException {
        String dir = "E:\\dataset\\hmusicss.dat";
        PrintWriter out = new PrintWriter(new File(dir));
        int count = 0;
        for (int u = 0; u < numUser; u++) {
            for (int i = 0; i < numItem; i++) {
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        out.println(u + "\t" + i + "\t" + entry.getKey().toString().replace("::", "\t") + Math.round(entry.getValue()));
                    }
                }
            }
        }
        System.out.print(count);
        out.close();
//        dir = "D:\\dataset\\hmusic.testset" + k;
//        out = new PrintWriter(new File(dir));
//        for (int i = 0; i < testingSet.size(); i++) {
//            int user_id = testingSet.get(i).userID;
//            for (Map.Entry<Integer, ItemScorePair> entry : testingSet.get(i).pairs.entrySet()) {
//                int item_id = entry.getValue().itemID;
//                double realRating = entry.getValue().score;
//                out.println(user_id + " " + item_id + " " + testingSet.get(i).context.substring(0, 1) + " " + Math.round(entry.getValue().score));
//            }
//        }
//        out.close();
    }

    public void processData() throws SQLException, ParseException, ClassNotFoundException {
        this.connect();
        this.readUser();
        this.readItem();
        this.readEmotion();
        this.readRating();
//        this.split();

    }

    public static void main(String[] args) {
        HMusic h = new HMusic();
        try {
            h.processData();
            h.output(1);
        } catch (Exception e) {
        }
    }
}
