package investmentGame.actor.game.coordinator;

import investmentGame.actor.game.PlayerInterface;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: martin
 * Date: 01/06/14
 * Time: 11:36
 * To change this template use File | Settings | File Templates.
 */
public class KMeansPlayerGroups {

    private PlayerInterface[] players;

    protected class Cluster{

        public double mean;
        public Set<PlayerInterface> playersInCluster = new HashSet<PlayerInterface>();

        public Cluster(double mean){
            this.mean = mean;
        }

        public void reCalculateMean(){
            double sum = 0;
            Iterator<PlayerInterface> iterator = playersInCluster.iterator();
            while (iterator.hasNext())
                sum += iterator.next().getCreditBalance();
            mean = sum / (playersInCluster.size());

        }

        public void iterate(){
            reCalculateMean();
            playersInCluster.clear();
        }

        public double calcVarianceImpact(PlayerInterface player){
            double diff = player.getCreditBalance() - mean;
            return diff*diff;
        }

        public void consumePlayer(PlayerInterface player){
            playersInCluster.add(player);
        }

        public String toString(){
            String res = "CLUSTER[mean="+mean+"]:";
            boolean first = true;
            Iterator<PlayerInterface> p = playersInCluster.iterator();
            while (p.hasNext()){
                res += (first?"":", ")+p.next().getPlayersName();
                first = false;
            }
            return res;
        }

        public double getMean() {
            return mean;
        }

        public SortedSet<PlayerInterface> getPlayersInClusterRanked() {
            Comparator<PlayerInterface> comparator = new Comparator<PlayerInterface>() {
                @Override
                public int compare(PlayerInterface o1, PlayerInterface o2) {
                    return (int)(o2.getCreditBalance()-o1.getCreditBalance());
                }
            };
            TreeSet<PlayerInterface> orderedSet = new TreeSet<PlayerInterface>(comparator);
            orderedSet.addAll(playersInCluster);
            return orderedSet;
        }
    }

    private int k;

    private Cluster[] clusters;

    private HashMap<String, Integer> associations = new HashMap<String, Integer>();

    public KMeansPlayerGroups(Collection<PlayerInterface> players, int k){
        this.players = new PlayerInterface[players.size()];
        Iterator<PlayerInterface> iterator = players.iterator();
        int i=0;
        while (iterator.hasNext()){
            this.players[i++] = iterator.next();
        }
        this.k = k;
        clusters = new Cluster[k];
    }

    private void initialize(){

        //choose k random means from data (players)
        List<PlayerInterface> p = new LinkedList<PlayerInterface>();

        for (int i=0;i<players.length;i++){
            p.add(players[i]);
        }

        for (int i=0;i<k;i++){
            int index = (int)Math.ceil(Math.random()*p.size())-1;
            PlayerInterface mean = p.remove(index);
            clusters[i] = new Cluster(mean.getCreditBalance());
            clusters[i].consumePlayer(mean);
        }
    }

    private boolean iterate(){
        for (int i=0;i<clusters.length;i++){
            clusters[i].iterate();
        }
        boolean change = false;
        for (int i=0;i<players.length;i++){
            double minImpact = Double.MAX_VALUE;
            int minImpactCluster = -1;
            for (int j=0;j<k;j++){
                double impact = clusters[j].calcVarianceImpact(players[i]);
                if (impact<minImpact){
                    minImpact = impact;
                    minImpactCluster = j;
                }
            }
            if (associations.containsKey(players[i].getPlayersName())){
                Integer oldAssociation = associations.get(players[i].getPlayersName());
                if (oldAssociation!=minImpactCluster){
                    change = true;
                }
            }else{
                change = true;
            }
            associations.put(players[i].getPlayersName(),minImpactCluster);
            clusters[minImpactCluster].consumePlayer(players[i]);
        }

        return change;

    }

    public void cluster(){
        initialize();
        boolean change = true;

        while (change){
            change=iterate();
        }
    }

    public String toString(){
        String res = "";
        for (int i=0;i<clusters.length;i++){
            res += " / "+clusters[i].toString();
        }
        return res;
    }

    public Cluster[] getClusters() {
        return clusters;
    }
}
