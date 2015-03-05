package cars.app;

import java.util.Vector;
import org.ejml.simple.SimpleMatrix;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Hiep
 */
public class MatrixFunc {

    public static float[] fromVector(Vector v) {
        float[] matrix = new float[v.size()];
        for (int i = 0; i < v.size(); i++) {
            matrix[i] = (Float) v.get(i);
        }
        return matrix;
    }

    public static Vector NxV(float number, Vector v) {
        Vector _v = new Vector();
        for (int i = 0; i < v.size(); i++) {
            _v.add(i, (Float) number * (Float) v.get(i));
        }
        return _v;
    }

    public static Vector VpV(Vector v1, Vector v2) {
        Vector _v = new Vector();
        for (int i = 0; i < v1.size(); i++) {
            _v.add(i, (Float) v1.get(i) + (Float) v2.get(i));
        }
        return _v;
    }

    public static Vector VsV(Vector v1, Vector v2) {
        Vector _v = new Vector();
        for (int i = 0; i < v1.size(); i++) {
            _v.add(i, (Float) v1.get(i) - (Float) v2.get(i));
        }
        return _v;
    }

    public static float module(Vector v) {
        float s = 0;
        for (int i = 0; i < v.size(); i++) {
            s += Math.pow((Float) v.get(i), 2);
        }
        return (float) (Math.sqrt(s));
    }

    public static float dot(float[] v1, float v2[]) {
        int res = 0;
        for (int i = 0; i < v1.length; i++) {
            res += v1[i] * v2[i];
        }
        return res;
    }
}
