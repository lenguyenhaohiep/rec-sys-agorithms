/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.data.DataManager;
import cars.data.structure.Dataset;
import cars.data.structure.Rating;
import java.sql.Time;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Hiep
 */
public class Test3 {

    public static void main(String[] arg) {
        DataManager dm = new DataManager();
        dm.dataProcessor("E:\\dataset\\Movielens.dat", 3);
        Dataset data = dm.getDataset(0);
        Map<String, Rating> map = dm.getDatasetMap(0);
        Date t1 = new Date();
        for (int u = 0; u < data.numUser; u++) {
            for (int i = 0; i < data.numItem; i++) {
                if (data.mRatingWithContext[u][i] != null) {
                }
            }
        }
        Date t2 = new Date();
        for (int u = 0; u < data.numUser; u++) {
            for (int i = 0; i < data.numItem; i++) {
                if (map.containsKey(u + "-" + i)) {
                }
            }
        }

        Date t3 = new Date();
        System.out.println((t2.getTime() - t1.getTime()));
        System.out.println((t3.getTime() - t2.getTime()));
    }
}
