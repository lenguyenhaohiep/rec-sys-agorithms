/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.memory;

/**
 *
 * @author Hiep
 */
public class WeightPoFUserBased extends PostFilterUserBased {

    public WeightPoFUserBased(int numNeighbors) {
        this.numNeighbor = numNeighbors;
        this.name = "Weight POF with User-based Top K";
    }

    public WeightPoFUserBased() {
        this.name = "Weight POF with User-based Top K";
    }

    @Override
    public float prediction(int u, int i, String c) {
        //float predict = svd.ratingSVDPrediction(u, i);
        //System.out.print("user-item-context:" + u + "," + i + "," + c + ":");
//        usbtk.numNeighbor = this.numNeighbor;
//        usbtk.dataset = this.dataset;
//        usbtk.neighbors(u);
        float predict = usbtk.prediction(u, i);
        float p = contextualProbability(u, i, c);
        return predict * p;
    }

    public double prediction2(int u, int i, String c) {
        float predict = usbtk.prediction(u, i);
        float p = contextualProbability(u, i, c);
        System.out.println(p*predict);
        return predict * p;
    }
}
