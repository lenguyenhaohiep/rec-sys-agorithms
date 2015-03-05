/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg.model;

import cars.alg.Algorithm;
import cars.alg.MatrixFunc;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.ejml.simple.SimpleMatrix;
import sun.awt.SunToolkit;

/**
 *
 * @author Hiep
 */
public class MFCACF extends AlgModel {
    /*
     * Input
     */

    public float lamda = (float) 0.05;
    public float gama = (float) 0.001;
    public int maxIteration = 10000;
    public int numRating;
    public int factor;
    private int numContextDimension;
    /*
     * output
     */
    public float[] avgScore;
    public float[] biasUser;
    public float[][][] biasItemContext;
    public Vector[] itemFactorVector;
    public Vector[] userFactorVector;
    //số lượng thay đổi trong tham số baseline
    int[] numQi;
    int[] numPu;
    int[] numBu;
    int[][][] numBicj;

    public MFCACF() {
        this.name = "Context Modelling with Matrix Factorization";
    }

    private void initializeLearningRate() {
        numBu = new int[dataset.numUser];
        numPu = new int[dataset.numUser];

        //Tính số lượng thay đổi cho từng tham số baseline
        for (int u = 0; u < dataset.numUser; u++) {
            for (int i = 0; i < dataset.numItem; i++) {
                if (dataset.mRatingWithContext[u][i] != null) {
                    numBu[u] += dataset.mRatingWithContext[u][i].contextScorePairs.size();
                    numPu[u] += dataset.mRatingWithContext[u][i].contextScorePairs.size();
                }
            }

        }

        //Tính số lượng cho tham số baseline qi
        numQi = new int[dataset.numItem];
        for (int i = 0; i < dataset.numItem; i++) {
            for (int u = 0; u < dataset.numUser; u++) {
                if (dataset.mRatingWithContext[u][i] != null) {
                    numQi[i] += dataset.mRatingWithContext[u][i].contextScorePairs.size();
                }
            }
        }

        //Khởi tạo cho tham số bicj
        numBicj = new int[dataset.numItem][numContextDimension][];
        for (int i = 0; i < dataset.numItem; i++) {
            for (int iContext = 0; iContext < contextProcessor.hierachy.size(); iContext++) {
                numBicj[i][iContext] = new int[contextProcessor.hierachy.get(iContext).size()];
            }
        }
        for (int i = 0; i < dataset.numItem; i++) {
            for (int u = 0; u < dataset.numUser; u++) {
                if (dataset.mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(entry.getKey());
                        for (int iContext = 0; iContext < arrayContext.length; iContext++) {
                            numBicj[i][iContext][arrayContext[iContext]]++;
                        }
                    }
                }
            }
        }
    }

    private void initialize() {
        numContextDimension = contextProcessor.hierachy.size();
        dataset.mRating = new float[dataset.numUser][dataset.numItem];
        avgScore = new float[dataset.numItem];
        int count = 0;
        float sumAll = 0;
        for (int i = 0; i < dataset.numItem; i++) {
            int sum = 0;
            int _count = 0;
            for (int u = 0; u < dataset.numUser; u++) {
                if (dataset.mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        dataset.mRating[u][i] = entry.getValue();
                        count++;
                        _count++;
                        sum += entry.getValue();
                        sumAll += entry.getValue();
                    }
                }

            }
            if (_count == 0) {
                avgScore[i] = 0;
            } else {
                avgScore[i] = (float) sum / _count;

            }
        }
        for (int i = 0; i < dataset.numItem; i++) {
            if (avgScore[i] == 0) {
                avgScore[i] = sumAll / count;
//                System.out.println(avgScore[i]);
            }
        }
        numRating = count;
