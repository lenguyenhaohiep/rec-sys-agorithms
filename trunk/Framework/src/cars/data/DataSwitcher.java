/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.data;

import cars.data.structure.Dataset;
import cars.alg.ContextProcessor;

/**
 *
 * @author Hiep
 */
public class DataSwitcher {

    public Dataset data;
    public ContextProcessor CP;

    public void DataChoosing(int index) {
        switch (index) {
            //Food DB
            case 1:
                Food Dset = new Food();
                try {
                    Dset.read(5);
                } catch (Exception ex) {
                }
                Dset.reduceContextDimension();
                //data = Dset;
                CP = new ContextProcessor();

                CP.numAttrAtContext = new int[]{3};
                CP.initializeContextHierachy(new int[][][]{
                            {{0}, {1}, {2}}});
                CP.generateContextCombinationFromHierachy();
                break;
            //HMusic
            case 2:
//                HMusic Dset2 = new HMusic();
//                try {
//                    Dset2.processData();
//                } catch (Exception ex) {
//                }
//                Dset2.reduceContextDimension();
                data = new Dataset();
                CP = new ContextProcessor();
//                CP.data = Dset2;
                CP.numAttrAtContext = new int[]{7, 5};
                CP.initializeContextHierachy(new int[][][]{
                            {{0}, {1}, {2}, {3}, {4}, {5}, {6}}, {{0}, {1}, {2}, {3}, {4}}});
//                CP.generateContextCombinationFromHierachy();
                break;
            //HGift
            case 3:
//                HGift Dset3 = new HGift();
//                try {
//                    Dset3.processData();
//                } catch (Exception ex) {
//                }
//                Dset3.reduceContextDimension();
//                data = Dset3;
                data = new Dataset();
                CP = new ContextProcessor();
//                CP.data = Dset3;
                CP.numAttrAtContext = new int[]{6, 2, 4, 10};
                CP.initializeContextHierachy(new int[][][]{
                            {{0}, {1}, {2}, {3}, {4}, {5}},
                            {{0}, {1}},
                            {{0}, {1}, {2}, {3}},
                            {{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}}
                        });
//                CP.generateContextCombinationFromHierachy();
                break;
            case 4:
                Movielens dset4 = new Movielens();
                dset4.readUserData();
                dset4.readDataFromFile();
                dset4.splitTestingSet();
                data = dset4;
                CP = new ContextProcessor();
                CP.initializeContextHierachy(new int[][][]{{{0}, {1}}, {{0}, {1}, {2}}});
                CP.generateContextCombinationFromHierachy();
                break;
            case 5:
                AIST dsetAist = new AIST();
                dsetAist.numItem = 20;
                dsetAist.numUser = 212;
                //data = dsetAist;
                data = new Dataset();
                data.numUser = dsetAist.numUser;
                data.numItem = dsetAist.numItem;
                CP = new ContextProcessor();
                CP.numAttrAtContext = new int[]{3, 2};
                CP.initializeContextHierachy(new int[][][]{
                            {{0}, {1}, {2}}, {{0}, {1}}});
                CP.generateContextCombinationFromHierachy();
                break;
            case 6:
                AIST dsetAist2 = new AIST();
                dsetAist2.numItem = 20;
                dsetAist2.numUser = 212;
                data = dsetAist2;
                CP = new ContextProcessor();
                CP.data = data;
                CP.numAttrAtContext = new int[]{3, 2};
                CP.initializeContextHierachy(new int[][][]{
                            {{0}, {1}, {2}}, {{0}, {1}}});
                CP.generateContextCombinationFromHierachy();
                break;
            case 7:
                Comoda comoda = new Comoda();
//                comoda.readData(1);
//                comoda.reduceContextDimension();
                comoda.numUser = 82;
                comoda.numItem = 1225;
                data = comoda;
                CP = new ContextProcessor();
                CP.data = data;
                CP.numAttrAtContext = new int[]{4, 3, 4, 3, 5, 7, 7, 7, 3, 2, 2, 2};

                CP.initializeContextHierachy(new int[][][]{
                            {{0}, {1}, {2}, {3}},
                            {{0}, {1}, {2}},
                            {{0}, {1}, {2}, {3}},
                            {{0}, {1}, {2}},
                            {{0}, {1}, {2}, {3}, {4}},
                            {{0}, {1}, {2}, {3}, {4}, {5}, {6}},
                            {{0}, {1}, {2}, {3}, {4}, {5}, {6}},
                            {{0}, {1}, {2}, {3}, {4}, {5}, {6}},
                            {{0}, {1}, {2}},
                            {{0}, {1}},
                            {{0}, {1}},
                            {{0}, {1}}
                        });
//                CP.generateContextCombinationFromHierachy();
                break;

            default:
                return;
        }
//        data.findRatedItemsByUser();
    }
}
