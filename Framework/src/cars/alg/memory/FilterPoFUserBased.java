/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.memory;

/**
 *
 * @author Hiep
 */
public class FilterPoFUserBased extends PostFilterUserBased {
    /*
     * Input
     */

    public FilterPoFUserBased(int numNeighbors) {
        numNeighbor = (numNeighbors);
        name = ("Filter POF with User-based Top K");
    }

    public FilterPoFUserBased() {
        name = ("Filter POF with User-based Top K");
    }

    @Override
    public float prediction(int u, int i, String c) {
        float probability = contextualProbability(u, i, c);
        float predict = 0;
        if (probability >= threshold) {
            predict = usbtk.prediction(u, i);
        } else {
            predict = 0;
        }

//        System.out.println(probability);
        return predict;
    }

    public float prediction2(int u, int i, String c) {
        threshold = (float) 0.1;
        float probability = contextualProbability(u, i, c);
        float predict = 0;
        if (probability >= threshold) {
            predict = usbtk.prediction(u, i);
        } else {
            predict = 0;
        }
        System.out.println(predict);
        return predict;
    }
}
