/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.alg;

import cars.data.structure.Dataset;
import cars.data.structure.Rating;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TestUtils;
import org.jgrapht.alg.HamiltonianCycle;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 *
 * @author Hiep
 */
public class ContextProcessor2 {

    public List<List<List<Integer>>> hierachy;
    public int[] numAttrAtContext;
    public int[] numAttrAtContextHierachy;
    public String[] contextsGereration;
    public Dataset data = new Dataset();
    public boolean hasRemovedContext = false;
    public List<Integer> noAffectContexts;
    public boolean combineContext = false;

    public void initializeContextHierachy(int[][][] matrix) {
        hierachy = (new ArrayList<List<List<Integer>>>());
        for (int i = 0; i < matrix.length; i++) {
            List<List<Integer>> level1 = new ArrayList<List<Integer>>();
            for (int j = 0; j < matrix[i].length; j++) {
                List<Integer> level2 = new ArrayList<Integer>();
                for (int k = 0; k < matrix[i][j].length; k++) {
                    level2.add(matrix[i][j][k]);
                }
                level1.add(level2);
            }
            hierachy.add(level1);
        }
    }

    public String convertContextToUpperLevel(String context) {
        if (!hasRemovedContext) {
            String resContext = "";
            String[] contexts = context.split("::");
            for (int i = 0; i < contexts.length; i++) {
                int currentContext = Integer.parseInt(contexts[i]);
                for (int iHerachy = 0; iHerachy < hierachy.get(i).size(); iHerachy++) {
                    for (int iContext = 0; iContext < hierachy.get(i).get(iHerachy).size(); iContext++) {
                        if (hierachy.get(i).get(iHerachy).get(iContext) == currentContext) {
                            resContext += iHerachy + "::";
                            break;
                        }
                    }
                }
            }
            if (resContext.length() <= 3) {
                int a = 0;
            }
            return resContext;
        } else {
            return convertToNewContextWithAdom(context, noAffectContexts);
        }
    }

    public boolean checkContextBelongsToHierachy(int hierachy, String context) {
        return contextsGereration[hierachy].trim().equals(convertContextToUpperLevel(context));
    }

    public void generateContextCombinationFromHierachy() {
        numAttrAtContextHierachy = (new int[hierachy.size()]);
        int count = 1;
        for (int i = 0; i < hierachy.size(); i++) {
            numAttrAtContextHierachy[i] = hierachy.get(i).size();
            count *= numAttrAtContextHierachy[i];
        }
        //contextsGereration = (new String[count]);
//        if (hierachy.size() == 1) {
//            for (int i = 0; i < contextsGereration.length; i++) {
//                contextsGereration[i] = i + "::";
//            }
//        } else {
//            if (hierachy.size() == 4) {
//                int index = 0;
//                for (int i = 0; i < hierachy.get(0).size(); i++) {
//                    for (int j = 0; j < hierachy.get(1).size(); j++) {
//                        for (int k = 0; k < hierachy.get(2).size(); k++) {
//                            for (int l = 0; l < hierachy.get(3).size(); l++) {
//                                contextsGereration[index] = i + "::" + j + "::" + k + "::" + l + "::";
//                                index++;
//                            }
//                        }
//                    }
//                }
//            } else {
//                if (hierachy.size() == 3) {
//                    int index = 0;
//                    for (int i = 0; i < hierachy.get(0).size(); i++) {
//                        for (int j = 0; j < hierachy.get(1).size(); j++) {
//                            for (int k = 0; k < hierachy.get(2).size(); k++) {
//                                contextsGereration[index] = i + "::" + j + "::" + k + "::";
//                                index++;
//                            }
//                        }
//                    }
//                }
//            }
//            if (hierachy.size() == 2) {
//                int index = 0;
//                for (int i = 0; i < hierachy.get(0).size(); i++) {
//                    for (int j = 0; j < hierachy.get(1).size(); j++) {
//                        contextsGereration[index] = i + "::" + j + "::";
//                        index++;
//                    }
//                }
//            }
//        }
        List<String> gene = new ArrayList<String>();
//        for (int u = 0; u < data.numUser; u++) {
//            for (int i = 0; i < data.numItem; i++) {
//                if (data.mRatingWithContext[u][i] != null) {
//                    for (Map.Entry<String, Float> entry : data.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
//                        if (!gene.contains(entry.getKey())) {
//                            gene.add(entry.getKey());
//                        }
//                    }
//                }
//            }
//        }

        for (int i = 0; i < data.testingSet.size(); i++) {
            if (!gene.contains((data.testingSet.get(i).context))) {
                gene.add((data.testingSet.get(i).context));
            }
        }
        contextsGereration = new String[gene.size()];
        for (int i = 0; i < gene.size(); i++) {
            contextsGereration[i] = gene.get(i);

        }
    }

