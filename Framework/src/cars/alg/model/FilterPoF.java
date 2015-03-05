/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.model;

/**
 *
 * @author Hiep
 */
public class FilterPoF extends PostFilter {

    public float threshold = (float) 0.1;

    public FilterPoF() {
        this.name = "Filter POF with SVD";
    }

    @Override
    public float prediction(int u, int i, String c) {
        //double predict = svd.ratingSVDPrediction(u, i);
        //System.out.print("user-item-context:" + u + "," + i + "," + c + ":");
        float probability = contextualProbability(u, i, c);
        float predict = 0;
        if (probability >= threshold) {
            predict = svd.prediction(u, i);
        } else {
            predict = 0;
        }
        return predict;
    }
}
