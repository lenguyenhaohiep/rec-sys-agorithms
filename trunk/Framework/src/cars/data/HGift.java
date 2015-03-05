/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.data.structure.TestInput;
import cars.data.structure.Rating;
import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class HGift extends Dataset {

    List<String> users = new ArrayList<String>();
    List<String> items = new ArrayList<String>();
    List<String> who = new ArrayList<String>();
    List<String> age = new ArrayList<String>();
    List<String> gender = new ArrayList<String>();
    List<String> why = new ArrayList<String>();
    double[][][][] mR;
    String dbUrl = "jdbc:mysql://localhost:3306/data";
    String dbClass = "com.mysql.jdbc.Driver";
    Connection con;
    public List<List<cars.data.structure.DataInput>> mFold = new ArrayList<List<cars.data.structure.DataInput>>();
    public List<List<cars.data.structure.DataInput>> mRatingByUser = new ArrayList<List<cars.data.structure.DataInput>>();

    public HGift() {
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
            String sql = "select distinct(username) from hgift_rating";
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
            String sql = "select distinct(iid) from hgift_rating";
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                items.add(res.getString(1));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private List<String> readContext(String context_condition) {
        List<String> contextList = new ArrayList<String>();
        try {
            String sql = "select distinct(" + context_condition + ") from hgift_rating";
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                contextList.add(res.getString(1));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        return contextList;
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
            String sql = "select * from hgift_rating";
            st = con.createStatement();
            res = st.executeQuery(sql);
            while (res.next()) {
                int iUser = users.indexOf(res.getString("username"));
                int iItem = items.indexOf(res.getString("iid"));
                int iAge = age.indexOf(res.getString("age"));
                int iWho = who.indexOf(res.getString("who"));
                int iGender = gender.indexOf(res.getString("gender"));
                int iWhy = why.indexOf(res.getString("why"));
                int score = res.getInt("score");

                if (mRatingWithContext[iUser][iItem] == null) {
                    mRatingWithContext[iUser][iItem] = new Rating();
                }
                mRatingWithContext[iUser][iItem].setValueRating(score, new int[]{iWho, iGender, iAge, iWhy});
                // mRatingWithContext[iUser][iItem].setValueRating(score, new int[]{iWho, iAge, iWhy});
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
        while (((double) numtestset / numRating) < 0.2) {
            for (int u = 0; u < numUser; u++) {
                boolean check = false;
                int count2 = 0;
                while (check == false) {
                    Random r = new Random();
                    count2++;
                    int iAge = r.nextInt(4);
                    int iWho = r.nextInt(6);
                    int iGender = r.nextInt(2);
                    int iWhy = r.nextInt(8);
                    String context = iWho + "::" + iGender + "::" + iAge + "::" + iWhy + "::";
                    //String context = iWho + "::" + iAge + "::" + iWhy + "::";
                    if (count2 == 4 * 6 * 2 * 8) {
                        check = true;
                    }

                    int count = 0;
                    for (int i = 0; i < numItem; i++) {
                        if (mRatingWithContext[u][i] != null) {
                            if (mRatingWithContext[u][i].contextScorePairs.containsKey(context)) {
                                if (check(i, context)) {
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

                                    check = true;
                                    if (count == 2) {
                                        break;
                                    }

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

    public void processData() throws SQLException, ParseException, ClassNotFoundException {
        this.connect();
        this.readUser();
        this.readItem();
        age = this.readContext("age");
        why = this.readContext("why");
        who = this.readContext("who");
        gender = this.readContext("gender");
        this.readRating();
//        this.split();
//        this.splitTestingSet();
    }

    public static void main(String[] args) {
        HGift h = new HGift();
        try {
            h.connect();
            h.processData();
            h.output(1);
            //6,2,4,8
//            int a = 0;
//            h.removeData();
        } catch (Exception e) {
        }
    }

    public void removeData() throws SQLException {
        Statement st;
        ResultSet res = null;


        String sql = "select distinct concat(who,gender,age,why) from hgift_rating";
        st = con.createStatement();
        res = st.executeQuery(sql);

        while (res.next()) {
            String context = res.getString(1);
            for (int i = 0; i < users.size(); i++) {
                String sql2 = "select r.*,concat(who,gender,age,why) as context from hgift_rating r where username='" + users.get(i) + "'";
//                System.out.println(sql2);

                Statement st2 = con.createStatement();
                ResultSet re = st2.executeQuery(sql2);
                List<Integer> ids = new ArrayList<Integer>();

                int k = 0;
                while (re.next()) {
//                    System.out.println(re.getString("id") + "=====" + re.getString("context") + "=====" + context);
                    if (re.getString("context").equals(context)) {
                        if (!ids.contains(re.getInt("id"))) {
                            ids.add(re.getInt("id"));
                        }
                        k++;
//                        System.out.println(context);
                    }
                }

                System.out.println(k + "------------------------");

                int count = 0;
                if (k == 20) {
                    int a = 0;
                }
                List<Integer> rev = new ArrayList<Integer>();
                while (ids.size() - count > 10) {
                    Random r = new Random();
                    int i_ = r.nextInt(ids.size());
                    if (!rev.contains(ids.get(i_))) {
                        rev.add(ids.get(i_));
                        count++;
                    }
                }
                for (int j = 0; j < rev.size(); j++) {
                    String sql3 = "delete from hgift_rating where id=" + rev.get(j);
//                    System.out.println(sql3);
                    boolean rs3 = con.createStatement().execute(sql3);
                }
            }
        }

    }

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
        public void output(int k) throws FileNotFoundException {
        String dir = "E:\\dataset\\hgift.datasetNO" + k;
        PrintWriter out = new PrintWriter(new File(dir));
        int count = 0;
        for (int u = 0; u < numUser; u++) {
            for (int i = 0; i < numItem; i++) {
                if (mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        out.println(u + "\t" + i + "\t" + entry.getKey().toString().replace("::", "\t" ) + Math.round(entry.getValue()));
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
}
