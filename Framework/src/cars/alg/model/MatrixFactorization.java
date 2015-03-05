/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.model;

import cars.alg.MatrixFunc;
import cars.evaluation.Estimation;
import java.util.Date;
import java.util.Vector;

/**
 *
 * @author Hiep
 */
public class MatrixFactorization extends AlgModel {

    /*
     * Input
     */
    public int numRating;
    public int factor;
    public int maxIteration = 30;
    public float GAMA = (float) 0.001;
    public float lamda4 = (float) 0.05;
    /*
     * Output
     */
    public Vector[] userFactorVector;
    public Vector[] itemFactorVector;

    public MatrixFactorization(int factor) {
        this.name = "SVD";
        this.factor = factor;
    }

    public void initialize() {
        int count = 0;
        numRating = 0;
        for (int u = 0; u < dataset.numUser; u++) {
            for (int i = 0; i < dataset.numItem; i++) {
                if (dataset.mRating[u][i] > 0) {
                    count++;
                }
            }
        }
        numRating = count;
    }

    /*
     * Initialize Factor vector for all users and items
     */
    public void initializeFactorVectors() {
        userFactorVector = new Vector[dataset.numUser];
        itemFactorVector = new Vector[dataset.numItem];
        Vector initialVector = new Vector();
        for (int f = 0; f < factor; f++) {
            initialVector.add(f, (float) (f * 0.009 + 0.01));
        }

        for (int i = 0; i < dataset.numItem; i++) {
            itemFactorVector[i] = initialVector;
        }
        for (int u = 0; u < dataset.numUser; u++) {
            userFactorVector[u] = initialVector;
        }
    }

    /*
     * Rating score is predicted by MFCF
     */
    @Override
    public float prediction(int u, int i) {
        float[] Pu = MatrixFunc.fromVector(userFactorVector[u]);
        float[] Qi = MatrixFunc.fromVector(itemFactorVector[i]);
        float predictedRating = (float) 0.0;
        predictedRating = MatrixFunc.dot(Pu, Qi);
        return predictedRating;
    }

    /*
     * Learn Model
     */
    public void SVD() {
        int iterator = 0;
        double epsilon = 0.000001;
        double oldSumErrors = 0;
        double sumErrors = 0;

        while (iterator < maxIteration) {
            iterator++;
            for (int u = 0; u < dataset.numUser; u++) {
                for (int i = 0; i < dataset.numItem; i++) {
                    float ratedValue = dataset.mRating[u][i];
                    if (ratedValue > 0) {
                        float error = ratedValue - prediction(u, i);
                        Vector Pu = userFactorVector[u];
                        Vector Qi = itemFactorVector[i];
                        Vector _Pu = MatrixFunc.VsV(MatrixFunc.NxV(error, Qi), MatrixFunc.NxV(lamda4, Pu));
                        Vector _Qi = MatrixFunc.VsV(MatrixFunc.NxV(error, Pu), MatrixFunc.NxV(lamda4, Qi));
                        itemFactorVector[i] = MatrixFunc.VpV(Qi, MatrixFunc.NxV(GAMA, _Qi));
                        userFactorVector[u] = MatrixFunc.VpV(Pu, MatrixFunc.NxV(GAMA, _Pu));
                    }
                }


            }

            //Compute sum of errors
            double minf = 0;
            for (int u = 0; u < dataset.numUser; u++) {
                for (int i = 0; i < dataset.numItem; i++) {
                    float ratedValue = dataset.mRating[u][i];
                    if (ratedValue > 0) {
                        minf +=
                                Math.pow(ratedValue - this.prediction(u, i), 2)
                                + lamda4 * (Math.pow(MatrixFunc.module(itemFactorVector[i]), 2)
                                + Math.pow(MatrixFunc.module(userFactorVector[u]), 2));
                    }
                }
            }
            //Check to stop Iterator
            oldSumErrors = sumErrors;
            sumErrors = minf;
            if (Math.abs(sumErrors - oldSumErrors) < epsilon) {
                break;
            }
        }
    }

    public void run() {
        Date t1 = new Date();
        initialize();
        initializeFactorVectors();
        SVD();
        Date t2 = new Date();
        evaluator.training_Time = (t2.getTime() - t1.getTime()) / 1000;
    }

    @Override
    public void runEvaluation() {
        Estimation.EvaluateErrors(this);
        System.out.println(this.name);
        evaluator.print();
    }
}
