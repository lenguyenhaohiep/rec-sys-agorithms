/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.stat;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hiep
 */
public class ClustersData {
    
    public int k;
    public double[][] data;
    public List<List<List<Double>>> clusters = new ArrayList<List<List<Double>>>();
    public List<List<Integer>> k_Cluster_label = new ArrayList<List<Integer>>();
    public List<List<Double>> centers = new ArrayList<List<Double>>();
    
    public ClustersData(double[][] data, int k, List<List<Double>> centers, List<List<List<Double>>> cluster, List<List<Integer>> k_Cluster_label) {
        this.data = data;
        this.k = k;
        this.centers.addAll(centers);
        this.clusters.addAll(cluster);
        this.k_Cluster_label.addAll(k_Cluster_label);
    }
    
    public ClustersData() {
        
    }
}
