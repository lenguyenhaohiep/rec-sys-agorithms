/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cars.studyadvisor.modeling;

import cars.alg.ContextProcessor;
import cars.data.structure.Dataset;
import cars.data.structure.Rating;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Hiep
 */
public class ModelBuilder {

    private List<String> users = new ArrayList<String>();
    private List<Integer> lastContexts = new ArrayList<Integer>();
    private List<String> items = new ArrayList<String>();
    private List<List<String>> contexts;
    private List<List<List<Integer>>> hierachy = new ArrayList<List<List<Integer>>>();
    private Dataset data = new Dataset();
    private CARSAlg model = new CARSAlg();

    private void obtainData(List<Integer> _users, List<Integer> _items, List<Integer> _context, List<RatingEntry> _ratings, List<Integer> _lastContexts) {
        try {

            lastContexts.addAll(_lastContexts);
            int numItem = _items.size();
            int numUser = _users.size();
            int numContext = _context.size();

            data.numUser = numUser;
            data.numItem = numItem;
            data.mRatingWithContext = new Rating[data.numUser][data.numItem];

            String user, item;

            for (int i = 0; i < _users.size(); i++) {
                user = _users.get(i).toString();
                if (!users.contains(user)) {
                    users.add(user);
                }
            }

            for (int i = 0; i < _items.size(); i++) {
                item = _items.get(i).toString();
                if (!items.contains(item)) {
                    items.add(item);
                }
            }
            contexts = new ArrayList<List<String>>();
            contexts.add(new ArrayList<String>());
            for (int i = 0; i < _context.size(); i++) {
                String c = _context.get(i).toString();
                contexts.get(0).add(c);
            }

            for (int i = 0; i < _ratings.size(); i++) {

                user = Integer.toString(_ratings.get(i).user_id);
                item = Integer.toString(_ratings.get(i).item_id);

                String[] jContext = _ratings.get(i).context.trim().split("-");

                float rating = _ratings.get(i).rating;
                String context = "";
                for (int j = 0; j < jContext.length; j++) {
                    context = context + contexts.get(j).indexOf(jContext[j]) + "::";
                }

                int u = users.indexOf(user);
                int ii = items.indexOf(item);
                if (data.mRatingWithContext[u][ii] == null) {
                    data.mRatingWithContext[u][ii] = new Rating();
                }
                data.mRatingWithContext[u][ii].setValueRating(rating, context);
            }

            for (int i = 0; i < contexts.size(); i++) {
                List<List<Integer>> level1 = new ArrayList<List<Integer>>();
                for (int j = 0; j < contexts.get(i).size(); j++) {
                    List<Integer> level2 = new ArrayList<Integer>();
                    level2.add(j);
                    level1.add(level2);
                }
                hierachy.add(level1);
            }
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }

    }

    private void buildModel() {
        model.dataset = data;
        model.tempDataset = data;
        model.contextProcessor = new ContextProcessor();
        model.contextProcessor.hierachy = hierachy;
        model.contextProcessor.generateContextCombinationFromHierachyForStudyAdvisor();
        model.mf.maxIteration = 10;
        model.run();
    }

    private void updateModel() throws SQLException, ClassNotFoundException {
        Parameters data = new Parameters();
        //Users
        for (int i = 0; i < model.mf.biasUser.length; i++) {
            UserVector u = new UserVector();
            u.bias = model.mf.biasUser[i];
            u.vector.addAll(model.mf.userFactorVector[i]);
            u.user_id = Integer.parseInt(users.get(i));
            u.last_context_id = (lastContexts.get(i));
            data.list_Users.add(u);
        }

        for (int i = 0; i < model.mf.biasItem.length; i++) {
            for (int j = 0; j < model.clusters.k_Cluster_label.get(i).size(); j++) {
                ItemVector item = new ItemVector();
                item.bias = model.mf.biasItem[i];
                item.vector.addAll(model.mf.itemFactorVector[i]);
                item.question_id = Integer.parseInt(items.get(model.itemProfiles.get(model.clusters.k_Cluster_label.get(i).get(j)).itemID));
                item.context_id = Integer.parseInt(model.itemProfiles.get(model.clusters.k_Cluster_label.get(i).get(j)).context.replaceAll("::", "").trim());
                data.list_Items.add(item);
            }
        }

        data.updateToDatabase();

    }

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        ModelBuilder builder = new ModelBuilder();
        builder.process();
    }

    private void process() throws SQLException, ClassNotFoundException {
        DataProcessor dp = new DataProcessor();
        dp.run();
        obtainData(dp.users, dp.items, dp.contexts, dp.ratings, dp.lastContext);
        System.out.println("Data obtained");
        buildModel();
        System.out.println("Model built");
        updateModel();
        System.out.println("Parameters update");

    }
}
