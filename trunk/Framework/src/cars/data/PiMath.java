/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.data.structure.Dataset;
import cars.data.structure.ItemScorePair;
import cars.data.structure.Rating;
import cars.data.structure.TestInput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Hiep
 */
public class PiMath extends Dataset {

    List<String> users = new ArrayList<String>();
    List<String> items = new ArrayList<String>();
    List<String> topics = new ArrayList<String>();
    double[][][] matrix;
    String dbUrl = "jdbc:mysql://localhost:3306/pimath";
    String dbClass = "com.mysql.jdbc.Driver";
    Connection con;
    public List<List<cars.data.structure.DataInput>> mFold = new ArrayList<List<cars.data.structure.DataInput>>();
    public List<List<cars.data.structure.DataInput>> mRatingByUser = new ArrayList<List<cars.data.structure.DataInput>>();

    public PiMath() {
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
            String sql = "select distinct(agentid) from sam_itemgrading_t where `AUTOSCORE` is not null";
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
            String sql = "select distinct(publisheditemid) from sam_itemgrading_t where `AUTOSCORE` is not null";
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                items.add(res.getString(1));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void readTopic() {
        try {
            String sql = "SELECT distinct(sectionid) FROM sam_publisheditem_t, sam_itemgrading_t where sam_publisheditem_t.itemid=  sam_itemgrading_t.PUBLISHEDITEMID and autoscore IS NOT NULL";
            Statement st = con.createStatement();
            ResultSet res = st.executeQuery(sql);
            while (res.next()) {
                topics.add(res.getString(1));
            }
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    private void readRating() throws FileNotFoundException {
        String dir = "E:\\dataset\\pimath.dat";
        PrintWriter out = new PrintWriter(new File(dir));
        numRating = 0;
        Statement st;
        ResultSet res = null;
        try {
            numItem = items.size();
            numUser = users.size();
            //matrix = new double[topics.size()][users.size()][items.size()];
            String sql = "SELECT sectionid, agentid, itemid, autoscore FROM sam_publisheditem_t, sam_itemgrading_t where sam_publisheditem_t.itemid=  sam_itemgrading_t.PUBLISHEDITEMID and autoscore IS NOT NULL ORDER BY SECTIONID,AGENTID,ITEMID";
            st = con.createStatement();
            res = st.executeQuery(sql);
            while (res.next()) {
                int iUser = users.indexOf(res.getString("agentid"));
                int iItem = items.indexOf(res.getString("itemid"));
                int iTopic = topics.indexOf(res.getString("sectionid"));
                int score = res.getInt("autoscore");
                int rating;
                if (score > 0) {
                    rating = 1;
                } else {
                    rating = 0;
                }
                //matrix[iTopic][iUser][iItem] = rating;
                out.println(iTopic + "\t" + iUser + "\t" + iItem + "\t" + rating);
            }
        } catch (Exception e) {
        }
        out.close();
    }

    public void output() {
        for (int t = 0; t < topics.size(); t++) {
            for (int u = 0; u < users.size(); u++) {
                for (int i = 0; i < items.size(); i++) {
                    System.out.println(t + "\t" + u + "\t" + i + "\t" + matrix[t][u][i]);
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        PiMath pimath = new PiMath();
        try {
            pimath.connect();
            pimath.readItem();
            pimath.readUser();
            pimath.readTopic();
            pimath.readRating();
//            pimath.output();
        } catch (Exception ex) {
        }
    }
}
