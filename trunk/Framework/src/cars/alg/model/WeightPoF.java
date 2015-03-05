/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.model;

/**
 *
 * @author Hiep
 */
public class WeightPoF extends PostFilter {

    public WeightPoF() {
        this.name = "Weight POF with SVD";
    }

    @Override
    public float prediction(int u, int i, String c) {
        //double predict = svd.ratingSVDPrediction(u, i);
        //System.out.print("user-item-context:" + u + "," + i + "," + c + ":");
        float probability = contextualProbability(u, i, c);
        float predict = svd.prediction(u, i);
        return predict * probability;
    }
}
