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
public class KDD extends Dataset {

    List<List<DataInput>> mFold = new ArrayList<List<DataInput>>();
    List<String> users = new ArrayList<String>();
    List<String> items = new ArrayList<String>();
    List<String> contexts = new ArrayList<String>();

    public void readData(String path1, String path2) {
        try {
            Scanner scanner = new Scanner(new File(path1));
            String dir = path2;
            PrintWriter out = new PrintWriter(new File(dir));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.contains("Row")) {
                } else {
                    try {
                        String[] elements = line.split("\t");
                        String userid = elements[1].trim();
                        String itemid = elements[17].trim();
                        String context = elements[4].trim();
                        String CFA = elements[13].trim();

                        if (!users.contains(userid)) {
                            users.add(userid);
                        }
                        if (!items.contains(itemid)) {
                            items.add(itemid);
                        }

                        if (!contexts.contains(context)) {
                            contexts.add(context);
                        }
                        System.out.println(context);

                        out.println(users.indexOf(userid) + "\t" + items.indexOf(itemid) + "\t" +contexts.indexOf(context) + "\t" + CFA);
                    } catch (Exception ex) {
                    }
                }
            }
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Food.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("context = " + contexts.size());
        System.out.println("user = " + users.size());
        System.out.println("item = " + items.size());
    }

    public static void main(String arg[]) {
        KDD kdd = new KDD();
        kdd.readData("D:\\algebra_2005_2006\\algebra_2005_2006_train.txt", "D:\\algebra_train.txt");
        kdd.readData("D:\\algebra_2005_2006\\algebra_2005_2006_master.txt", "D:\\algebra_test.txt");
    }
}
