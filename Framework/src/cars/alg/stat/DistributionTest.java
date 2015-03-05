/*
 Copyright (c) 2009-2011
 Speech Group at Informatik 5, Univ. Erlangen-Nuremberg, GERMANY
 Korbinian Riedhammer
 Tobias Bocklet

 This file is part of the Java Speech Toolkit (JSTK).

 The JSTK is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 The JSTK is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with the JSTK. If not, see <http://www.gnu.org/licenses/>.
 */
package cars.alg.stat;

import java.util.Arrays;
import java.util.HashMap;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

/**
 * A collection of statistical tests for normal distribution of data
 *
 * @author sikoried
 *
 */
public abstract class DistributionTest {

    /**
     * Test whether or not a given data array satisfies a normal distribution
     * using the Anderson Darling test at the given significance niveau.
     *
     * @param data sample data, will be sorted afterwards
     * @param p significance niveau (0.1, 0.05, 0.025, 0.001, 0.0001)
     * @return true if data seems to be normal distributed
     */
    public static boolean andersonDarlingNormal(double[] data, double p) {
        // available critical values, feel free to extend
        HashMap<Double, Double> adt_map = new HashMap<Double, Double>();
        adt_map.put(0.1500, 0.576);
        adt_map.put(0.1000, 0.656);
        adt_map.put(0.0500, 0.787);
        adt_map.put(0.0250, 0.918);
        adt_map.put(0.0010, 1.092);
        adt_map.put(0.0001, 1.8692);

        double val = andersonDarlingNormalCriticalValue(data);
        boolean result = val < adt_map.get(p);
        return result;
    }

    /**
     * Compute the Anderson Darling critical value for the given 1D data and
     * significance niveau. The larger the value, the more likely the data is
     * result of a normal distribution
     *
     * @param data
     * @return
     */
    public static double andersonDarlingNormalCriticalValue(double[] data) {
        // normalize to N(0,1)
        double m = 0;
        double s = 0;
        int n = data.length;
        for (int i = 0; i < n; i++) {
            m += data[i];
        }

        m /= n;

        for (int i = 0; i < n; i++) {
            s += (data[i] - m) * (data[i] - m);
        }
        s = Math.sqrt(s / (n - 1));

        double[] data_mod = new double[data.length];
        for (int i = 0; i < n; i++) {
            data_mod[i] = (data[i] - m) / s;
        }

        // sort
        Arrays.sort(data_mod);

        // compute F values
        for (int i = 0; i < n; i++) {
            data_mod[i] = Density.cdf(data_mod[i]);
        }

        // evaluate
        return andersonDarlingSatistic(data_mod, true);
    }

    /**
     * Compute the Anderson Darling statistic
     *
     * @param sortedData Sample data normalized to (0,1) and sorted
     * @param normalize Perform Stephen's normalization
     * @return
     */
    private static double andersonDarlingSatistic(double[] F, boolean normalize) {
        int n = F.length;
        double[] f2 = new double[n + 1];

        for (int i = 1; i <= n; i++) {
            f2[i] = F[i - 1];
        }

        // accumulate
        double sum = 0.;
        for (int i = 1; i <= n; i++) {
            double z1 = StrictMath.log(f2[i]);
            //double z2 = StrictMath.log1p(-F[n - i - 1]); // supposed to be more accurate
            //sum += (2. * i + 1) * (z1 + z2);

            double z2 = StrictMath.log(1 - f2[n + 1 - i]);
            sum += (2. * i - 1) * (z1 + z2);
        }

        double a2 = -sum / (double) n - (double) n;

//        if (normalize) {
//            a2 *= (1. + 4. / (double) n - 25. / ((double) (n * n)));
//        }

        return a2;
    }

    public static void main(String[] a) {
        andersonDarlingNormalCriticalValue(new double[]{1, 2, 3});
    }
}
