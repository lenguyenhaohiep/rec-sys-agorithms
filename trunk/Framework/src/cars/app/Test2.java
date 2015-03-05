/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.app;

import cars.alg.ContextProcessor;
import cars.alg.ContextProcessor2;
import cars.alg.model.FilterPoF;
import cars.alg.model.MFCACF;
import cars.alg.model.Prefilter;
import cars.alg.model.MFCF;
import cars.alg.model.WeightPoF;
import cars.data.AIST;
import cars.data.Comoda;
import cars.data.DataManager;
import cars.data.Food;
import cars.data.HGift;
import cars.data.HMusic;
import cars.data.Movielens;
import cars.data.structure.Dataset;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.alg.HamiltonianCycle;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 *
 * @author Hiep
 */
public class Test2 {

    public static void main(String[] arg) {
        ContextProcessor2 c = new ContextProcessor2();
//
////        Kiểm định dữ liệu Food
//        DataManager dm = new DataManager();
//        String path = "E:\\dataset\\food.dat";
//        dm.dataProcessor(path, 3);
//        Dataset Dset = dm.getFullDataset();
//        c.numAttrAtContext = new int[]{3, 2};
//        c.contextMattersCase2(0, Dset, 3, (float) 0.95);
//        c.contextMattersCase2(1, Dset, 3, (float) 0.95);
//        c.contextMattersCase3(0, Dset, 3, (float) 0.95);
//        c.contextMattersCase3(1, Dset, 3, (float) 0.95);
//        
//        Kiểm định dữ liệu HMusic
        DataManager dm1 = new DataManager();
        String path1 = "C:\\Users\\Hiep\\Dropbox\\Recommender_Systems\\dataset_full\\hmusic.l3";
        dm1.dataProcessor(path1, 3);
        Dataset Dset1 = dm1.getFullDataset();
        c.numAttrAtContext = new int[]{7,5};
        c.contextMattersCase2(0, Dset1, 3, (float) 0.95);
        c.contextMattersCase2(1, Dset1, 3, (float) 0.95);
        c.contextMattersCase3(0, Dset1, 3, (float) 0.95);
        c.contextMattersCase3(1, Dset1, 3, (float) 0.95);
//        
////        Kiểm định dữ liệu HGift
//        DataManager dm2 = new DataManager();
//        String path2 = "E:\\dataset\\hgift.dat";
//        dm2.dataProcessor(path2, 3);
//        Dataset Dset2 = dm2.getFullDataset();
//        c.numAttrAtContext = new int[]{6, 2, 4, 8};
//        c.contextMattersCase2(0, Dset2, 3, (float) 0.95);
//        c.contextMattersCase2(1, Dset2, 3, (float) 0.95);
//        c.contextMattersCase2(2, Dset2, 3, (float) 0.95);
//        c.contextMattersCase2(3, Dset2, 3, (float) 0.95);
//        
//        c.contextMattersCase3(0, Dset2, 3, (float) 0.95);
//        c.contextMattersCase3(1, Dset2, 3, (float) 0.95);
//        c.contextMattersCase3(2, Dset2, 3, (float) 0.95);
//        c.contextMattersCase3(3, Dset2, 3, (float) 0.95);
//
////        Kiểm định dữ liệu Movielens
//        DataManager dm3 = new DataManager();
//        String path3 = "E:\\dataset\\movielens.dat";
//        dm3.dataProcessor(path3, 3);
//        Dataset Dset3 = dm3.getFullDataset();
//        c.numAttrAtContext = new int[]{2, 3};
//        c.contextMattersCase2(0, Dset3, 3, (float) 0.95);
//        c.contextMattersCase2(1, Dset3, 3, (float) 0.95);
//        c.contextMattersCase3(0, Dset3, 3, (float) 0.95);
//        c.contextMattersCase3(1, Dset3, 3, (float) 0.95);
//        
//        //Kiểm định dữ liệu Comoda
//        DataManager dm4 = new DataManager();
//        String path4 = "C:\\Users\\Hiep\\Dropbox\\Recommender_Systems\\dataset_full\\comoda.dat";
//        dm4.dataProcessor(path4, 3);
//        Dataset Dset4 = dm4.getFullDataset();
//        c.numAttrAtContext = new int[]{4, 3, 4, 3, 5, 7, 7, 7, 3, 2, 2, 2};
//        c.contextMattersCase2(0, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(1, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(2, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(3, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(4, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(5, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(6, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(7, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(8, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(9, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(10, Dset4, 3, (float) 0.95);
//        c.contextMattersCase2(11, Dset4, 3, (float) 0.95);
//        
//        c.contextMattersCase3(0, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(1, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(2, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(3, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(4, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(5, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(6, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(7, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(8, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(9, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(10, Dset4, 3, (float) 0.95);
//        c.contextMattersCase3(11, Dset4, 3, (float) 0.95);

    }
}
