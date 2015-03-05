/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.studyadvisor.modeling;

import cars.studyadvisor.modeling.UserVector;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import sun.awt.Symbol;

/**
 *
 * @author Hiep
 */
public class DataProcessor {

    public static String dbUrl = "jdbc:mysql://112.78.2.23:3306/stu45bac_studyadvisor";
    public static String dbClass = "com.mysql.jdbc.Driver";
    Connection con;
    List<Integer> lastContext = new ArrayList<Integer>();
    List<Integer> users = new ArrayList<Integer>();
    List<Integer> items = new ArrayList<Integer>();
    List<Integer> contexts = new ArrayList<Integer>();
    List<RatingEntry> ratings = new ArrayList<RatingEntry>();

    public DataProcessor() throws SQLException, ClassNotFoundException {
        this.connect();
        for (int i = 0; i < 7; i++) {
            contexts.add(i);
        }
        ResultSet r = con.createStatement().executeQuery("select * from quizuit_questions");
        while (r.next()) {
            items.add(r.getInt(1));
        }
    }

    private void connect() throws SQLException, ClassNotFoundException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbUrl, "stu45bac", "w8848XNR1AQ");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

//    public void test(){
//                try {
//            String sql = "select id from quizuit_questions";
//            Statement st = con.createStatement();
//            ResultSet res = st.executeQuery(sql);
//            while (res.next()) {
//                String id = (res.getString(1));
//                Connection con2 = DriverManager.getConnection("jdbc:mysql://localhost:3306/hiep2", "root", "");
//                Statement st2 = con2.createStatement();
//                ResultSet res2=st2.executeQuery("select level from quizuit_questions where id="+id);
//                String level = "0";
//                while (res2.next()){
//                    level = (res2.getString(1));
//                    break;
//                }
//                con2.close();
//                System.out.println("update quizuit_questions set level="+level+" where id ="+id+";");
//                
//            }
//        } catch (Exception e) {
//            System.out.print(e.getMessage());
//        }
//    }
    public Vector getImprovementVector(String userID, boolean time) throws SQLException {
        //time=true -> thoi diem t, time = false; thoi diem 1
        Vector imp = new Vector();
        for (int i = 0; i <= 9; i++) {
            imp.add(i, -1.0);
        }
        String sql = "";
        if (time) {
            sql = "SELECT R.user_id, R.level, COUNT( result ) , SUM( result ) "
                    + " FROM (SELECT r1 . * , r2.level FROM  `quizuit_study_result` r1,  `quizuit_questions` r2 "
                    + " WHERE r2.id = r1.question_id AND  `user_id`=" + userID.trim() + ") R "
                    + " GROUP BY R.user_id, R.level ";
        } else {
            sql = "SELECT R.user_id, R.level, COUNT( result ) , SUM( result ) "
                    + " FROM (SELECT r1 . * , r2.level FROM  `quizuit_study_result` r1,  `quizuit_questions` r2 "
                    + " WHERE r2.id = r1.question_id AND  `user_id`=" + userID.trim() + " and is_labelled IS NOT NULL) R "
                    + " GROUP BY R.user_id, R.level ";
        }
//        System.out.print(sql);
        Statement st = con.createStatement();
        ResultSet res = st.executeQuery(sql);
        while (res.next()) {
            Double level = res.getDouble(2);
            Double percent = res.getDouble(4) / res.getDouble(3);
            int dif = (int) (level * 10);
            imp.add(dif - 1, percent);
        }

        return imp;
    }

    private double getPercentImprovement(Vector impt_1, Vector impt) {
        int size = impt_1.size();
        double sum = 0;
        int count = 0;
        for (int i = 0; i < size; i++) {
            double v1 = (Double) (impt_1.get(i));
            double v2 = (Double) (impt.get(i));
            if (v1 >= 0 && v2 >= 0) {
                sum += (v2 - v1);
                count++;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        return (sum / count);
    }

    public void inferContext() throws SQLException {
        //Lấy DS người dùng
        int contextChange = 0;

        String sql = "select id from quizuit_user where group_id=2";
        Statement st = con.createStatement();
        ResultSet res = st.executeQuery(sql);
        while (res.next()) {
            String userId = (res.getString(1));
            //lấy vector impt(t-1)           
            Vector impt_1 = getImprovementVector(userId, false);
            //lấy vector impt(t)
            Vector impt = getImprovementVector(userId, true);
            double percentImprovement = getPercentImprovement(impt_1, impt);
            if (percentImprovement < -0.25) {
                contextChange = -2;
            }
            if (percentImprovement < 0 && percentImprovement >= -0.25) {
                contextChange = -1;
            }
            if (percentImprovement < 0.25 && percentImprovement >= 0) {
                contextChange = 0;
            }
            if (percentImprovement < 0.5 && percentImprovement >= 0.25) {
                contextChange = 1;
            }
            if (percentImprovement > 5) {
                contextChange = 2;
            }

            Statement s = con.createStatement();
            ResultSet r = s.executeQuery("select last_context_id from model_user_vector where user_id=" + userId);
            int oldContext = 0;
            while (r.next()) {
                oldContext = r.getInt(1);
                break;
            }
            int newContext = oldContext + contextChange;
            if (newContext > 9) {
                newContext = 6;
            }
            if (newContext < 0) {
                newContext = 0;
            }
            lastContext.add(newContext);
            sql = "update quizuit_study_result set is_labelled=" + newContext + " where is_labelled IS NULL and user_id=" + userId;
            con.createStatement().execute(sql);

            users.add(Integer.parseInt(userId));
        }
    }

    public void ratingAcquistion() throws SQLException {
        String sql = "  select user_id,question_id,is_labelled , corrects / times as rating "
                + "from ("
                + "             select  r1.user_id as user_id, "
                + "             r1.question_id as question_id, "
                + "     	r1.is_labelled as is_labelled, "
                + "             ("
                + "                 select count( * ) "
                + "                 from  `quizuit_study_result` r2 "
                + "                 where r2.user_id = r1.user_id "
                + "                 and r2.question_id = r1.question_id "
                + "                 and r2.is_labelled = r1.is_labelled "
                + "             ) as times, "
                + "             ("
                + "                 select count( * ) "
                + "                 from  `quizuit_study_result` r2 "
                + "                 where r2.user_id = r1.user_id "
                + "                 and r2.question_id = r1.question_id "
                + "                 and r2.result =1 "
                + "                 and r2.is_labelled = r1.is_labelled "
                + "             ) as corrects "
                + "             from  `quizuit_study_result` r1 "
                + "             where  is_labelled is not null "
                + "             group by user_id, question_id, is_labelled "
                + "             ) table1 ";

        ResultSet r = con.createStatement().executeQuery(sql);
        while (r.next()) {
            RatingEntry entry = new RatingEntry();
            entry.user_id = r.getInt(1);
            entry.item_id = r.getInt(2);
            entry.context = r.getString(3);
            entry.rating = r.getFloat(4);
            ratings.add(entry);
        }
    }

    public void run() throws SQLException, ClassNotFoundException {
        this.inferContext();
        this.ratingAcquistion();
        con.close();
    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        DataProcessor d = new DataProcessor();
        d.run();
    }
}
