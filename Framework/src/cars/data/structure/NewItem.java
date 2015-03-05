/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data.structure;

/**
 *
 * @author Hiep
 */
public class NewItem {

    public int itemID;
    public int contextID;
    public int valContext;
    public int group;
    public int newID;

    public NewItem() {
    }

    public NewItem(int item_id, int context_id, int valueOfContext, int group) {
        this.itemID = item_id;
        this.contextID = context_id;
        this.valContext = valueOfContext;
        this.group = group;

    }
}
