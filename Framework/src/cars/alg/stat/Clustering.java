/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.stat;

import cars.data.structure.Dataset;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TestUtils;

/**
 *
 * @author Hiep
 */
public class Clustering {

    private List<Double> convertToList(double[] x) {
        List<Double> lst = new ArrayList<Double>();
        for (int i = 0; i < x.length; i++) {
            lst.add(x[i]);
        }
        return lst;
    }

    private boolean matchesCluster1(double[] x, double[] c1, double[] c2) {
        double dist1 = euclidean(x, c1);
        double dist2 = euclidean(x, c2);
        return dist1 < dist2;
    }

    public double[] convert(List<Double> vector) {
        double[] v = new double[vector.size()];
        for (int i = 0; i < vector.size(); i++) {
            v[i] = vector.get(i);
        }
        return v;
    }

    public Sample convertToSample(List<Double> vector) {
        Sample s = new Sample((short) 1, convert(vector));
        return s;
    }

    public List<Sample> convertToListSample(List<List<Double>> data) {
        List<Sample> result = new ArrayList<Sample>();
        for (int i = 0; i < data.size(); i++) {
            result.add(convertToSample(data.get(i)));
        }
        return result;
    }

    public List<Double> meanVector(List<List<Double>> data) {
        List<Double> mean = new ArrayList<Double>();

        int f = data.get(0).size();

        for (int j = 0; j < f; j++) {
            double sum = 0;
            for (int i = 0; i < data.size(); i++) {
                sum += data.get(i).get(j);
            }
            mean.add(sum / data.size());
        }
        return mean;
    }

    public List<List<Double>> meanCenter(double[][] data) {
        List<Double> mean = convertToList(computeMean(data));
        List<List<Double>> oneCenter = new ArrayList<List<Double>>();
        oneCenter.add(mean);
        return oneCenter;
    }