    public void generateContextCombinationFromHierachy2() {
        numAttrAtContextHierachy = (new int[hierachy.size()]);
        int count = 1;
        for (int i = 0; i < hierachy.size(); i++) {
            numAttrAtContextHierachy[i] = hierachy.get(i).size();
            count *= numAttrAtContextHierachy[i];
        }
        //contextsGereration = (new String[count]);
//        if (hierachy.size() == 1) {
//            for (int i = 0; i < contextsGereration.length; i++) {
//                contextsGereration[i] = i + "::";
//            }
//        } else {
//            if (hierachy.size() == 4) {
//                int index = 0;
//                for (int i = 0; i < hierachy.get(0).size(); i++) {
//                    for (int j = 0; j < hierachy.get(1).size(); j++) {
//                        for (int k = 0; k < hierachy.get(2).size(); k++) {
//                            for (int l = 0; l < hierachy.get(3).size(); l++) {
//                                contextsGereration[index] = i + "::" + j + "::" + k + "::" + l + "::";
//                                index++;
//                            }
//                        }
//                    }
//                }
//            } else {
//                if (hierachy.size() == 3) {
//                    int index = 0;
//                    for (int i = 0; i < hierachy.get(0).size(); i++) {
//                        for (int j = 0; j < hierachy.get(1).size(); j++) {
//                            for (int k = 0; k < hierachy.get(2).size(); k++) {
//                                contextsGereration[index] = i + "::" + j + "::" + k + "::";
//                                index++;
//                            }
//                        }
//                    }
//                }
//            }
//            if (hierachy.size() == 2) {
//                int index = 0;
//                for (int i = 0; i < hierachy.get(0).size(); i++) {
//                    for (int j = 0; j < hierachy.get(1).size(); j++) {
//                        contextsGereration[index] = i + "::" + j + "::";
//                        index++;
//                    }
//                }
//            }
//        }
        List<String> gene = new ArrayList<String>();
        for (int u = 0; u < data.numUser; u++) {
            for (int i = 0; i < data.numItem; i++) {
                if (data.mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : data.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        if (!gene.contains(entry.getKey())) {
                            gene.add(entry.getKey());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < data.testingSet.size(); i++) {
            if (!gene.contains((data.testingSet.get(i).context))) {
                gene.add((data.testingSet.get(i).context));
            }
        }
        contextsGereration = new String[gene.size()];
        for (int i = 0; i < gene.size(); i++) {
            contextsGereration[i] = gene.get(i);

        }
    }

    public void test() {
        for (int k = 0; k < contextsGereration.length; k++) {
            int count = 0;
            for (int u = 0; u < data.numUser; u++) {
                for (int i = 0; i < data.numItem; i++) {
                    if (data.mRatingWithContext[u][i] != null) {
                        if (data.mRatingWithContext[u][i].contextScorePairs.containsKey(contextsGereration[k])) {
                            count++;
                        }
                    }
                }
            }
            System.out.println(count);
        }
    }

    public int getIndexOfContextInHeirachy(String context) {
        String contextInHeirachy = convertContextToUpperLevel(context);
        for (int i = 0; i < contextsGereration.length; i++) {
            if (contextsGereration[i].trim().equals(contextInHeirachy)) {
                return i;
            }
        }
        return 0;
    }

    public int[] getArrayOfContextInHeirachy(String context) {
        String[] contexts = context.split("::");
        int[] arrayContext = new int[contexts.length];
        for (int i = 0; i < contexts.length; i++) {
            int currentContext = Integer.parseInt(contexts[i]);
            for (int iHerachy = 0; iHerachy < hierachy.get(i).size(); iHerachy++) {
                for (int iContext = 0; iContext < hierachy.get(i).get(iHerachy).size(); iContext++) {
                    if (hierachy.get(i).get(iHerachy).get(iContext).equals(currentContext)) {
                        arrayContext[i] = iHerachy;
                        break;
                    }
                }
            }
        }
        return arrayContext;
    }

    public int[] convertStringContextToArray(String context) {
        String[] contexts = context.split("::");
        int[] arrayContext = new int[contexts.length];
        for (int i = 0; i < contexts.length; i++) {
            arrayContext[i] = Integer.parseInt(contexts[i]);
        }
        return arrayContext;
    }

    /*
     * iContext: ngữ cảnh thứ iContext
     * Dataset: Dữ liệu
     * minimumRating: số lượng đánh giá tối thiểu (các bộ dữ liệu đều có đánh giá tối)
     * Signigicant: tham số độ tin cậy
     */
    public boolean contextMattersCase2(int iContext, Dataset dataset, int minimumRating, float significant) {
        List<List<Double>> samples = new ArrayList<List<Double>>();
        for (int iSample = 0; iSample < numAttrAtContext[iContext]; iSample++) {
            List<Double> sampleInContextValue = new ArrayList<Double>(dataset.numUser);
            for (int u = 0; u < dataset.numUser; u++) {
                int count = 0;
                int sumRating = 0;
                for (int i = 0; i < dataset.numItem; i++) {
                    if (dataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            int[] arrayContext = this.convertStringContextToArray(entry.getKey());
                            if (arrayContext[iContext] == iSample) {
                                sumRating += entry.getValue();
                                count++;
                            }
                        }
                    }
                }
                if (count >= minimumRating) {
                //if (count > 0) {
                    sampleInContextValue.add((double) sumRating / count);
                } else {
                    sampleInContextValue.add(0.0);
                }


            }
            samples.add(sampleInContextValue);
        }

        SimpleWeightedGraph graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (int v = 0; v < samples.size(); v++) {
            graph.addVertex(v);
        }

        boolean[][] a = new boolean[samples.size()][samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            for (int j = 0; j < samples.size(); j++) {
                if (i == j) {
                    a[i][j] = true;
                } else {
                    a[i][j] = false;
                }
            }
        }

        boolean isMatter = true;
        int countAffect = 0;
        for (int i = 0; i < samples.size() - 1; i++) {
            boolean check = false;
            for (int j = i + 1; j < samples.size(); j++) {
                if (i != j) {
                    check = computePairTTestAlpha(samples.get(i), samples.get(j), 1 - significant);
                    //Nếu tìm được 1 kiểm định mà trong đó 2 giá trị trung bình bằng nhau thì bỏ luôn ngữ cảnh đó
                    if (check==false) {
                        isMatter = false;
                        break;
                    }
                }
            }
        }

        if (isMatter) {
            System.out.println("Context " + iContext + "-th, Case 2 - Matter");
        } else {
            System.out.println("Context " + iContext + "-th, Case 2 - No Matter");
        }
        return isMatter;
    }

    public boolean contextMattersCase3(int iContext, Dataset dataset, int minimumRating, float significant) {
        List<List<Double>> samples = new ArrayList<List<Double>>();
        for (int iSample = 0; iSample < numAttrAtContext[iContext]; iSample++) {
            List<Double> sampleInContextValue = new ArrayList<Double>(dataset.numUser);
            for (int u = 0; u < dataset.numUser; u++) {
                int count = 0;
                int sumRating = 0;
                for (int i = 0; i < dataset.numItem; i++) {
                    if (dataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            int[] arrayContext = this.convertStringContextToArray(entry.getKey());
                            if (arrayContext[iContext] == iSample) {
                                sumRating += entry.getValue();
                                count++;
                            }
                        }
                    }
                }
                if (count >= minimumRating) {
                    sampleInContextValue.add((double) sumRating / count);
                } else {
                    sampleInContextValue.add(0.0);
                }


            }
            samples.add(sampleInContextValue);
        }

        SimpleWeightedGraph graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (int v = 0; v < samples.size(); v++) {
            graph.addVertex(v);
        }

        boolean[][] a = new boolean[samples.size()][samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            for (int j = 0; j < samples.size(); j++) {
                if (i == j) {
                    a[i][j] = true;
                } else {
                    a[i][j] = false;
                }
            }
        }

        boolean isMatter = false;
        int countAffect = 0;
        for (int i = 0; i < samples.size() - 1; i++) {
            boolean check = false;
            for (int j = i + 1; j < samples.size(); j++) {
                if (i != j) {
                    check = computePairTTestAlpha(samples.get(i), samples.get(j), 1 - significant);
                    //Nếu tìm được ít nhất 1 kiểm định bác bỏ hai giá trị trung bình khác nhau => Giữ ngữ cảnh
                    if (check==true) {
                        isMatter = true;
                    }
                }
            }
        }

        if (isMatter) {
            System.out.println("Context " + iContext + "-th, Case 3 - Matter");
        } else {
            System.out.println("Context " + iContext + "-th, Case 3 - No Matter");
        }
        return isMatter;
    }

    public void warShall(int intContext, int n, boolean[][] a) {

        boolean[] free = new boolean[n];
        for (int i = 0; i < n; i++) {
            free[i] = true;
        }
        for (int k = 0; k < n; k++) {
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    a[u][v] = a[u][v] || a[u][k] && a[k][v];
                }
            }
        }
        List<List<Integer>> level2 = new ArrayList<List<Integer>>();
        for (int u = 0; u < n; u++) {
            if (free[u]) {
                List<Integer> level1 = new ArrayList<Integer>();
                for (int v = 0; v < n; v++) {
                    if (a[u][v]) {
                        //System.out.print(v + "-");
                        free[v] = false;
                        level1.add(v);
                    }
                }
                level2.add(level1);
                //System.out.println();
            }
        }
        hierachy.add(level2);
    }

    public float computePairTTest(List<Double> lstSample1, List<Double> lstSample2) {
        List<Double> s1 = new ArrayList<Double>();
        List<Double> s2 = new ArrayList<Double>();
        for (int i = 0; i < lstSample1.size(); i++) {
            if (lstSample1.get(i) > 0 && lstSample2.get(i) > 0) {
                s1.add(lstSample1.get(i));
                s2.add(lstSample2.get(i));
            }
        }
        if (s1.size() == 0) {
            return 1;
        }
        try {
            double[] sample1 = ArrayUtils.toPrimitive((Double[]) s1.toArray(new Double[s1.size()]));
            double[] sample2 = ArrayUtils.toPrimitive((Double[]) s2.toArray(new Double[s2.size()]));
            //System.out.print(TestUtils.pairedTTest(sample1, sample2));
            return (float) TestUtils.pairedTTest(sample1, sample2);
        } catch (Exception ex) {
            return 2;
        }
    }

    public void generateHierachyFromGraph(SimpleWeightedGraph graph) {
        List<Integer> cycle = HamiltonianCycle.getApproximateOptimalForCompleteGraph(graph);
        int sumWeight = 0;
        for (int i = 0; i < cycle.size(); i++) {
            int v1 = i;
            int v2 = i + 1;
            if (v2 == cycle.size()) {
                v2 = 0;
            }
            sumWeight += graph.getEdgeWeight(graph.getEdge(v1, v2));
            System.out.print(v1 + "->" + v2 + ":" + graph.getEdgeWeight(graph.getEdge(v1, v2)));
        }
        System.out.println();
        System.out.println("Sum of Weight = " + sumWeight);
        //EulerianCircuit.getEulerianCircuitVertices(graph);
    }

    public static void main(String[] arg) throws IllegalArgumentException, MathException {
//        ContextProcessor c = new ContextProcessor();
//        c.hierachy = new int[][][]{{{0}, {1}, {2}}};
//        c.generateContextCombinationFromHierachy();
//        System.out.print(c.convertContextToUpperLevel("2::0::"));
//        List<List<List<Integer>>> i = new ArrayList<List<List<Integer>>>();
        double[] sample1 = new double[]{1, 3, 3, 4, 2, 7, 6, 6};
        double[] sample2 = new double[]{1, 2, 3, 3, 5};
        System.out.print(TestUtils.pairedT(sample1, sample2));
    }

    public void runGFSG(Dataset dataset, int minimumRating, float significant) {
        int numContextDims = hierachy.size();
        hierachy = (new ArrayList<List<List<Integer>>>());
        for (int iContext = 0; iContext < numContextDims; iContext++) {
            this.contextMattersCase3(iContext, dataset, minimumRating, significant);
        }
    }

    public String convertToNewContextWithAdom(String oldContext, List<Integer> noAffectContexts) {
        String newContext = "";
        String[] contexts = oldContext.split("::");
        for (int i = 0; i < contexts.length; i++) {
            boolean check = true;
            for (int j = 0; j < noAffectContexts.size(); j++) {
                if (noAffectContexts.get(j).equals(i)) {
                    check = false;
                    break;
                }
            }
            if (check) {
                newContext += contexts[i] + "::";
            }
        }
        return newContext;
    }

    public void updateHeirachyAfterConverting() {
        List<List<List<Integer>>> _h = new ArrayList<List<List<Integer>>>();
        for (int icontext = 0; icontext < hierachy.size(); icontext++) {
            List<List<Integer>> level1 = new ArrayList<List<Integer>>();
            for (int ilevel1 = 0; ilevel1 < hierachy.get(icontext).size(); ilevel1++) {
                List<Integer> level2 = new ArrayList<Integer>();
                level2.add(ilevel1);
                level1.add(level2);
            }
            _h.add(level1);
        }
        hierachy = _h;
    }

    public Dataset runADOM(Dataset dataset) {
//        Dataset temp = dataset;
//        temp = temp.combineTestAndTrainSet();
//        int numContextDims = hierachy.size();
        List<Integer> _noAffectContexts = new ArrayList<Integer>();
        List<List<List<Integer>>> _hierachy = (new ArrayList<List<List<Integer>>>());
//        for (int iContext = 0; iContext < numContextDims; iContext++) {
//            boolean matter = this.runAdomMethod(iContext, temp, 3, (float) 0.95);
//            if (!matter) {
//                noAffectContexts.add(iContext);
//            } else {
//                _hierachy.add(hierachy.get(iContext));
//            }
//        }
        //for (int i = 0; i <= 10; i++) {
        //_noAffectContexts.add(i);
        //}
        _noAffectContexts.add(1);
        //_noAffectContexts.add(0);

        _hierachy.add(hierachy.get(0));


        this.hasRemovedContext = true;
        this.noAffectContexts = _noAffectContexts;
        hierachy = _hierachy;
        generateContextCombinationFromHierachy();
        dataset = convertToNewDataset(dataset);
        this.data = dataset;
        this.hasRemovedContext = false;

//        dataset = convertToNewDataset(dataset);
        return dataset;
    }

    public Dataset convertToNewDataset(Dataset old) {
        Dataset _new = new Dataset();
        _new.ratedItemsByUser = old.ratedItemsByUser;
        _new.numItem = old.numItem;
        _new.numUser = old.numUser;
        _new.mRatingWithContext = new Rating[_new.numUser][_new.numItem];

        for (int c = 0; c < contextsGereration.length; c++) {
            for (int u = 0; u < _new.numUser; u++) {
                for (int i = 0; i < _new.numItem; i++) {
                    float sum = 0;
                    int count = 0;
                    if (old.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : old.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            int _c = getIndexOfContextInHeirachy(convertContextToUpperLevel(entry.getKey()));
                            if (_c == c) {
                                sum += entry.getValue();
                                count++;
                            }
                        }
                        if (count > 1) {
                            int a = 0;
                        }
                        if (count > 0) {
                            for (Map.Entry<String, Float> entry : old.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                                int _c = getIndexOfContextInHeirachy(convertContextToUpperLevel(entry.getKey()));
                                if (_c == c) {
                                    if (_new.mRatingWithContext[u][i] == null) {
                                        _new.mRatingWithContext[u][i] = new Rating();
                                    }
                                    _new.mRatingWithContext[u][i].setValueRating(sum / count, convertContextToUpperLevel(entry.getKey()));
                                    _new.numRating++;
                                }
                            }
                        }
                    }
                }
            }
        }
        _new.testingSet = old.testingSet;
        for (int i = 0; i < old.testingSet.size(); i++) {
            _new.testingSet.get(i).context = convertContextToUpperLevel(_new.testingSet.get(i).context);
        }

        return _new;
    }

    public boolean runAdomMethod(int iContext, Dataset dataset, int minimumRating, float significant) {
        List<List<Double>> samples = new ArrayList<List<Double>>();
        for (int iSample = 0; iSample < numAttrAtContext[iContext]; iSample++) {
            List<Double> sampleInContextValue = new ArrayList<Double>(dataset.numUser);
            for (int u = 0; u < dataset.numUser; u++) {
                int count = 0;
                int sumRating = 0;
                for (int i = 0; i < dataset.numItem; i++) {
                    if (dataset.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : dataset.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            int[] arrayContext = this.convertStringContextToArray(entry.getKey());
                            if (arrayContext[iContext] == iSample) {
                                sumRating += entry.getValue();
                                count++;
                            }
                        }
                    }
                }
                if (count >= minimumRating) {
                    sampleInContextValue.add((double) sumRating / count);
                } else {
                    sampleInContextValue.add(0.0);
                }


            }
            samples.add(sampleInContextValue);
        }


        int countAffect = 0;
        for (int i = 0; i < samples.size(); i++) {
            float minWeight = 10000;
            for (int j = 1; j < samples.size(); j++) {
                if (i != j) {
                    float ttest = computePairTTest(samples.get(i), samples.get(j));
                    if ((1 - ttest) < significant) {
                        return false;
                    }
                }

            }
        }

        return true;
    }

    public void generateContextCombinationFromHierachyGFSG() {
        List<String> gene = new ArrayList<String>();
//        for (int u = 0; u < data.numUser; u++) {
//            for (int i = 0; i < data.numItem; i++) {
//                if (data.mRatingWithContext[u][i] != null) {
//                    for (Map.Entry<String, Float> entry : data.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
//                        String _newContext = convertContextToUpperLevel(entry.getKey());
//                        if (!gene.contains(_newContext)) {
//                            gene.add(_newContext);
//                        }
//                    }
//                }
//            }
//        }

        for (int i = 0; i < data.testingSet.size(); i++) {
            String _newContext = convertContextToUpperLevel(data.testingSet.get(i).context);
            if (!gene.contains(_newContext)) {
                gene.add(_newContext);
            }
        }
        contextsGereration = new String[gene.size()];
        for (int i = 0; i < gene.size(); i++) {
            contextsGereration[i] = gene.get(i);
        }
    }

    public boolean isContained(String context, Rating lstRatings) {
        for (Map.Entry<String, Float> entry : lstRatings.contextScorePairs.entrySet()) {
            String _context = convertContextToUpperLevel(entry.getKey());
            if (_context.equals(context)) {
                return true;
            }
        }
        return false;
    }

    public float getRating(String context, Rating lstRatings) {
        for (Map.Entry<String, Float> entry : lstRatings.contextScorePairs.entrySet()) {
            String _context = convertContextToUpperLevel(entry.getKey());
            if (_context.equals(context)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public Dataset runGSG(Dataset data) {
        String before = "";
        String after = "";
//        do {
//            before = convertHeirachyToString(hierachy);
        this.runGFSG(data, 3, (float) 0.95);
        this.generateContextCombinationFromHierachyGFSG();
        data = this.convertToNewDataset(data);
        this.updateHeirachyAfterConverting();
//            after = convertHeirachyToString(hierachy);
//            System.out.println("1");
//        } while (!before.equals(after));
        return data;
    }

    public String convertHeirachyToString(List<List<List<Integer>>> hierachy) {
        String res = "";
        for (int i = 0; i < hierachy.size(); i++) {
            for (int j = 0; j < hierachy.get(i).size(); j++) {
                for (int k = 0; k < hierachy.get(i).get(j).size(); k++) {
                    res += hierachy.get(i).get(j) + "::";
                }
            }
        }
        return res;
    }

    //New
    public Dataset newAlg(Dataset data) {
        //Tìm tất cả các tổ hợp có thể của ngữ cảnh hiện tại
        List<String> gene = new ArrayList<String>();
        for (int u = 0; u < data.numUser; u++) {
            for (int i = 0; i < data.numItem; i++) {
                if (data.mRatingWithContext[u][i] != null) {
                    for (Map.Entry<String, Float> entry : data.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                        if (!gene.contains(entry.getKey())) {
                            gene.add(entry.getKey());
                        }
                    }
                }
            }
        }

        for (int i = 0; i < data.testingSet.size(); i++) {
            if (!gene.contains(convertContextToUpperLevel(data.testingSet.get(i).context))) {
                gene.add(convertContextToUpperLevel(data.testingSet.get(i).context));
            }
        }

        //Phát sinh tổ hợp và tạo danh sách t-test
        contextsGereration = new String[gene.size()];
        List<List<Double>> ratingInContext = new ArrayList<List<Double>>();
        for (int k = 0; k < gene.size(); k++) {
            contextsGereration[k] = gene.get(k);
            List<Double> ratedbyUser = new ArrayList<Double>();
            for (int u = 0; u < data.numUser; u++) {
                Double sum = 0.0;
                int count = 0;
                for (int i = 0; i < data.numItem; i++) {
                    if (data.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : data.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            if (entry.getKey().equals(contextsGereration[k])) {
                                sum += entry.getValue();
                                count++;
                            }
                        }
                    }
                }
                if (count > 0) {
                    ratedbyUser.add(sum / count);
                } else {
                    ratedbyUser.add(0.0);
                }
            }
            ratingInContext.add(ratedbyUser);
        }

        //tạo đồ thị
        SimpleWeightedGraph graph = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        for (int v = 0; v < ratingInContext.size(); v++) {
            graph.addVertex(v);
        }
        boolean[][] a = new boolean[ratingInContext.size()][ratingInContext.size()];
        for (int i = 0; i < ratingInContext.size(); i++) {
            for (int j = 0; j < ratingInContext.size(); j++) {
                if (i == j) {
                    a[i][j] = true;
                } else {
                    a[i][j] = false;
                }
            }
        }
        for (int i = 0; i < ratingInContext.size(); i++) {
            float minWeight = 10000;
            int vertex = -1;
            boolean check = false;
            for (int j = 1; j < ratingInContext.size(); j++) {
                if (i != j) {
                    graph.addEdge(i, j);
                    float ttest = computePairTTest(ratingInContext.get(i), ratingInContext.get(j));
                    if ((1 - ttest) >= 0.95) {
                        check = true;
                        //graph.setEdgeWeight(graph.getEdge(i, j), 1);
                        //System.out.println(i + "->" + j + ":1");
                    } else {
                        //graph.setEdgeWeight(graph.getEdge(i, j), 0);

                        if ((1 - ttest) < minWeight) {
                            minWeight = 1 - ttest;
                            vertex = j;
                        }
                    }
                }
            }
            if (vertex != -1 && !check) {
                a[i][vertex] = true;
                a[vertex][i] = true;
                //System.out.println(i + "->" + vertex + ":0");
            }
        }
        int n = ratingInContext.size();

        boolean[] free = new boolean[n];
        for (int i = 0; i < n; i++) {
            free[i] = true;
        }
        for (int k = 0; k < n; k++) {
            for (int u = 0; u < n; u++) {
                for (int v = 0; v < n; v++) {
                    a[u][v] = a[u][v] || a[u][k] && a[k][v];
                }
            }
        }

        //Tính các thành phần liên thông
        List<List<Integer>> level2 = new ArrayList<List<Integer>>();
        for (int u = 0; u < n; u++) {
            if (free[u]) {
                List<Integer> level1 = new ArrayList<Integer>();
                for (int v = 0; v < n; v++) {
                    if (a[u][v]) {
                        //System.out.print(v + "-");
                        free[v] = false;
                        level1.add(v);
                    }
                }
                level2.add(level1);
                //System.out.println();
            }
        }

        //tạo dữ liệu mới
        Dataset _new = new Dataset();
        _new.testingSet = data.testingSet;
        _new.numItem = data.numItem;
        _new.numUser = data.numUser;
        _new.mRatingWithContext = new Rating[_new.numUser][_new.numItem];
        for (int c = 0; c < contextsGereration.length; c++) {
            for (int u = 0; u < _new.numUser; u++) {
                for (int i = 0; i < _new.numItem; i++) {
                    float sum = 0;
                    int count = 0;
                    if (data.mRatingWithContext[u][i] != null) {
                        for (Map.Entry<String, Float> entry : data.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                            if (entry.getKey().equals(contextsGereration[c])) {
                                sum += entry.getValue();
                                count++;
                            }
                        }
                        if (count > 0) {
                            for (Map.Entry<String, Float> entry : data.mRatingWithContext[u][i].contextScorePairs.entrySet()) {
                                if (entry.getKey().equals(contextsGereration[c])) {
                                    if (_new.mRatingWithContext[u][i] == null) {
                                        _new.mRatingWithContext[u][i] = new Rating();
                                    }
                                    String context = getIndex(c, level2) + "::";
                                    _new.mRatingWithContext[u][i].setValueRating(sum / count, context);
                                    _new.numRating++;
                                }
                            }
                        }
                    }
                }
            }

            for (int i = 0; i < data.testingSet.size(); i++) {
                if (_new.testingSet.get(i).context.equals(contextsGereration[c])) {
                    String context = getIndex(c, level2) + "::";
                    _new.testingSet.get(i).context = context;
                }
            }
        }
        hierachy = new ArrayList<List<List<Integer>>>();
        List<List<List<Integer>>> _h = new ArrayList<List<List<Integer>>>();
        List<List<Integer>> level1 = new ArrayList<List<Integer>>();

        for (int icontext = 0; icontext < level2.size(); icontext++) {
            List<Integer> _level2 = new ArrayList<Integer>();
            _level2.add(icontext);
            level1.add(_level2);
        }

        _h.add(level1);
        hierachy = _h;
        this.combineContext = true;
        return _new;
    }

    public int getIndex(int c, List<List<Integer>> a) {
        for (int i = 0; i < a.size(); i++) {
            for (int j = 0; j < a.get(i).size(); j++) {
                if (a.get(i).get(j).equals(c)) {
                    return i;
                }
            }
        }
        return -1;
    }

    //Kiểm tra 2 mẫu có giá trị trung bình giống nhau
    private boolean computePairTTestAlpha(List<Double> lstSample1, List<Double> lstSample2, float f) {
        List<Double> s1 = new ArrayList<Double>();
        List<Double> s2 = new ArrayList<Double>();
        for (int i = 0; i < lstSample1.size(); i++) {
            if (lstSample1.get(i) > 0 && lstSample2.get(i) > 0) {
                s1.add(lstSample1.get(i));
                s2.add(lstSample2.get(i));
            }
        }
        if (s1.size() == 0) {
            return false;
        }
        try {
            //Mẫu 1 và mẫu 2
            double[] sample1 = ArrayUtils.toPrimitive((Double[]) s1.toArray(new Double[s1.size()]));
            double[] sample2 = ArrayUtils.toPrimitive((Double[]) s2.toArray(new Double[s2.size()]));
            //Giá trị alpha
//            double alpha = TestUtils.pairedT(sample1, sample2);
//            double result = TestUtils.pairedTTest(sample1, sample2);
//            boolean re = TestUtils.pairedTTest(sample1, sample2, f); 
//            
//            
//            if (alpha >= f)
//                return true;
//            return  false;
            //Trả về false nếu chấp nhận là 2 cái như nhau
            double w = TestUtils.pairedT(sample1, sample2);
            if (w > 1.96)
                //bác bỏ
                return true;
            else
                return false;
            //return TestUtils.pairedTTest(sample1, sample2, f);
        } catch (Exception ex) {
            return false;
        }
    }
}