//        System.out.println(sumAll / count);
    }

    public void initializeFactorVectors() {
        userFactorVector = new Vector[dataset.numUser];
        itemFactorVector = new Vector[dataset.numItem];
        Vector initialVector = new Vector();
        for (int f = 0; f < factor; f++) {
            Random r = new Random();
            //initialVector.add(f, (double) r.nextInt(10) / 1000);
            initialVector.add(f, (float) (f * 0.009 + 0.01));
        }

        for (int i = 0; i < dataset.numItem; i++) {
            itemFactorVector[i] = initialVector;
        }
        for (int u = 0; u < dataset.numUser; u++) {
            userFactorVector[u] = initialVector;
        }
    }

    private void baselinePredictorBias() {
        biasUser = new float[dataset.numUser];
        biasItemContext = new float[dataset.numItem][numContextDimension][];


        float lamda3 = (float) numRating / dataset.numUser;
        float lamda2 = (float) numRating / dataset.numItem;


        for (int i = 0; i < dataset.numItem; i++) {
            for (int iContext = 0; iContext < contextProcessor.hierachy.size(); iContext++) {
                biasItemContext[i][iContext] = new float[contextProcessor.hierachy.get(iContext).size()];
                for (int c = 0; c < contextProcessor.hierachy.get(iContext).size(); c++) {
                    int numRatedItem = 0;
                    float sumOfErrors = 0;
                    for (int u = 0; u < dataset.numUser; u++) {
                        if (dataset.mRatingWithContext[u][i] != null) {
                            for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                                int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(entry.getKey());
                                if (arrayContext[iContext] == c) {
                                    float ratedValue = entry.getValue();
                                    if (ratedValue > 0) {
                                        numRatedItem++;
                                        sumOfErrors += ratedValue - avgScore[i];
                                    }
                                }
                            }
                        }
                    }
                    biasItemContext[i][iContext][c] = (float) sumOfErrors / (lamda2 + numRatedItem);
//                    System.out.println(biasItemContext[i][iContext][c]);
                }
            }
        }

        for (int u = 0; u < dataset.numUser; u++) {
            int numRatedUser = 0;
            float sumOfErrors = 0;
            for (int iContext = 0; iContext < contextProcessor.hierachy.size(); iContext++) {
                for (int c = 0; c < contextProcessor.hierachy.get(iContext).size(); c++) {

                    for (int i = 0; i < dataset.numItem; i++) {
                        if (dataset.mRatingWithContext[u][i] != null) {
                            for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                                int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(entry.getKey());
                                if (arrayContext[iContext] == c) {
                                    float ratedValue = entry.getValue();
                                    if (ratedValue > 0) {
                                        numRatedUser++;
                                        sumOfErrors += ratedValue - avgScore[i] - biasItemContext[i][iContext][c];
                                    }
                                }
                            }
                        }
                    }

                }
            }
            biasUser[u] = (float) sumOfErrors / (lamda3 + numRatedUser);
            //System.out.println(biasUser[u]);
        }
        //Calculate bias for User;
