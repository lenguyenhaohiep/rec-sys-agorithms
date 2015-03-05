/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.model;

import cars.alg.Algorithm;
import cars.evaluation.Estimation;
import cars.alg.MatrixFunc;
import java.util.Date;
import java.util.Random;
import java.util.Vector;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Hiep
 */
public class MFCF extends AlgModel {

    /*
     * Input
     */
    public int numRating;
    public float avgScore;
    public int factor;
    public int maxIteration = 10000;
    public float GAMA = (float) 0.001;
    public float lamda4 = (float) 0.05;
    /*
     * Output
     */
    public float[] biasUser;
    public float[] biasItem;
    public Vector[] userFactorVector;
    public Vector[] itemFactorVector;

    public MFCF(int factor) {
        this.name = "SVD";
        this.factor = factor;
    }

    public void initialize() {
        int count = 0;
        if (avgScore == 0) {
            numRating = 0;
            for (int u = 0; u < dataset.numUser; u++) {
                for (int i = 0; i < dataset.numItem; i++) {
                    if (dataset.mRating[u][i] > 0) {
                        count++;
                        avgScore += dataset.mRating[u][i];
                    }
                }
            }
            avgScore = avgScore / count;
            numRating = count;
        }

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
        predictedRating = avgScore + biasUser[u] + biasItem[i] + MatrixFunc.dot(Pu, Qi);
        return predictedRating;
    }

    /*
     * Calculate bias
     */
    public void baselinePredictorBias() {
        float lamda2 = (float) numRating / dataset.numItem;
        float lamda3 = (float) numRating / dataset.numUser;
        biasItem = new float[dataset.numItem];
        biasUser = new float[dataset.numUser];

        //Calculate bias for Item;
        for (int i = 0; i < dataset.numItem; i++) {
            biasItem[i] = 0;
            int numRatedItem = 0;
            float sumOfErrors = 0;
            for (int u = 0; u < dataset.numUser; u++) {
                float ratedValue = dataset.mRating[u][i];
                if (ratedValue > 0) {
                    numRatedItem++;
                    sumOfErrors += ratedValue - avgScore;
                }
            }
            biasItem[i] = (float) sumOfErrors / (lamda2 + numRatedItem);
        }

        //Calculate bias for User;
        for (int u = 0; u < dataset.numUser; u++) {
            biasUser[u] = 0;
            int numRatedUser = 0;
            float sumOfErrors = 0;
            for (int i = 0; i < dataset.numItem; i++) {
                float ratedValue = dataset.mRating[u][i];
                if (ratedValue > 0) {
                    numRatedUser++;
                    sumOfErrors += ratedValue - avgScore - biasItem[i];
                }
            }
            biasUser[u] = (float) sumOfErrors / (lamda3 + numRatedUser);
        }
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
                        biasUser[u] = biasUser[u] + GAMA * (error - lamda4 * biasUser[u]);
                        biasItem[i] = biasItem[i] + GAMA * (error - lamda4 * biasItem[i]);
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
                                + lamda4 * (Math.pow(biasItem[i], 2)
                                + Math.pow(biasUser[u], 2)
                                + Math.pow(MatrixFunc.module(itemFactorVector[i]), 2)
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
        baselinePredictorBias();
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
