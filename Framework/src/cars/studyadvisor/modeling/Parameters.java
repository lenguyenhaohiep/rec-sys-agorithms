/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.studyadvisor.modeling;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hiep
 */
public class Parameters {

    Connection con;
    public List<UserVector> list_Users = new ArrayList<UserVector>();
    public List<ItemVector> list_Items = new ArrayList<ItemVector>();

    private void connect() throws SQLException, ClassNotFoundException {
        try {
            String dbUrl = DataProcessor.dbUrl;
            String dbClass = DataProcessor.dbClass;
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(dbUrl, "stu45bac", "w8848XNR1AQ");
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
    }

    public void updateToDatabase() throws SQLException, ClassNotFoundException {
        connect();
        con.createStatement().execute("truncate table model_user_vector");
        con.createStatement().execute("truncate table model_question_vector");
        //update model_user_vector
        for (int i = 0; i < list_Users.size(); i++) {
            UserVector u = list_Users.get(i);
            Gson gson = new Gson();
            String jData = gson.toJson(u.vector);
            String sql = "insert into model_user_vector (user_id,vector,last_context_id,bias) values(" + u.user_id + ",'" + jData + "'," + u.last_context_id + "," + u.bias + ")";
            System.out.println(sql);
            con.createStatement().execute(sql);
        }
        //update model_item_vector
        System.out.println(list_Items.size());
        for (int i = 0; i < list_Items.size(); i++) {
            ItemVector item = list_Items.get(i);
            Gson gson = new Gson();
            String jData = gson.toJson(item.vector);
            String sql = "insert into model_question_vector (question_id,vector,context_id,bias) values(" + item.question_id + ",'" + jData + "'," + item.context_id + "," + item.bias + ")";
//            System.out.println(sql);
            con.createStatement().execute(sql);
            System.out.println(i);
        }
        con.close();
    }
}