//        for (int u = 0; u < numUser; u++) {
//            int numRatedUser = 0;
//            double sumOfErrors = 0;
//            for (int i = 0; i < numItem; i++) {
//                if (mRatingWithContext[u][i] != null) {
//                    for (Map.Entry<String, Double> entry : mRatingWithContext[u][i].contextScorePairs.entrySet()) {
//                        double ratedValue = entry.getValue();
//                        if (ratedValue > 0) {
//                            numRatedUser++;
//                            sumOfErrors += ratedValue - avgScore[i];
//                        }
//                    }
//                }
//            }
//            biasUser[u] = (double) sumOfErrors / (lamda3 + numRatedUser);
//        }
    }

    @Override
    public float prediction(int u, int i, String context) {
        int c = contextProcessor.getIndexOfContextInHeirachy(context);
        float[] Pu = MatrixFunc.fromVector(userFactorVector[u]);
        float[] Qi = MatrixFunc.fromVector(itemFactorVector[i]);
        float predictedRating = (float) 0.0;
        float sumbiasItemContext = 0;
        int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(context);
        for (int iContext = 0; iContext < arrayContext.length; iContext++) {
            sumbiasItemContext += biasItemContext[i][iContext][arrayContext[iContext]];
        }
        predictedRating = avgScore[i] + biasUser[u] + sumbiasItemContext + MatrixFunc.dot(Pu, Qi);
        //System.out.println(u + ":" + i + ":" + c + ":" + predictedRating);
        //return Math.round(predictedRating);
//        if (u == 6 && i == 598) {
//            System.out.println(avgScore[i] + "--" + biasUser[u] + "--" + sumbiasItemContext + "--" + MatrixFunc.dot(Pu, Qi));
//        }
        return predictedRating;
    }

    public void MF() {

        int iterator = 0;
        double oldSumErrors = 0;
        double sumErrors = 0;

        double epsilon = 0.000001;
        while (iterator < maxIteration) {
            iterator++;
//            System.out.println("iterator=" + iterator);

            for (int u = 0; u < dataset.numUser; u++) {
                for (int i = 0; i < dataset.numItem; i++) {
                    if (dataset.mRatingWithContext[u][i] != null) {

                        //tính các tham số
                        if (numBu[u] == 0 || numBu[u] == 1) {
                            numBu[u] = 10;
                            numPu[u] = 10;
                        }
                        if (numQi[i] == 0 || numQi[i] == 1) {
                            numQi[i] = 10;
                        }

                        float GAMAbu = (float) (gama / Math.log(numBu[u]));
                        float GAMAbicj;
                        float GAMApu = (float) (gama / Math.log(numPu[u]));
                        float GAMAqi = (float) (gama / Math.log(numQi[i]));


                        for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            float ratedValue = entry.getValue();
                            if (ratedValue > 0) {
                                float error = ratedValue - prediction(u, i, entry.getKey());
                                biasUser[u] = biasUser[u] + GAMAbu * (error - lamda * biasUser[u]);
                                int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(entry.getKey());
                                for (int iContext = 0; iContext < arrayContext.length; iContext++) {
                                    if (numBicj[i][iContext][arrayContext[iContext]] == 0 || numBicj[i][iContext][arrayContext[iContext]] == 1) {
                                        numBicj[i][iContext][arrayContext[iContext]] = 10;
                                    }
                                    GAMAbicj = (float) (gama / Math.log(numBicj[i][iContext][arrayContext[iContext]]));
                                    if (Double.isInfinite(GAMAbicj)) {
                                        System.out.println(gama);
                                        System.out.println(numBicj[i][iContext][arrayContext[iContext]]);
                                    }
                                    //System.out.println(GAMAbicj);
                                    biasItemContext[i][iContext][arrayContext[iContext]] = biasItemContext[i][iContext][arrayContext[iContext]] + GAMAbicj * (error - lamda * biasItemContext[i][iContext][arrayContext[iContext]]);
                                    //System.out.println("biasItem " + biasItemContext[i][iContext][arrayContext[iContext]]);
                                }
                                Vector Pu = userFactorVector[u];
                                Vector Qi = itemFactorVector[i];
                                Vector _Pu = MatrixFunc.VsV(MatrixFunc.NxV(error, Qi), MatrixFunc.NxV(lamda, Pu));
                                Vector _Qi = MatrixFunc.VsV(MatrixFunc.NxV(error, Pu), MatrixFunc.NxV(lamda, Qi));
                                itemFactorVector[i] = MatrixFunc.VpV(Qi, MatrixFunc.NxV(GAMAqi, _Qi));
                                userFactorVector[u] = MatrixFunc.VpV(Pu, MatrixFunc.NxV(GAMApu, _Pu));
                            }

                        }
                    }
                }
            }

            double minf = 0;
            for (int u = 0; u < dataset.numUser; u++) {
                for (int i = 0; i < dataset.numItem; i++) {
                    if (dataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            double ratedValue = entry.getValue();
                            if (ratedValue > 0) {
                                int[] arrayContext = contextProcessor.getArrayOfContextInHeirachy(entry.getKey());
                                double sumbiasItemContext = 0;
                                for (int iContext = 0; iContext < arrayContext.length; iContext++) {
                                    sumbiasItemContext += biasItemContext[i][iContext][arrayContext[iContext]];
                                }
                                minf +=
                                        Math.pow(ratedValue - this.prediction(u, i, entry.getKey()), 2)
                                        + lamda * (Math.pow(sumbiasItemContext, 2)
                                        + Math.pow(biasUser[u], 2)
                                        + Math.pow(MatrixFunc.module(itemFactorVector[i]), 2)
                                        + Math.pow(MatrixFunc.module(userFactorVector[u]), 2));

                            }

                        }
                    }
                }
            }
//            System.out.println("error=" + minf);            
            //Check to stop Iterator
            oldSumErrors = sumErrors;
            sumErrors = minf;
            //System.out.println("ABS Sum of Errors = " + Math.abs(oldSumErrors - sumErrors));
            if (Math.abs(sumErrors - oldSumErrors) < epsilon) {
                //System.out.println("iterator = " + iterator);
                //System.out.println("min(f) = " + Math.min(oldSumErrors, sumErrors));
                break;
            }
        }
    }

    public void run() {
        Date t1 = new Date();
        this.initialize();
        this.initializeLearningRate();
        this.baselinePredictorBias();
        this.initializeFactorVectors();
        this.MF();
        Date t2 = new Date();
        evaluator.training_Time = (Long) Math.round((double) (t2.getTime() - t1.getTime()) / 1000);
    }

    public static void main(String[] args) {
        System.out.print(Math.log(0));
    }
}