    public boolean checkCenters(List<List<Double>> centers1, List<List<Double>> centers2, int k) {
        for (int i = 0; i < k; i++) {
            if (!checkList(centers1.get(i), centers2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean checkList(List<Double> a, List<Double> b) {
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) != b.get(i)) {
                return false;
            }
        }
        return true;
    }

    public List<Double> computeCenter(List<List<Double>> x) {
        if (!x.isEmpty()) {
            List<Double> center = new ArrayList<Double>();
            for (int i = 0; i < x.get(0).size(); i++) {
                double sum = 0;
                for (int j = 0; j < x.size(); j++) {
                    sum += x.get(j).get(i);
                }
                center.add(sum / x.size());
            }
            return center;
        }
        return new ArrayList<Double>();
    }

    public double distance(double[] a, double[] b) {
        double dis = 0;
        for (int i = 0; i < a.length; i++) {
            dis += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(dis);
    }

    public ClustersData kmeansCluster(double[][] data, List<List<Double>> centers) {
        //initialize new Kmeans Clustering
        Clustering clustering = new Clustering();
        List<List<Double>> centers2 = new ArrayList<List<Double>>();
        List<List<List<Double>>> cluster = new ArrayList<List<List<Double>>>();
        List<List<Integer>> k_Cluster_label = new ArrayList<List<Integer>>();

        //Identify the number of Clusters
        int k = centers.size();


        do {
            cluster = new ArrayList<List<List<Double>>>();
            k_Cluster_label = new ArrayList<List<Integer>>();
            for (int i = 0; i < k; i++) {
                cluster.add(new ArrayList<List<Double>>());
                k_Cluster_label.add(new ArrayList<Integer>());
            }

            for (int i = 0; i < data.length; i++) {
                double min = 10000;
                int c = 0;
                for (int j = 0; j < k; j++) {
                    double dis = distance(data[i], convert(centers.get(j)));
                    if (dis < min) {
                        min = dis;
                        c = j;
                    }
                    if (dis == min) {
                        if (cosin(data[i], convert(centers.get(j))) > cosin(data[i], convert(centers.get(c)))) {
                            c = j;
                        }
                    }
                }

                k_Cluster_label.get(c).add(i);
                cluster.get(c).add(convertToList(data[i]));
            }
            centers2 = centers;
            for (int _c = 0; _c < k; _c++) {
                List<Double> _center = computeCenter(cluster.get(_c));
                if (_center.size() > 0) {
                    centers.set(_c, _center);
                }
            }
        } while (checkCenters(centers, centers2, k) == false);

        //assgin results
        ClustersData clustersData = new ClustersData(data, k, centers, cluster, k_Cluster_label);
        return clustersData;
    }

    public ClustersData gmeansCluster(double[][] data, double alpha, int maxClusters) {

        //Cluster with one center
        List<List<Double>> centers = meanCenter(data);
        ClustersData clustersData = new ClustersData();
        int count = 0;


        while (true) {
            //Run Kmeans with only one center
            System.out.println(clustersData.k);
            clustersData = kmeansCluster(data, centers);
            if (clustersData.k >= maxClusters || centers.size() > clustersData.k) {
                break;
            }

            //Check every center
            for (int i = 0; i < clustersData.k; i++) {
                if (clustersData.clusters.get(i).size() > 1) {
                    int len = clustersData.clusters.get(i).get(0).size();

                    double[][] _data = new double[clustersData.clusters.get(i).size()][len];
                    for (int _i = 0; _i < clustersData.clusters.get(i).size(); _i++) {
                        _data[_i] = convert(clustersData.clusters.get(i).get(_i));
                    }
                    double[] co = computeMean(_data);
                    // compute the pca
                    PCA pca = new PCA(len);
                    pca.accumulate(_data);
                    pca.estimate();

                    // starting from the centroid, follow the principal component
                    double[] m = Arithmetics.smul1(pca.getProjection()[0], Math.sqrt(2. * pca.getEigenvalues()[0] / Math.PI));
                    double[] c1 = Arithmetics.vadd1(co, m);
                    double[] c2 = Arithmetics.vsub1(co, m);

                    // run basic 2-means
                    double[] _c1 = new double[c1.length];
                    double[] _c2 = new double[c2.length];
                    int _cnt1 = 0, _cnt2 = 0;

                    double _ch = 1.;
                    while (_ch > 1e-10) {
                        // compute new means
                        for (int _j = 0; _j < _data.length; _j++) {
                            if (matchesCluster1(_data[_j], c1, c2)) {
                                Arithmetics.vadd2(_c1, _data[_j]);
                                _cnt1++;
                            } else {
                                Arithmetics.vadd2(_c2, _data[_j]);
                                _cnt2++;
                            }
                        }

                        // normalize, compute change
                        _ch = 0;
                        for (int j = 0; j < _c1.length; ++j) {
                            _c1[j] /= (double) _cnt1;
                            _c2[j] /= (double) _cnt2;

                            _ch += Math.abs(c1[j] - _c1[j]);
                            _ch += Math.abs(c2[j] - _c2[j]);

                            c1[j] = _c1[j];
                            _c1[j] = 0;
                            c2[j] = _c2[j];
                            _c2[j] = 0;
                        }

                        _cnt1 = _cnt2 = 0;
                    }

                    /* Reduce the dimension of the features by projecting on the
                     * data onto the principal component and apply the distribution
                     * test to this 'flat' version of the cluster.
                     */
                    double[] ps = new double[_data.length];
                    double[] v = Arithmetics.vsub1(c1, c2);
                    double vn = Arithmetics.norm2(v);
                    for (int j = 0; j < ps.length; ++j) {
                        ps[j] = Arithmetics.dotp(_data[j], v) / vn;
                    }

                    if (!DistributionTest.andersonDarlingNormal(ps, alpha)) {
                        centers.remove(i);

                        centers.add(convertToList(c1));
                        centers.add(convertToList(c2));
                    }

                    if (centers.size() >= maxClusters) {
                        break;
                    }
                }
            }

            if (centers.size() == clustersData.k) {
                break;
            }
        }
        //return the clusters with learned k-value
        return clustersData;
    }

    private double euclidean(double[] a, double[] b) {
        double dis = 0;
        for (int i = 0; i < a.length; i++) {
            dis += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(dis);
    }

    private double cosin(double[] a, double[] b) {
        double x = 0, y = 0, z = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0 && b[i] > 0) {
                x = a[i] * b[i];
                y = a[i] * a[i];
                z = b[i] * b[i];
            }
        }
        if (y == 0 || z == 0) {
            return 0;
        } else {
            return (x / Math.sqrt(y * z));
        }
    }

    private double[] computeMean(double[][] _data) {
        int x = _data.length;
        int y = _data[0].length;
        double[] mean = new double[y];



        for (int j = 0; j < y; j++) {
            double sum = 0;
            for (int i = 0; i < x; i++) {
                sum += _data[i][j];
            }
            mean[j] = (sum / x);
        }
        return mean;
    }
}
