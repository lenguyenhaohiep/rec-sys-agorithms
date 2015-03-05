/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.newalg;

/**
 *
 * @author Hiep
 */
public class ItemProfile {

    public int itemID;
    public String context;
    public double[] ratings;

    public void init(int numUser) {
        ratings = new double[numUser];
        for (int i = 0; i < numUser; i++) {
            ratings[i] = -1;
        }
    }
}
